package ca.ulaval.glo2004.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.nio.file.Files;
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

class GCodeTest {

    private CncController controller;
    private ToolDto saw;
    private ToolDto bit;

    private static Point2D.Double p(double x, double y) {
        return new Point2D.Double(x, y);
    }

    @BeforeEach
    void create() {
        saw = new ToolDto(UUID.randomUUID(), "Saw", 4, true);
        bit = new ToolDto(UUID.randomUUID(), "Bit", 6, false);
        // width 900 (x), length 1200 (y), thickness 18
        controller = new CncController(new PanelDto(UUID.randomUUID(), 1200, 900, 18, new ArrayList<>(), List.of(saw, bit)));
    }

    private List<String> lines() {
        return controller.gCode().lines().toList();
    }

    @Test
    void headerAndFooter() {
        controller.addVerticalCut(300, null);
        List<String> g = lines();
        assertTrue(g.contains("G21 G90 G17"), "mm, absolute, XY plane");
        assertEquals("M30", g.get(g.size() - 1));
        assertTrue(g.contains("M5"));
    }

    @Test
    void aVerticalCutIsOnePassWithYFlipped() {
        controller.addVerticalCut(300.5, null, 18.0);
        String g = controller.gCode();
        // Drawn from y = 0 (top) to y = 1200 (bottom): machine Y goes from 1200 down to 0.
        assertTrue(g.contains("G0 X300.5 Y1200\nG1 Z-18 F300\nG1 X300.5 Y0 F1000\nG0 Z5\n"), g);
    }

    @Test
    void aRectangleIsClosedAndUsesItsDepth() {
        controller.addRectangularCut(p(100, 100), p(300, 200), null, 6.0);
        String g = controller.gCode();
        // Path offset by half the 4 mm tool inside the 200 x 100 rectangle: corners (102, 102) and (298, 198).
        assertTrue(g.contains("G0 X102 Y1098\nG1 Z-6 F300\n"), g);
        assertTrue(g.contains("G1 X298 Y1098 F1000\nG1 X298 Y1002\nG1 X102 Y1002\nG1 X102 Y1098\nG0 Z5"), g);
    }

    @Test
    void cutsAreGroupedByToolWithAStopForEachTool() {
        controller.addVerticalCut(100, saw.id());
        controller.addVerticalCut(200, bit.id());
        controller.addVerticalCut(300, saw.id());
        String g = controller.gCode();
        assertEquals(2, g.split("M0 ").length - 1, "one stop per tool");
        int sawStop = g.indexOf("M0 (Load the tool Saw");
        int bitStop = g.indexOf("M0 (Load the tool Bit");
        assertTrue(sawStop < g.indexOf("Cut 3") && g.indexOf("Cut 3") < bitStop, "both saw cuts before the bit");
    }

    @Test
    void anLIsTwoArmsFromItsStart() {
        controller.addLCut(p(100, 100), p(300, 400), null);
        String g = controller.gCode();
        assertTrue(g.contains("G0 X100 Y800\nG1 Z-18 F300\nG1 X300 Y800 F1000\nG1 X300 Y1100\n"), g);
    }

    @Test
    void refusedWhenEmptyOrWhenACutTouchesAZone() {
        assertThrows(IllegalArgumentException.class, () -> controller.gCode());
        controller.addVerticalCut(500, null);
        controller.addZone(p(450, 500), p(550, 600));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> controller.gCode());
        assertTrue(e.getMessage().contains("forbidden zone"));
    }

    @Test
    void writtenToAFile(@TempDir Path folder) throws Exception {
        controller.addHorizontalCut(600, null);
        Path file = folder.resolve("panel.nc");
        controller.exportGCode(file.toString());
        assertEquals(controller.gCode(), Files.readString(file));
    }

    @Test
    void numbersAreShortAndUseADot() {
        assertEquals("0", GCodeWriter.n(-0.0001));
        assertEquals("12.5", GCodeWriter.n(12.5));
        assertEquals("3.175", GCodeWriter.n(3.175));
        assertEquals("-18", GCodeWriter.n(-18));
    }
}
