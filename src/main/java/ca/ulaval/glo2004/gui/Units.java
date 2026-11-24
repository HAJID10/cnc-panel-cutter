package ca.ulaval.glo2004.gui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats and reads lengths shown to the user. The model always works in millimetres; this is the only place
 * where values are converted for display.
 */
public final class Units {
    // Up to three decimals (enough for 1/8" = 3.175 mm), no trailing zeros, always a dot as decimal separator.
    private static final DecimalFormat FORMAT = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private Units() {
    }

    /** "12.5" for 12.5 mm. */
    public static String format(double mm) {
        return FORMAT.format(mm);
    }

    /** "12.5 mm" for 12.5 mm. */
    public static String withUnit(double mm) {
        return format(mm) + " " + symbol();
    }

    public static String symbol() {
        return "mm";
    }

    /** Reads a length typed by the user (a comma is accepted as decimal separator). */
    public static double parse(String text) {
        return Double.parseDouble(text.trim().replace(',', '.'));
    }
}
