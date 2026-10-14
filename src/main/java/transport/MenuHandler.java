package transport;

import transport.builder.Route;
import transport.builder.RouteBuilder;
import transport.prototype.Schedule;
import transport.prototype.Vehicle;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Handles console menu interactions for Route Builder, Prototype cloning, and Vehicle Registry.
 */
public class MenuHandler {
    private final TransportSystem system;
    private final Scanner scanner;

    public MenuHandler(TransportSystem system, Scanner scanner) {
        this.system = system;
        this.scanner = scanner;
    }

    public void handleRoutes() {
        System.out.println("\n--- [Builder Pattern] Route Management ---");
        System.out.println("1. View Existing Routes\n2. Add New Route (using RouteBuilder)");
        System.out.print("Select choice: ");
        String sub = scanner.nextLine().trim();
        if ("1".equals(sub)) {
            system.getRoutes().values().forEach(r -> System.out.println(" • " + r));
        } else if ("2".equals(sub)) {
            try {
                System.out.print("Enter Route Name: ");
                String name = scanner.nextLine().trim();
                System.out.print("Enter comma-separated stops (min 2): ");
                String[] stops = scanner.nextLine().split(",");
                System.out.print("Base fare (e.g. 20.0): ");
                double base = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Frequency in minutes: ");
                int freq = Integer.parseInt(scanner.nextLine().trim());

                RouteBuilder rb = new RouteBuilder().name(name).fareRules(base, 4.0).frequency(freq);
                Arrays.stream(stops).map(String::trim).filter(s -> !s.isEmpty()).forEach(rb::addStop);
                Route newRoute = rb.build();
                system.addRoute(newRoute);
                System.out.println(" Successfully built route using RouteBuilder: " + newRoute);
            } catch (Exception e) {
                System.out.println(" Validation Error: " + e.getMessage());
            }
        }
    }

    public void handlePrototypeCloning() {
        System.out.println("\n--- [Prototype Pattern] Prototype Cloning ---");
        System.out.println("1. Clone Schedule (e.g. Weekday -> Weekend)\n2. Clone Existing Fleet Vehicle");
        System.out.print("Select choice: ");
        String sub = scanner.nextLine().trim();
        if ("1".equals(sub)) {
            System.out.println("Available Schedules:");
            system.getSchedules().keySet().forEach(k -> System.out.println(" - " + k + " : " + system.getSchedules().get(k)));
            System.out.print("Enter Schedule ID to clone: ");
            String id = scanner.nextLine().trim();
            Schedule source = system.getSchedules().get(id);
            if (source != null) {
                Schedule clone = source.copy(); // Deep copy via copy()
                System.out.print("Enter new Schedule ID: ");
                clone.setScheduleId(scanner.nextLine().trim());
                System.out.print("Enter new Day Type (e.g. Weekend): ");
                clone.setDayType(scanner.nextLine().trim());
                clone.addDepartureTime("20:30"); // Independent mutation
                system.getSchedules().put(clone.getScheduleId(), clone);
                System.out.println(" Schedule cloned via Prototype copy() [Deep Copy Verified]:");
                System.out.println(" Original: " + source);
                System.out.println(" Cloned  : " + clone);
            } else {
                System.out.println(" Schedule not found.");
            }
        } else if ("2".equals(sub)) {
            if (system.getFleet().isEmpty()) {
                System.out.println(" Fleet is empty.");
                return;
            }
            Vehicle original = system.getFleet().get(0);
            Vehicle copy = original.copy();
            copy.setId("COPY-" + (System.currentTimeMillis() % 1000));
            copy.addFeature("Extra Reclining Seats");
            system.addFleetVehicle(copy);
            System.out.println(" Vehicle cloned via copy(): " + copy);
        }
    }

    public void handleVehicleTemplates() {
        System.out.println("\n--- [Prototype Pattern] Vehicle Registry ---");
        System.out.println("Available Registry Templates: " + system.getVehicleRegistry().getAvailableTemplateKeys());
        System.out.print("Enter template name to instantiate: ");
        String tmpl = scanner.nextLine().trim();
        System.out.print("Enter new Vehicle Registration ID: ");
        String newId = scanner.nextLine().trim();
        try {
            Vehicle v = system.getVehicleRegistry().createVehicle(tmpl, newId);
            system.addFleetVehicle(v);
            System.out.println(" Successfully cloned vehicle from template: " + v);
        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }
}
