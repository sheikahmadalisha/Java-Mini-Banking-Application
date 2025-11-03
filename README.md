# Java Mini Banking Application

A console-based banking management system built using Java and MySQL.
Developed with core features including account creation, deposits, withdrawals, and fund transfers, with MySQL integration for secure and efficient data handling.

---

## **Features**
- Create a new account with a unique username
- Login using username and 4-digit PIN
- Deposit money into your account
- Withdraw money from your account
- Transfer funds to another account
- Check account balance
- Change PIN
- Delete account
- View account details

---

## **Technology Stack**
- **Language:** Java (Console-based)
- **Database:** MySQL
- **Database Connectivity:** JDBC

---

## **Database Setup**
1. Create a MySQL database:

```sql
CREATE DATABASE gkbankdb;

USE gkbankdb;

CREATE TABLE accounts (
    account_no INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    pin VARCHAR(10) NOT NULL,
    balance DOUBLE DEFAULT 0.0
) AUTO_INCREMENT = 1001;
```
2. Update database credentials in DatabaseConnection.java:

   ```java
   String url = "jdbc:mysql://localhost:3306/gkbankdb";
   String user = "root"; // replace with your MySQL username
   String pass = "your_password_here"; // replace with your MySQL password
   ```

3. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/yourusername/Java-Mini-Banking-Application.git
   ```



