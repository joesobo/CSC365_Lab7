DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS rooms;

CREATE TABLE rooms (RoomCode CHAR(5) PRIMARY KEY, RoomName VARCHAR(30), Beds INTEGER, bedType VARCHAR(8), maxOcc INTEGER, basePrice FLOAT, decor VARCHAR(20), UNIQUE(RoomName));
CREATE TABLE reservations (CODE INTEGER PRIMARY KEY, Room CHAR(5), CheckIn DATE, Checkout DATE, Rate FLOAT, LastName VARCHAR(15), FirstName VARCHAR(15), Adults INTEGER, Kids INTEGER, FOREIGN KEY (Room) REFERENCES rooms (RoomCode));

-- setup constraints
-- CREATE TRIGGER no_double_bookings BEFORE INSERT ON reservations FOR EACH ROW CALL "database.NoDoubleBookingsTrigger";
-- CREATE TRIGGER no_double_bookings_update BEFORE UPDATE ON reservations FOR EACH ROW CALL "database.NoDoubleBookingsTrigger";
-- CREATE TRIGGER check_max_occupancy BEFORE INSERT ON reservations FOR EACH ROW CALL "database.CheckMaxOccTrigger";
-- CREATE TRIGGER check_max_occupancy_update BEFORE UPDATE ON reservations FOR EACH ROW CALL "database.CheckMaxOccTrigger";