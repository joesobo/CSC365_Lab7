import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.api.Trigger;

public class DoubleBookingsTrigger implements Trigger {

    private final int roomCode = 1;
    private final int checkIn = 2;
    private final int checkOut = 3;

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type)
            throws SQLException {}

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        
        StringBuilder statementBuilder = new StringBuilder("SELECT checkout FROM lab7_reservations WHERE room = ? AND checkout > ? AND checkin < ?");
        if (oldRow != null) {
            statementBuilder.append(" AND code <> ");
            statementBuilder.append(oldRow[0]);
        }
        try (PreparedStatement stmt = conn.prepareStatement(statementBuilder.toString())) {
            stmt.setObject(roomCode, newRow[roomCode]);
            stmt.setObject(checkIn, newRow[checkIn]);
            stmt.setObject(checkOut, newRow[checkOut]);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                throw new SQLException("There is already a booking for this room durring your stay");
            }
        }
    }

    @Override
    public void close() throws SQLException {}

    @Override
    public void remove() throws SQLException {}
    
}
