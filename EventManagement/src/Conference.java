public class Conference extends Event {
    private String speaker = "Unknown Speaker";
    private String topic = "General";

    public Conference() { super(); }

    public Conference(String name, String date, String location, double price,
                      String speaker, String topic, Organizer org, Sponsor sp) {
        super(name, date, location, price, org, sp);
        this.speaker = speaker;
        this.topic = topic;
    }

    public String getSpeaker() { return speaker; }
    public void setSpeaker(String s) { this.speaker = s; }
    public String getTopic() { return topic; }
    public void setTopic(String t) { this.topic = t; }

    public void openRegistration(int maxSeats) {
        ensureNotCancelled();
        System.out.println("Registration is now open for '" + getEventName() + "'. Available seats: " + maxSeats);
    }

    @Override
    public String getCategory() { return "Business"; }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Category: Conference");
        System.out.println("Speaker: " + speaker);
        System.out.println("Topic: " + topic);
    }

    @Override
    public double calculateTotalRevenue(int attendees, int sponsoredAttendees) {
        return (attendees - sponsoredAttendees) * getTicketPrice();
    }

    @Override
    public void advertiseEvent() {
        System.out.println("Sending email invitations to IT professionals and posting on LinkedIn for " + topic);
    }
}
