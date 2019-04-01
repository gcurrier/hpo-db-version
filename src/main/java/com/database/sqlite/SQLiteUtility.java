package com.database.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteUtility {

    private Connection conn;
    private String url;
    private String dbPath;
    private String dbName;

    public SQLiteUtility(String dbPath, String dbFileName) {
        this.url = String.format("jdbc:sqlite:%s", dbPath);
        this.dbPath = dbPath;
        this.dbName = dbFileName.replace(".db", "");
    }

    public Connection getConnection() {
        return this.conn;
    }

    public void connect() {
        String stmt = String.format("ATTACH DATABASE \"%s\" AS %s", this.dbPath, this.dbName);
        try {
            this.conn = DriverManager.getConnection(this.url);
            this.conn.prepareStatement(stmt).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
