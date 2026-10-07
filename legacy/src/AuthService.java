import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {
    public UserAccount login(String username, String password) {
        String sql = "SELECT user_id, username, role, empid FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Integer empid = null;
                Object empObj = rs.getObject("empid");

                if (empObj != null) {
                    empid = rs.getInt("empid");
                }

                return new UserAccount(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        Role.valueOf(rs.getString("role")),
                        empid
                );
            }

        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }
}