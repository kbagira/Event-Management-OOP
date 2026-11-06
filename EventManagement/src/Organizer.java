
public class Organizer {
    public static final String DEFAULT_NAME = "Global Media";

    private final String name;

    public Organizer(String name) { this.name = name; }

    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Organizer && name.equals(((Organizer) o).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }

    @Override
    public String toString() { return "Organizer{" + name + "}"; }
}
