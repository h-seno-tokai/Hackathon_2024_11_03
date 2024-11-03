package WebApp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Databaseクラス
 */
public class Database {
    private static Connection connection;

    public static void initializeDatabase() {
        try {
            // データベースへの接続を作成
            connection = DriverManager.getConnection("jdbc:h2:./data/semnetdb");


            // linksテーブルの作成
            Statement stmt = connection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS links (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "label VARCHAR(255), " +
                    "tail VARCHAR(255), " +
                    "head VARCHAR(255))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
