public class UserAccount {
    private int userId;
    private String username;
    private Role role;
    private Integer empid;

    public UserAccount(int userId, String username, Role role, Integer empid) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.empid = empid;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public Integer getEmpid() {
        return empid;
    }
}