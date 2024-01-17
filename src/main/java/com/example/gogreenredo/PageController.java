package com.example.gogreenredo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.sql.Timestamp;
import java.util.Date;

import java.io.IOException;
import java.sql.*;

import org.mindrot.jbcrypt.BCrypt;

public class PageController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    private String userEmail;

    @FXML
    private TextField emailInputField;
    @FXML
    public TextField userEmailField;
    @FXML
    private PasswordField passwordInputField;
    @FXML
    private PasswordField passwordInputField1;
    @FXML
    private Button registerButton;
    @FXML
    private Button closeButton;
    @FXML
    private Label warningLabel;
    @FXML
    private TextField currentPasswordField;
    @FXML
    private TextField newPasswordField;

    public PageController() {
    }

    @FXML

    public String hashPassword(String plainTextPassword) {
        String salt = BCrypt.gensalt();
        return BCrypt.hashpw(plainTextPassword, salt);
    }

    public boolean verifyPassword(String enteredPassword, String storedHash) {
        return BCrypt.checkpw(enteredPassword, storedHash);
    }

    private void loadPage(String fxmlFileName, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent root = loader.load();

            // Get the scene from the loaded root
            Scene scene = new Scene(root);

            // Set the scene to the stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            // Store the scene in the class variable
            this.scene = scene;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onLoginButtonClick(ActionEvent e) {
        if (emailInputField.getText().isBlank() && passwordInputField.getText().isBlank())  {
            warningLabel.setText("Kijk goed of u alle velden heeft ingevuld.");
        } else {
            stage = (Stage)((Node)e.getSource()).getScene().getWindow();
            handleLogin();
        }
    }
    public void onRegisterButtonClick(ActionEvent event) throws IOException {
        loadPage("register.fxml", event);
    }

    public void onUsageButtonClick(ActionEvent event) {
        loadPage("usage.fxml", event);
        populateChart();
    }

    public void onSubscriptionsButtonClick(ActionEvent event) {
        loadPage("subscriptions.fxml", event);
    }

    public void onMessagesButtonClick(ActionEvent event) throws IOException {
        loadPage("inbox.fxml", event);
    }

    public void onProfileButtonClick(ActionEvent event) throws IOException {
        loadPage("profile.fxml", event);
        userEmailField.setText(userEmail);
    }

    public void onCloseButtonClick(ActionEvent e) {
        // afsluiten applicatie
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void handleLogin() {
        // Voor het inloggen van een gebruiker
        DatabaseConnection connectLogin = new DatabaseConnection();
        Connection connectDB = connectLogin.getConnection();

        String verifyLogin = "SELECT password FROM user WHERE email = '" + emailInputField.getText() + "';";

        try (PreparedStatement preparedStatement = connectDB.prepareStatement(verifyLogin)) {
            ResultSet queryResult = preparedStatement.executeQuery();

            if (queryResult.next()) {
                String storedHash = queryResult.getString("password");

                if (verifyPassword(passwordInputField.getText(), storedHash)) {
                    userEmail = emailInputField.getText();
                    Parent root = FXMLLoader.load(getClass().getResource("home.fxml"));
                    scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } else {
                    warningLabel.setText("Onjuiste gegevens, check of u alle velden goed heeft ingevuld.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRegisterUserButtonClick(ActionEvent event) {
        // voor het maken van een gebruiker
        String userEmail = emailInputField.getText();
        String password = passwordInputField.getText();
        String repeatedPassword = passwordInputField1.getText();

        if (!password.equals(repeatedPassword)) {
            warningLabel.setText("Wachtwoorden komen niet overeen.");
            return;
        }

        String hashedPassword = hashPassword(password);
        if (registerUserInDatabase(userEmail, hashedPassword)) {
            System.out.println("test succesvokl");
        } else {
            warningLabel.setText("Registratie mislukt. Probeer het opnieuw.");
        }
    }

    private boolean registerUserInDatabase(String userEmail, String hashedPassword) {
        // voor het zetten van een gemaakte gebruiker in de db
        DatabaseConnection connectRegister = new DatabaseConnection();
        try (Connection connectDB = connectRegister.getConnection()) {
            String insertQuery = "INSERT INTO user (email, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connectDB.prepareStatement(insertQuery)) {
                preparedStatement.setString(1, userEmail);
                preparedStatement.setString(2, hashedPassword);
                preparedStatement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void onEditProfileButtonClick(ActionEvent event) {
        String currentUserEmail = userEmailField.getText();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();

        // Checken of de wachtwoord klopt
        if (!validateCurrentPassword(currentUserEmail, currentPassword)) {
            warningLabel.setText("Huidig wachtwoord is onjuist.");
            return;
        }

        String hashedNewPassword = hashPassword(newPassword);

        if (updateUserPassword(currentUserEmail, hashedNewPassword)) {
            warningLabel.setText("Wachtwoord succesvol gewijzigd.");
        } else {
            warningLabel.setText("Wachtwoord wijzigen mislukt. Probeer het opnieuw.");
        }
    }

    private boolean validateCurrentPassword(String userEmail, String currentPassword) {
        DatabaseConnection connectValidatePassword = new DatabaseConnection();
        try (Connection connectDB = connectValidatePassword.getConnection()) {
            String selectQuery = "SELECT password FROM user WHERE email = ?";
            try (PreparedStatement preparedStatement = connectDB.prepareStatement(selectQuery)) {
                preparedStatement.setString(1, userEmail);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String storedHashedPassword = resultSet.getString("password");
                        return BCrypt.checkpw(currentPassword, storedHashedPassword);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateUserPassword(String userEmail, String hashedNewPassword) {
        DatabaseConnection connectEditProfile = new DatabaseConnection();
        try (Connection connectDB = connectEditProfile.getConnection()) {
            String updateQuery = "UPDATE user SET password = ? WHERE email = ?";
            try (PreparedStatement preparedStatement = connectDB.prepareStatement(updateQuery)) {
                preparedStatement.setString(1, hashedNewPassword);
                preparedStatement.setString(2, userEmail);
                preparedStatement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void populateChart() {
        if (scene == null) {
            System.err.println("Scene is null. Unable to populate chart.");
            return;
        }

        // Get data from the database and populate the chart
        DatabaseConnection connectChartData = new DatabaseConnection();
        try (Connection connectDB = connectChartData.getConnection()) {
            String selectQuery = "SELECT measurement_time, sensor_value FROM sensor_data";
            try (PreparedStatement preparedStatement = connectDB.prepareStatement(selectQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                StackedAreaChart<Number, Number> stackedAreaChart = (StackedAreaChart<Number, Number>) scene.lookup("#areaChart");
                if (stackedAreaChart == null) {
                    System.err.println("StackedAreaChart is null. Unable to populate chart.");
                    return;
                }

                stackedAreaChart.getData().clear();

                XYChart.Series<Number, Number> series = new XYChart.Series<>();

                while (resultSet.next()) {
                    Timestamp timestamp = resultSet.getTimestamp("measurement_time");
                    float sensorValue = resultSet.getFloat("sensor_value");

                    // Convert timestamp to a numeric value
                    long timestampMillis = timestamp.getTime();
                    Date date = new Date(timestampMillis);
                    double numericValue = date.getTime();

                    series.getData().add(new XYChart.Data<>(numericValue, sensorValue));
                }

                stackedAreaChart.getData().add(series);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}