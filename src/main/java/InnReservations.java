import java.io.FileNotFoundException;
import java.sql.*;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.h2.store.Data;

public class InnReservations {

  private static final String JDBC_URL = "jdbc:h2:~/csc365_lab7";
  private static final String JDBC_USER = "";
  private static final String JDBC_PASSWORD = "";

  public static void main(String[] args) {
    try {
      Database.init(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
    } catch (SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
    } catch (FileNotFoundException e) {
      System.err.println("Couldn't find file: " + e.getMessage());
    }

    menu();
  }

  private static void menu() {
    Scanner in = new Scanner(System.in);

    loop: while (true) {
      System.out.println("Menu (Select an option)");
      System.out.println("1: Rooms and Rates");
      System.out.println("2: Make Reservation");
      System.out.println("3: Change Reservation");
      System.out.println("4: Cancel Reservation");
      System.out.println("5: Revenue Summary");
      System.out.println("6: Exit");

      try {
        int input = in.nextInt();
        switch (input) {
          case 1:
            roomsAndRates();
            break;
          case 2:
            makeReservation();
            break;
          case 3:
            changeReservation();
            break;
          case 4:
            cancelReservation();
            break;
          case 5:
            summary();
            break;
          case 6: break loop;
          default: throw new InputMismatchException();
        }
      } catch (InputMismatchException e) {
        System.out.println("Must enter a correct number\n");
      }
    }

    in.close();
  }

  private static void roomsAndRates() {
    System.out.println("Test");
  }

  private static void makeReservation() {
    System.out.println("Test");
  }

  private static void changeReservation() {
    System.out.println("Test");
  }

  private static void cancelReservation() {
    Scanner in = new Scanner(System.in);

    System.out.print("\nPlease Enter Reservation Number: ");
    try {
      int code = in.nextInt();

      try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {

        String sqlStatment = "SELECT CODE FROM lab7_reservations WHERE CODE = ?";

        try (PreparedStatement psmt = conn.prepareStatement(sqlStatment, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
          psmt.setInt(1, code);
          ResultSet rs = psmt.executeQuery();
          
          if (!rs.next()) {
            System.out.println("Reservation: " + code + ", Could not be located\n");
            return;
          }

          System.out.println("Reservation: " + code + " Found");
          System.out.println("Confirm Cancelization (Yes/No)?");
          String confirm = in.next();
          if (confirm.toUpperCase().equals("YES")) {
            rs.deleteRow();
            System.out.println("Reservation: " + code + " Sucessfully Canceled\n");
          } else {
            System.out.println("Reservation: " + code + " Was Not Canceled\n");
          }

        }
      } catch (SQLException e) {
        System.out.println("Reservation: " + code + ", Could not be canceled\n");
      }

    } catch (InputMismatchException e) {
      System.out.println("Reservation Numbers are numbers\n");
    } finally {
      in.close();
    }
  }

  private static void summary() {
    System.out.println("Test");
  }
}
