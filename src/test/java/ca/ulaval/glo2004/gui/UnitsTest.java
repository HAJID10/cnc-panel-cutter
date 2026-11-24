package ca.ulaval.glo2004.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class UnitsTest {

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
}
