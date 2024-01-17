package com.example.gogreenredo;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    public Connection databaseLink;
    public Connection getConnection(){
        String dbName = "GoGreen";
        String dbUser = "root";
        String dbPass = "pluto";
        String dbURL = "jdbc:mysql://localhost/" + dbName;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(dbURL, dbUser, dbPass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return databaseLink;
    }
}
