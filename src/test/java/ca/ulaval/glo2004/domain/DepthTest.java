package ca.ulaval.glo2004.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ToolDto;

/** Each cut has its own depth: through the panel by default, or a groove. */
class DepthTest {

    private Panel panel;
    private Tool saw;

    @BeforeEach
    void create() {
        panel = new Panel(1200, 900, 18);
        saw = new Tool("Saw", 4.0);
        panel.addTool(saw);
        panel.setDefaultTool(saw.getId());
    }

    private Cut first() {
        return panel.getCuts().get(0);
    }

    @Test
    void aNewCutGoesRightThroughByDefault() {
        panel.addVerticalCut(300, null);
        assertEquals(18, first().getDepth());
    }

    @Test
    void aGrooveKeepsItsDepth() {
        panel.addHorizontalCut(300, null, 6.5);
        assertEquals(6.5, first().getDepth());
    }

    @Test
    void depthMustBeBetweenZeroAndTheThickness() {
        assertThrows(IllegalArgumentException.class, () -> panel.addVerticalCut(300, null, 0.0));
        assertThrows(IllegalArgumentException.class, () -> panel.addVerticalCut(300, null, -2.0));
        assertThrows(IllegalArgumentException.class, () -> panel.addVerticalCut(300, null, 18.5));
        panel.addVerticalCut(300, null, 18.0);
        assertThrows(IllegalArgumentException.class, () -> panel.setCutDepth(first().getId(), 20));
        assertEquals(18, first().getDepth());
    }

    @Test
    void depthNoLongerFollowsTheToolWidth() {
        // Original bug: the depth was set to the tool width (4 mm deep for a 4 mm bit).
        panel.addVerticalCut(300, null, 10.0);
        panel.setToolWidth(saw.getId(), 8);
        Tool bit = new Tool("Bit", 6);
        panel.addTool(bit);
        panel.setCutTool(first().getId(), bit.getId());
        assertEquals(10, first().getDepth());
    }

    @Test
    void depthSurvivesUndoAndEdit() {
        ToolDto tool = new ToolDto(UUID.randomUUID(), "Saw", 4, true);
        CncController controller = new CncController(new PanelDto(UUID.randomUUID(), 1200, 900, 18,
                new ArrayList<>(), List.of(tool)));
        controller.addVerticalCut(100, null, 5.0);
        UUID id = controller.getPanel().cuts().get(0).id();
        controller.editCut(id, null, null, null, null, 12.0);
        assertEquals(12, controller.getPanel().cuts().get(0).depth());
        controller.undo();
        assertEquals(5, controller.getPanel().cuts().get(0).depth());
    }
}
