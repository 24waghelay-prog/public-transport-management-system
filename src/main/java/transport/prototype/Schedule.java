package transport.prototype;

import transport.memento.ScheduleMemento;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Schedule represents a scheduled transit service on a specific route.
 * Implements deep copying for Prototype cloning and snapshot capture/restoration
 * for Memento support.
 *
 * @pattern Prototype / Memento
 * @role Concrete Prototype / Originator
 */
public class Schedule {
    private String scheduleId;
    private String routeName;
    private String dayType;
    private List<String> departureTimes;
    private List<String> stops;
    private String vehicleId;
    private boolean active;

    public Schedule(String scheduleId, String routeName, String dayType,
                    List<String> departureTimes, List<String> stops,
                    String vehicleId, boolean active) {
        this.scheduleId = scheduleId;
        this.routeName = routeName;
        this.dayType = dayType;
        this.departureTimes = departureTimes != null ? new ArrayList<>(departureTimes) : new ArrayList<>();
        this.stops = stops != null ? new ArrayList<>(stops) : new ArrayList<>();
        this.vehicleId = vehicleId;
        this.active = active;
    }

    /**
     * Copy constructor performing deep copying of mutable lists.
     *
     * @param other source schedule to copy
     */
    public Schedule(Schedule other) {
        this.scheduleId = other.scheduleId;
        this.routeName = other.routeName;
        this.dayType = other.dayType;
        this.departureTimes = new ArrayList<>(other.departureTimes);
        this.stops = new ArrayList<>(other.stops);
        this.vehicleId = other.vehicleId;
        this.active = other.active;
    }

    /**
     * Creates a deep copy of the schedule.
     */
    public Schedule copy() {
        return new Schedule(this);
    }

    // --- Memento Originator Methods ---

    /**
     * Captures current schedule state into an immutable memento snapshot.
     */
    public ScheduleMemento createMemento() {
        return new ScheduleMemento(
                scheduleId, routeName, dayType,
                List.copyOf(departureTimes), List.copyOf(stops),
                vehicleId, active
        );
    }

    /**
     * Restores schedule state from a memento snapshot.
     */
    public void restore(ScheduleMemento memento) {
        if (memento == null) throw new IllegalArgumentException("Memento cannot be null");
        this.scheduleId = memento.scheduleId();
        this.routeName = memento.routeName();
        this.dayType = memento.dayType();
        this.departureTimes = new ArrayList<>(memento.departureTimes());
        this.stops = new ArrayList<>(memento.stops());
        this.vehicleId = memento.vehicleId();
        this.active = memento.active();
    }

    // --- Getters & Setters ---
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getDayType() { return dayType; }
    public void setDayType(String dayType) { this.dayType = dayType; }
    public List<String> getDepartureTimes() { return departureTimes; }
    public void addDepartureTime(String time) { this.departureTimes.add(time); }
    public void removeDepartureTime(String time) { this.departureTimes.remove(time); }
    public List<String> getStops() { return stops; }
    public void addStop(String stop) { this.stops.add(stop); }
    public void removeStop(String stop) { this.stops.remove(stop); }
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Schedule s)) return false;
        return active == s.active &&
                Objects.equals(scheduleId, s.scheduleId) &&
                Objects.equals(routeName, s.routeName) &&
                Objects.equals(dayType, s.dayType) &&
                Objects.equals(departureTimes, s.departureTimes) &&
                Objects.equals(stops, s.stops) &&
                Objects.equals(vehicleId, s.vehicleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId, routeName, dayType, departureTimes, stops, vehicleId, active);
    }

    @Override
    public String toString() {
        return String.format("[%s] Route: '%s' | Day: %s | Vehicle: %s | Times: %s | Stops: %s | Active: %b",
                scheduleId, routeName, dayType, vehicleId, departureTimes, stops, active);
    }
}
