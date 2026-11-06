import java.util.List;

/** Автоматический сценарий для отчёта и защиты. Работает только в памяти (файлы не трогает). */
public class DemoScenario {
    public static void main(String[] args) {
        EventManagerService service = new EventManagerService();

        title("1. Registration (users and admins)");
        service.addUser(new UserAccount("admin", "admin123", Role.ADMIN));
        System.out.println("register USER  -> " + service.register("user", "user123", Role.USER, ""));
        System.out.println("register USER  -> " + service.register("alice", "secr8pass", Role.USER, ""));
        System.out.println("register ADMIN -> " + service.register("boss", "pass1234", Role.ADMIN, EventManagerService.ADMIN_CODE));
        expectError("ADMIN with wrong code", () -> service.register("hacker", "pass1234", Role.ADMIN, "0000"));
        expectError("Duplicate login (ALICE = alice)", () -> service.register("ALICE", "other123", Role.USER, ""));
        System.out.println("login user/user123 -> " + service.authenticate("user", "user123"));
        System.out.println("login USER/wrong   -> " + service.authenticate("USER", "wrong"));
        System.out.println("Users by role: ADMIN=" + service.getUsersByRole(Role.ADMIN) + " USER=" + service.getUsersByRole(Role.USER));

        title("2. Events (loaded like from events.txt) and displayInfo");
        Organizer media = new Organizer("Global Media");
        Sponsor tech = new Sponsor("TechCorp");
        service.addEvent(EventFileProcessor.parseEvent("CONCERT,Jazz Night,20.10.2026,Art Hall,3000,0,Local Band,Jazz,500,0,Global Media,TechCorp"));
        service.addEvent(EventFileProcessor.parseEvent("CONCERT,Shakira Live,18.10.2026,Bishkek Arena,15000,0,Shakira,Pop,2000,0,Global Media,TechCorp"));
        service.addEvent(EventFileProcessor.parseEvent("CONFERENCE,Tech Summit,12.10.2026,Tech Hub,1000,0,John Doe,AI,Global Media,TechCorp"));
        service.addEvent(EventFileProcessor.parseEvent("SPORTS,Derby,01.12.2026,Dolen Omurzakov,700,0,Dordoi,Abdysh-Ata,150,Sport Union,Beeline"));
        Event derby = service.getEvent("Derby");
        derby.displayInfo();
        System.out.println("Default constructors: " + new Concert() + " | " + new Conference() + " | " + new SportsMatch());

        title("3. Primary key index and categorization indexes");
        System.out.println("findEvent(\"shakira live\") -> " + service.findEvent("shakira live"));
        System.out.println("findEvent(\"Unknown\")      -> " + service.findEvent("Unknown"));
        for (String category : service.getCategories()) {
            System.out.println(category + " -> " + service.getEventsByCategory(category));
        }
        System.out.println("Organizers: " + service.getOrganizers() + " | Sport Union -> " + service.getEventsByOrganizer("Sport Union"));
        System.out.println("Sponsors:   " + service.getSponsors() + " | TechCorp -> " + service.getEventsBySponsor("TechCorp"));

        title("4. Sorting: Comparable + 4 Comparators + filter");
        print("Natural (name)   ", service.getNaturalSorted());
        print("BY_PRICE         ", service.getSorted(EventComparators.BY_PRICE));
        print("BY_DATE          ", service.getSorted(EventComparators.BY_DATE));
        print("BY_LOCATION      ", service.getSorted(EventComparators.BY_LOCATION));
        print("BY_CATEGORY+PRICE", service.getSorted(EventComparators.BY_CATEGORY_THEN_PRICE_DESC));
        print("price <= 1500    ", service.filterByMaxPrice(1500));

        title("5. equals / hashCode contract");
        Event duplicate = new Conference("TECH SUMMIT", "01.01.2027", "Other", 1, "X", "Y", media, tech);
        Event original = service.getEvent("Tech Summit");
        System.out.println("Same business key -> equals=" + duplicate.equals(original)
                + ", hash equal=" + (duplicate.hashCode() == original.hashCode()) + ", compareTo=" + duplicate.compareTo(original));
        System.out.println("Concert() equals Conference() (both have key 'event'): " + new Concert().equals(new Conference()));
        expectError("addEvent(duplicate)", () -> service.addEvent(duplicate));

        title("6. Queue: USER submits, ADMIN polls (REFUND has priority)");
        service.submitRequest(RequestType.PURCHASE, "user", "Jazz Night");
        service.submitRequest(RequestType.PURCHASE, "alice", "Jazz Night");
        service.submitRequest(RequestType.PURCHASE, "user", "Tech Summit");
        service.submitRequest(RequestType.PURCHASE, "alice", "Shakira Live");
        service.submitRequest(RequestType.PURCHASE, "alice", "Derby");
        System.out.println("Queue size: " + service.getQueueSize() + ", head (peek): " + service.peekNextRequest());
        System.out.println(service.processNextRequest());
        System.out.println(service.processNextRequest());
        System.out.println("-- user asks a refund while 3 purchases are still waiting:");
        service.submitRequest(RequestType.REFUND, "user", "Jazz Night");
        for (TicketRequest r : service.getPendingRequests()) System.out.println("   " + r);
        for (String line : service.processAllRequests()) System.out.println(line);
        System.out.println("poll() on empty queue -> " + service.processNextRequest());
        System.out.println("Tickets of alice: " + service.getTicketsOf("alice"));
        System.out.println("Tickets of 'Jazz Night': " + service.getTicketsOfEvent("Jazz Night"));

        title("7. Event-specific methods (Concert / Conference / SportsMatch + Streamable)");
        Concert jazz = (Concert) service.getEvent("Jazz Night");
        jazz.advertiseEvent();
        jazz.requestSong("Summertime");
        jazz.requestSong("Blue in Green");
        System.out.println("Song requests: " + jazz.getSongRequests());
        jazz.setArtist("Jazz Quartet");
        jazz.setMusicGenre("Smooth Jazz");
        jazz.setMerchPrice(600);
        System.out.println("After setters: " + jazz.getArtist() + ", " + jazz.getMusicGenre() + ", merch " + jazz.getMerchPrice());
        System.out.println("Concert revenue (1 ticket, 10 merch): " + jazz.calculateTotalRevenue(1, 10));
        Conference summit = (Conference) original;
        summit.advertiseEvent();
        summit.openRegistration(200);
        summit.setSpeaker("Jane Roe");
        summit.setTopic("Machine Learning");
        System.out.println("After setters: " + summit.getSpeaker() + ", " + summit.getTopic());
        System.out.println("Conference revenue (50 guests, 10 sponsored): " + summit.calculateTotalRevenue(50, 10));
        SportsMatch match = (SportsMatch) derby;
        match.advertiseEvent();
        match.updateScore(2, 1);
        match.checkViewerCount(service.getTicketsOfEvent("Derby").size());
        match.sendChatAnnouncement("Second half starts!");
        match.setHomeTeam("Alga");
        match.setAwayTeam("Neftchi");
        match.setStreamPrice(200);
        System.out.println("After setters: " + match.getHomeTeam() + " vs " + match.getAwayTeam() + ", stream " + match.getStreamPrice());
        System.out.println("Match revenue (1 ticket, 5 streams): " + match.calculateTotalRevenue(1, 5));

        title("8. Setters and notifications");
        original.setTicketPrice(1200);
        original.setDate("15.10.2026");
        original.setLocation("Hyatt Hall");
        System.out.println(original);
        System.out.println("Notified ticket holders of Jazz Night: " + service.notifyParticipants("Jazz Night", "doors open at 17:30"));

        title("9. Synchronized UPDATE (rename moves tickets, requests and indexes)");
        service.submitRequest(RequestType.PURCHASE, "alice", "Tech Summit");
        service.updateEvent("Tech Summit", new Conference("AI Summit", "12.10.2026", "Tech Hub", 1200, "John Doe", "AI", media, tech));
        System.out.println("old key found: " + (service.findEvent("Tech Summit") != null) + " | new key found: " + (service.findEvent("AI Summit") != null));
        System.out.println("user tickets: " + service.getTicketsOf("user"));
        System.out.println("pending request renamed: " + service.peekNextRequest());
        System.out.println(service.processNextRequest());

        title("10. Cancel event (Cancellable) and synchronized REMOVE");
        service.submitRequest(RequestType.PURCHASE, "user", "Shakira Live"); // будет отклонена: событие отменят раньше
        int shakiraTickets = service.getTicketsOfEvent("Shakira Live").size();
        double cancelRefund = service.cancelEvent("Shakira Live", "Artist is ill");
        System.out.println("Cancelled Shakira Live, refunded " + shakiraTickets + " ticket(s), " + cancelRefund + " som");
        System.out.println("alice tickets after cancel: " + service.getTicketsOf("alice"));
        System.out.println(service.processNextRequest());
        expectError("Cancel twice", () -> service.cancelEvent("Shakira Live", "again"));
        expectError("Buy ticket for cancelled event", () -> service.submitRequest(RequestType.PURCHASE, "alice", "Shakira Live"));
        expectError("Update cancelled event", () -> service.updateEvent("Shakira Live", new Concert()));
        int derbyTickets = service.getTicketsOfEvent("Derby").size();
        System.out.println("Removed Derby, refunded " + derbyTickets + " ticket(s), " + service.removeEvent("Derby") + " som");
        System.out.println("categories now: " + service.getCategories() + " | sponsors now: " + service.getSponsors());

        title("11. Error handling");
        expectError("Refund without a ticket", () -> service.submitRequest(RequestType.REFUND, "user", "Jazz Night"));
        expectError("Request for missing event", () -> service.submitRequest(RequestType.PURCHASE, "alice", "Nope"));
        expectError("Request from unknown user", () -> service.submitRequest(RequestType.PURCHASE, "ghost", "Jazz Night"));
        expectError("Invalid date", () -> new Concert("X", "31.13.2026", "Y", 1, "a", "b", 1, media, tech));

        title("12. Analytics");
        System.out.print(service.buildReport());
    }

    private static void title(String text) {
        System.out.println("\n=== " + text + " ===");
    }

    private static void print(String label, List<Event> events) {
        StringBuilder sb = new StringBuilder(label + ": ");
        for (Event e : events) sb.append(e.getEventName()).append(" | ");
        System.out.println(sb);
    }

    /** Выполняет действие, которое должно упасть, и печатает ошибку. */
    private static void expectError(String label, Runnable action) {
        try {
            action.run();
            System.out.println(label + " -> (no error)");
        } catch (RuntimeException e) {
            System.out.println(label + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
