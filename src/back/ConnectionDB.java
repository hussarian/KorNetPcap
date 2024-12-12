package back;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {
    // 데이터베이스 접속 정보
    private static final String URL = "jdbc:mariadb://221.168.128.40:3306/network";
    private static final String USER = "root"; // DB 사용자명
    private static final String PASSWORD = "qwer1234"; // DB 비밀번호

    // 데이터베이스 연결 메서드
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}