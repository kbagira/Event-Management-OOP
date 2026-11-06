import java.util.Comparator;


public class EventComparators {

    public static final Comparator<Event> BY_PRICE =
            Comparator.comparingDouble(Event::getTicketPrice).thenComparing(Comparator.naturalOrder());


    public static final Comparator<Event> BY_DATE =
            Comparator.comparing(Event::getLocalDate).thenComparing(Comparator.naturalOrder());


    public static final Comparator<Event> BY_LOCATION =
            Comparator.comparing(Event::getLocation, String.CASE_INSENSITIVE_ORDER).thenComparing(Comparator.naturalOrder());

    public static final Comparator<Event> BY_CATEGORY_THEN_PRICE_DESC =
            Comparator.comparing(Event::getCategory)
                    .thenComparing(Event::getTicketPrice, Comparator.reverseOrder())
                    .thenComparing(Comparator.naturalOrder());
}
