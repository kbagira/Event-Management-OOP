public class SportsMatch extends Event implements Streamable {
    private String homeTeam = "Team A";
    private String awayTeam = "Team B";
    private double streamPrice = 0.0;

    public SportsMatch() { super(); }

    public SportsMatch(String name, String date, String location, double price,
                       String home, String away, double streamPrice, Organizer org, Sponsor sp) {
        super(name, date, location, price, org, sp);
        this.homeTeam = home;
        this.awayTeam = away;
        this.streamPrice = streamPrice;
    }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String h) { this.homeTeam = h; }
    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String a) { this.awayTeam = a; }
    public double getStreamPrice() { return streamPrice; }
    public void setStreamPrice(double s) { this.streamPrice = s; }

    public void updateScore(int homeGoals, int awayGoals) {
        ensureNotCancelled();
        System.out.println("Live Score: " + homeTeam + " " + homeGoals + " - " + awayGoals + " " + awayTeam);
    }

    @Override
    public String getCategory() { return "Sports"; }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Category: Sports Match");
        System.out.println("Teams: " + homeTeam + " vs " + awayTeam);
    }


    @Override
    public double calculateTotalRevenue(int attendees, int streamsSold) {
        return attendees * getTicketPrice() + streamsSold * streamPrice;
    }

    @Override
    public void advertiseEvent() {
        System.out.println("Broadcasting promo trailers on sports TV channels for " + homeTeam + " vs " + awayTeam);
    }

    @Override
    public void checkViewerCount(int num) {
        System.out.println("Current live match stream viewers (" + homeTeam + " vs " + awayTeam + "): " + num);
    }

    @Override
    public void sendChatAnnouncement(String message) {
        ensureNotCancelled();
        System.out.println("Match Chat Alert: " + message);
    }
}
