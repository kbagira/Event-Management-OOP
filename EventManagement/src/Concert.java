import java.util.ArrayList;
import java.util.List;

public class Concert extends Event {
    private String artist = "Unknown";
    private String musicGenre = "Unknown";
    private double merchPrice = 0.0;
    private final List<String> songRequests = new ArrayList<>();

    public Concert() { super(); }

    public Concert(String name, String date, String location, double price,
                   String artist, String genre, double merchPrice, Organizer org, Sponsor sp) {
        super(name, date, location, price, org, sp);
        this.artist = artist;
        this.musicGenre = genre;
        this.merchPrice = merchPrice;
    }

    public String getArtist() { return artist; }
    public void setArtist(String a) { this.artist = a; }
    public String getMusicGenre() { return musicGenre; }
    public void setMusicGenre(String g) { this.musicGenre = g; }
    public double getMerchPrice() { return merchPrice; }
    public void setMerchPrice(double m) { this.merchPrice = m; }
    public List<String> getSongRequests() { return new ArrayList<>(songRequests); }

    /** Зритель предлагает песню для исполнителя. */
    public void requestSong(String songName) {
        ensureNotCancelled();
        songRequests.add(songName);
        System.out.println("Success! Song '" + songName + "' has been requested for " + artist
                + " (total requests: " + songRequests.size() + ").");
    }

    @Override
    public String getCategory() { return "Music"; }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Category: Concert");
        System.out.println("Artist: " + artist);
        System.out.println("Genre: " + musicGenre);
    }

    @Override
    public double calculateTotalRevenue(int attendees, int merchSold) {
        return attendees * getTicketPrice() + merchSold * merchPrice;
    }

    @Override
    public void advertiseEvent() {
        System.out.println("Launching social media ad campaign and billboards for " + artist + "'s world tour!");
    }
}
