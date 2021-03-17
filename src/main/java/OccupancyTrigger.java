import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.api.Trigger;

public class OccupancyTrigger implements Trigger {

    private final int roomCode = 1;
    private final int adults = 7;
    private final int kids = 8;

    @Override
    public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type)
            throws SQLException {}

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT maxOcc FROM lab7_rooms WHERE roomcode = ?;")) {
            stmt.setObject(roomCode, newRow[roomCode]);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Room: " + newRow[roomCode] + ", does not exist");
            } else {
                int maxOcc = rs.getInt("maxOcc");
                if (maxOcc < (int)newRow[adults] + (int)newRow[kids]) {
                    throw new SQLException("Room: " + newRow[roomCode] + ", can only hold " + maxOcc + " your party is to large to reserve this room");
                }
            }
        }
    }

    @Override
    public void close() throws SQLException {}

    @Override
    public void remove() throws SQLException {}
    
}
