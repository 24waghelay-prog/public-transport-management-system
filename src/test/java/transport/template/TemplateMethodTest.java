package transport.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import transport.builder.Ticket;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Template Method Pattern Unit Tests")
class TemplateMethodTest {

    @Test
    @DisplayName("BusBooking should calculate per-stop fare, assign seat, and fire afterBooking hook")
    void shouldExecuteBusBookingWorkflow() {
        BusBooking busBooking = new BusBooking();
        // 4 stops, 0% discount
        BookingRequest request = new BookingRequest(
                "Alice Smith", "Station Central", "Tech Park", 4, 12.0, "STANDARD", null, "UPI", 0.0
        );

        Ticket ticket = busBooking.book(request);

        assertNotNull(ticket);
        assertEquals("BUS", ticket.getTransportType());
        assertEquals("Alice Smith", ticket.getPassengerName());
        // 10 base + 4 * 5 = 30
        assertEquals(30.0, ticket.getBaseFare(), 0.001);
        assertEquals(30.0, ticket.getFinalFare(), 0.001);
        assertTrue(ticket.getSeatNumber().startsWith("Bus-Seat-"));
        assertTrue(busBooking.getLastHookMessage().contains("Alice Smith"));
    }

    @Test
    @DisplayName("MetroBooking should calculate distance slab fare and assign open seating")
    void shouldExecuteMetroBookingWorkflow() {
        MetroBooking metroBooking = new MetroBooking();

        // Short trip: 4.5 km -> slab 1 ($20)
        BookingRequest reqShort = new BookingRequest(
                "Bob Jones", "MG Road", "Indiranagar", 3, 4.5, "METRO", null, "CARD", 10.0
        );
        Ticket ticketShort = metroBooking.book(reqShort);
        assertEquals(20.0, ticketShort.getBaseFare(), 0.001);
        assertEquals(18.0, ticketShort.getFinalFare(), 0.001); // 10% discount
        assertEquals("Open / Standing (Unreserved)", ticketShort.getSeatNumber());
        assertTrue(metroBooking.getLastHookMessage().contains("smart token activated"));

        // Long trip: 20 km -> slab 3 ($50)
        BookingRequest reqLong = new BookingRequest(
                "Charlie Ray", "MG Road", "Whitefield", 15, 20.0, "METRO", null, "CARD", 0.0
        );
        Ticket ticketLong = metroBooking.book(reqLong);
        assertEquals(50.0, ticketLong.getBaseFare(), 0.001);
    }

    @Test
    @DisplayName("TrainBooking should calculate class-based fare and assign class coach berth")
    void shouldExecuteTrainBookingWorkflow() {
        TrainBooking trainBooking = new TrainBooking();

        // 1AC: 500 + 100 * 2.5 = 750
        BookingRequest req1AC = new BookingRequest(
                "David Miller", "New Delhi", "Agra Cantt", 1, 100.0, "AC_1_TIER", null, "NET_BANKING", 0.0
        );
        Ticket ticket1AC = trainBooking.book(req1AC);
        assertEquals(750.0, ticket1AC.getBaseFare(), 0.001);
        assertTrue(ticket1AC.getSeatNumber().contains("Coach H1"));

        // Sleeper: 120 + 100 * 0.8 = 200
        BookingRequest reqSleeper = new BookingRequest(
                "Eva Green", "New Delhi", "Agra Cantt", 1, 100.0, "SLEEPER", null, "CASH", 0.0
        );
        Ticket ticketSL = trainBooking.book(reqSleeper);
        assertEquals(200.0, ticketSL.getBaseFare(), 0.001);
        assertTrue(ticketSL.getSeatNumber().contains("Coach S2"));
    }

    @Test
    @DisplayName("Template Method should enforce validation in template method before steps")
    void shouldEnforceTemplateValidation() {
        BusBooking busBooking = new BusBooking();

        // Blank passenger name
        assertThrows(IllegalArgumentException.class, () ->
                busBooking.book(new BookingRequest("", "Station A", "Station B", 2, 5.0, "STANDARD", null, "CASH", 0.0)));

        // Matching source and destination
        assertThrows(IllegalArgumentException.class, () ->
                busBooking.book(new BookingRequest("Frank", "Station A", "Station A", 2, 5.0, "STANDARD", null, "CASH", 0.0)));
    }
}
