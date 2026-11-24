package ca.ulaval.glo2004.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ToolDto;

/** Forbidden zones: areas the tool must never touch. */
class ForbiddenZoneTest {

    private Panel panel;
    private Tool saw;

    @BeforeEach
    void create() {
        panel = new Panel(1200, 900, 18);
        saw = new Tool("Saw", 4.0);
        panel.addTool(saw);
        panel.setDefaultTool(saw.getId());
        // A clamp in the top-left corner: x 0..100, y 0..80
        panel.addZone(new Point2D.Double(0, 0), new Point2D.Double(100, 80));
    }

    private static Point2D.Double p(double x, double y) {
        return new Point2D.Double(x, y);
    }

    @Test
    void aNewCutTouchingAZoneIsRefused() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> panel.addVerticalCut(50, null));
        assertTrue(e.getMessage().contains("forbidden zone"));
        assertThrows(IllegalArgumentException.class, () -> panel.addRectangularCut(p(90, 70), p(200, 200), null));
        assertTrue(panel.getCuts().isEmpty());
    }

    @Test
    void theToolWidthCounts() {
        // The tool path at x = 101 is outside the zone (x 0..100), but a 4 mm tool removes x 99..103.
        assertThrows(IllegalArgumentException.class, () -> panel.addVerticalCut(101, null));
        panel.addVerticalCut(103, null); // removes x 101..105: just clear of the zone
        assertEquals(1, panel.getCuts().size());
    }

    @Test
    void movingACutIntoAZoneIsRefused() {
        panel.addRectangularCut(p(300, 300), p(400, 400), null);
        Cut r = panel.getCuts().get(0);
        assertThrows(IllegalArgumentException.class, () -> panel.moveCut(r.getId(), p(50, 40)));
        assertEquals(p(300, 300), r.getOrigin(), "the cut stays where it was");
    }

    @Test
    void aWiderToolCannotReachAZone() {
        panel.addVerticalCut(105, null); // removes x 103..107
        assertThrows(IllegalArgumentException.class, () -> panel.setToolWidth(saw.getId(), 12)); // would be 99..111
        assertEquals(4, saw.getWidth());
    }

    @Test
    void aZoneMayCoverExistingCutsWhichAreThenReported() {
        panel.addVerticalCut(500, null);
        Cut cut = panel.getCuts().get(0);
        assertFalse(panel.isInForbiddenZone(cut));
        panel.addZone(p(450, 500), p(550, 600)); // allowed
        assertTrue(panel.isInForbiddenZone(cut));
        // The conflicting cut can be moved out of the zone, but not inside it.
        assertThrows(IllegalArgumentException.class, () -> panel.moveCut(cut.getId(), p(520, 0)));
        panel.moveCut(cut.getId(), p(700, 0));
        assertFalse(panel.isInForbiddenZone(cut));
    }

    @Test
    void zonesStayOnThePanelAndCanBeEdited() {
        assertThrows(IllegalArgumentException.class, () -> panel.addZone(p(850, 10), p(950, 50)));
        assertThrows(IllegalArgumentException.class, () -> panel.addZone(p(10, 10), p(10, 50)), "zero width");
        UUID id = panel.getZones().get(0).getId();
        panel.editZone(id, p(200, 200), 50, 60);
        assertEquals(p(200, 200), panel.getZones().get(0).getOrigin());
        assertThrows(IllegalArgumentException.class, () -> panel.editZone(id, p(880, 200), 50, 60));
        assertEquals(p(200, 200), panel.getZones().get(0).getOrigin(), "a refused edit changes nothing");
        assertTrue(panel.findZoneAt(p(210, 210)).isPresent());
        panel.removeZone(id);
        assertTrue(panel.getZones().isEmpty());
    }

    @Test
    void zonesAreUndoableAndSaved(@TempDir Path folder) throws Exception {
        ToolDto tool = new ToolDto(UUID.randomUUID(), "Saw", 4, true);
        CncController controller = new CncController(new PanelDto(UUID.randomUUID(), 1200, 900, 18,
                new ArrayList<>(), List.of(tool)));
        controller.addVerticalCut(500, null);
        controller.addZone(p(450, 500), p(550, 600));
        assertTrue(controller.getPanel().cuts().get(0).inForbiddenZone(), "reported to the interface");

        String file = folder.resolve("zones.json").toString();
        controller.save(file);
        controller.undo();
        assertTrue(controller.getPanel().zones().isEmpty());
        assertFalse(controller.getPanel().cuts().get(0).inForbiddenZone());

        controller.load(file);
        assertEquals(1, controller.getPanel().zones().size());
        assertEquals(100, controller.getPanel().zones().get(0).width());
        assertTrue(controller.getPanel().cuts().get(0).inForbiddenZone(), "a saved conflict can still be opened");
    }
}
