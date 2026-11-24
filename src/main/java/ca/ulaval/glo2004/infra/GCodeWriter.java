package ca.ulaval.glo2004.infra;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes a simple G-code program (RS-274, as read by GRBL, Mach3, LinuxCNC…) for a list of tool paths.
 * <p>
 * Conventions, also written as comments at the top of the file:
 * <ul>
 *   <li>Units: millimetres (G21), absolute coordinates (G90).</li>
 *   <li>Origin X0 Y0: bottom-left corner of the panel; Z0: top surface of the panel. The app draws y downwards,
 *       so Y is flipped: machine Y = panel length − y.</li>
 *   <li>Each cut is one pass at its full depth: rapid move above the start, plunge, follow the path, lift.</li>
 *   <li>Cuts are grouped by tool; the program stops (M0) before each tool so the operator can change the bit.</li>
 * </ul>
 * Feed rates and the safe height are fixed defaults; they suit a small router in wood and must be checked for
 * the real machine before use.
 */
public class GCodeWriter {
    /** Height above the surface for rapid moves (mm). */
    public static final double SAFE_Z = 5.0;
    /** Cutting speed along the path (mm/min). */
    public static final double FEED = 1000;
    /** Speed when going down into the wood (mm/min). */
    public static final double PLUNGE = 300;
    /** Spindle speed (rpm). */
    public static final int SPINDLE = 18000;

    /** One cut to machine: a path (in app coordinates, mm) at a depth, with a tool. */
    public record Job(String label, String toolName, double toolWidth, double depth, List<Point2D.Double> path) {
    }

    /**
     * @param panelLength used to flip the y axis
     */
    public String write(double panelWidth, double panelLength, double thickness, List<Job> jobs) {
        if (jobs.isEmpty()) {
            throw new IllegalArgumentException("There are no cuts to export.");
        }
        StringBuilder g = new StringBuilder();
        line(g, "(CNC Panel Cutter - panel %s x %s x %s mm)", n(panelWidth), n(panelLength), n(thickness));
        line(g, "(Origin: bottom-left corner of the panel, Z0 = top surface. Units: mm.)");
        line(g, "(Feed %s mm/min, plunge %s mm/min, safe height %s mm: check them for your machine.)",
                n(FEED), n(PLUNGE), n(SAFE_Z));
        line(g, "G21 G90 G17");
        line(g, "G0 Z%s", n(SAFE_Z));

        // Group by tool, keeping the order in which tools are first used.
        Map<String, List<Job>> byTool = new LinkedHashMap<>();
        for (Job job : jobs) {
            byTool.computeIfAbsent(job.toolName() + " (" + n(job.toolWidth()) + " mm)", k -> new ArrayList<>()).add(job);
        }

        for (Map.Entry<String, List<Job>> tool : byTool.entrySet()) {
            line(g, "");
            line(g, "(Tool: %s)", tool.getKey());
            line(g, "M5");
            line(g, "M0 (Load the tool %s, then resume)", tool.getKey());
            line(g, "M3 S%d", SPINDLE);
            for (Job job : tool.getValue()) {
                writeJob(g, job, panelLength);
            }
        }

        line(g, "");
        line(g, "G0 Z%s", n(SAFE_Z));
        line(g, "M5");
        line(g, "G0 X0 Y0");
        line(g, "M30");
        return g.toString();
    }

    private void writeJob(StringBuilder g, Job job, double panelLength) {
        List<Point2D.Double> path = job.path();
        if (path.size() < 2) {
            return;
        }
        line(g, "(%s, depth %s mm)", job.label(), n(job.depth()));
        Point2D.Double first = path.get(0);
        line(g, "G0 X%s Y%s", n(first.x), n(panelLength - first.y));
        line(g, "G1 Z%s F%s", n(-job.depth()), n(PLUNGE));
        for (int i = 1; i < path.size(); i++) {
            Point2D.Double p = path.get(i);
            // The feed is given on the first cutting move only (G-code keeps it until changed).
            line(g, i == 1 ? "G1 X%s Y%s F" + n(FEED) : "G1 X%s Y%s", n(p.x), n(panelLength - p.y));
        }
        line(g, "G0 Z%s", n(SAFE_Z));
    }

    private static void line(StringBuilder g, String format, Object... args) {
        g.append(String.format(Locale.ROOT, format, args)).append('\n');
    }

    /** Up to 3 decimals, dot separator, no trailing zeros, and never "-0". */
    static String n(double value) {
        double rounded = Math.round(value * 1000) / 1000.0;
        if (rounded == 0) {
            return "0";
        }
        String s = String.format(Locale.ROOT, "%.3f", rounded);
        s = s.replaceAll("0+$", "");
        return s.endsWith(".") ? s.substring(0, s.length() - 1) : s;
    }
}
