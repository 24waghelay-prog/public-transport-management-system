package transport.memento;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Caretaker managing the undo and redo history stacks for Schedule snapshots.
 * Ensures the redo stack is cleared whenever a new mutation is recorded.
 *
 * @pattern Memento
 * @role Caretaker
 */
public class ScheduleHistory {
    private final Deque<ScheduleMemento> undoStack = new ArrayDeque<>();
    private final Deque<ScheduleMemento> redoStack = new ArrayDeque<>();
    private final int maxHistorySize;

    public ScheduleHistory() {
        this(50);
    }

    public ScheduleHistory(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * Records a new state snapshot before making an edit.
     * Clears the redo stack to prevent invalid forward history branches.
     *
     * @param memento state snapshot to record
     */
    public void saveState(ScheduleMemento memento) {
        if (memento == null) {
            throw new IllegalArgumentException("Memento cannot be null");
        }
        if (undoStack.size() >= maxHistorySize) {
            undoStack.removeLast();
        }
        undoStack.push(memento);
        redoStack.clear(); // Clear forward redo branch on new modification
    }

    /**
     * Checks if undo operation is available.
     *
     * @return true if previous states exist in the undo stack
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Checks if redo operation is available.
     *
     * @return true if states exist in the redo stack
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Undoes the last change, pushing current state to the redo stack and returning
     * the prior state from the undo stack.
     *
     * @param currentState the current state of the originator
     * @return prior ScheduleMemento state
     * @throws IllegalStateException if undo stack is empty
     */
    public ScheduleMemento undo(ScheduleMemento currentState) {
        if (!canUndo()) {
            throw new IllegalStateException("No schedule history available to undo");
        }
        if (currentState != null) {
            redoStack.push(currentState);
        }
        return undoStack.pop();
    }

    /**
     * Redoes the last undone change, pushing current state to the undo stack and returning
     * the future state from the redo stack.
     *
     * @param currentState the current state of the originator
     * @return restored ScheduleMemento state
     * @throws IllegalStateException if redo stack is empty
     */
    public ScheduleMemento redo(ScheduleMemento currentState) {
        if (!canRedo()) {
            throw new IllegalStateException("No forward history available to redo");
        }
        if (currentState != null) {
            undoStack.push(currentState);
        }
        return redoStack.pop();
    }

    public int getUndoCount() {
        return undoStack.size();
    }

    public int getRedoCount() {
        return redoStack.size();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public List<ScheduleMemento> getUndoHistory() {
        return Collections.unmodifiableList(List.copyOf(undoStack));
    }
}
