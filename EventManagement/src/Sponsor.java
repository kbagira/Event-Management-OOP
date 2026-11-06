
public class Sponsor {
    public static final String DEFAULT_NAME = "TechCorp";

    private final String name;

    public Sponsor(String name) { this.name = name; }

    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Sponsor && name.equals(((Sponsor) o).name);
    }

    @Override
    public int hashCode() { return name.hashCode(); }

    @Override
    public String toString() { return "Sponsor{" + name + "}"; }
}
