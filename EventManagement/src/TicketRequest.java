
public class TicketRequest {
    private final long sequence;
    private final RequestType type;
    private final String username;
    private String eventName; // меняется при переименовании события

    public TicketRequest(long sequence, RequestType type, String username, String eventName) {
        this.sequence = sequence;
        this.type = type;
        this.username = username;
        this.eventName = eventName;
    }

    public long getSequence() { return sequence; }
    public RequestType getType() { return type; }
    public String getUsername() { return username; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    @Override
    public boolean equals(Object o) { return o instanceof TicketRequest && sequence == ((TicketRequest) o).sequence; }

    @Override
    public int hashCode() { return Long.hashCode(sequence); }

    @Override
    public String toString() { return "Request#" + sequence + " " + type + " " + username + " -> " + eventName; }
}
