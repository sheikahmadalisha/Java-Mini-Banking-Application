package com.learn.jdbc;

//import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.SQLIntegrityConstraintViolationException;

import java.util.Scanner;

public class BankOperations {
    private Connection conn;
    private Scanner sc;
    private int loggedInAccount = -1;

    public BankOperations(Connection conn, Scanner sc) {
        this.conn = conn;
        this.sc = sc;
    }

    // Create new account
    public void createAccount() throws SQLException {
        sc.nextLine(); // clear buffer
        System.out.print("Enter your full name: ");
        String name = sc.nextLine();

        System.out.print("Create username: ");
        String username = sc.nextLine();

        System.out.print("Create a 4-digit PIN: ");
        String pin = sc.nextLine();

        if (!pin.matches("\\d{4}")) {
            System.out.println("⚠️ PIN must be exactly 4 digits!");
            return;
        }

        String sql = "INSERT INTO accounts (name, username, pin, balance) VALUES (?, ?, ?, 0.0)";
        try {
            PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, name);
            pst.setString(2, username);
            pst.setString(3, pin);
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                System.out.println("🎉 Account created successfully!");
                System.out.println("Your Account Number: " + rs.getInt(1));
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("⚠️ Username already exists! Try another one.");
        }
    }

    // Login
    public void login() throws SQLException {
        sc.nextLine(); // clear buffer
        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter 4-digit PIN: ");
        String pin = sc.nextLine();

        String sql = "SELECT account_no FROM accounts WHERE username = ? AND pin = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, username);
        pst.setString(2, pin);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            loggedInAccount = rs.getInt("account_no");
            System.out.println("✅ Login successful! Welcome back " + username + ".");
            userMenu();
        } else {
            System.out.println("❌ Invalid username or PIN!");
        }
    }

    // User operations menu
    private void userMenu() throws SQLException {
        while (true) {
            System.out.println("\n===== ACCOUNT MENU =====");
            System.out.println("1. Deposit Money");
            System.out.println("2. Withdraw Money");
            System.out.println("3. Check Balance");
            System.out.println("4. Transfer Funds");
            System.out.println("5. Change PIN");
            System.out.println("6. Delete Account");
            System.out.println("7. View Account Details");
            System.out.println("8. Logout");
            System.out.print("Enter your choice: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> depositMoney();
                case 2 -> withdrawMoney();
                case 3 -> checkBalance();
                case 4 -> transferFunds();
                case 5 -> changePin();
                case 6 -> deleteAccount();
                case 7 -> viewAccountDetails();
                case 8 -> {
                    System.out.println("🚪 Logged out successfully.");
                    loggedInAccount = -1;
                    return;
                }
                default -> System.out.println("❌ Invalid choice!");
            }
        }
    }

    // Deposit Money
    private void depositMoney() throws SQLException {
        System.out.print("Enter amount to deposit: ");
        double amount = sc.nextDouble();

        if (amount <= 0) {
            System.out.println("⚠️ Amount must be positive!");
            return;
        }

        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_no = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setDouble(1, amount);
        pst.setInt(2, loggedInAccount);
        pst.executeUpdate();

        System.out.println("✅ ₹" + amount + " deposited successfully!");
    }

    // Withdraw Money
    private void withdrawMoney() throws SQLException {
        System.out.print("Enter amount to withdraw: ");
        double amount = sc.nextDouble();

        if (amount <= 0) {
            System.out.println("⚠️ Amount must be positive!");
            return;
        }

        String checkSql = "SELECT balance FROM accounts WHERE account_no = ?";
        PreparedStatement checkPst = conn.prepareStatement(checkSql);
        checkPst.setInt(1, loggedInAccount);
        ResultSet rs = checkPst.executeQuery();

        if (rs.next()) {
            double balance = rs.getDouble("balance");
            if (balance >= amount) {
                String updateSql = "UPDATE accounts SET balance = balance - ? WHERE account_no = ?";
                PreparedStatement pst = conn.prepareStatement(updateSql);
                pst.setDouble(1, amount);
                pst.setInt(2, loggedInAccount);
                pst.executeUpdate();
                System.out.println("💸 ₹" + amount + " withdrawn successfully!");
                System.out.println("Remaining balance: ₹" + (balance - amount));
            } else {
                System.out.println("⚠️ Insufficient balance!");
            }
        }
    }

    // Check balance
    private void checkBalance() throws SQLException {
        String sql = "SELECT name, balance FROM accounts WHERE account_no = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, loggedInAccount);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            System.out.println("👤 Name: " + rs.getString("name"));
            System.out.println("💰 Balance: ₹" + rs.getDouble("balance"));
        }
    }

    // Transfer Funds
    private void transferFunds() throws SQLException {
        System.out.print("Enter receiver's account number: ");
        int receiverAcc = sc.nextInt();
        System.out.print("Enter amount to transfer: ");
        double amount = sc.nextDouble();

        if (amount <= 0) {
            System.out.println("⚠️ Amount must be positive!");
            return;
        }

        if (receiverAcc == loggedInAccount) {
            System.out.println("⚠️ Cannot transfer to your own account!");
            return;
        }

        conn.setAutoCommit(false);
        try {
            // Check sender balance
            String checkSql = "SELECT balance FROM accounts WHERE account_no = ?";
            PreparedStatement checkSender = conn.prepareStatement(checkSql);
            checkSender.setInt(1, loggedInAccount);
            ResultSet senderRs = checkSender.executeQuery();

            if (!senderRs.next()) {
                System.out.println("❌ Your account not found!");
                conn.rollback();
                return;
            }

            double senderBalance = senderRs.getDouble("balance");
            if (senderBalance < amount) {
                System.out.println("⚠️ Insufficient balance!");
                conn.rollback();
                return;
            }

            // Check receiver exists
            PreparedStatement checkReceiver = conn.prepareStatement(checkSql);
            checkReceiver.setInt(1, receiverAcc);
            ResultSet receiverRs = checkReceiver.executeQuery();

            if (!receiverRs.next()) {
                System.out.println("❌ Receiver account not found!");
                conn.rollback();
                return;
            }

            // Deduct & Add
            PreparedStatement deduct = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE account_no = ?");
            deduct.setDouble(1, amount);
            deduct.setInt(2, loggedInAccount);
            deduct.executeUpdate();

            PreparedStatement add = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE account_no = ?");
            add.setDouble(1, amount);
            add.setInt(2, receiverAcc);
            add.executeUpdate();

            conn.commit();
            System.out.println("✅ ₹" + amount + " transferred successfully!");

        } catch (SQLException e) {
            conn.rollback();
            System.out.println("❌ Transfer failed. Transaction rolled back.");
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Change PIN
    private void changePin() throws SQLException {
        sc.nextLine(); // clear buffer
        System.out.print("Enter current PIN: ");
        String currentPin = sc.nextLine();

        // Verify current PIN
        String checkSql = "SELECT pin FROM accounts WHERE account_no = ?";
        PreparedStatement checkPst = conn.prepareStatement(checkSql);
        checkPst.setInt(1, loggedInAccount);
        ResultSet rs = checkPst.executeQuery();

        if (rs.next()) {
            String existingPin = rs.getString("pin");
            if (!existingPin.equals(currentPin)) {
                System.out.println("❌ Incorrect current PIN!");
                return;
            }
        } else {
            System.out.println("❌ Account not found!");
            return;
        }

        System.out.print("Enter new 4-digit PIN: ");
        String newPin = sc.nextLine();

        if (!newPin.matches("\\d{4}")) {
            System.out.println("⚠️ PIN must be exactly 4 digits!");
            return;
        }

        String updateSql = "UPDATE accounts SET pin = ? WHERE account_no = ?";
        PreparedStatement updatePst = conn.prepareStatement(updateSql);
        updatePst.setString(1, newPin);
        updatePst.setInt(2, loggedInAccount);
        int rows = updatePst.executeUpdate();

        if (rows > 0) {
            System.out.println("✅ PIN changed successfully!");
        } else {
            System.out.println("❌ Failed to change PIN!");
        }
    }

    // Delete Account
    private void deleteAccount() throws SQLException {
        sc.nextLine(); // clear buffer
        System.out.print("⚠️ Are you sure you want to delete your account? (yes/no): ");
        String confirm = sc.nextLine();

        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("✅ Account deletion cancelled.");
            return;
        }

        System.out.print("Enter your 4-digit PIN to confirm: ");
        String pin = sc.nextLine();

        String sql = "DELETE FROM accounts WHERE account_no = ? AND pin = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, loggedInAccount);
        pst.setString(2, pin);
        int rows = pst.executeUpdate();

        if (rows > 0) {
            System.out.println("🗑️ Account deleted successfully!");
            loggedInAccount = -1;
            System.out.println("👋 You have been logged out.");
        } else {
            System.out.println("❌ Incorrect PIN or account not found!");
        }
    }

    // View Account Details
    private void viewAccountDetails() throws SQLException {
        String sql = "SELECT account_no, name, username, balance FROM accounts WHERE account_no = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, loggedInAccount);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            System.out.println("\n===== ACCOUNT DETAILS =====");
            System.out.println("👤 Name: " + rs.getString("name"));
            System.out.println("📄 Username: " + rs.getString("username"));
            System.out.println("🏦 Account Number: " + rs.getInt("account_no"));
            System.out.println("💰 Balance: ₹" + rs.getDouble("balance"));
            System.out.println("============================");
        } else {
            System.out.println("❌ Account not found!");
        }
    }
}
