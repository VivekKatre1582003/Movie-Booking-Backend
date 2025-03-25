package service;

import config.DataBaseConfig;

import java.sql.*;
import java.util.List;

public class BookMyMovieSys {
    // Display Movies
    public void displayMovies() {
        try (Connection con = DataBaseConfig.getConnection()) { // Using try-with-resources to ensure con is closed automatically
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM movies");
            System.out.println("----------Available Movies----------");
            while (rs.next()) {
                System.out.println(rs.getInt("movie_id")
                        + "." + rs.getString("title")
                        + " (" + rs.getString("genre") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Show theaters in cities
    public void displayTheaters(String city) {
        try (Connection con = DataBaseConfig.getConnection()) { // Using try-with-resources for connection management
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM theaters WHERE city = ?");
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Theaters in " + city + ":");
            while (rs.next()) {
                System.out.println(rs.getInt("theater_id") + "." + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Display shows
    public void displayShows(int movieId, int theaterId) {
        try (Connection con = DataBaseConfig.getConnection()) { // Using try-with-resources for connection management
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM shows WHERE movie_id = ? AND theater_id = ?");
            stmt.setInt(1, movieId);
            stmt.setInt(2, theaterId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("Available shows:");
            while (rs.next()) {
                System.out.println(rs.getInt("show_id") + ". " + rs.getString("timing")
                        + " - Seats Available: " + rs.getString("available_seats"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Book tickets
    public void bookTickets(int userId, int showId, List<String> selectedSeats) {
        Connection con = null; // Declare the connection here so it can be used throughout the method
        try {
            con = DataBaseConfig.getConnection();
            con.setAutoCommit(false); // Start transaction

            // Check if seats are available
            boolean alreadyBookedSeat = false;
            for (String seat : selectedSeats) {
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM seat WHERE seat_number = ? AND show_id = ?");
                stmt.setString(1, seat);
                stmt.setInt(2, showId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getBoolean("is_booked")) {
                    alreadyBookedSeat = true;
                    System.out.println("Seat " + seat + " is already booked. Choose another seat.");
                    break;
                }
            }

            if (alreadyBookedSeat) {
                System.out.println("Booking failed! Some seats are already booked.");
                con.rollback(); // Rollback the transaction
                return;
            }

            // Update seat booking status
            for (String seat : selectedSeats) {
                PreparedStatement stmt = con.prepareStatement("UPDATE seat SET is_booked = TRUE WHERE seat_number = ? AND show_id = ?");
                stmt.setString(1, seat);
                stmt.setInt(2, showId);
                stmt.executeUpdate();
            }

            // Calculate total price
            double seatPrice = 200.0;
            double totalPrice = selectedSeats.size() * seatPrice;

            // Insert booking details
            PreparedStatement stmt = con.prepareStatement("INSERT INTO booking(user_id, show_id, seats_booked, total_price) VALUES(?, ?, ?, ?)");
            stmt.setInt(1, userId);
            stmt.setInt(2, showId);
            stmt.setString(3, String.join(",", selectedSeats));
            stmt.setDouble(4, totalPrice);
            stmt.executeUpdate();

            con.commit(); // Commit the transaction
            System.out.println("Booking successful. Seats: " + selectedSeats + " | Total Price: " + totalPrice);

        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback(); // Rollback the transaction if an error occurs
                }
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            // Close the connection if it's not closed already
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
