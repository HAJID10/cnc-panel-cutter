package ca.ulaval.glo2004.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.ulaval.glo2004.dto.PanelDto;

class HistoryTest {

    private static PanelDto state(double length) {
        return new PanelDto(UUID.randomUUID(), length, 100, 10, List.of(), List.of());
    }

    @Test
    void historyIsLimited() {
        History history = new History();
        for (int i = 1; i <= History.MAX_STEPS + 20; i++) {
            history.record(state(i));
        }
        assertEquals(History.MAX_STEPS, history.undoSteps());
        PanelDto oldest = null;
        PanelDto current = state(999);
        for (PanelDto s = history.undo(current); s != null; s = history.undo(s)) {
            oldest = s;
        }
        assertEquals(21, oldest.length(), "the 20 oldest steps were forgotten");
    }

    @Test
    void nothingToUndoOrRedo() {
        History history = new History();
        assertNull(history.undo(state(1)));
        assertNull(history.redo(state(1)));
    }
}
