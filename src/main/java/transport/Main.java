package transport;

import java.util.Scanner;

/**
 * Console Application Entry Point for Public Transport Management System (PTMS).
 * Demonstrates 5 GoF patterns: Prototype, Memento, Template Method, Builder, Mediator.
 */
public class Main {
    public static final int DEFAULT_PORT = 8085;
    private static TransportWebServer webServerInstance;

    public static void main(String[] args) {
        int port = resolvePort(args);
        TransportSystem system = new TransportSystem();
        startWebServer(system, port);

        // Register shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[INFO] Shutdown signal received. Stopping web server...");
            if (webServerInstance != null) {
                webServerInstance.stop();
            }
        }));

        if (isHeadlessMode(args)) {
            System.out.println("=============================================================");
            System.out.println("     PUBLIC TRANSPORT MANAGEMENT SYSTEM (PTMS) - SERVER MODE");
            System.out.println("     Web Dashboard active at: http://localhost:" + port);
            System.out.println("     Running as persistent daemon/background service.");
            System.out.println("=============================================================");
            keepAlive();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        MenuHandler menuHandler = new MenuHandler(system, scanner);
        SimulationHandler simHandler = new SimulationHandler(system, scanner);

        System.out.println("=============================================================");
        System.out.println("     PUBLIC TRANSPORT MANAGEMENT SYSTEM (PTMS)");
        System.out.println("       GoF Design Patterns Demonstration in Java 17");
        System.out.println("     Web Dashboard: http://localhost:" + port);
        System.out.println("=============================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            System.out.print("Enter choice (1-7): ");
            if (!scanner.hasNextLine()) {
                System.out.println("\n[INFO] Console input disconnected (terminal closed or detached).");
                System.out.println("[INFO] Web server will stay running in background on port " + port + "...");
                keepAlive();
                break;
            }
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> menuHandler.handleRoutes();
                case "2" -> menuHandler.handlePrototypeCloning();
                case "3" -> menuHandler.handleVehicleTemplates();
                case "4" -> simHandler.handleBooking();
                case "5" -> simHandler.handleScheduleMemento();
                case "6" -> simHandler.handleMediatorSimulation();
                case "7" -> {
                    System.out.println("\nThank you for using Public Transport Management System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid selection. Please choose an option between 1 and 7.");
            }
        }
    }

    private static void keepAlive() {
        Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static int resolvePort(String[] args) {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isBlank()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        for (String arg : args) {
            if (arg.matches("^\\d+$")) {
                try {
                    return Integer.parseInt(arg);
                } catch (NumberFormatException ignored) {}
            }
        }
        return DEFAULT_PORT;
    }

    private static boolean isHeadlessMode(String[] args) {
        if ("true".equalsIgnoreCase(System.getenv("HEADLESS")) || "true".equalsIgnoreCase(System.getenv("SERVER_MODE"))) {
            return true;
        }
        for (String arg : args) {
            if ("--headless".equalsIgnoreCase(arg) || "--server".equalsIgnoreCase(arg) || "-d".equalsIgnoreCase(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void startWebServer(TransportSystem system, int port) {
        try {
            webServerInstance = new TransportWebServer(system, port);
            webServerInstance.start();
            System.out.println("[INFO] Embedded Web Server started at: http://localhost:" + port);
        } catch (Exception e) {
            System.out.println("[WARN] Web server could not start on port " + port + ": " + e.getMessage());
        }
    }

    private static void printMainMenu() {
        System.out.println("\n----------------- MAIN MENU -----------------");
        System.out.println("1. Add / View Routes (Builder Pattern)");
        System.out.println("2. Clone Route or Schedule (Prototype Pattern)");
        System.out.println("3. Add Vehicles from Templates (Prototype Registry)");
        System.out.println("4. Book / Cancel Ticket (Template Method Pattern)");
        System.out.println("5. Edit Schedule with Undo / Redo (Memento Pattern)");
        System.out.println("6. Simulate Delay / Breakdown (Mediator Pattern)");
        System.out.println("7. Exit");
        System.out.println("---------------------------------------------");
    }
}
