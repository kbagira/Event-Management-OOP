import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        EventManagerService service = new EventManagerService();
        UserFileHandler.loadUsers(service);
        EventFileProcessor.loadEvents(service);
        UserFileHandler.loadTickets(service);

        ConsoleInput in = new ConsoleInput(new Scanner(System.in));
        try {
            UserAccount user = startScreen(service, in);
            while (user != null) {
                System.out.println("\nLogged in as: " + user.getUsername());
                runSession(service, in, user);
                user = startScreen(service, in);
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nInput stream closed.");
        }
        System.out.println("Shutting down. Goodbye!");
    }


    private static UserAccount startScreen(EventManagerService service, ConsoleInput in) {
        while (true) {
            System.out.println("1. Login");
            System.out.println("2. Register as USER");
            System.out.println("3. Register as ADMIN (secret code required)");
            System.out.println("0. Exit");
            String choice = in.readLine("Select an option: ");
            try {
                if (choice.equals("0")) {
                    return null;
                } else if (choice.equals("1")) {
                    UserAccount user = service.authenticate(in.readLine("Username: "), in.readLine("Password: "));
                    if (user != null) return user;
                    System.out.println("Incorrect username or password.");
                } else if (choice.equals("2")) {
                    return register(service, in, Role.USER);
                } else if (choice.equals("3")) {
                    return register(service, in, Role.ADMIN);
                } else {
                    System.out.println("Invalid choice.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR" + e.getMessage());
            }
        }
    }

    private static UserAccount register(EventManagerService service, ConsoleInput in, Role role) {
        String username = in.readLine("Choose username: ");
        String password = in.readLine("Choose password: ");
        String code = role == Role.ADMIN ? in.readLine("Admin secret code: ") : "";
        UserAccount user = service.register(username, password, role, code);
        UserFileHandler.saveUsers(service.getAllUsers());
        System.out.println(" Account '" + user.getUsername() + "' registered as " + role + ".");
        return user;
    }



    private static void runSession(EventManagerService service, ConsoleInput in, UserAccount user) {
        while (true) {
            printMenu(user.getRole());
            String choice = in.readLine("Select an option: ");
            if (choice.equals("0")) {
                System.out.println("Logged out.");
                return;
            }
            try {
                dispatch(choice, service, in, user);
            } catch (IllegalArgumentException | IllegalStateException | DateTimeParseException e) {
                System.out.println("ERROR " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void dispatch(String choice, EventManagerService service, ConsoleInput in, UserAccount user) {
        switch (choice) {
            case "1": viewAllEvents(service, in); break;
            case "2": filterByPrice(service, in); break;
            case "3": findEventByName(service, in); break;
            case "4": viewByCategory(service, in); break;
            case "5": viewByOrganizerOrSponsor(service, in); break;
            default:
                if (user.getRole() == Role.ADMIN) dispatchAdmin(choice, service, in);
                else dispatchUser(choice, service, in, user);
        }
    }

    private static void dispatchUser(String choice, EventManagerService service, ConsoleInput in, UserAccount user) {
        switch (choice) {
            case "6": submitRequest(RequestType.PURCHASE, service, in, user); break;
            case "7": submitRequest(RequestType.REFUND, service, in, user); break;
            case "8": showMyTickets(service, user); break;
            case "9": eventExtras(service, in, user); break;
            default: System.out.println(" Invalid choice.");
        }
    }

    private static void dispatchAdmin(String choice, EventManagerService service, ConsoleInput in) {
        switch (choice) {
            case "6": addEvent(service, in); break;
            case "7": updateEvent(service, in); break;
            case "8": removeEvent(service, in); break;
            case "9": manageEvent(service, in); break;
            case "10": processNext(service); break;
            case "11": processAll(service); break;
            case "12": showQueue(service); break;
            case "13": System.out.print(service.buildReport()); break;
            case "14": showUsers(service); break;
            default: System.out.println("[ERROR] Invalid choice.");
        }
    }

    private static void printMenu(Role role) {
        System.out.println("MAIN MENU (" + role + ")");
        System.out.println("1. View all events (choose sorting)");
        System.out.println("2. Filter events by max price");
        System.out.println("3. Find event by name");
        System.out.println("4. View events by category");
        System.out.println("5. View events by organizer / sponsor");
        if (role == Role.USER) {
            System.out.println("6. Request ticket purchase (goes to queue)");
            System.out.println("7. Request ticket refund (goes to queue)");
            System.out.println("8. View my tickets and pending requests");
            System.out.println("9. Event extras (song request / live match stream)");
        } else {
            System.out.println("6. Add new event");
            System.out.println("7. Update event (re-enter all fields)");
            System.out.println("8. Remove event");
            System.out.println("9. Manage event (cancel, advertise, price, date, ...)");
            System.out.println("10. Process next request in queue");
            System.out.println("11. Process ALL requests in queue");
            System.out.println("12. View pending queue");
            System.out.println("13. Analytics report");
            System.out.println("14. View registered users");
        }
        System.out.println("0. Logout");
    }


    private static void viewAllEvents(EventManagerService service, ConsoleInput in) {
        System.out.println("Sort by: 1) Name  2) Price  3) Date  4) Location  5) Category + price (desc)");
        List<Event> events;
        switch (in.readLine("Choose sorting [1]: ")) {
            case "2": events = service.getSorted(EventComparators.BY_PRICE); break;
            case "3": events = service.getSorted(EventComparators.BY_DATE); break;
            case "4": events = service.getSorted(EventComparators.BY_LOCATION); break;
            case "5": events = service.getSorted(EventComparators.BY_CATEGORY_THEN_PRICE_DESC); break;
            default: events = service.getNaturalSorted();
        }
        System.out.println("\nAll Events");
        printEvents(events, "No events available in the system.");
    }

    private static void filterByPrice(EventManagerService service, ConsoleInput in) {
        double maxPrice = in.readDouble("Enter maximum ticket price: ");
        System.out.println("\nEvents up to " + maxPrice + " som");
        printEvents(service.filterByMaxPrice(maxPrice), "No events found within this price range.");
    }

    private static void findEventByName(EventManagerService service, ConsoleInput in) {
        Event event = service.findEvent(in.readLine("Enter event name: "));
        if (event == null) {
            System.out.println("Event not found.");
            return;
        }
        event.displayInfo();
        System.out.println("Tickets sold: " + service.getTicketsOfEvent(event.getEventName()).size());
    }

    private static void viewByCategory(EventManagerService service, ConsoleInput in) {
        System.out.println("Available categories: " + service.getCategories());
        printEvents(service.getEventsByCategory(in.readLine("Enter category: ")), "No events in this category.");
    }

    private static void viewByOrganizerOrSponsor(EventManagerService service, ConsoleInput in) {
        if (in.readLine("1) By organizer  2) By sponsor [1]: ").equals("2")) {
            System.out.println("Sponsors: " + service.getSponsors());
            printEvents(service.getEventsBySponsor(in.readLine("Enter sponsor: ")), "No events for this sponsor.");
        } else {
            System.out.println("Organizers: " + service.getOrganizers());
            printEvents(service.getEventsByOrganizer(in.readLine("Enter organizer: ")), "No events for this organizer.");
        }
    }

    private static void printEvents(Collection<Event> events, String emptyMessage) {
        if (events.isEmpty()) System.out.println(emptyMessage);
        for (Event e : events) {
            System.out.println(e.getEventName() + " (" + e.getDate() + ") - " + e.getLocation()
                    + " [" + e.getTicketPrice() + " som] {" + e.getCategory() + "}" + (e.isCancelled() ? " CANCELLED" : ""));
        }
    }


    private static void submitRequest(RequestType type, EventManagerService service, ConsoleInput in, UserAccount user) {
        TicketRequest request = service.submitRequest(type, user.getUsername(), in.readLine("Exact event name: "));
        System.out.println(request + " submitted. Requests waiting in queue: " + service.getQueueSize());
    }

    private static void showMyTickets(EventManagerService service, UserAccount user) {
        System.out.println("\nMy Purchased Tickets");
        List<Ticket> tickets = service.getTicketsOf(user.getUsername());
        double total = 0;
        for (Ticket t : tickets) {
            System.out.println(t);
            total += t.getPrice();
        }
        System.out.println(tickets.isEmpty() ? "You have no purchased tickets yet." : "Total spent: " + total + " som");

        System.out.println("\nMy Pending Requests");
        boolean any = false;
        for (TicketRequest r : service.getPendingRequests()) {
            if (r.getUsername().equals(user.getUsername())) {
                System.out.println(r.getType() + " - " + r.getEventName());
                any = true;
            }
        }
        if (!any) System.out.println("No pending requests.");
    }

    private static void eventExtras(EventManagerService service, ConsoleInput in, UserAccount user) {
        Event event = service.getEvent(in.readLine("Exact event name: "));
        if (event instanceof Concert) {
            Concert concert = (Concert) event;
            concert.requestSong(in.readLine("Song to request for " + concert.getArtist() + ": "));
        } else if (event instanceof SportsMatch) {
            SportsMatch match = (SportsMatch) event;
            if (in.readLine("1) Live viewers  2) Send chat message [1]: ").equals("2")) {
                match.sendChatAnnouncement(user.getUsername() + ": " + in.readLine("Message: "));
            } else {
                match.checkViewerCount(service.getTicketsOfEvent(match.getEventName()).size());
            }
        } else {
            System.out.println("This event has no extra features for guests.");
        }
    }


    private static void addEvent(EventManagerService service, ConsoleInput in) {
        service.addEvent(readEvent(in));
        persist(service);
        System.out.println(" Event added and saved.");
    }

    private static void updateEvent(EventManagerService service, ConsoleInput in) {
        String targetName = in.readLine("Exact name of the event to update: ");
        service.getEvent(targetName); // ошибка "не найдено" до ввода всех полей
        System.out.println("Enter new details for the event:");
        service.updateEvent(targetName, readEvent(in));
        persist(service);
        System.out.println(" Event updated.");
    }

    private static void removeEvent(EventManagerService service, ConsoleInput in) {
        Event event = service.getEvent(in.readLine("Exact event name to remove: "));
        int tickets = service.getTicketsOfEvent(event.getEventName()).size();
        double refunded = service.removeEvent(event.getEventName());
        persist(service);
        System.out.println("Event '" + event.getEventName() + "' removed. Tickets refunded: " + tickets + " (" + refunded + " som).");
    }

    private static void manageEvent(EventManagerService service, ConsoleInput in) {
        Event event = service.getEvent(in.readLine("Exact event name: "));
        String name = event.getEventName();
        int sold = service.getTicketsOfEvent(name).size();

        System.out.println("Manage " + name );
        System.out.println("1. Advertise   2. Cancel event (refund all tickets)   3. Change ticket price");
        System.out.println("4. Change date   5. Change location   6. Notify ticket holders   7. Show revenue");
        if (event instanceof Concert) System.out.println("8. Change artist");
        if (event instanceof Conference) System.out.println("8. Open registration");
        if (event instanceof SportsMatch) System.out.println("8. Update live score");

        switch (in.readLine("Choose action: ")) {
            case "1":
                event.advertiseEvent();
                break;
            case "2":
                double refunded = service.cancelEvent(name, in.readLine("Reason: "));
                persist(service);
                System.out.println("Cancelled. Tickets refunded: " + sold + " (" + refunded + " som).");
                break;
            case "3":
                event.setTicketPrice(in.readDouble("New ticket price: "));
                persist(service);
                System.out.println(" Price is now " + event.getTicketPrice() + " som.");
                break;
            case "4":
                event.setDate(in.readLine("New date (dd.MM.yyyy): "));
                persist(service);
                System.out.println(" Date is now " + event.getDate());
                break;
            case "5":
                event.setLocation(in.readLine("New location: "));
                persist(service);
                System.out.println("Location is now " + event.getLocation());
                break;
            case "6":
                System.out.println(" Notified ticket holders: " + service.notifyParticipants(name, in.readLine("Message: ")));
                break;
            case "7":
                int extra = in.readInt(extraMetricPrompt(event));
                System.out.println("Tickets sold: " + sold + ". Total revenue: " + event.calculateTotalRevenue(sold, extra) + " som");
                break;
            case "8":
                manageSpecific(event, service, in);
                break;
            default:
                throw new IllegalArgumentException("Invalid action.");
        }
    }

    private static String extraMetricPrompt(Event event) {
        if (event instanceof Concert) return "Merch items sold: ";
        if (event instanceof SportsMatch) return "Online streams sold: ";
        return "Sponsored (free) guests: ";
    }

    private static void manageSpecific(Event event, EventManagerService service, ConsoleInput in) {
        if (event instanceof Concert) {
            ((Concert) event).setArtist(in.readLine("New artist: "));
            persist(service);
        } else if (event instanceof Conference) {
            ((Conference) event).openRegistration(in.readInt("Number of seats: "));
        } else if (event instanceof SportsMatch) {
            ((SportsMatch) event).updateScore(in.readInt("Home goals: "), in.readInt("Away goals: "));
        }
    }

    private static void processNext(EventManagerService service) {
        String result = service.processNextRequest();
        if (result == null) {
            System.out.println("Queue is empty. Nothing to process.");
            return;
        }
        persist(service);
        System.out.println(result);
    }

    private static void processAll(EventManagerService service) {
        List<String> results = service.processAllRequests();
        if (results.isEmpty()) {
            System.out.println("Queue is empty. Nothing to process.");
            return;
        }
        persist(service);
        for (String line : results) System.out.println(line);
        System.out.println("Processed requests: " + results.size());
    }

    private static void showQueue(EventManagerService service) {
        List<TicketRequest> pending = service.getPendingRequests();
        System.out.println("\n Pending Queue ");
        if (pending.isEmpty()) {
            System.out.println("Queue is empty.");
            return;
        }
        System.out.println("Next to process: " + service.peekNextRequest());
        int position = 1;
        for (TicketRequest r : pending) System.out.println("   " + position++ + ". " + r);
    }

    private static void showUsers(EventManagerService service) {
        System.out.println("\nRegistered users ");
        for (Role role : Role.values()) System.out.println(role + ": " + service.getUsersByRole(role));
    }

    private static void persist(EventManagerService service) {
        EventFileProcessor.saveEvents(service.getAllEvents());
        UserFileHandler.saveTickets(service.getAllTickets());
    }

    private static Event readEvent(ConsoleInput in) {
        String type = in.readLine("Event type (CONCERT / CONFERENCE / SPORTS): ").toUpperCase();
        String name = in.readLine("Name: ");
        String date = in.readLine("Date (dd.MM.yyyy): ");
        String location = in.readLine("Location: ");
        double price = in.readDouble("Ticket price: ");

        if (type.equals("CONCERT")) {
            String artist = in.readLine("Artist: ");
            String genre = in.readLine("Music genre: ");
            double merch = in.readDouble("Merch price: ");
            return new Concert(name, date, location, price, artist, genre, merch, readOrganizer(in), readSponsor(in));
        } else if (type.equals("CONFERENCE")) {
            String speaker = in.readLine("Keynote speaker: ");
            String topic = in.readLine("Topic: ");
            return new Conference(name, date, location, price, speaker, topic, readOrganizer(in), readSponsor(in));
        } else if (type.equals("SPORTS")) {
            String home = in.readLine("Home team: ");
            String away = in.readLine("Away team: ");
            double stream = in.readDouble("Stream price: ");
            return new SportsMatch(name, date, location, price, home, away, stream, readOrganizer(in), readSponsor(in));
        }
        throw new IllegalArgumentException("Unknown event type '" + type + "'.");
    }

    private static Organizer readOrganizer(ConsoleInput in) {
        return new Organizer(in.readText("Organizer", Organizer.DEFAULT_NAME));
    }

    private static Sponsor readSponsor(ConsoleInput in) {
        return new Sponsor(in.readText("Sponsor", Sponsor.DEFAULT_NAME));
    }
}
