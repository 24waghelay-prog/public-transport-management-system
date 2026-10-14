package transport.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Builder Pattern Unit Tests")
class BuilderTest {

    @Test
    @DisplayName("RouteBuilder should assemble valid Route with fluent API and preserve stop order")
    void shouldBuildValidRoute() {
        Route route = new RouteBuilder()
                .id("RT-GREEN-LINE")
                .name("Green Express Corridor")
                .addStop("Terminal 1")
                .addStop("Financial Hub")
                .addStop("Cyber City")
                .addTiming("06:00")
                .addTiming("06:30")
                .fareRules(25.0, 4.0)
                .frequency(15)
                .build();

        assertNotNull(route);
        assertEquals("RT-GREEN-LINE", route.getId());
        assertEquals("Green Express Corridor", route.getName());
        assertEquals(List.of("Terminal 1", "Financial Hub", "Cyber City"), route.getStops());
        assertEquals(2, route.getTimings().size());
        assertEquals(25.0, route.getBaseFare());
        assertEquals(4.0, route.getPerStopFare());
        assertEquals(15, route.getFrequencyMinutes());
        assertTrue(route.isActive());
    }

    @Test
    @DisplayName("RouteBuilder should fail validation when fewer than 2 stops are provided")
    void shouldFailRouteWithInsufficientStops() {
        RouteBuilder builder = new RouteBuilder()
                .name("Incomplete Route")
                .addStop("Lone Station");

        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertTrue(ex.getMessage().contains("at least 2 stops"));
    }

    @Test
    @DisplayName("RouteBuilder should fail validation for blank name, zero fare, or invalid frequency")
    void shouldFailRouteOnInvalidParameters() {
        // Blank name
        assertThrows(IllegalStateException.class, () ->
                new RouteBuilder().name("").addStop("A").addStop("B").build());

        // Zero or negative base fare
        assertThrows(IllegalStateException.class, () ->
                new RouteBuilder().name("R1").addStop("A").addStop("B").baseFare(0.0).build());

        // Negative per stop fare
        assertThrows(IllegalStateException.class, () ->
                new RouteBuilder().name("R1").addStop("A").addStop("B").baseFare(10.0).perStopFare(-1.0).build());

        // Zero or negative frequency
        assertThrows(IllegalStateException.class, () ->
                new RouteBuilder().name("R1").addStop("A").addStop("B").baseFare(10.0).frequency(0).build());
    }

    @Test
    @DisplayName("TicketBuilder should construct Ticket with sensible defaults for optional fields")
    void shouldBuildTicketWithDefaults() {
        Ticket ticket = new TicketBuilder()
                .passenger("Grace Hopper")
                .from("Central Station")
                .to("University Campus")
                .fare(45.0)
                .build();

        assertNotNull(ticket.getTicketId());
        assertTrue(ticket.getTicketId().startsWith("TKT-"));
        assertEquals("Grace Hopper", ticket.getPassengerName());
        assertEquals("Central Station", ticket.getSource());
        assertEquals("University Campus", ticket.getDestination());
        assertEquals("Unassigned", ticket.getSeatNumber());
        assertEquals(0.0, ticket.getDiscount());
        assertEquals(45.0, ticket.getFinalFare());
        assertEquals("CASH", ticket.getPaymentMode());
        assertEquals("CONFIRMED", ticket.getStatus());
        assertNotNull(ticket.getBookingTime());
    }

    @Test
    @DisplayName("TicketBuilder should compute discounted fare and support cancellation")
    void shouldComputeDiscountAndSupportCancellation() {
        Ticket ticket = new TicketBuilder()
                .passenger("Alan Turing")
                .from("Bletchley")
                .to("London Euston")
                .seat("Car-3 Seat-14")
                .fare(100.0)
                .discount(20.0) // 20% off
                .paymentMode("CREDIT_CARD")
                .build();

        assertEquals(100.0, ticket.getBaseFare());
        assertEquals(20.0, ticket.getDiscount());
        assertEquals(80.0, ticket.getFinalFare());
        assertEquals("Car-3 Seat-14", ticket.getSeatNumber());
        assertEquals("CREDIT_CARD", ticket.getPaymentMode());
        assertFalse(ticket.isCancelled());

        ticket.cancel();
        assertTrue(ticket.isCancelled());
        assertEquals("CANCELLED", ticket.getStatus());
    }

    @Test
    @DisplayName("TicketBuilder should reject invalid passenger, identical stops, and negative fare")
    void shouldRejectInvalidTicketInputs() {
        // Missing passenger
        assertThrows(IllegalStateException.class, () ->
                new TicketBuilder().from("A").to("B").fare(10.0).build());

        // Missing source
        assertThrows(IllegalStateException.class, () ->
                new TicketBuilder().passenger("User").to("B").fare(10.0).build());

        // Identical source and destination
        assertThrows(IllegalStateException.class, () ->
                new TicketBuilder().passenger("User").from("Station A").to("Station A").fare(10.0).build());

        // Zero or negative fare
        assertThrows(IllegalStateException.class, () ->
                new TicketBuilder().passenger("User").from("Station A").to("Station B").fare(0.0).build());
    }
}
