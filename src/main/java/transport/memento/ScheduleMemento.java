package transport.memento;

import java.util.List;

/**
 * Immutable snapshot representing the memento of a Schedule at a specific point in time.
 *
 * @pattern Memento
 * @role Memento
 */
public record ScheduleMemento(
        String scheduleId,
        String routeName,
        String dayType,
        List<String> departureTimes,
        List<String> stops,
        String vehicleId,
        boolean active
) {
    public ScheduleMemento {
        // Defensive copy ensuring immutability of lists
        departureTimes = departureTimes != null ? List.copyOf(departureTimes) : List.of();
        stops = stops != null ? List.copyOf(stops) : List.of();
    }
}
