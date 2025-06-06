import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    private static final String LOG_FILE = "logs.txt";
    private static final String USER_FILE = "users.txt";
    private static final String TRANSACTION_FILE = "transactions.txt";

    private static String currentUser = null;
    private static final Map<String, OrderItem> orderList = new LinkedHashMap<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n1 - Sign Up\n2 - Login\n3 - Exit");
            System.out.print("Choose option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> signUp(sc);
                case "2" -> {
                    if (login(sc)) {
                        menu(sc);
                    }
                }
                case "3" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void signUp(Scanner sc) {
        System.out.print("Enter new username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter new password: ");
        String password = sc.nextLine().trim();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(username + ":" + password);
            writer.newLine();
            System.out.println("User registered successfully.");
        } catch (IOException e) {
            System.out.println("Error registering user.");
        }
    }

    private static boolean login(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(username + ":" + password)) {
                    currentUser = username;
                    System.out.println("Login successful!");
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user file.");
        }

        System.out.println("Invalid username or password.");
        return false;
    }

    private static void menu(Scanner sc) {
        while (true) {
            System.out.println("\n1 - Add Order\n2 - Update Quantity\n3 - Remove Order\n4 - Show Orders");
            System.out.println("5 - Checkout\n6 - Read Logs\n7 - Read Transactions\n8 - Logout");
            System.out.print("Choose option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1" -> addOrder(sc);
                case "2" -> updateOrder(sc);
                case "3" -> removeOrder(sc);
                case "4" -> showOrders();
                case "5" -> checkout();
                case "6" -> readFromFile(LOG_FILE);
                case "7" -> readFromFile(TRANSACTION_FILE);
                case "8" -> {
                    currentUser = null;
                    orderList.clear();
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void addOrder(Scanner sc) {
        try {
            System.out.print("Item name: ");
            String item = sc.nextLine().trim();

            System.out.print("Quantity: ");
            int qty = Integer.parseInt(sc.nextLine().trim());

            System.out.print("Price: ");
            double price = Double.parseDouble(sc.nextLine().trim());

            orderList.put(item, new OrderItem(item, qty, price));
            System.out.println("Order added.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private static void updateOrder(Scanner sc) {
        System.out.print("Item to update: ");
        String item = sc.nextLine().trim();

        if (orderList.containsKey(item)) {
            try {
                System.out.print("New quantity: ");
                int newQty = Integer.parseInt(sc.nextLine().trim());
                orderList.get(item).quantity = newQty;
                System.out.println("Order updated.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity.");
            }
        } else {
            System.out.println("Item not found.");
        }
    }

    private static void removeOrder(Scanner sc) {
        System.out.print("Item to remove: ");
        String item = sc.nextLine().trim();

        if (orderList.remove(item) != null) {
            System.out.println("Order removed.");
        } else {
            System.out.println("Item not found.");
        }
    }

    private static void showOrders() {
        if (orderList.isEmpty()) {
            System.out.println("No orders.");
            return;
        }

        System.out.println("\nOrders:");
        double total = 0;
        for (OrderItem item : orderList.values()) {
            System.out.printf("- %s: %d x %.2f = %.2f%n", item.name, item.quantity, item.price, item.total());
            total += item.total();
        }
        System.out.printf("Total: %.2f%n", total);
    }

    private static void checkout() {
        if (orderList.isEmpty()) {
            System.out.println("No orders to checkout.");
            return;
        }

        double total = 0;
        StringBuilder transaction = new StringBuilder();
        transaction.append("Date: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append("\nUser: ").append(currentUser).append("\nItems:\n");

        for (OrderItem item : orderList.values()) {
            transaction.append(String.format("- %s: %d x %.2f = %.2f%n",
                    item.name, item.quantity, item.price, item.total()));
            total += item.total();
        }

        transaction.append(String.format("Total Amount: %.2f\n", total));
        transaction.append("-----\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRANSACTION_FILE, true))) {
            writer.write(transaction.toString());
            System.out.println("Transaction saved.");
        } catch (IOException e) {
            System.out.println("Error writing transaction.");
        }

        orderList.clear();
    }

    private static void readFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            System.out.println("\n" + fileName + " contents:");
            String line;
            while ((line = reader.readLine()) != null) System.out.println(line);
        } catch (IOException e) {
            System.out.println("Error reading file.");
        }
    }

    static class OrderItem {
        String name;
        int quantity;
        double price;

        OrderItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }

        double total() {
            return quantity * price;
        }
    }
}
