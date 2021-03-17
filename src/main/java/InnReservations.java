import java.io.FileNotFoundException;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;

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

    loop:while (true) {
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
            makeReservation(in);
            break;
          case 3:
            changeReservation(in);
            break;
          case 4:
            cancelReservation(in);
            break;
          case 5:
            summary();
            break;
          case 6:
            break loop;
          default:
            throw new InputMismatchException();
        }
      } catch (InputMismatchException e) {
        System.out.println("Must enter a correct number\n");
      }
    }

    in.close();
  }

  private static void roomsAndRates() {
    String[] headers = {
      "Room Code",
      "Room Name",
      "Beds",
      "Bed Type",
      "Max Occupancy",
      "Base Price",
      "Decor",
      "Next Available",
      "Next Reservation",
    };
    String format =
      "%-9s | %-25s | %-4s | %-8s | %-13s | %-10s | %-11s | %-15s | %-15s%n";
    String heading = String.format(format, (Object[]) headers);
    DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
    String query =
      "with lastCheckin as (\n" +
      "select room, max(checkin) as maxCheckin\n" +
      "from lab7_reservations\n" +
      "where checkin <= curdate()\n" +
      "group by room\n" +
      "), soonestReservation as (\n" +
      "select res.room, dateadd(day, 1, checkout) as dayAfterReservation\n" +
      "from lab7_reservations as res\n" +
      "join lastCheckin as lc on res.room = lc.room\n" +
      "where\n" +
      "maxCheckin <= checkin and\n" +
      "dateadd(day, 1, checkout) not in (\n" +
      "select checkin from lab7_reservations\n" +
      ")\n" +
      "), nextReservations as (\n" +
      "select room, checkin\n" +
      "from lab7_reservations\n" +
      "where curdate() <= checkin\n" +
      ")\n" +
      "select\n" +
      "roomcode,\n" +
      "roomname,\n" +
      "beds,\n" +
      "bedtype,\n" +
      "maxOcc,\n" +
      "basePrice,\n" +
      "decor,\n" +
      "case when\n" +
      "curdate() >= ifnull(min(sr.dayAfterReservation), curdate())\n" +
      "then null\n" +
      "else min(sr.dayAfterReservation)\n" +
      "end as nextAvailable,\n" +
      "min(nr.checkin) as nextReservation\n" +
      "from lab7_rooms as r\n" +
      "left join soonestReservation as sr on r.roomcode = sr.room\n" +
      "left join nextReservations as nr on r.roomcode = nr.room\n" +
      "group by r.roomcode\n" +
      "order by r.roomname";

    try (
      Connection conn = DriverManager.getConnection(
        JDBC_URL,
        JDBC_USER,
        JDBC_PASSWORD
      )
    ) {
      try (
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(query)
      ) {
        System.out.println("Rooms and Rates:");
        System.out.print(heading);

        while (rs.next()) {
          Date nextAvailable = rs.getDate("nextavailable");
          Date nextReservation = rs.getDate("nextreservation");
          String roomCode = rs.getString("roomcode");
          String roomName = rs.getString("roomname");
          String beds = rs.getString("beds");
          String bedType = rs.getString("bedtype");
          String maxOcc = rs.getString("maxocc");
          String basePrice = rs.getString("baseprice");
          String decor = rs.getString("decor");
          String nextAvailableDate = nextAvailable == null
            ? "Today"
            : dateFormat.format(nextAvailable);
          String nextReservationDate = nextReservation == null
            ? "None"
            : dateFormat.format(nextReservation);

          System.out.printf(
            format,
            roomCode,
            roomName,
            beds,
            bedType,
            maxOcc,
            basePrice,
            decor,
            nextAvailableDate,
            nextReservationDate
          );
        }
      }
    } catch (SQLException e) {
      System.out.println("\n ERROR: Could retrieve rooms and rates data");
      e.printStackTrace();
    }
    System.out.println();
  }

  private static void makeReservation(Scanner in) {

    System.out.println("Make a new Reservation");
    in.nextLine();

    System.out.println("First Name: ");
    String firstName = in.nextLine().toUpperCase();

    System.out.println("Last Name: ");
    String lastName = in.nextLine().toUpperCase();

    System.out.println("Room Code of Desired Room: ");
    String roomCode = in.nextLine().toUpperCase();

    System.out.println("Date of Check In(yyyy-mm-dd): ");
    Date checkIn = Date.valueOf(in.nextLine());

    System.out.println("Date of Check Out(yyyy-mm-dd): ");
    Date checkOut = Date.valueOf(in.nextLine());

    System.out.println("Number of Children(17 or younger): ");
    int children = in.nextInt();

    System.out.println("Number of Adults(18+): ");
    int adults = in.nextInt();

    System.out.println("Checking availability for "+roomCode+" between "+checkIn+" and "+checkOut+" for "+children+" children and "+adults+" adults");

    try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {

      String sqlInsertString = "INSERT INTO lab7_reservations " + 
      "(CODE, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids) " +
      "VALUES ((SELECT max(CODE) + 1 FROM lab7_reservations), ?, ?, ?, " +
      "(SELECT basePrice FROM lab7_rooms where RoomCode = ?), ?, ?, ?, ?)";

      try (PreparedStatement pStatement = conn.prepareStatement(sqlInsertString)) {
        pStatement.setString(1, roomCode);
        pStatement.setDate(2, checkIn);
        pStatement.setDate(3, checkOut);
        pStatement.setString(4, roomCode);
        pStatement.setString(5, lastName);
        pStatement.setString(6, firstName);
        pStatement.setInt(7, adults);
        pStatement.setInt(8, children);

        if (pStatement.executeUpdate() > 0) {

          String sqlConfirmationString = "SELECT CODE, FirstName, LastName, RoomCode, RoomName, bedType, CheckIn, " +
          "CheckOut, Adults, kids, Rate FROM lab7_reservations JOIN lab7_rooms on Room = RoomCode " +
          "WHERE CheckIn = ? and CheckOut = ? and Room = ?";

          try (PreparedStatement confirmiationPStatement = conn.prepareStatement(sqlConfirmationString)) {
            confirmiationPStatement.setDate(1, checkIn);
            confirmiationPStatement.setDate(2, checkOut);
            confirmiationPStatement.setString(3, roomCode);
            ResultSet resultSet = confirmiationPStatement.executeQuery();

            if (resultSet.next()) {
              String confirmationCode = resultSet.getString("CODE");
              String confirmFirstName = resultSet.getString("FirstName");
              String confirmLastName = resultSet.getString("LastName");
              String confirmRoomCode = resultSet.getString("RoomCode");
              String confirmRoomName = resultSet.getString("RoomName");
              String confirmBedType = resultSet.getString("bedType");
              LocalDate confirmCheckIn = resultSet.getDate("CheckIn").toLocalDate();
              LocalDate confirmCheckOut = resultSet.getDate("CheckOut").toLocalDate();
              int confirmAdults = resultSet.getInt("Adults");
              int confirmKids = resultSet.getInt("Kids");
              int rate = resultSet.getInt("Rate");
              long totalDays = DAYS.between(confirmCheckIn, confirmCheckOut);
              Predicate<LocalDate> isWeekend = date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
              long weekendDays = Stream.iterate(confirmCheckIn, date -> date.plusDays(1)).limit(totalDays)
                .filter(isWeekend).count();
              long weekDays = totalDays - weekendDays;
              double totalCharge = rate * weekDays + (rate * 1.1) * weekendDays;
              
              System.out.println("Confirmation\n\nReservation Code :" + confirmationCode + "\n" +
                confirmFirstName + " " + confirmLastName + "\n" +
                confirmRoomName + " (" + confirmRoomCode + "), " + confirmBedType + " bed\n" +
                confirmAdults + " Number of Adults, and " + confirmKids + " Children\n\n" +
                "Total Cost for " + totalDays + " is: " + totalCharge + "$\n\n");
            }
          }
        }
      }
    } catch (SQLException e) {
      System.err.println("Could not create reservation: " + e.getMessage().split(";")[0] + "\n");
    }
  }

  private static void changeReservation(Scanner in) {
    final String noChange = "NO CHANGE";
    System.out.println("Change your Reservation\n");

    System.out.println("Please Enter your Reservation Code:");
    int reservationCode = in.nextInt();
    in.nextLine();

    System.out.println("\n***If you do not want to change a field please enter " + noChange + "***\n");
    System.out.println("New First Name:");
    String firstName = in.nextLine().toUpperCase();

    System.out.println("New Last Name");
    String lastName = in.nextLine().toUpperCase();

    System.out.println("New Check In Date (yyyy-mm-dd):");
    String checkInString = in.nextLine();

    System.out.println("New Check Out Date (yyyy-mm-dd):");
    String checkOutString = in.nextLine();

    System.out.println("New Number of Children");
    String childrenString = in.nextLine();

    System.out.println("New Number of Adults");
    String adultsString = in.nextLine();

    List<Object> values = new ArrayList<>();
    StringBuilder sqlQueryBuilder = new StringBuilder("UPDATE lab7_reservations SET ");
    StringJoiner stringJoiner = new StringJoiner(", ");

    if (!firstName.toUpperCase().equals(noChange)) {
      stringJoiner.add("FirstName = ?");
      values.add(firstName);
    }
    if (!lastName.toUpperCase().equals(noChange)) {
      stringJoiner.add("LastName = ?");
      values.add(lastName);
    }
    if (!checkInString.toUpperCase().equals(noChange)) {
      stringJoiner.add("CheckIn = ?");
      values.add(Date.valueOf(checkInString));
    }
    if (!checkOutString.toUpperCase().equals(noChange)) {
      stringJoiner.add("CheckOut = ?");
      values.add(Date.valueOf(checkOutString));
    }
    if (!childrenString.toUpperCase().equals(noChange)) {
      stringJoiner.add("Kids = ?");
      values.add(Integer.valueOf(childrenString));
    }
    if (!adultsString.toUpperCase().equals(noChange)) {
      stringJoiner.add("Adults = ?");
      values.add(Integer.valueOf(adultsString));
    }

    sqlQueryBuilder.append(stringJoiner.toString());
    sqlQueryBuilder.append(" WHERE CODE = ?");

    try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
      
      try (PreparedStatement pStatement = conn.prepareStatement(sqlQueryBuilder.toString())) {
        for (int i = 0; i < values.size(); i++) {
          Object currentObject = values.get(i);

          if (currentObject instanceof Integer) {
            pStatement.setInt(i + 1, (int)currentObject);
          } else if (currentObject instanceof String) {
            pStatement.setString(i + 1, (String)currentObject);
          } else if (currentObject instanceof Date) {
            pStatement.setDate(i + 1, (Date)currentObject);
          }
        }

        pStatement.setInt(values.size() + 1, reservationCode);
        long changeCount = pStatement.executeUpdate();
        System.out.println("Changed: " + changeCount + " fields\n");

      }
    } catch (SQLException e) {
      System.err.println("Couldn't Update Reservation " + reservationCode + ": " + e.getMessage().split(";")[0] + "\n");
    }
  }

  private static void cancelReservation(Scanner in) {
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
    }
  }

  private static void summary() {
    String[] headers = {
      "Code",
      "Name",
      "JANUARY",
      "FEBRUARY",
      "MARCH",
      "APRIL",
      "MAY",
      "JUNE",
      "JULY",
      "AUGUST",
      "SEPTEMBER",
      "OCTOBER",
      "NOVEMBER",
      "DECEMBER",
      "TOTAL",
    };
    String format =
      "%-4s | %-24s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s | %-9s%n";
    String heading = String.format(format, (Object[]) headers);

    try (
      Connection conn = DriverManager.getConnection(
        JDBC_URL,
        JDBC_USER,
        JDBC_PASSWORD
      )
    ) {
      String query =
        "with reservationcost as (\n" +
        "select *, datediff(day, checkin, checkout) * rate as cost\n" +
        "from lab7_rooms rooms\n" +
        "join lab7_reservations rs on rs.room = rooms.roomcode\n" +
        "), monthrevenue as (\n" +
        "select roomcode, roomname, month(checkout) as month, sum(cost) monthtotal\n" +
        "from reservationcost\n" +
        "group by month(checkout), roomcode\n" +
        ")\n" +
        "select *\n" +
        "from monthrevenue\n" +
        "order by roomcode, month";

      try (
        Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = statement.executeQuery(query)
      ) {
        float[] monthFinalRevenue = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        float finalRevenue = 0;

        System.out.println("Revenue Summary:");
        System.out.print(heading);

        while (rs.next()) {
          String roomCode = rs.getString("roomcode");
          String tempCode;
          String roomName = rs.getString("roomname");
          float total = 0;
          float[] months = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

          while (true) {
            tempCode = rs.getString("roomcode");

            if (!roomCode.equals(tempCode)) {
              tempCode = roomCode;
              rs.previous();
              break;
            }

            int month = rs.getInt("month");
            float monthTotal = rs.getFloat("monthtotal");

            months[month - 1] = monthTotal;
            monthFinalRevenue[month - 1] += monthTotal;
            total += monthTotal;
            finalRevenue += monthTotal;

            if (!rs.next()) break;
          }

          System.out.format(
            "%-4s | %-24s %s %n",
            tempCode,
            roomName,
            yearFormat(months, total)
          );
        }
        System.out.format(
          "%-31s %s %n",
          "FINAL TOTAL:",
          yearFormat(monthFinalRevenue, finalRevenue)
        );
      }
    } catch (SQLException e) {
      System.out.println("\n ERROR: Could retrieve revenue summary");
      e.printStackTrace();
    }
    System.out.println();
  }

  private static String yearFormat(float[] totalArr, float total) {
    StringBuilder totals = new StringBuilder();

      for (float v : totalArr) {
          String monthTotal = String.format("$%s", Math.round(v));
          String padded = String.format("| %-10s", monthTotal);
          totals.append(padded);
      }

    String monthTotal = String.format("$%s", Math.round(total));
    return totals + String.format("| %-9s", monthTotal);
  }
}
