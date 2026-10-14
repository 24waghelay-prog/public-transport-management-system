package transport.template;

import transport.builder.Ticket;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concrete booking implementation for intercity trains with class-based pricing and reserved berths.
 *
 * @pattern Template Method
 * @role Concrete Class
 */
public class TrainBooking extends BookingProcess {
    private static final AtomicInteger BERTH_SEQUENCE = new AtomicInteger(10);
    private String lastHookMessage = "";

    @Override
    protected double calculateFare(BookingRequest request) {
        double dist = Math.max(10.0, request.distanceKm());
        String travelClass = request.travelClass().toUpperCase();
        return switch (travelClass) {
            case "AC_1_TIER", "1AC" -> 500.0 + (dist * 2.5);
            case "AC_3_TIER", "3AC" -> 250.0 + (dist * 1.5);
            case "SLEEPER", "SL" -> 120.0 + (dist * 0.8);
            case "GENERAL", "GEN" -> 50.0 + (dist * 0.4);
            default -> 120.0 + (dist * 0.8);
        };
    }

    @Override
    protected String reserveSeat(BookingRequest request) {
        if (request.preferredSeat() != null && !request.preferredSeat().isBlank()) {
            return "Coach TR - " + request.preferredSeat().trim();
        }
        int berth = BERTH_SEQUENCE.getAndIncrement();
        String travelClass = request.travelClass().toUpperCase();
        String coachPrefix = switch (travelClass) {
            case "AC_1_TIER", "1AC" -> "H1";
            case "AC_3_TIER", "3AC" -> "B3";
            case "GENERAL", "GEN" -> "GS";
            default -> "S2";
        };
        return String.format("Coach %s - Berth %d (%s)", coachPrefix, berth, travelClass);
    }

    @Override
    protected void afterBooking(Ticket ticket) {
        this.lastHookMessage = "Train PNR manifest updated for Passenger: " + ticket.getPassengerName();
    }

    @Override
    public String getTransportType() {
        return "TRAIN";
    }

    public String getLastHookMessage() {
        return lastHookMessage;
    }
}
