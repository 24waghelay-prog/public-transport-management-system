package transport.builder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable product class representing a configured public transit route.
 *
 * @pattern Builder
 * @role Product
 */
public class Route {
    private final String id;
    private final String name;
    private final List<String> stops;
    private final List<String> timings;
    private final double baseFare;
    private final double perStopFare;
    private final int frequencyMinutes;
    private final boolean active;

    Route(RouteBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.stops = List.copyOf(builder.stops);
        this.timings = List.copyOf(builder.timings);
        this.baseFare = builder.baseFare;
        this.perStopFare = builder.perStopFare;
        this.frequencyMinutes = builder.frequencyMinutes;
        this.active = builder.active;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getStops() {
        return stops;
    }

    public List<String> getTimings() {
        return timings;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getPerStopFare() {
        return perStopFare;
    }

    public int getFrequencyMinutes() {
        return frequencyMinutes;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route route)) return false;
        return Objects.equals(id, route.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] '%s' | Stops (%d): %s | BaseFare: $%.2f | +$%.2f/stop | Every %d min",
                id, name, stops.size(), stops, baseFare, perStopFare, frequencyMinutes);
    }
}
