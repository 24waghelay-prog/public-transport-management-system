package transport;

import transport.builder.Ticket;
import transport.mediator.Bus;
import transport.mediator.TransportControlCenter;
import transport.memento.ScheduleMemento;
import transport.prototype.Schedule;
import transport.template.BookingProcess;
import transport.template.BookingRequest;

import java.util.Scanner;

/**
 * Handles console menu interactions for Template Method booking, Memento undo/redo,
 * and Mediator emergency operations.
 */
public class SimulationHandler {
    private final TransportSystem system;
    private final Scanner scanner;

    public SimulationHandler(TransportSystem system, Scanner scanner) {
        this.system = system;
        this.scanner = scanner;
    }

    public void handleBooking() {
        System.out.println("\n--- [Template Method & Builder] Ticket Booking ---");
        System.out.println("1. Book Ticket\n2. Cancel Ticket\n3. View All Tickets");
        System.out.print("Select choice: ");
        String sub = scanner.nextLine().trim();
        if ("1".equals(sub)) {
            System.out.print("Transport Mode (BUS / METRO / TRAIN): ");
            String mode = scanner.nextLine().trim().toUpperCase();
            System.out.print("Passenger Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Source Station: ");
            String src = scanner.nextLine().trim();
            System.out.print("Destination Station: ");
            String dst = scanner.nextLine().trim();

            BookingRequest req = new BookingRequest(name, src, dst, 3, 15.0, "AC_3_TIER", null, "CARD", 10.0);
            try {
                BookingProcess proc = switch (mode) {
                    case "METRO" -> system.getMetroBooking();
                    case "TRAIN" -> system.getTrainBooking();
                    default -> system.getBusBooking();
                };
                Ticket ticket = proc.book(req); // Executes Template Method algorithm
                system.registerTicket(ticket);
                System.out.println(" Ticket issued successfully via Template Method: " + ticket);
            } catch (Exception e) {
                System.out.println(" Booking failed: " + e.getMessage());
            }
        } else if ("2".equals(sub)) {
            System.out.print("Enter Ticket ID to cancel: ");
            String tid = scanner.nextLine().trim();
            Ticket t = system.getTickets().get(tid);
            if (t != null) {
                t.cancel();
                System.out.println(" Ticket cancelled: " + t);
            } else {
                System.out.println(" Ticket ID not found.");
            }
        } else if ("3".equals(sub)) {
            if (system.getTickets().isEmpty()) System.out.println(" No tickets booked yet.");
            else system.getTickets().values().forEach(t -> System.out.println(" • " + t));
        }
    }

    public void handleScheduleMemento() {
        System.out.println("\n--- [Memento Pattern] Schedule Editor (Undo/Redo) ---");
        System.out.println("Schedules:");
        system.getSchedules().keySet().forEach(k -> System.out.println(" - " + k + " : " + system.getSchedules().get(k)));
        System.out.println("Actions: 1. Edit Departure Time | 2. Undo Last Edit | 3. Redo Undone Edit");
        System.out.print("Choice: ");
        String opt = scanner.nextLine().trim();
        if ("1".equals(opt)) {
            System.out.print("Enter Schedule ID: ");
            String sid = scanner.nextLine().trim();
            Schedule s = system.getSchedules().get(sid);
            if (s != null) {
                system.getScheduleHistory().saveState(s.createMemento()); // Save Memento to Caretaker
                System.out.print("Enter new departure time to add (e.g. 19:45): ");
                String t = scanner.nextLine().trim();
                s.addDepartureTime(t);
                System.out.println(" Edited schedule. Snapshot pushed to Memento Caretaker.");
                System.out.println(" Current State: " + s);
            }
        } else if ("2".equals(opt)) {
            if (system.getScheduleHistory().canUndo()) {
                Schedule s = system.getSchedules().values().iterator().next();
                ScheduleMemento prior = system.getScheduleHistory().undo(s.createMemento());
                s.restore(prior);
                System.out.println(" Undo successful! Restored state from Memento: " + s);
            } else {
                System.out.println(" Undo stack is empty.");
            }
        } else if ("3".equals(opt)) {
            if (system.getScheduleHistory().canRedo()) {
                Schedule s = system.getSchedules().values().iterator().next();
                ScheduleMemento next = system.getScheduleHistory().redo(s.createMemento());
                s.restore(next);
                System.out.println(" Redo successful! Restored state from Memento: " + s);
            } else {
                System.out.println(" Redo stack is empty.");
            }
        }
    }

    public void handleMediatorSimulation() {
        System.out.println("\n--- [Mediator Pattern] Transport Control Center Simulation ---");
        System.out.println("1. Simulate Bus Delay\n2. Simulate Bus Breakdown & Emergency Replacement\n3. View Station Boards & Alerts");
        System.out.print("Choice: ");
        String opt = scanner.nextLine().trim();
        TransportControlCenter cc = system.getControlCenter();
        if ("1".equals(opt)) {
            System.out.print("Enter Bus ID (e.g. BUS-VOLVO-01): ");
            String bid = scanner.nextLine().trim();
            Bus bus = cc.getBus(bid);
            if (bus != null) {
                System.out.print("Enter Delay in minutes: ");
                int mins = Integer.parseInt(scanner.nextLine().trim());
                bus.reportDelay(mins); // Bus notifies Mediator
                System.out.println(" Bus reported delay through ControlCenter Mediator.");
            } else {
                System.out.println(" Bus not found in mediator registry.");
            }
        } else if ("2".equals(opt)) {
            System.out.print("Enter Bus ID (e.g. BUS-VOLVO-01): ");
            String bid = scanner.nextLine().trim();
            Bus bus = cc.getBus(bid);
            if (bus != null) {
                System.out.print("Enter Breakdown Reason: ");
                String reason = scanner.nextLine().trim();
                bus.reportBreakdown(reason); // Bus notifies Mediator
                System.out.println(" Breakdown reported! Mediator dispatched replacement vehicle and notified driver.");
            }
        } else if ("3".equals(opt)) {
            System.out.println("\nStation Electronic Display Boards:");
            cc.getStations().values().forEach(st -> {
                System.out.println(" [" + st.getName() + " Board]");
                st.getDisplayBoard().forEach(m -> System.out.println("    " + m));
            });
            System.out.println("\nPassenger Push Notification Feed:");
            cc.getPassengerNotifications().forEach(p -> System.out.println("    " + p));
        }
    }
}
