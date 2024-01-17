package com.example.gogreenredo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

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
    @FXML
    private Label liveFeedLabel;


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
        // populateChart();
        populateTable();
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

                // Calculate start time (4 hours ago)
                long fourHoursAgoMillis = System.currentTimeMillis() - (4 * 60 * 60 * 1000);

                while (resultSet.next()) {
                    Timestamp timestamp = resultSet.getTimestamp("measurement_time");
                    float sensorValue = resultSet.getFloat("sensor_value");

                    // Only add data within the past 4 hours
                    if (timestamp.getTime() >= fourHoursAgoMillis) {
                        // Convert timestamp to a numeric value
                        double numericValue = timestamp.getTime();
                        series.getData().add(new XYChart.Data<>(numericValue, sensorValue));
                    }
                }

                stackedAreaChart.getData().add(series);

                // Set the X-axis bounds
                NumberAxis xAxis = (NumberAxis) stackedAreaChart.getXAxis();
                xAxis.setLowerBound(fourHoursAgoMillis);
                xAxis.setUpperBound(System.currentTimeMillis());

                // Set X-axis label formatter (example format, adjust as needed)
                xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {
                    @Override
                    public String toString(Number object) {
                        return new SimpleDateFormat("HH:mm").format(new Date(object.longValue()));
                    }
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private TableView<DataItem> dataTable;

    private void populateTable() {
        if (scene == null) {
            System.err.println("Scene is null. Unable to populate table.");
            return;
        }

        // Get data from the database and populate the table
        DatabaseConnection connectTableData = new DatabaseConnection();
        try (Connection connectDB = connectTableData.getConnection()) {
            // Fetch data for the current day
            String selectQuery = "SELECT measurement_time, sensor_value FROM sensor_data " +
                    "WHERE DATE(measurement_time) = CURDATE()";

            try (PreparedStatement preparedStatement = connectDB.prepareStatement(selectQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                // Initialize the TableView and columns
                dataTable = new TableView<>();
                TableColumn<DataItem, String> timeColumn = new TableColumn<>("Time");
                TableColumn<DataItem, Float> valueColumn = new TableColumn<>("Sensor Value");

                timeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                        new SimpleDateFormat("HH:mm:ss").format(new Date(cellData.getValue().getTime()))));
                valueColumn.setCellValueFactory(cellData -> new SimpleFloatProperty(cellData.getValue().getValue()).asObject());

                dataTable.getColumns().addAll(timeColumn, valueColumn);

                // Style the TableView
                dataTable.setPrefSize(300, 150); // Adjust the preferred size as needed
                dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

                // Initialize the data list
                ObservableList<DataItem> dataList = FXCollections.observableArrayList();

                while (resultSet.next()) {
                    Timestamp timestamp = resultSet.getTimestamp("measurement_time");
                    float sensorValue = resultSet.getFloat("sensor_value");

                    dataList.add(new DataItem(timestamp.getTime(), sensorValue));
                }

                dataTable.setItems(dataList);

                // Add the TableView to the center of the scene with a scrollbar
                AnchorPane tableAnchorPane = (AnchorPane) scene.lookup("#tableAnchorPane");
                tableAnchorPane.getChildren().add(dataTable);

                ScrollPane scrollPane = new ScrollPane(dataTable);
                scrollPane.setFitToWidth(true);
                scrollPane.setFitToHeight(true);

                AnchorPane.setTopAnchor(scrollPane, (tableAnchorPane.getHeight() - scrollPane.getHeight()) / 4);
                AnchorPane.setLeftAnchor(scrollPane, (tableAnchorPane.getWidth() - scrollPane.getWidth()) / 5);

                tableAnchorPane.getChildren().add(scrollPane);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Define a simple class to hold data for the TableView
    public static class DataItem {
        private final SimpleObjectProperty<Long> time;
        private final SimpleFloatProperty value;

        public DataItem(long time, float value) {
            this.time = new SimpleObjectProperty<>(time);
            this.value = new SimpleFloatProperty(value);
        }

        public Long getTime() {
            return time.get();
        }

        public float getValue() {
            return value.get();
        }
    }
    private Timeline liveFeedTimeline;

    @FXML
    public void initialize() {
        // Initialize the Timeline for updating live feed every 3 seconds
        liveFeedTimeline = new Timeline(new KeyFrame(Duration.seconds(3), this::updateLiveFeed));
        liveFeedTimeline.setCycleCount(Timeline.INDEFINITE);
        liveFeedTimeline.play();
    }

    private void updateLiveFeed(ActionEvent event) {
        // Fetch the latest sensor data and update the liveFeedLabel
        DatabaseConnection connectLatestSensorData = new DatabaseConnection();
        try (Connection connectDB = connectLatestSensorData.getConnection()) {
            String selectLatestDataQuery = "SELECT sensor_value FROM sensor_data ORDER BY measurement_time DESC LIMIT 1";

            try (PreparedStatement preparedStatement = connectDB.prepareStatement(selectLatestDataQuery);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                if (resultSet.next()) {
                    float latestSensorValue = resultSet.getFloat("sensor_value");
                    liveFeedLabel.setText(String.format("%.2f", latestSensorValue));
                } else {
                    liveFeedLabel.setText("Laadfout");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}