package edu.touro.mco364;

import java.sql.*;

public class EmailUploader {
	private static String url = "******;useBulkCopyForBatchInsert=true",
			username = "******",
			password = "******",
			baseQuery = "Insert Into Tager.dbo.Emails Values (?)";
	private static Connection conn;
	private static PreparedStatement insertStatement;

	public static void uploadEmails() {
		try {
			conn = DriverManager.getConnection(url, username, password);
			insertStatement = conn.prepareStatement(baseQuery);

			for(String email : Main.emailsFound) {
					insertStatement.setString(1, email);
					insertStatement.addBatch();
			}
			insertStatement.executeBatch();
			conn.commit();
			conn.close();
		}
		catch(SQLException e) {
//			e.printStackTrace();
		}
	}
}
