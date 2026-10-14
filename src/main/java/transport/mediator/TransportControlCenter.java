package transport.mediator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Concrete mediator coordinating buses, stations, drivers, and passenger alerts.
 * Ensures zero direct coupling between transport colleague components.
 *
 * @pattern Mediator
 * @role Concrete Mediator
 */
public class TransportControlCenter implements ControlCenter {
    private final Map<String, Bus> buses = new LinkedHashMap<>();
    private final Map<String, Station> stations = new LinkedHashMap<>();
    private final Map<String, Driver> drivers = new LinkedHashMap<>();
    private final Queue<Bus> standbyBuses = new ArrayDeque<>();
    private final List<String> passengerNotifications = new ArrayList<>();
    private final List<String> eventLog = new ArrayList<>();

    @Override
    public void notify(Object sender, String event, Object data) {
        String logEntry = String.format("[EVENT] %s from %s with data: %s", event, sender, data);
        eventLog.add(logEntry);

        switch (event) {
            case "BUS_DELAYED" -> {
                if (sender instanceof Bus bus) {
                    handleBusDelay(bus, (Integer) data);
                }
            }
            case "BUS_BREAKDOWN" -> {
                if (sender instanceof Bus bus) {
                    handleBusBreakdown(bus, String.valueOf(data));
                }
            }
            case "DRIVER_AVAILABLE" -> {
                if (sender instanceof Driver driver) {
                    driver.setStatus("STANDBY");
                }
            }
            default -> eventLog.add("[WARN] Unhandled event: " + event);
        }
    }

    private void handleBusDelay(Bus bus, int delayMinutes) {
        String alert = String.format("ALERT: Bus %s (Route %s) delayed by %d mins.",
                bus.getBusId(), bus.getRouteId(), delayMinutes);

        // Update all passenger displays at stations
        for (Station station : stations.values()) {
            station.updateDisplay(alert);
        }
        // Broadcast passenger notification
        passengerNotifications.add("[Push Notification] " + alert);
    }

    private void handleBusBreakdown(Bus brokenBus, String reason) {
        brokenBus.setStatus("BREAKDOWN");

        // 1. Assign replacement bus from standby pool or generate emergency unit
        Bus replacementBus = standbyBuses.poll();
        if (replacementBus == null) {
            replacementBus = new Bus("REP-" + brokenBus.getBusId(), brokenBus.getRouteId(), this);
        } else {
            replacementBus.assignRoute(brokenBus.getRouteId());
        }

        // 2. Dispatch available driver
        Driver assignedDriver = findAvailableDriver(brokenBus.getBusId());
        if (assignedDriver != null) {
            String dispatchMsg = String.format("URGENT: Bus %s breakdown (%s). Board replacement %s for Route %s.",
                    brokenBus.getBusId(), reason, replacementBus.getBusId(), brokenBus.getRouteId());
            assignedDriver.receiveDispatch(dispatchMsg, replacementBus.getBusId());
        }

        // 3. Update all station digital boards
        String stationMsg = String.format("SERVICE UPDATE: Bus %s broken down (%s). Replacement Bus %s deployed.",
                brokenBus.getBusId(), reason, replacementBus.getBusId());
        for (Station station : stations.values()) {
            station.updateDisplay(stationMsg);
        }

        // 4. Broadcast to passengers
        passengerNotifications.add("[Passenger Alert] " + stationMsg);
    }

    private Driver findAvailableDriver(String brokenBusId) {
        // Prioritize driver previously on broken bus or a standby driver
        for (Driver driver : drivers.values()) {
            if (brokenBusId.equals(driver.getAssignedBusId()) || "STANDBY".equals(driver.getStatus())) {
                return driver;
            }
        }
        return drivers.values().stream().findFirst().orElse(null);
    }

    @Override
    public void registerBus(Bus bus) {
        buses.put(bus.getBusId(), bus);
    }

    public void addStandbyBus(Bus bus) {
        standbyBuses.offer(bus);
    }

    @Override
    public void registerStation(Station station) {
        stations.put(station.getStationId(), station);
    }

    @Override
    public void registerDriver(Driver driver) {
        drivers.put(driver.getDriverId(), driver);
    }

    @Override
    public Bus getBus(String busId) { return buses.get(busId); }

    @Override
    public Station getStation(String stationId) { return stations.get(stationId); }

    @Override
    public Driver getDriver(String driverId) { return drivers.get(driverId); }

    public List<String> getPassengerNotifications() {
        return Collections.unmodifiableList(passengerNotifications);
    }

    public List<String> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    public Map<String, Bus> getBuses() { return Collections.unmodifiableMap(buses); }

    public Map<String, Station> getStations() { return Collections.unmodifiableMap(stations); }

    public Map<String, Driver> getDrivers() { return Collections.unmodifiableMap(drivers); }
}
