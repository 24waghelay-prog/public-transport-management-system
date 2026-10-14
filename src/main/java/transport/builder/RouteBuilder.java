package transport.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fluent Builder for constructing Route instances with integrity validation.
 *
 * @pattern Builder
 * @role Builder
 */
public class RouteBuilder {
    private static final AtomicInteger ROUTE_COUNTER = new AtomicInteger(100);

    String id;
    String name;
    final List<String> stops = new ArrayList<>();
    final List<String> timings = new ArrayList<>();
    double baseFare = 15.0;
    double perStopFare = 5.0;
    int frequencyMinutes = 20;
    boolean active = true;

    public RouteBuilder() {
        this.id = "RT-" + ROUTE_COUNTER.incrementAndGet();
    }

    public RouteBuilder id(String id) {
        if (id != null && !id.isBlank()) {
            this.id = id.trim();
        }
        return this;
    }

    public RouteBuilder name(String name) {
        this.name = name;
        return this;
    }

    public RouteBuilder addStop(String stop) {
        if (stop != null && !stop.isBlank()) {
            this.stops.add(stop.trim());
        }
        return this;
    }

    public RouteBuilder stops(List<String> stops) {
        if (stops != null) {
            stops.forEach(this::addStop);
        }
        return this;
    }

    public RouteBuilder addTiming(String time) {
        if (time != null && !time.isBlank()) {
            this.timings.add(time.trim());
        }
        return this;
    }

    public RouteBuilder timings(List<String> timings) {
        if (timings != null) {
            timings.forEach(this::addTiming);
        }
        return this;
    }

    public RouteBuilder fareRules(double baseFare, double perStopFare) {
        this.baseFare = baseFare;
        this.perStopFare = perStopFare;
        return this;
    }

    public RouteBuilder baseFare(double baseFare) {
        this.baseFare = baseFare;
        return this;
    }

    public RouteBuilder perStopFare(double perStopFare) {
        this.perStopFare = perStopFare;
        return this;
    }

    public RouteBuilder frequency(int minutes) {
        this.frequencyMinutes = minutes;
        return this;
    }

    public RouteBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    /**
     * Validates route constraints and instantiates the Route.
     * Enforces >= 2 stops, positive base fare, non-negative per-stop fare,
     * positive frequency, and non-blank name.
     *
     * @return constructed Route
     * @throws IllegalStateException if validation fails
     */
    public Route build() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Route name cannot be empty");
        }
        if (stops.size() < 2) {
            throw new IllegalStateException("A route must have at least 2 stops; currently has: " + stops.size());
        }
        if (baseFare <= 0.0) {
            throw new IllegalStateException("Base fare must be strictly positive (> 0.0); got: " + baseFare);
        }
        if (perStopFare < 0.0) {
            throw new IllegalStateException("Per-stop fare cannot be negative; got: " + perStopFare);
        }
        if (frequencyMinutes <= 0) {
            throw new IllegalStateException("Route frequency must be greater than 0 minutes; got: " + frequencyMinutes);
        }
        return new Route(this);
    }
}
