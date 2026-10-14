package transport.mediator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mediator Pattern Unit Tests")
class MediatorTest {

    private TransportControlCenter controlCenter;
    private Bus bus1;
    private Bus standbyBus;
    private Station stationCentral;
    private Station stationAirport;
    private Driver driverJohn;

    @BeforeEach
    void setUp() {
        controlCenter = new TransportControlCenter();

        // Register stations
        stationCentral = new Station("ST-01", "Central Terminus", controlCenter);
        stationAirport = new Station("ST-02", "International Airport", controlCenter);

        // Register buses
        bus1 = new Bus("BUS-501", "ROUTE-A1", controlCenter);
        standbyBus = new Bus("BUS-STANDBY-01", "UNASSIGNED", controlCenter);
        standbyBus.setStatus("STANDBY");
        controlCenter.addStandbyBus(standbyBus);

        // Register driver
        driverJohn = new Driver("DRV-10", "John Doe", controlCenter);
        driverJohn.receiveDispatch("Initial assignment", bus1.getBusId());
    }

    @Test
    @DisplayName("Should broadcast bus delay to station display boards and passenger notifications")
    void shouldHandleBusDelayScenario() {
        // Bus reports delay to mediator
        bus1.reportDelay(25);

        assertEquals("DELAYED", bus1.getStatus());
        assertEquals(25, bus1.getCurrentDelayMinutes());

        // Verify station displays were updated by the mediator
        assertEquals(1, stationCentral.getDisplayBoard().size());
        assertTrue(stationCentral.getLatestAnnouncement().contains("Bus BUS-501"));
        assertTrue(stationCentral.getLatestAnnouncement().contains("delayed by 25 mins"));

        assertEquals(1, stationAirport.getDisplayBoard().size());
        assertTrue(stationAirport.getLatestAnnouncement().contains("delayed by 25 mins"));

        // Verify passenger notification was dispatched
        assertFalse(controlCenter.getPassengerNotifications().isEmpty());
        assertTrue(controlCenter.getPassengerNotifications().get(0).contains("delayed by 25 mins"));
    }

    @Test
    @DisplayName("Should handle bus breakdown by deploying replacement bus, notifying driver and updating stations")
    void shouldHandleBusBreakdownScenario() {
        // Bus reports breakdown
        bus1.reportBreakdown("Transmission Failure");

        // Verify broken bus status
        assertEquals("BREAKDOWN", bus1.getStatus());

        // Verify replacement bus was taken from standby pool and assigned to Route-A1
        assertEquals("ROUTE-A1", standbyBus.getRouteId());
        assertEquals("IN_SERVICE", standbyBus.getStatus());

        // Verify driver received dispatch for replacement bus
        assertEquals("DISPATCHED", driverJohn.getStatus());
        assertEquals(standbyBus.getBusId(), driverJohn.getAssignedBusId());
        assertTrue(driverJohn.getLastDispatchMessage().contains("Transmission Failure"));
        assertTrue(driverJohn.getLastDispatchMessage().contains(standbyBus.getBusId()));

        // Verify station display boards were updated with breakdown and replacement notice
        assertTrue(stationCentral.getLatestAnnouncement().contains("SERVICE UPDATE"));
        assertTrue(stationCentral.getLatestAnnouncement().contains("BUS-STANDBY-01 deployed"));

        // Verify passenger notifications were broadcast
        assertTrue(controlCenter.getPassengerNotifications().stream()
                .anyMatch(msg -> msg.contains("BUS-STANDBY-01 deployed")));
    }

    @Test
    @DisplayName("Should dynamically create emergency replacement bus if standby pool is empty")
    void shouldCreateEmergencyReplacementWhenNoStandbyAvailable() {
        TransportControlCenter emptyPoolCenter = new TransportControlCenter();
        Station station = new Station("ST-99", "Suburban Depot", emptyPoolCenter);
        Driver driver = new Driver("DRV-99", "Sarah Connor", emptyPoolCenter);
        Bus soloBus = new Bus("BUS-999", "ROUTE-NIGHT", emptyPoolCenter);
        driver.receiveDispatch("Initial night run", soloBus.getBusId());

        // Breakdown occurs when standby pool is empty
        soloBus.reportBreakdown("Flat tire");

        assertEquals("BREAKDOWN", soloBus.getStatus());
        assertNotNull(driver.getAssignedBusId());
        assertTrue(driver.getAssignedBusId().startsWith("REP-BUS-999"));
        assertTrue(station.getLatestAnnouncement().contains("REP-BUS-999 deployed"));
    }
}
