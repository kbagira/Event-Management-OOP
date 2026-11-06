import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class UserFileHandler {
    private static final Path USERS_FILE = FileStorage.DATA_DIR.resolve("users.txt");
    private static final Path TICKETS_FILE = FileStorage.DATA_DIR.resolve("tickets.txt");

    public static int loadUsers(EventManagerService service) {
        int loaded = 0;
        try {
            for (String line : FileStorage.readLines(USERS_FILE)) {
                try {
                    String[] p = line.split("\\s*,\\s*");
                    service.addUser(new UserAccount(p[0], p[1], Role.valueOf(p[2].toUpperCase())));
                    loaded++;
                } catch (RuntimeException e) {
                    System.out.println(" Skipping user line '" + line + "': " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot load users: " + e.getMessage());
        }
        return loaded;
    }

    public static void saveUsers(List<UserAccount> users) {
        List<String> lines = new ArrayList<>();
        for (UserAccount u : users) lines.add(u.getUsername() + "," + u.getPassword() + "," + u.getRole());
        try {
            FileStorage.writeLines(USERS_FILE, lines);
        } catch (IOException e) {
            System.out.println("[ERROR] Cannot save users: " + e.getMessage());
        }
    }

    public static int loadTickets(EventManagerService service) {
        int loaded = 0;
        try {
            for (String line : FileStorage.readLines(TICKETS_FILE)) {
                try {
                    String[] p = line.split("\\s*,\\s*");
                    if (service.restoreTicket(p[0], p[1], Double.parseDouble(p[2]))) loaded++;
                    else System.out.println(" Ticket skipped (unknown user or event): " + line);
                } catch (RuntimeException e) {
                    System.out.println("Skipping ticket line '" + line + "': " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println(" Cannot load tickets: " + e.getMessage());
        }
        return loaded;
    }

    public static void saveTickets(List<Ticket> tickets) {
        List<String> lines = new ArrayList<>();
        for (Ticket t : tickets) {
            lines.add(String.format(Locale.ROOT, "%s,%s,%.1f", t.getUsername(), t.getEventName(), t.getPrice()));
        }
        try {
            FileStorage.writeLines(TICKETS_FILE, lines);
        } catch (IOException e) {
            System.out.println("Cannot save tickets: " + e.getMessage());
        }
    }
}
