import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public abstract class Event implements Comparable<Event>, Cancellable {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final String id;
    private final String eventName;
    private LocalDate date;          // неверная дата -> DateTimeParseException уже при разборе
    private String location;
    private double ticketPrice;
    private final Organizer organizer;
    private final Sponsor sponsor;
    private final EventSchedule schedule = new EventSchedule("18:00 - 22:00"); // композиция
    private boolean cancelled;

    public Event() {
        this("Event", "01.01.2026", "Bishkek arena", 0, new Organizer("TBA"), new Sponsor("TBA"));
    }

    public Event(String name, String date, String location, double price, Organizer organizer, Sponsor sponsor) {
        this.eventName = name;
        this.id = toId(name);
        this.date = LocalDate.parse(date, DATE_FORMAT);
        this.location = location;
        this.ticketPrice = price;
        this.organizer = organizer;
        this.sponsor = sponsor;
    }

    public static String toId(String name) { return name.trim().toLowerCase(); }

    protected void ensureNotCancelled() {
        if (cancelled) throw new IllegalStateException("Event '" + eventName + "' is cancelled.");
    }

    public String getId() { return id; }
    public String getEventName() { return eventName; }
    public String getDate() { return date.format(DATE_FORMAT); }
    public LocalDate getLocalDate() { return date; }
    public void setDate(String d) { this.date = LocalDate.parse(d.trim(), DATE_FORMAT); }
    public String getLocation() { return location; }
    public void setLocation(String l) { this.location = l; }
    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double p) { this.ticketPrice = p; }
    public Organizer getOrganizer() { return organizer; }
    public Sponsor getSponsor() { return sponsor; }
    public EventSchedule getSchedule() { return schedule; }
    public boolean isCancelled() { return cancelled; }
    void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    @Override
    public void cancelEvent(String reason) {
        ensureNotCancelled();
        cancelled = true;
        System.out.println("'" + eventName + "' is CANCELLED! Reason: " + reason);
    }

    @Override
    public double calculateRefundAmount(int ticketsBought, double ticketPrice) {
        return ticketsBought * ticketPrice;
    }

    public void bookTicket() {
        ensureNotCancelled();
        System.out.println("Ticket successfully booked for '" + eventName + "' at " + location + ". Price: " + ticketPrice + " som");
    }

    public void displayInfo() {
        System.out.println("Name: " + eventName + (cancelled ? "  [CANCELLED]" : ""));
        System.out.println("Date: " + getDate());
        System.out.println("Location: " + location);
        System.out.println("Ticket Price: " + ticketPrice + " som");
        System.out.println("Organizer: " + organizer.getName() + " | Sponsor: " + sponsor.getName());
        schedule.printSchedule();
    }

    public void notifyParticipants(NotificationService notifier, String message) {
        notifier.sendNotification("Alert for " + eventName + ": " + message);
    }

    public abstract String getCategory();

    public abstract double calculateTotalRevenue(int attendees, int secondaryMetric);

    public abstract void advertiseEvent();

    @Override
    public boolean equals(Object o) {
        return o instanceof Event && id.equals(((Event) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public int compareTo(Event other) { return id.compareTo(other.id); }

    @Override
    public String toString() {
        return eventName + " (" + getDate() + ", " + location + ", " + ticketPrice + " som)";
    }
}
