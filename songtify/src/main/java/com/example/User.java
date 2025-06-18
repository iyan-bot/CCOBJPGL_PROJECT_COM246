package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private static final String DB_FILE = "users.txt";

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    // === File Database Methods ===
    public static void saveUser(User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DB_FILE, true))) {
            writer.write(user.getUsername() + "," + user.getPassword());
            writer.newLine();
        }
    }

    public static List<User> loadUsers() throws IOException {
        List<User> users = new ArrayList<>();
        File file = new File(DB_FILE);

        if (!file.exists()) {
            file.createNewFile(); // Create file if it doesn't exist
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(DB_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    users.add(new User(parts[0], parts[1]));
                }
            }
        }
        return users;
    }

    public static boolean userExists(String username) throws IOException {
        return loadUsers().stream().anyMatch(u -> u.getUsername().equals(username));
    }
}