package ca.ulaval.glo2004.gui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import ca.ulaval.glo2004.dto.ToolDto;

/**
 * Formats and reads lengths shown to the user, in the unit chosen in the View menu. The model and the project
 * files always work in millimetres; this is the only place where values are converted.
 */
public final class Units {

    /** A display unit and how many millimetres it is worth. */
    public enum Unit {
        MM("mm", 1.0, 0.5),
        INCH("in", 25.4, 25.4 / 32); // clicks snap to 1/32 inch

        private final String symbol;
        private final double mm;
        private final double clickStepMm;

        Unit(String symbol, double mm, double clickStepMm) {
            this.symbol = symbol;
            this.mm = mm;
            this.clickStepMm = clickStepMm;
        }
    }

    // Up to three decimals (enough for 1/8" = 3.175 mm or 0.125 in), no trailing zeros, always a dot.
    private static final DecimalFormat FORMAT = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static Unit current = Unit.MM;

    private Units() {
    }

    public static Unit current() {
        return current;
    }

    public static void setCurrent(Unit unit) {
        current = unit;
    }

    /** Millimetres to the current unit. */
    public static double toDisplay(double mm) {
        return mm / current.mm;
    }

    /** Current unit to millimetres. */
    public static double toMm(double value) {
        return value * current.mm;
    }

    /** "12.5" for 12.5 mm (or "0.492" in inches). */
    public static String format(double mm) {
        return FORMAT.format(toDisplay(mm));
    }

    /** "12.5 mm" */
    public static String withUnit(double mm) {
        return format(mm) + " " + symbol();
    }

    public static String symbol() {
        return current.symbol;
    }

    /** Form label: "Width (mm):" */
    public static String label(String name) {
        return name + " (" + symbol() + "):";
    }

    /** Reads a length typed by the user in the current unit and returns millimetres (comma or dot accepted). */
    public static double parse(String text) {
        return toMm(Double.parseDouble(text.trim().replace(',', '.')));
    }

    /** How precise a mouse click is, in millimetres. */
    public static double clickStepMm() {
        return current.clickStepMm;
    }

    /** "Saw (3.175 mm)" */
    public static String describe(ToolDto tool) {
        return tool.name() + " (" + withUnit(tool.width()) + ")";
    }
}
