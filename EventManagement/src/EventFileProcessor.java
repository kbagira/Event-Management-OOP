import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class EventFileProcessor {
    private static final Path FILE = FileStorage.DATA_DIR.resolve("events.txt");

    public static int loadEvents(EventManagerService service) {
        int loaded = 0;
        try {
            for (String line : FileStorage.readLines(FILE)) {
                try {
                    service.addEvent(parseEvent(line));
                    loaded++;
                } catch (RuntimeException e) {
                    System.out.println("Skipping event line '" + line + "': " + e);
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot read " + FILE + ": " + e.getMessage());
        }
        return loaded;
    }

    public static void saveEvents(List<Event> events) {
        List<String> lines = new ArrayList<>();
        for (Event event : events) lines.add(toLine(event));
        try {
            FileStorage.writeLines(FILE, lines);
        } catch (IOException e) {
            System.out.println(" Cannot write " + FILE + ": " + e.getMessage());
        }
    }

    static Event parseEvent(String line) {
        String[] p = line.split("\\s*,\\s*");
        String type = p[0].toUpperCase();
        String name = p[1], date = p[2], location = p[3];
        double price = Double.parseDouble(p[4]);
        Organizer organizer = new Organizer(p[p.length - 2]);
        Sponsor sponsor = new Sponsor(p[p.length - 1]);

        boolean hasFlag = p[5].equals("0") || p[5].equals("1");
        int i = hasFlag ? 6 : 5;

        Event event;
        if (type.equals("CONCERT")) {
            event = new Concert(name, date, location, price, p[i], p[i + 1], Double.parseDouble(p[i + 2]), organizer, sponsor);
        } else if (type.equals("CONFERENCE")) {
            event = new Conference(name, date, location, price, p[i], p[i + 1], organizer, sponsor);
        } else if (type.equals("SPORTS")) {
            event = new SportsMatch(name, date, location, price, p[i], p[i + 1], Double.parseDouble(p[i + 2]), organizer, sponsor);
        } else {
            throw new IllegalArgumentException("unknown event type " + type);
        }
        event.setCancelled(p[5].equals("1"));
        return event;
    }

    static String toLine(Event e) {
        String head = e.getEventName() + "," + e.getDate() + "," + e.getLocation() + "," + num(e.getTicketPrice())
                + "," + (e.isCancelled() ? 1 : 0);
        String tail = "," + e.getOrganizer().getName() + "," + e.getSponsor().getName();

        if (e instanceof Concert) {
            Concert c = (Concert) e;
            return "CONCERT," + head + "," + c.getArtist() + "," + c.getMusicGenre() + "," + num(c.getMerchPrice()) + ",0" + tail; // ",0": резервное поле формата файла
        } else if (e instanceof Conference) {
            Conference c = (Conference) e;
            return "CONFERENCE," + head + "," + c.getSpeaker() + "," + c.getTopic() + tail;
        }
        SportsMatch m = (SportsMatch) e;
        return "SPORTS," + head + "," + m.getHomeTeam() + "," + m.getAwayTeam() + "," + num(m.getStreamPrice()) + tail;
    }

    private static String num(double value) {
        return value == (long) value ? String.valueOf((long) value) : String.valueOf(value);
    }
}
