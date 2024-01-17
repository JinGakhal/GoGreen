package com.example.gogreenredo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class testing {
    public static void main(String[] args) {
        try {
            String pythonScript = "C:\\Users\\TheCr\\IdeaProjects\\GoGreenRedo\\src\\main\\python\\reader.py";
            String pythonInterpreter = "python3";

            // Build the command
            ProcessBuilder processBuilder = new ProcessBuilder(pythonInterpreter, pythonScript);

            // Start the process
            Process process = processBuilder.start();

            // Read the output of the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("Python script executed with exit code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

