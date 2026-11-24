package ca.ulaval.glo2004.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Positions and sizes keep their decimals (they used to be rounded to whole millimetres). */
class DecimalPositionTest {

    private Panel panel;

    @BeforeEach
    void create() {
        panel = new Panel(1219.2, 914.4, 18); // 4 × 3 feet
        Tool saw = new Tool("Saw", 3.175);   // 1/8 inch
        panel.addTool(saw);
        panel.setDefaultTool(saw.getId());
    }

    @Test
    void aCutKeepsItsDecimals() {
        panel.addVerticalCut(300.25, null);
        assertEquals(300.25, panel.getCuts().get(0).getOrigin().x);
        assertEquals(1219.2, panel.getCuts().get(0).getDestination().y, "goes all the way down a decimal panel");
    }

    @Test
    void aBorderIsExactlyCentred() {
        panel.addBorderCut(901, 1200, null); // (914.4 - 901) / 2 = 6.7, not rounded to 6
        Cut border = panel.getCuts().get(0);
        assertEquals(6.7, border.getOrigin().x, 1e-9);
        assertEquals(9.6, border.getOrigin().y, 1e-9);
        assertEquals(901, border.getWidth(), 1e-9);
    }

    @Test
    void aCutCanTouchTheDecimalEdge() {
        panel.addVerticalCut(914.4, null);
        panel.addRectangularCut(new Point2D.Double(814.4, 1119.2), new Point2D.Double(914.4, 1219.2), null);
        assertEquals(2, panel.getCuts().size());
    }

    @Test
    void movingByAFractionOfAMillimetre() {
        panel.addRectangularCut(new Point2D.Double(100, 100), new Point2D.Double(200, 150), null);
        Cut r = panel.getCuts().get(0);
        panel.moveCut(r.getId(), new Point2D.Double(100.5, 99.75));
        assertEquals(new Point2D.Double(200.5, 149.75), r.getDestination());
    }
}
