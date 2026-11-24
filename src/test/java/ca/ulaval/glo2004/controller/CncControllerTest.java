package ca.ulaval.glo2004.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ToolDto;

class CncControllerTest {

    private CncController controller;
    private ToolDto saw;

    @BeforeEach
    void create() {
        saw = new ToolDto(UUID.randomUUID(), "Saw", 4.0, true);
        controller = new CncController(new PanelDto(UUID.randomUUID(), 1200, 900, 18, new ArrayList<>(), List.of(saw)));
    }

    private List<CutDto> cuts() {
        return controller.getPanel().cuts();
    }

    @Test
    void undoAndRedo() {
        controller.addVerticalCut(100, null);
        controller.addVerticalCut(200, null);
        UUID id = cuts().get(0).id();

        controller.undo();
        assertEquals(1, cuts().size());
        // Original bug: ids changed after each undo.
        assertEquals(id, cuts().get(0).id());

        controller.redo();
        assertEquals(2, cuts().size());
        // Original bug: the first undo after a redo did nothing.
        controller.undo();
        assertEquals(1, cuts().size());
        controller.undo();
        assertEquals(0, cuts().size());
    }

    @Test
    void selectingDoesNotFillTheHistory() {
        controller.addVerticalCut(100, null);
        UUID id = cuts().get(0).id();
        controller.selectCut(id);
        controller.deselectAll();
        controller.selectCut(id);
        controller.undo(); // undoes the add, not the selection
        assertTrue(cuts().isEmpty());
    }

    @Test
    void movingWithADto() {
        controller.addVerticalCut(100, null);
        // Original bug: a copy of the cut was moved.
        controller.moveCut(cuts().get(0).id(), new Point(400, 0));
        assertEquals(400, cuts().get(0).origin().x);
    }

    @Test
    void resizingWithADto() {
        controller.addBorderCut(800, 1000, null); // width, length
        controller.resizeCut(cuts().get(0).id(), 600, 800);
        assertEquals(600, cuts().get(0).width());

        controller.addRectangularCut(new Point(100, 100), new Point(200, 200), null);
        controller.resizeCut(cuts().get(1).id(), 300, 50);
        assertEquals(300, cuts().get(1).width());
        assertEquals(50, cuts().get(1).height());
    }

    @Test
    void editCutIsOneUndoStep() {
        controller.addRectangularCut(new Point(100, 100), new Point(200, 200), null);
        CutDto before = cuts().get(0);
        controller.editCut(before.id(), new Point(300, 300), 50, 60, null);
        assertEquals(new Point(300, 300), cuts().get(0).origin());
        controller.undo();
        assertEquals(before, cuts().get(0));
    }

    @Test
    void selectingByClick() {
        controller.addHorizontalCut(500, null);
        assertTrue(controller.findCutAt(new Point(300, 501)).isPresent());
        assertTrue(controller.findCutAt(new Point(300, 600)).isEmpty());
    }

    @Test
    void toolWidthIsAppliedToCuts() {
        controller.addVerticalCut(100, null);
        controller.setToolWidth(saw.id(), 8.0);
        assertEquals(8.0, cuts().get(0).tool().width());
    }

    @Test
    void aRefusedActionLeavesNoUndoStep() {
        controller.addVerticalCut(100, null);
        assertThrows(IllegalArgumentException.class, () -> controller.addVerticalCut(5000, null));
        controller.undo(); // must undo the first cut, not an empty step
        assertTrue(cuts().isEmpty());
    }

    @Test
    void aRefusedEditChangesNothing() {
        controller.addRectangularCut(new Point(100, 100), new Point(200, 200), null);
        CutDto before = cuts().get(0);
        // The resize is fine but the move goes outside the panel: nothing at all must change.
        assertThrows(IllegalArgumentException.class,
                () -> controller.editCut(before.id(), new Point(880, 100), 50, 50, null));
        assertEquals(before, cuts().get(0));
    }

    @Test
    void thePanelKeepsItsIdAfterUndo() {
        UUID id = controller.getPanel().id();
        controller.addVerticalCut(100, null);
        controller.undo();
        assertEquals(id, controller.getPanel().id());
    }

    @Test
    void saveAndOpen(@TempDir Path folder) throws Exception {
        controller.addVerticalCut(100, null);
        controller.addLCut(new Point(100, 100), new Point(300, 300), null);
        List<CutDto> before = cuts();
        String file = folder.resolve("panel.cnc").toString();
        controller.save(file);

        controller.newPanel(new PanelDto(UUID.randomUUID(), 500, 500, 10, new ArrayList<>(), List.of(saw)));
        assertTrue(cuts().isEmpty());

        controller.load(file);
        assertEquals(before, cuts());
        assertEquals(1200, controller.getPanel().length());
    }
}
