package ca.ulaval.glo2004.dto.mappers;

import ca.ulaval.glo2004.domain.ForbiddenZone;
import ca.ulaval.glo2004.domain.Panel;
import ca.ulaval.glo2004.domain.Tool;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ToolDto;
import ca.ulaval.glo2004.dto.ZoneDto;

/** Converts the whole panel between the domain and DTOs. */
public class PanelMapper {
    private final ToolMapper toolMapper;
    private final CutMapper cutMapper;

    public PanelMapper(ToolMapper toolMapper, CutMapper cutMapper) {
        this.toolMapper = toolMapper;
        this.cutMapper = cutMapper;
    }

    public Panel toDomain(PanelDto dto) {
        Panel panel = new Panel(dto.id(), dto.length(), dto.width(), dto.thickness());
        for (ToolDto toolDto : dto.tools()) {
            panel.addTool(toolMapper.toDomain(toolDto));
        }
        for (CutDto cutDto : dto.cuts()) {
            // Cuts share the panel's tool objects: changing a tool's width updates its cuts.
            Tool tool = panel.findTool(cutDto.tool().id()).orElseGet(() -> toolMapper.toDomain(cutDto.tool()));
            panel.addCut(cutMapper.toDomain(cutDto, tool));
        }
        // Zones come last: a saved cut may lie under a zone added after it, which is allowed.
        for (ZoneDto zoneDto : dto.zones()) {
            ForbiddenZone zone = new ForbiddenZone(zoneDto.origin(), zoneDto.width(), zoneDto.height());
            zone.setId(zoneDto.id());
            panel.addZone(zone);
        }
        return panel;
    }

    public PanelDto toDto(Panel panel) {
        return new PanelDto(panel.getId(), panel.getLength(), panel.getWidth(), panel.getThickness(),
                panel.getCuts().stream().map(cut -> cutMapper.toDto(cut, panel.isInForbiddenZone(cut))).toList(),
                panel.getTools().stream().map(toolMapper::toDto).toList(),
                panel.getZones().stream().map(PanelMapper::toDto).toList());
    }

    private static ZoneDto toDto(ForbiddenZone zone) {
        return new ZoneDto(zone.getId(), zone.getOrigin(), zone.getWidth(), zone.getHeight());
    }
}
