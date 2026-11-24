package ca.ulaval.glo2004.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import ca.ulaval.glo2004.domain.cuts.BorderCut;
import ca.ulaval.glo2004.domain.cuts.HorizontalCut;
import ca.ulaval.glo2004.domain.cuts.LCut;
import ca.ulaval.glo2004.domain.cuts.RectangularCut;
import ca.ulaval.glo2004.domain.cuts.VerticalCut;

class CutTest {

    private final Tool saw = new Tool("Saw", 4.0);

    @Test
    void toolIsRequiredWithAClearMessage() {
        // Original bug: NullPointerException, because the tool was used before being checked.
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new VerticalCut(new Point2D.Double(10, 0), new Point2D.Double(10, 100), null));
        assertTrue(e.getMessage().contains("tool"));
    }

    @Test
    void straightCutsCheckTheirDirection() {
        assertThrows(IllegalArgumentException.class, () -> new VerticalCut(new Point2D.Double(10, 0), new Point2D.Double(11, 100), saw));
        assertThrows(IllegalArgumentException.class, () -> new HorizontalCut(new Point2D.Double(0, 10), new Point2D.Double(100, 11), saw));
    }

    @Test
    void cutAreaIsAsWideAsTheTool() {
        // Original bug: the area was twice the tool width.
        VerticalCut cut = new VerticalCut(new Point2D.Double(50, 0), new Point2D.Double(50, 100), saw);
        Rectangle area = cut.getCutArea().getBounds();
        assertEquals(4, area.width);
    }

    @Test
    void selectionHasATolerance() {
        VerticalCut cut = new VerticalCut(new Point2D.Double(50, 0), new Point2D.Double(50, 100), saw);
        assertTrue(cut.contains(new Point2D.Double(50, 50)));
        assertTrue(cut.contains(new Point2D.Double(54, 50)));   // 2 mm half tool + 3 mm tolerance
        assertFalse(cut.contains(new Point2D.Double(60, 50)));
    }

    @Test
    void lCutCanBeSelectedOnBothArms() {
        // Corner at (100, 80): horizontal arm back to x = 20, vertical arm back to y = 10.
        LCut cut = new LCut(new Point2D.Double(20, 10), new Point2D.Double(100, 80), saw);
        assertTrue(cut.contains(new Point2D.Double(40, 80)), "horizontal arm");
        assertTrue(cut.contains(new Point2D.Double(100, 30)), "vertical arm (lost in the original version)");
        assertFalse(cut.contains(new Point2D.Double(40, 30)), "inside the L");
        assertEquals(new Point2D.Double(100, 80), cut.referencePoint());
    }

    @Test
    void rectangularCutCanBeSelectedFromInside() {
        RectangularCut cut = new RectangularCut(new Point2D.Double(100, 100), 200, 50, saw);
        assertTrue(cut.contains(new Point2D.Double(200, 125)));
        assertFalse(cut.contains(new Point2D.Double(350, 125)));
    }

    @Test
    void movingARectangleDoesNotStretchIt() {
        RectangularCut cut = new RectangularCut(new Point2D.Double(100, 100), 200, 50, saw);
        cut.translate(-90, -80);
        assertEquals(new Point2D.Double(210, 70), cut.getDestination());
        assertTrue(cut.contains(new Point2D.Double(100, 40)));
        assertFalse(cut.contains(new Point2D.Double(200, 125)), "the area followed the move");
    }

    @Test
    void borderCanBeSelected() {
        // Original bug: the border shape was four points at (0, 0).
        BorderCut border = new BorderCut(new Point2D.Double(10, 10), 500, 300, saw);
        assertTrue(border.contains(new Point2D.Double(10, 150)));
        assertTrue(border.contains(new Point2D.Double(510, 150)));
        assertFalse(border.contains(new Point2D.Double(250, 150)), "the middle of the panel is not cut");
        assertEquals(new Point2D.Double(260, 160), border.referencePoint());
    }

    @Test
    void resizingABorderKeepsItCentred() {
        BorderCut border = new BorderCut(new Point2D.Double(10, 10), 500, 300, saw);
        border.resize(300, 100);
        assertEquals(new Point2D.Double(110, 110), border.getOrigin());
        assertEquals(new Point2D.Double(260, 160), border.referencePoint());
        assertThrows(IllegalArgumentException.class, () -> border.resize(0, 100));
    }

    @Test
    void changingToolChangesTheWidth() {
        VerticalCut cut = new VerticalCut(new Point2D.Double(50, 0), new Point2D.Double(50, 100), saw);
        cut.setTool(new Tool("Router bit", 10.0));
        assertEquals(10, cut.getCutArea().getBounds().width);
    }

    @Test
    void pointsAreNotShared() {
        Point2D.Double origin = new Point2D.Double(50, 0);
        VerticalCut cut = new VerticalCut(origin, new Point2D.Double(50, 100), saw);
        origin.setLocation(80, 0);
        cut.getOrigin().setLocation(80, 0);
        assertEquals(new Point2D.Double(50, 0), cut.getOrigin());
    }

    @Test
    void straightCutsOnlySlideAcross() {
        VerticalCut vertical = new VerticalCut(new Point2D.Double(50, 0), new Point2D.Double(50, 100), saw);
        HorizontalCut horizontal = new HorizontalCut(new Point2D.Double(0, 50), new Point2D.Double(100, 50), saw);
        assertEquals(new Point2D.Double(70, 0), vertical.movedOrigin(new Point2D.Double(70, 30)));
        assertEquals(new Point2D.Double(0, 70), horizontal.movedOrigin(new Point2D.Double(30, 70)));
    }
}
