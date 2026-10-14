package transport.template;

/**
 * Encapsulates the passenger parameters for initiating a booking process.
 */
public record BookingRequest(
        String passengerName,
        String source,
        String destination,
        int stopCount,
        double distanceKm,
        String travelClass,
        String preferredSeat,
        String paymentMode,
        double discountPercent
) {
    public BookingRequest {
        travelClass = travelClass != null && !travelClass.isBlank() ? travelClass.toUpperCase() : "STANDARD";
        paymentMode = paymentMode != null && !paymentMode.isBlank() ? paymentMode.toUpperCase() : "CASH";
    }
}
