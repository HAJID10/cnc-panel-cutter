package ca.ulaval.glo2004.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import ca.ulaval.glo2004.dto.ToolDto;

class UnitsTest {

    /** The unit is global: put it back so other tests see millimetres. */
    @AfterEach
    void backToMillimetres() {
        Units.setCurrent(Units.Unit.MM);
    }

    @Test
    void formatsWithoutUselessZeros() {
        assertEquals("300", Units.format(300.0));
        assertEquals("12.5", Units.format(12.5));
        assertEquals("3.175", Units.format(3.175));
        assertEquals("0.333", Units.format(1.0 / 3));
    }

    @Test
    void readsCommaOrDot() {
        assertEquals(12.5, Units.parse(" 12,5 "));
        assertEquals(12.5, Units.parse("12.5"));
        assertThrows(NumberFormatException.class, () -> Units.parse("abc"));
    }

    @Test
    void inchesAreShownAndReadButTheModelStaysInMillimetres() {
        Units.setCurrent(Units.Unit.INCH);
        assertEquals("48", Units.format(1219.2));
        assertEquals("0.125 in", Units.withUnit(3.175));
        assertEquals("Width (in):", Units.label("Width"));
        assertEquals(914.4, Units.parse("36"), 1e-9);
        assertEquals(25.4 / 32, Units.clickStepMm(), 1e-12);
        assertEquals("Saw (0.125 in)", Units.describe(new ToolDto(UUID.randomUUID(), "Saw", 3.175, true)));
    }

    @Test
    void roundTripThroughTheDisplay() {
        for (Units.Unit unit : Units.Unit.values()) {
            Units.setCurrent(unit);
            assertEquals(300.25, Units.toMm(Units.toDisplay(300.25)), 1e-9);
        }
    }
}
