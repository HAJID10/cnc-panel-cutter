package ca.ulaval.glo2004.infra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ToolDto;

class ProjectFileTest {

    private final ProjectFile file = new ProjectFile();
    private final ToolDto saw = new ToolDto(UUID.randomUUID(), "Saw \"1/8\"", 3.175, true);
    private final ToolDto bit = new ToolDto(UUID.randomUUID(), "Bit", 6, false);

    private PanelDto sample() {
        CutDto vertical = new CutDto(UUID.randomUUID(), CutType.VERTICAL, new Point2D.Double(300.25, 0),
                new Point2D.Double(300.25, 1219.2), 0, 1219.2, 18, saw, false);
        CutDto groove = new CutDto(UUID.randomUUID(), CutType.L, new Point2D.Double(500, 500),
                new Point2D.Double(400, 300), 100, 200, 6.5, bit, false);
        return new PanelDto(UUID.randomUUID(), 1219.2, 914.4, 18, List.of(vertical, groove), List.of(saw, bit));
    }

    @Test
    void saveAndOpenGiveBackTheSamePanel(@TempDir Path folder) throws IOException {
        PanelDto panel = sample();
        String path = folder.resolve("p.json").toString();
        file.save(panel, path);
        assertEquals(panel, file.load(path));
    }

    @Test
    void theFileIsReadableJson(@TempDir Path folder) throws IOException {
        String path = folder.resolve("p.json").toString();
        file.save(sample(), path);
        String text = Files.readString(Path.of(path));
        assertTrue(text.contains("\"format\": \"cnc-panel-cutter\""));
        assertTrue(text.contains("\"origin\": [300.25, 0]"));
        assertTrue(text.contains("\"name\": \"Saw \\\"1/8\\\"\""));
    }

    @Test
    void badFilesGiveAClearMessage() {
        assertMessage("{ not json", "Invalid JSON");
        assertMessage("{\"format\": \"something else\"}", "not a CNC Panel Cutter project");
        assertMessage("{\"format\": \"cnc-panel-cutter\", \"version\": 99, \"panel\": {}}", "newer version");
        assertMessage("{\"format\": \"cnc-panel-cutter\", \"version\": 1, \"panel\": {\"id\": \"x\"}}", "");
        // A cut pointing to a tool that is not listed in the file.
        String unknownTool = file.toJson(sample())
                .replaceFirst("\"toolId\": \"[^\"]+\"", "\"toolId\": \"" + UUID.randomUUID() + "\"");
        assertMessage(unknownTool, "tool that is not in the file");
    }

    private void assertMessage(String json, String expected) {
        IOException e = assertThrows(IOException.class, () -> file.fromJson(json));
        assertTrue(e.getMessage().contains(expected), e.getMessage());
    }
}
