package transport.prototype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Prototype Pattern Unit Tests")
class PrototypeTest {

    @Test
    @DisplayName("Should deeply clone BusVehicle without Cloneable/Object.clone")
    void shouldCloneBusVehicleDeeply() {
        List<String> features = new ArrayList<>(List.of("WiFi", "GPS", "AC"));
        BusVehicle original = new BusVehicle("BUS-101", "Volvo 9400", 40, true, "DIESEL", features);

        Vehicle cloned = original.copy();

        assertNotSame(original, cloned, "Cloned instance must have a different memory reference");
        assertEquals(original, cloned, "Cloned instance must be equal in state initially");

        // Mutate mutable features list on clone
        cloned.addFeature("USB-Charger");
        cloned.setId("BUS-102");

        assertTrue(cloned.getFeatures().contains("USB-Charger"));
        assertFalse(original.getFeatures().contains("USB-Charger"),
                "Original vehicle features list must remain unchanged after mutating clone (deep copy)");
        assertEquals("BUS-101", original.getId());
        assertEquals("BUS-102", cloned.getId());
    }

    @Test
    @DisplayName("Should retrieve and clone registered vehicle templates from VehicleRegistry")
    void shouldCloneFromVehicleRegistry() {
        VehicleRegistry registry = new VehicleRegistry();

        assertTrue(registry.hasTemplate("AC Bus"));
        assertTrue(registry.hasTemplate("Non-AC Bus"));
        assertTrue(registry.hasTemplate("Electric Bus"));

        Vehicle acBus1 = registry.createVehicle("AC Bus", "FLEET-001");
        Vehicle acBus2 = registry.createVehicle("AC Bus", "FLEET-002");

        assertNotNull(acBus1);
        assertNotNull(acBus2);
        assertNotSame(acBus1, acBus2);
        assertEquals("FLEET-001", acBus1.getId());
        assertEquals("FLEET-002", acBus2.getId());

        // Modifying features of acBus1 should not affect acBus2 or template
        acBus1.addFeature("Sunroof");
        assertTrue(acBus1.getFeatures().contains("Sunroof"));
        assertFalse(acBus2.getFeatures().contains("Sunroof"));
    }

    @Test
    @DisplayName("Should clone Schedule deeply for Weekday to Weekend transformation")
    void shouldCloneScheduleDeeply() {
        List<String> times = new ArrayList<>(List.of("06:00", "09:00", "12:00", "17:00", "20:00"));
        List<String> stops = new ArrayList<>(List.of("Central", "Tech Park", "Airport"));

        Schedule weekdaySchedule = new Schedule(
                "SCH-12-WD", "Route 12", "Weekday", times, stops, "BUS-101", true
        );

        // Clone for weekend
        Schedule weekendSchedule = weekdaySchedule.copy();
        weekendSchedule.setScheduleId("SCH-12-WE");
        weekendSchedule.setDayType("Weekend");

        // Weekend has fewer timings and one additional tourist stop
        weekendSchedule.getDepartureTimes().clear();
        weekendSchedule.addDepartureTime("08:00");
        weekendSchedule.addDepartureTime("14:00");
        weekendSchedule.addDepartureTime("19:00");
        weekendSchedule.addStop("Heritage Museum");

        // Verify independent state
        assertEquals("SCH-12-WD", weekdaySchedule.getScheduleId());
        assertEquals("Weekday", weekdaySchedule.getDayType());
        assertEquals(5, weekdaySchedule.getDepartureTimes().size(), "Original schedule timings count unchanged");
        assertEquals(3, weekdaySchedule.getStops().size(), "Original schedule stops count unchanged");

        assertEquals("SCH-12-WE", weekendSchedule.getScheduleId());
        assertEquals("Weekend", weekendSchedule.getDayType());
        assertEquals(3, weekendSchedule.getDepartureTimes().size());
        assertEquals(4, weekendSchedule.getStops().size());
        assertTrue(weekendSchedule.getStops().contains("Heritage Museum"));
        assertFalse(weekdaySchedule.getStops().contains("Heritage Museum"));
    }
}
