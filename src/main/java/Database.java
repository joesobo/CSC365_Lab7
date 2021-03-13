import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Database {
  private final static String CREATE_TABLE = "data/InnCreation.sql";
  private final static String POPULATE_TABLE = "data/InnPopulation.sql";

  public static void init(String url, String user, String password)
    throws FileNotFoundException, SQLException {
    runFile(CREATE_TABLE, url, user, password);
    runFile(POPULATE_TABLE, url, user, password);
  }

  private static void runFile(String file, String url, String user, String password)
    throws FileNotFoundException, SQLException {
    try (
      Scanner scanner = new Scanner(new File(file));
      Connection conn = DriverManager.getConnection(url, user, password)
    ) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (line.isEmpty()) {
          continue;
        }
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(line);
        }
      }
    }
  }
}
