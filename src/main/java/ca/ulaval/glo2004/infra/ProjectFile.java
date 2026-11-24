package ca.ulaval.glo2004.infra;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ToolDto;
import ca.ulaval.glo2004.dto.ZoneDto;

/**
 * Saves and opens projects as readable JSON. Example:
 * <pre>
 * {
 *   "format": "cnc-panel-cutter",
 *   "version": 1,
 *   "panel": { "id": "...", "length": 1219.2, "width": 914.4, "thickness": 18,
 *              "tools": [ { "id": "...", "name": "Saw", "width": 4, "default": true } ],
 *              "cuts":  [ { "id": "...", "type": "VERTICAL", "origin": [300, 0], "destination": [300, 1219.2],
 *                           "width": 0, "height": 1219.2, "depth": 18, "toolId": "..." } ],
 *              "zones": [ { "id": "...", "origin": [0, 0], "width": 100, "height": 80 } ] }
 * }
 * </pre>
 * Unlike Java serialization, the file survives changes to the classes, can be read by other programs, and opening
 * a file from someone else cannot run any code.
 */
public class ProjectFile {
    static final String FORMAT = "cnc-panel-cutter";
    static final int VERSION = 1;

    public void save(PanelDto panel, String path) throws IOException {
        Files.writeString(Path.of(path), toJson(panel), StandardCharsets.UTF_8);
    }

    public PanelDto load(String path) throws IOException {
        return fromJson(Files.readString(Path.of(path), StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------ PanelDto -> JSON

    public String toJson(PanelDto panel) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", panel.id().toString());
        p.put("length", panel.length());
        p.put("width", panel.width());
        p.put("thickness", panel.thickness());
        p.put("tools", panel.tools().stream().map(ProjectFile::tool).toList());
        p.put("cuts", panel.cuts().stream().map(ProjectFile::cut).toList());
        p.put("zones", panel.zones().stream().map(ProjectFile::zone).toList());

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("format", FORMAT);
        root.put("version", VERSION);
        root.put("panel", p);
        return Json.write(root);
    }

    private static Map<String, Object> tool(ToolDto tool) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("id", tool.id().toString());
        t.put("name", tool.name());
        t.put("width", tool.width());
        t.put("default", tool.isDefault());
        return t;
    }

    private static Map<String, Object> cut(CutDto cut) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("id", cut.id().toString());
        c.put("type", cut.type().name());
        c.put("origin", List.of(cut.origin().x, cut.origin().y));
        c.put("destination", List.of(cut.destination().x, cut.destination().y));
        c.put("width", cut.width());
        c.put("height", cut.height());
        c.put("depth", cut.depth());
        c.put("toolId", cut.tool().id().toString()); // tools are listed once, cuts refer to them
        return c;
    }

    private static Map<String, Object> zone(ZoneDto zone) {
        Map<String, Object> z = new LinkedHashMap<>();
        z.put("id", zone.id().toString());
        z.put("origin", List.of(zone.origin().x, zone.origin().y));
        z.put("width", zone.width());
        z.put("height", zone.height());
        return z;
    }

    // ------------------------------------------------------------------ JSON -> PanelDto

    /** Reads a project. Any problem (bad JSON, missing field, unknown tool) becomes an IOException with a clear message. */
    public PanelDto fromJson(String text) throws IOException {
        try {
            Map<String, Object> root = object(Json.read(text), "file");
            if (!FORMAT.equals(root.get("format"))) {
                throw new IllegalArgumentException("this is not a CNC Panel Cutter project");
            }
            int version = (int) number(root, "version");
            if (version > VERSION) {
                throw new IllegalArgumentException("the file was made by a newer version (format " + version + ")");
            }
            Map<String, Object> p = object(root.get("panel"), "panel");

            List<ToolDto> tools = new ArrayList<>();
            for (Object o : list(p, "tools")) {
                Map<String, Object> t = object(o, "tool");
                tools.add(new ToolDto(uuid(t, "id"), string(t, "name"), number(t, "width"), bool(t, "default")));
            }

            List<CutDto> cuts = new ArrayList<>();
            for (Object o : list(p, "cuts")) {
                Map<String, Object> c = object(o, "cut");
                UUID toolId = uuid(c, "toolId");
                ToolDto tool = tools.stream().filter(t -> t.id().equals(toolId)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("a cut uses a tool that is not in the file"));
                cuts.add(new CutDto(uuid(c, "id"), CutType.valueOf(string(c, "type")), point(c, "origin"),
                        point(c, "destination"), number(c, "width"), number(c, "height"), number(c, "depth"),
                        tool, false));
            }
            List<ZoneDto> zones = new ArrayList<>();
            for (Object o : p.containsKey("zones") ? list(p, "zones") : List.of()) { // optional: may be absent
                Map<String, Object> z = object(o, "zone");
                zones.add(new ZoneDto(uuid(z, "id"), point(z, "origin"), number(z, "width"), number(z, "height")));
            }
            return new PanelDto(uuid(p, "id"), number(p, "length"), number(p, "width"), number(p, "thickness"),
                    cuts, tools, zones);
        } catch (IllegalArgumentException | ClassCastException e) {
            throw new IOException("This project file can't be read: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value, String what) {
        if (!(value instanceof Map)) {
            throw new IllegalArgumentException(what + " should be a JSON object");
        }
        return (Map<String, Object>) value;
    }

    private static Object field(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException("missing \"" + key + "\"");
        }
        return map.get(key);
    }

    private static double number(Map<String, Object> map, String key) {
        return ((Number) field(map, key)).doubleValue();
    }

    private static String string(Map<String, Object> map, String key) {
        return (String) field(map, key);
    }

    private static boolean bool(Map<String, Object> map, String key) {
        return (Boolean) field(map, key);
    }

    private static UUID uuid(Map<String, Object> map, String key) {
        return UUID.fromString(string(map, key));
    }

    private static List<?> list(Map<String, Object> map, String key) {
        return (List<?>) field(map, key);
    }

    private static Point2D.Double point(Map<String, Object> map, String key) {
        List<?> xy = list(map, key);
        if (xy.size() != 2) {
            throw new IllegalArgumentException("\"" + key + "\" should be [x, y]");
        }
        return new Point2D.Double(((Number) xy.get(0)).doubleValue(), ((Number) xy.get(1)).doubleValue());
    }
}
