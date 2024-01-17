package com.example.gogreenredo;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PasswordUpdater {

    public static void updatePassword(String userEmail, String newPassword) {
        // Hash the new password
        String hashedPassword = hashPassword(newPassword);

        // Update the hashed password in the database
        updatePasswordInDatabase(userEmail, hashedPassword);
    }

    private static String hashPassword(String plainTextPassword) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(plainTextPassword, salt);
    }

    private static void updatePasswordInDatabase(String userEmail, String hashedPassword) {
        // Use your existing DatabaseConnection class
        DatabaseConnection connectUpdate = new DatabaseConnection();
        try (Connection connectDB = connectUpdate.getConnection()) {
            String updateQuery = "UPDATE user SET password = ? WHERE email = ?";
            try (PreparedStatement preparedStatement = connectDB.prepareStatement(updateQuery)) {
                preparedStatement.setString(1, hashedPassword);
                preparedStatement.setString(2, userEmail);
                preparedStatement.executeUpdate();
                System.out.println("Password updated successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error updating password in the database.");
        }
    }

    public static void main(String[] args) {
        // Example usage: Update password for user with email "email@email.com"
        updatePassword("email@email.com", "email@email.com");
    }
}
