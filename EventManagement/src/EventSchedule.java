import java.util.Objects;

public class EventSchedule {
    private final String time;

    public EventSchedule(String time) { this.time = time; }

    public String getTime() { return time; }

    public void printSchedule() { System.out.println("Schedule Time: " + time); }

    @Override
    public boolean equals(Object o) {
        return o instanceof EventSchedule && Objects.equals(time, ((EventSchedule) o).time);
    }

    @Override
    public int hashCode() { return Objects.hash(time); }
}
