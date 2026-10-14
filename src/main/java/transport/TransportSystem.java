package transport;

import transport.builder.*;
import transport.mediator.*;
import transport.memento.*;
import transport.prototype.*;
import transport.template.*;

import java.util.*;

/**
 * In-memory state manager orchestrating domain components and initial seed data.
 */
public class TransportSystem {
    private final Map<String, Route> routes = new LinkedHashMap<>();
    private final Map<String, Schedule> schedules = new LinkedHashMap<>();
    private final VehicleRegistry vehicleRegistry = new VehicleRegistry();
    private final List<Vehicle> fleet = new ArrayList<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final TransportControlCenter controlCenter = new TransportControlCenter();
    private final ScheduleHistory scheduleHistory = new ScheduleHistory();
    private final BusBooking busBooking = new BusBooking();
    private final MetroBooking metroBooking = new MetroBooking();
    private final TrainBooking trainBooking = new TrainBooking();

    public TransportSystem() {
        seedInitialData();
    }

    private void seedInitialData() {
        // Seed initial routes using RouteBuilder
        Route r1 = new RouteBuilder().id("RT-101").name("Airport Express")
                .addStop("Central Station").addStop("Tech Park").addStop("Airport T1").addStop("Airport T2")
                .addTiming("06:00").addTiming("09:00").addTiming("14:00").addTiming("18:00")
                .fareRules(20.0, 5.0).frequency(30).build();
        Route r2 = new RouteBuilder().id("RT-202").name("Metro Blue Line")
                .addStop("North Terminal").addStop("Market Square").addStop("City Center").addStop("South Gate")
                .addTiming("05:30").addTiming("08:00").addTiming("12:00").addTiming("17:00")
                .fareRules(15.0, 3.5).frequency(10).build();
        routes.put(r1.getId(), r1);
        routes.put(r2.getId(), r2);

        // Seed vehicles from Prototype Registry
        Vehicle v1 = vehicleRegistry.createVehicle("AC Bus", "BUS-VOLVO-01");
        Vehicle standby = vehicleRegistry.createVehicle("Electric Bus", "EV-STANDBY-01");
        fleet.add(v1);
        fleet.add(standby);

        // Seed mediator components
        Station s1 = new Station("STN-01", "Central Station", controlCenter);
        Station s2 = new Station("STN-02", "Airport T1", controlCenter);
        Driver d1 = new Driver("DRV-01", "Alex Smith", controlCenter);
        Bus b1 = new Bus(v1.getId(), r1.getId(), controlCenter);
        Bus bStandby = new Bus(standby.getId(), "UNASSIGNED", controlCenter);
        bStandby.setStatus("STANDBY");
        controlCenter.addStandbyBus(bStandby);
        d1.receiveDispatch("Morning Express Run", b1.getBusId());

        // Seed schedule
        Schedule sch1 = new Schedule("SCH-101-WD", r1.getName(), "Weekday",
                new ArrayList<>(r1.getTimings()), new ArrayList<>(r1.getStops()), v1.getId(), true);
        schedules.put(sch1.getScheduleId(), sch1);
    }

    public void addRoute(Route route) { routes.put(route.getId(), route); }
    public Map<String, Route> getRoutes() { return Collections.unmodifiableMap(routes); }
    public Map<String, Schedule> getSchedules() { return schedules; }
    public VehicleRegistry getVehicleRegistry() { return vehicleRegistry; }
    public List<Vehicle> getFleet() { return Collections.unmodifiableList(fleet); }
    public Map<String, Ticket> getTickets() { return tickets; }
    public TransportControlCenter getControlCenter() { return controlCenter; }
    public ScheduleHistory getScheduleHistory() { return scheduleHistory; }
    public BusBooking getBusBooking() { return busBooking; }
    public MetroBooking getMetroBooking() { return metroBooking; }
    public TrainBooking getTrainBooking() { return trainBooking; }

    public void addFleetVehicle(Vehicle vehicle) { fleet.add(vehicle); }
    public void registerTicket(Ticket ticket) { tickets.put(ticket.getTicketId(), ticket); }
}
