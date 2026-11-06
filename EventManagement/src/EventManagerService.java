import java.util.*;


public class EventManagerService {

    public static final String ADMIN_CODE = "ADMIN2026";

    private static class RequestComparator implements Comparator<TicketRequest> {
        @Override
        public int compare(TicketRequest a, TicketRequest b) {
            int result = Integer.compare(a.getType().getPriority(), b.getType().getPriority());
            return result != 0 ? result : Long.compare(a.getSequence(), b.getSequence());
        }
    }

    private final Map<String, Event> eventsById = new LinkedHashMap<>();
    private final Map<String, List<Event>> eventsByCategory = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Set<Event>> eventsByOrganizer = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, Set<Event>> eventsBySponsor = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private final Map<String, UserAccount> usersByName = new LinkedHashMap<>();
    private final Map<Role, List<UserAccount>> usersByRole = new EnumMap<>(Role.class);
    private final Map<String, List<Ticket>> ticketsByUser = new HashMap<>();
    private final Map<String, List<Ticket>> ticketsByEvent = new HashMap<>();

    private final Queue<TicketRequest> requestQueue = new PriorityQueue<>(new RequestComparator());
    private final NotificationService notifier = new NotificationService();

    private long nextTicketId = 1;
    private long nextRequestSequence = 1;


    public void addUser(UserAccount user) {
        String key = UserAccount.toKey(user.getUsername());
        if (usersByName.containsKey(key)) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken.");
        }
        usersByName.put(key, user);
        usersByRole.computeIfAbsent(user.getRole(), r -> new ArrayList<>()).add(user);
    }

    public UserAccount register(String username, String password, Role role, String adminCode) {
        if (role == Role.ADMIN && !ADMIN_CODE.equals(adminCode)) {
            throw new IllegalArgumentException("Invalid admin code.");
        }
        UserAccount user = new UserAccount(username, password, role);
        addUser(user);
        return user;
    }

    public UserAccount authenticate(String username, String password) {
        UserAccount account = usersByName.get(UserAccount.toKey(username));
        return account != null && account.getPassword().equals(password) ? account : null;
    }

    public List<UserAccount> getAllUsers() {
        return new ArrayList<>(usersByName.values());
    }

    public List<UserAccount> getUsersByRole(Role role) {
        return new ArrayList<>(usersByRole.getOrDefault(role, new ArrayList<>()));
    }


    public void addEvent(Event event) {
        if (eventsById.containsKey(event.getId())) {
            throw new IllegalArgumentException("Event with name '" + event.getEventName() + "' already exists.");
        }
        indexEvent(event);
    }

    public Event findEvent(String name) {
        return eventsById.get(Event.toId(name));
    }

    public Event getEvent(String name) {
        Event event = findEvent(name);
        if (event == null) throw new IllegalArgumentException("Event '" + name + "' not found.");
        return event;
    }

    public void updateEvent(String targetName, Event updated) {
        Event old = getEvent(targetName);
        if (old.isCancelled()) {
            throw new IllegalStateException("Cancelled event '" + old.getEventName() + "' cannot be updated.");
        }
        if (!old.equals(updated) && eventsById.containsKey(updated.getId())) {
            throw new IllegalArgumentException("Event with name '" + updated.getEventName() + "' already exists.");
        }
        unindexEvent(old);
        indexEvent(updated);

        List<Ticket> tickets = ticketsByEvent.remove(old.getId());
        if (tickets != null) {
            for (Ticket t : tickets) t.setEventName(updated.getEventName());
            ticketsByEvent.put(updated.getId(), tickets);
        }
        for (TicketRequest r : requestQueue) {
            if (Event.toId(r.getEventName()).equals(old.getId())) r.setEventName(updated.getEventName());
        }
    }

    public double removeEvent(String name) {
        Event event = getEvent(name);
        unindexEvent(event);
        return refundAllTickets(event);
    }
    public double cancelEvent(String name, String reason) {
        Event event = getEvent(name);
        event.cancelEvent(reason);
        return refundAllTickets(event);
    }

    private double refundAllTickets(Event event) {
        double refunded = 0;
        List<Ticket> tickets = ticketsByEvent.remove(event.getId());
        if (tickets != null) {
            for (Ticket t : tickets) {
                removeTicket(ticketsByUser, UserAccount.toKey(t.getUsername()), t);
                refunded += event.calculateRefundAmount(1, t.getPrice());
            }
        }
        return refunded;
    }

    private void indexEvent(Event e) {
        eventsById.put(e.getId(), e);
        eventsByCategory.computeIfAbsent(e.getCategory(), k -> new ArrayList<>()).add(e);
        eventsByOrganizer.computeIfAbsent(e.getOrganizer().getName(), k -> new TreeSet<>()).add(e);
        eventsBySponsor.computeIfAbsent(e.getSponsor().getName(), k -> new TreeSet<>()).add(e);
    }

    private void unindexEvent(Event e) {
        eventsById.remove(e.getId());
        removeFromGroup(eventsByCategory, e.getCategory(), e);
        removeFromGroup(eventsByOrganizer, e.getOrganizer().getName(), e);
        removeFromGroup(eventsBySponsor, e.getSponsor().getName(), e);
    }

    private static void removeFromGroup(Map<String, ? extends Collection<Event>> index, String key, Event e) {
        Collection<Event> group = index.get(key);
        group.remove(e);
        if (group.isEmpty()) index.remove(key);
    }


    public int getEventCount() { return eventsById.size(); }

    public List<Event> getAllEvents() { return new ArrayList<>(eventsById.values()); }

    public List<Event> getNaturalSorted() {
        List<Event> all = getAllEvents();
        Collections.sort(all);
        return all;
    }

    public List<Event> getSorted(Comparator<Event> comparator) {
        List<Event> all = getAllEvents();
        all.sort(comparator);
        return all;
    }

    public List<Event> filterByMaxPrice(double maxPrice) {
        List<Event> result = new ArrayList<>();
        for (Event e : getNaturalSorted()) {
            if (e.getTicketPrice() <= maxPrice) result.add(e);
        }
        return result;
    }

    public List<Event> getEventsByCategory(String category) {
        List<Event> copy = new ArrayList<>(eventsByCategory.getOrDefault(category.trim(), new ArrayList<>()));
        Collections.sort(copy);
        return copy;
    }

    public Set<String> getCategories() { return new TreeSet<>(eventsByCategory.keySet()); }

    public Set<String> getOrganizers() { return new TreeSet<>(eventsByOrganizer.keySet()); }

    public Set<String> getSponsors() { return new TreeSet<>(eventsBySponsor.keySet()); }

    public Set<Event> getEventsByOrganizer(String name) {
        return new TreeSet<>(eventsByOrganizer.getOrDefault(name.trim(), new TreeSet<>()));
    }

    public Set<Event> getEventsBySponsor(String name) {
        return new TreeSet<>(eventsBySponsor.getOrDefault(name.trim(), new TreeSet<>()));
    }


    public List<Ticket> getTicketsOf(String username) {
        return new ArrayList<>(ticketsByUser.getOrDefault(UserAccount.toKey(username), new ArrayList<>()));
    }

    public List<Ticket> getTicketsOfEvent(String eventName) {
        return new ArrayList<>(ticketsByEvent.getOrDefault(Event.toId(eventName), new ArrayList<>()));
    }

    public List<Ticket> getAllTickets() {
        List<Ticket> all = new ArrayList<>();
        for (List<Ticket> list : ticketsByUser.values()) all.addAll(list);
        Collections.sort(all); // Comparable<Ticket>
        return all;
    }

    public boolean restoreTicket(String username, String eventName, double price) {
        Event event = findEvent(eventName);
        UserAccount user = usersByName.get(UserAccount.toKey(username));
        if (event == null || user == null) return false;
        issueTicket(user.getUsername(), event, price);
        return true;
    }

    private Ticket issueTicket(String username, Event event, double price) {
        Ticket ticket = new Ticket(nextTicketId++, username, event.getEventName(), price);
        ticketsByUser.computeIfAbsent(UserAccount.toKey(username), k -> new ArrayList<>()).add(ticket);
        ticketsByEvent.computeIfAbsent(event.getId(), k -> new ArrayList<>()).add(ticket);
        return ticket;
    }

    private static void removeTicket(Map<String, List<Ticket>> index, String key, Ticket ticket) {
        List<Ticket> group = index.get(key);
        if (group != null) {
            group.remove(ticket);
            if (group.isEmpty()) index.remove(key);
        }
    }

    private Ticket findLatestTicket(String username, String eventName) {
        List<Ticket> list = ticketsByUser.getOrDefault(UserAccount.toKey(username), new ArrayList<>());
        for (int i = list.size() - 1; i >= 0; i--) {
            if (Event.toId(list.get(i).getEventName()).equals(Event.toId(eventName))) return list.get(i);
        }
        return null;
    }

    public int notifyParticipants(String eventName, String message) {
        Event event = getEvent(eventName);
        Set<String> recipients = new LinkedHashSet<>();
        for (Ticket t : getTicketsOfEvent(eventName)) recipients.add(t.getUsername());
        for (String username : recipients) event.notifyParticipants(notifier, "to " + username + ": " + message);
        return recipients.size();
    }

    // ---------------------------------------------------------------- очередь заявок (USER подаёт, ADMIN обрабатывает через poll())

    public TicketRequest submitRequest(RequestType type, String username, String eventName) {
        UserAccount user = usersByName.get(UserAccount.toKey(username));
        if (user == null) throw new IllegalArgumentException("Unknown user '" + username + "'.");
        Event event = getEvent(eventName);
        if (type == RequestType.PURCHASE && event.isCancelled()) {
            throw new IllegalStateException("Event '" + event.getEventName() + "' is cancelled.");
        }
        if (type == RequestType.REFUND && findLatestTicket(user.getUsername(), event.getEventName()) == null) {
            throw new IllegalStateException("You have no ticket for '" + event.getEventName() + "'.");
        }
        TicketRequest request = new TicketRequest(nextRequestSequence++, type, user.getUsername(), event.getEventName());
        requestQueue.offer(request);
        return request;
    }

    /** ADMIN: извлекает следующую заявку (poll) и обрабатывает. null: очередь пуста. */
    public String processNextRequest() {
        TicketRequest request = requestQueue.poll();
        return request == null ? null : handle(request);
    }

    public List<String> processAllRequests() {
        List<String> results = new ArrayList<>();
        String result;
        while ((result = processNextRequest()) != null) results.add(result);
        return results;
    }

    private String handle(TicketRequest request) {
        Event event = findEvent(request.getEventName());
        if (event == null) return "REJECTED  " + request + " (event no longer exists)";
        if (event.isCancelled()) return "REJECTED  " + request + " (event is cancelled)";

        if (request.getType() == RequestType.PURCHASE) {
            Ticket ticket = issueTicket(request.getUsername(), event, event.getTicketPrice());
            event.bookTicket();
            return "APPROVED  " + request + " (ticket #" + ticket.getId() + ", " + ticket.getPrice() + " som)";
        }

        Ticket ticket = findLatestTicket(request.getUsername(), event.getEventName());
        if (ticket == null) return "REJECTED  " + request + " (no ticket to refund)";
        removeTicket(ticketsByUser, UserAccount.toKey(ticket.getUsername()), ticket);
        removeTicket(ticketsByEvent, event.getId(), ticket);
        return "APPROVED  " + request + " (refunded " + event.calculateRefundAmount(1, ticket.getPrice()) + " som)";
    }

    public TicketRequest peekNextRequest() { return requestQueue.peek(); }

    public int getQueueSize() { return requestQueue.size(); }

    public List<TicketRequest> getPendingRequests() {
        List<TicketRequest> list = new ArrayList<>(requestQueue);
        list.sort(new RequestComparator());
        return list;
    }

    public String buildReport() {
        StringBuilder sb = new StringBuilder("ANALYTICS REPORT \n");
        sb.append("Users: ").append(usersByName.size())
                .append(" (admins=").append(getUsersByRole(Role.ADMIN).size())
                .append(", users=").append(getUsersByRole(Role.USER).size()).append(")\n");
        if (eventsById.isEmpty()) {
            return sb.append("No events in the system.\nPending requests: ").append(requestQueue.size()).append('\n').toString();
        }

        int totalTickets = 0, cancelledCount = 0;
        double totalRevenue = 0;
        for (Event e : eventsById.values()) {
            totalTickets += ticketsSold(e);
            totalRevenue += revenueOf(e);
            if (e.isCancelled()) cancelledCount++;
        }
        sb.append(String.format("Events: %d (cancelled: %d) | Tickets sold: %d | Total revenue: %.1f som%n",
                eventsById.size(), cancelledCount, totalTickets, totalRevenue));

        sb.append("\nBy category:\n");
        for (Map.Entry<String, List<Event>> entry : eventsByCategory.entrySet()) {
            double priceSum = 0, revenue = 0;
            int sold = 0;
            for (Event e : entry.getValue()) {
                priceSum += e.getTicketPrice();
                revenue += revenueOf(e);
                sold += ticketsSold(e);
            }
            int count = entry.getValue().size();
            sb.append(String.format("  %-9s events=%d  avgPrice=%.1f  sold=%d  revenue=%.1f%n",
                    entry.getKey(), count, priceSum / count, sold, revenue));
        }

        sb.append("\nMost expensive: ").append(Collections.max(eventsById.values(), EventComparators.BY_PRICE)).append('\n');
        sb.append("Cheapest:       ").append(Collections.min(eventsById.values(), EventComparators.BY_PRICE)).append('\n');

        sb.append("\nTop events by tickets sold:\n");
        List<Event> top = getSorted((a, b) -> {
            int result = Integer.compare(ticketsSold(b), ticketsSold(a));
            return result != 0 ? result : a.compareTo(b);
        });
        for (int i = 0; i < 3 && i < top.size(); i++) {
            sb.append(String.format("  %d. %s - %d tickets%n", i + 1, top.get(i).getEventName(), ticketsSold(top.get(i))));
        }

        sb.append("\nEvents per organizer:\n");
        for (Map.Entry<String, Set<Event>> entry : eventsByOrganizer.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue().size()).append('\n');
        }

        int refunds = 0;
        for (TicketRequest r : requestQueue) {
            if (r.getType() == RequestType.REFUND) refunds++;
        }
        sb.append("\nPending requests: ").append(requestQueue.size())
                .append(" (refund=").append(refunds).append(", purchase=").append(requestQueue.size() - refunds).append(")\n");
        return sb.toString();
    }

    private int ticketsSold(Event e) {
        return ticketsByEvent.getOrDefault(e.getId(), new ArrayList<>()).size();
    }

    private double revenueOf(Event e) {
        return e.calculateTotalRevenue(ticketsSold(e), 0);
    }
}
