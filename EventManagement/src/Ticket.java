
public class Ticket implements Comparable<Ticket> {
    private final long id;
    private final String username;
    private String eventName; // меняется при переименовании события
    private final double price;

    public Ticket(long id, String username, String eventName, double price) {
        this.id = id;
        this.username = username;
        this.eventName = eventName;
        this.price = price;
    }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public double getPrice() { return price; }


    @Override
    public int compareTo(Ticket other) { return Long.compare(id, other.id); }

    @Override
    public boolean equals(Object o) { return o instanceof Ticket && id == ((Ticket) o).id; }

    @Override
    public int hashCode() { return Long.hashCode(id); }

    @Override
    public String toString() { return "Ticket#" + id + "{" + username + " -> " + eventName + ", " + price + " som}"; }
}
