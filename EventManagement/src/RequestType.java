
public enum RequestType {
    REFUND(0),
    PURCHASE(1);

    private final int priority;

    RequestType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
