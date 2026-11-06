
public interface Cancellable {
    void cancelEvent(String reason);

    double calculateRefundAmount(int ticketsBought, double ticketPrice);
}
