package transport.memento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import transport.prototype.Schedule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Memento Pattern Unit Tests")
class MementoTest {

    private Schedule schedule;
    private ScheduleHistory history;

    @BeforeEach
    void setUp() {
        schedule = new Schedule(
                "SCH-100",
                "Downtown Express",
                "Weekday",
                new ArrayList<>(List.of("07:00", "09:00", "11:00")),
                new ArrayList<>(List.of("Station A", "Station B", "Station C")),
                "BUS-401",
                true
        );
        history = new ScheduleHistory();
    }

    @Test
    @DisplayName("Should create immutable snapshot via ScheduleMemento record")
    void shouldCreateImmutableMemento() {
        ScheduleMemento memento = schedule.createMemento();

        assertEquals("SCH-100", memento.scheduleId());
        assertEquals("Downtown Express", memento.routeName());
        assertEquals(3, memento.departureTimes().size());
        assertEquals(3, memento.stops().size());

        // Verify immutability of memento lists
        assertThrows(UnsupportedOperationException.class, () ->
                memento.departureTimes().add("13:00"));
        assertThrows(UnsupportedOperationException.class, () ->
                memento.stops().add("Station D"));

        // Mutating schedule should not affect already captured memento
        schedule.addDepartureTime("14:00");
        schedule.setDayType("Weekend");

        assertEquals(3, memento.departureTimes().size());
        assertEquals("Weekday", memento.dayType());
    }

    @Test
    @DisplayName("Should support undo operation through ScheduleHistory caretaker")
    void shouldUndoScheduleEdits() {
        // Initial state: 3 times, Weekday
        history.saveState(schedule.createMemento());

        // Edit 1: change day type and add time
        schedule.setDayType("Weekend");
        schedule.addDepartureTime("16:00");
        history.saveState(schedule.createMemento());

        // Edit 2: change route name and add stop
        schedule.setRouteName("Weekend Shuttle");
        schedule.addStop("Beach Resort");

        assertEquals("Weekend Shuttle", schedule.getRouteName());
        assertEquals(4, schedule.getStops().size());

        // First Undo: revert Edit 2 back to Edit 1
        ScheduleMemento previous1 = history.undo(schedule.createMemento());
        schedule.restore(previous1);

        assertEquals("Downtown Express", schedule.getRouteName());
        assertEquals(3, schedule.getStops().size());
        assertEquals("Weekend", schedule.getDayType());
        assertEquals(4, schedule.getDepartureTimes().size());

        // Second Undo: revert Edit 1 back to Initial state
        ScheduleMemento previous2 = history.undo(schedule.createMemento());
        schedule.restore(previous2);

        assertEquals("Downtown Express", schedule.getRouteName());
        assertEquals("Weekday", schedule.getDayType());
        assertEquals(3, schedule.getDepartureTimes().size());
        assertFalse(history.canUndo());
    }

    @Test
    @DisplayName("Should support redo operation after undo")
    void shouldRedoScheduleEdits() {
        history.saveState(schedule.createMemento());

        // Mutate schedule
        schedule.setRouteName("Late Night Loop");
        schedule.addDepartureTime("23:30");

        // Undo
        ScheduleMemento undoneState = history.undo(schedule.createMemento());
        schedule.restore(undoneState);

        assertEquals("Downtown Express", schedule.getRouteName());
        assertFalse(schedule.getDepartureTimes().contains("23:30"));
        assertTrue(history.canRedo());

        // Redo
        ScheduleMemento redoneState = history.redo(schedule.createMemento());
        schedule.restore(redoneState);

        assertEquals("Late Night Loop", schedule.getRouteName());
        assertTrue(schedule.getDepartureTimes().contains("23:30"));
        assertFalse(history.canRedo());
    }

    @Test
    @DisplayName("Should clear redo stack when a new modification is made after undo")
    void shouldClearRedoStackOnNewEdit() {
        history.saveState(schedule.createMemento());

        // Edit 1
        schedule.setDayType("Weekend");
        history.saveState(schedule.createMemento());

        // Edit 2
        schedule.setVehicleId("BUS-999");

        // Undo Edit 2
        ScheduleMemento memento = history.undo(schedule.createMemento());
        schedule.restore(memento);
        assertTrue(history.canRedo(), "Redo stack should have state after undo");

        // New edit occurs (Edit 3) instead of redo
        history.saveState(schedule.createMemento());
        schedule.setRouteName("Airport Flyer");

        // Redo stack must be cleared
        assertFalse(history.canRedo(), "Redo stack must be cleared when a new edit is saved");
        assertEquals(2, history.getUndoCount());
    }

    @Test
    @DisplayName("Should throw exceptions when undo/redo is performed on empty stacks")
    void shouldThrowOnEmptyStackAccess() {
        assertThrows(IllegalStateException.class, () -> history.undo(schedule.createMemento()));
        assertThrows(IllegalStateException.class, () -> history.redo(schedule.createMemento()));
    }
}
