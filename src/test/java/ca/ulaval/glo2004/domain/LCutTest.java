package ca.ulaval.glo2004.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ToolDto;

/** Editing an L cut by its start and corner (new L-cut panel). */
class LCutTest {

    private CncController controller;

    private static Point2D.Double p(double x, double y) {
        return new Point2D.Double(x, y);
    }

    @BeforeEach
    void create() {
        ToolDto saw = new ToolDto(UUID.randomUUID(), "Saw", 4, true);
        controller = new CncController(new PanelDto(UUID.randomUUID(), 1200, 900, 18, new ArrayList<>(), List.of(saw)));
        controller.addLCut(p(100, 100), p(300, 400), null, 9.0);
    }

    private CutDto l() {
        return controller.getPanel().cuts().get(0);
    }

    @Test
    void anLNeedsTwoArms() {
        assertThrows(IllegalArgumentException.class, () -> controller.addLCut(p(500, 500), p(500, 700), null));
        assertThrows(IllegalArgumentException.class, () -> controller.addLCut(p(500, 500), p(700, 500), null));
        assertThrows(IllegalArgumentException.class, () -> controller.addLCut(p(500, 500), p(500, 500), null));
    }

    @Test
    void startAndCornerChangeInOneStep() {
        CutDto before = l();
        controller.editLCut(before.id(), p(600, 700), p(450, 500), null, 12.0); // now pointing up and left
        assertEquals(p(600, 700), l().origin());
        assertEquals(p(450, 500), l().destination());
        assertEquals(150, l().width());
        assertEquals(12, l().depth());
        controller.undo();
        assertEquals(before, l());
    }

    @Test
    void aRefusedEditChangesNothing() {
        CutDto before = l();
        assertThrows(IllegalArgumentException.class,
                () -> controller.editLCut(before.id(), p(100, 100), p(950, 400), null, 3.0)); // corner off the panel
        assertEquals(before, l());
    }

    @Test
    void onlyAnLCanBeReshaped() {
        controller.addVerticalCut(700, null);
        UUID vertical = controller.getPanel().cuts().get(1).id();
        assertThrows(IllegalArgumentException.class,
                () -> controller.editLCut(vertical, p(700, 0), p(710, 100), null, null));
    }
}
