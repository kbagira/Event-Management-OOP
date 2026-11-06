
public class UserAccount {
    private final String username;
    private final String password;
    private final Role role;

    public UserAccount(String username, String password, Role role) {
        this.username = username.trim();
        this.password = password;
        this.role = role;
    }


    public static String toKey(String username) { return username.trim().toLowerCase(); }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserAccount && toKey(username).equals(toKey(((UserAccount) o).username));
    }

    @Override
    public int hashCode() { return toKey(username).hashCode(); }

    @Override
    public String toString() { return username + " [" + role + "]"; }
}
