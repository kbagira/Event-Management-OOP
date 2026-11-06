import java.util.Scanner;
public class ConsoleInput {
    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) { this.scanner = scanner; }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readText(String prompt, String defaultValue) {
        String value = readLine(prompt + " (Enter = " + defaultValue + "): ");
        return value.isEmpty() ? defaultValue : value;
    }

    public double readDouble(String prompt) { return Double.parseDouble(readLine(prompt).replace(',', '.')); }

    public int readInt(String prompt) { return Integer.parseInt(readLine(prompt)); }
}
