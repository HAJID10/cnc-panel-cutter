package ca.ulaval.glo2004.infra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.Deque;

import ca.ulaval.glo2004.dto.PanelDto;

/** Undo / redo history made of full snapshots of the panel. */
public class History {
    private final Deque<PanelDto> undoStack = new ArrayDeque<>();
    private final Deque<PanelDto> redoStack = new ArrayDeque<>();

    /** Records the state before a change. A new change makes the redo history meaningless, so it is cleared. */
    public void record(PanelDto stateBeforeChange) {
        undoStack.push(copy(stateBeforeChange));
        redoStack.clear();
    }

    /** Returns the previous state, or null if there is nothing to undo. */
    public PanelDto undo(PanelDto currentState) {
        if (undoStack.isEmpty()) {
            return null;
        }
        redoStack.push(copy(currentState));
        return undoStack.pop();
    }

    /** Returns the next state, or null if there is nothing to redo. */
    public PanelDto redo(PanelDto currentState) {
        if (redoStack.isEmpty()) {
            return null;
        }
        undoStack.push(copy(currentState));
        return redoStack.pop();
    }

    /** Deep copy, because java.awt.Point inside the DTOs is mutable. */
    private PanelDto copy(PanelDto panel) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
                out.writeObject(panel);
            }
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
                return (PanelDto) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Could not copy the panel state.", e);
        }
    }
}
