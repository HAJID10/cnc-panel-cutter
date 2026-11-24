package ca.ulaval.glo2004.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.ulaval.glo2004.domain.cuts.BorderCut;

class PanelTest {

    private Panel panel;
    private Tool saw;

    @BeforeEach
    void create() {
        // length (y axis) 1200 mm, width (x axis) 900 mm
        panel = new Panel(1200, 900, 18);
        saw = new Tool("Saw", 4.0);
        panel.addTool(saw);
        panel.setDefaultTool(saw.getId());
    }

    private Cut cut(int index) {
        return panel.getCuts().get(index);
    }

    @Test
    void dimensionsAreChecked() {
        assertThrows(IllegalArgumentException.class, () -> new Panel(0, 900, 18));
        assertThrows(IllegalArgumentException.class, () -> new Panel(1200, -1, 18));
    }

    @Test
    void straightCutsCrossThePanel() {
        panel.addVerticalCut(300, null);
        panel.addHorizontalCut(400, null);
        assertEquals(2, panel.getCuts().size());
        assertEquals(new Point(300, 1200), cut(0).getDestination());
        assertEquals(new Point(900, 400), cut(1).getDestination());
        assertThrows(IllegalArgumentException.class, () -> panel.addVerticalCut(950, null));
    }

    @Test
    void withoutDefaultToolTheMessageIsClear() {
        Panel empty = new Panel(100, 100, 10);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> empty.addVerticalCut(10, null));
        assertTrue(e.getMessage().contains("tool"));
    }

    @Test
    void aChosenToolIsUsedInsteadOfTheDefault() {
        Tool bit = new Tool("Bit", 8.0);
        panel.addTool(bit);
        panel.addVerticalCut(300, bit.getId());
        assertEquals(bit, cut(0).getTool());
    }

    @Test
    void rectangleCanBeDrawnInAnyDirection() {
        panel.addRectangularCut(new Point(300, 300), new Point(100, 200), null);
        assertEquals(new Point(100, 200), cut(0).getOrigin());
        assertEquals(200, cut(0).getWidth());
        assertEquals(100, cut(0).getHeight());
    }

    @Test
    void movingACut() {
        panel.addVerticalCut(300, null);
        Cut cut = cut(0);
        panel.moveCut(cut.getId(), new Point(500, 0));
        assertEquals(500, cut.getOrigin().x);
        assertTrue(cut.contains(new Point(500, 600)));
        assertFalse(cut.contains(new Point(300, 600)));
        assertThrows(IllegalArgumentException.class, () -> panel.moveCut(cut.getId(), new Point(1000, 0)));
    }

    @Test
    void movingAnLCut() {
        panel.addLCut(new Point(100, 100), new Point(300, 400), null);
        Cut l = cut(0);
        panel.moveCut(l.getId(), new Point(200, 150));
        assertEquals(new Point(400, 450), l.getDestination());
        assertThrows(IllegalArgumentException.class, () -> panel.moveCut(l.getId(), new Point(800, 100)));
    }

    @Test
    void aBorderCannotBeMoved() {
        panel.addBorderCut(800, 1000, null);
        assertThrows(IllegalArgumentException.class, () -> panel.moveCut(cut(0).getId(), new Point(0, 0)));
    }

    @Test
    void resizingARectangle() {
        panel.addRectangularCut(new Point(100, 100), new Point(300, 200), null);
        Cut r = cut(0);
        panel.resizeCut(r.getId(), 400, 150);
        assertEquals(new Point(500, 250), r.getDestination());
        assertThrows(IllegalArgumentException.class, () -> panel.resizeCut(r.getId(), 900, 150));
        assertEquals(new Point(500, 250), r.getDestination(), "a refused resize leaves the cut unchanged");
    }

    @Test
    void borderIsCentredAndResizable() {
        panel.addBorderCut(800, 1000, null);
        BorderCut b = (BorderCut) cut(0);
        assertEquals(new Point(50, 100), b.getOrigin());
        panel.resizeCut(b.getId(), 600, 800);
        assertEquals(new Point(150, 200), b.getOrigin());
        assertThrows(IllegalArgumentException.class, () -> panel.resizeCut(b.getId(), 1000, 800));
    }

    @Test
    void changingAToolWidthUpdatesItsCuts() {
        panel.addVerticalCut(300, null);
        panel.setToolWidth(saw.getId(), 12.0);
        assertEquals(12, cut(0).getCutArea().getBounds().width);
        assertThrows(IllegalArgumentException.class, () -> panel.setToolWidth(saw.getId(), 0));
    }

    @Test
    void onlyOneCutIsSelected() {
        panel.addVerticalCut(100, null);
        panel.addVerticalCut(200, null);
        panel.selectCut(cut(0).getId());
        panel.selectCut(cut(1).getId());
        assertFalse(cut(0).isSelected());
        assertEquals(cut(1), panel.getSelectedCut().orElseThrow());
    }

    @Test
    void cutListIsReadOnly() {
        panel.addVerticalCut(100, null);
        assertThrows(UnsupportedOperationException.class, () -> panel.getCuts().clear());
    }

    @Test
    void onlyOneDefaultTool() {
        Tool bit = new Tool("Bit", 10.0);
        panel.addTool(bit);
        panel.setDefaultTool(bit.getId());
        assertEquals(bit, panel.getDefaultTool());
        assertFalse(saw.isDefault());
        assertThrows(IllegalArgumentException.class, () -> panel.addTool(bit));
    }
}
