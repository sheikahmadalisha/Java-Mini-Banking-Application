package com.learn.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class BankApplication {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection();
             Scanner sc = new Scanner(System.in)) { 

            if (conn == null) {
                System.out.println("❌ Could not connect to database. Exiting...");
                return;
            }

            BankOperations bank = new BankOperations(conn, sc);

            while (true) {
                System.out.println("\n===== WELCOME TO GK BANK =====");
                System.out.println("1. Create Account");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();

                switch (choice) {
                    case 1 -> bank.createAccount();
                    case 2 -> bank.login();
                    case 3 -> {
                        System.out.println("👋 Thank you for using GK Bank!");
                        return;
                    }
                    default -> System.out.println("❌ Invalid choice! Try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
