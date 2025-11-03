package com.learn.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
	
	public static Connection getConnection() {
		String url = "jdbc:mysql://localhost:3306/gkbankdb";
		String user = "root";
		String pass = "1234567890";
		Connection con = null;
		
		try { 
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(url, user, pass);
			System.out.println("✅ Connected to database!");
			
		} catch(Exception e) {
			System.out.println("❌ Exception occurred while connecting to database");
			e.printStackTrace();
		}
		return con;
	}
}
