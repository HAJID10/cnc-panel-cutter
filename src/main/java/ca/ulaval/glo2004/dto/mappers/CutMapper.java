package ca.ulaval.glo2004.dto.mappers;

import ca.ulaval.glo2004.domain.Cut;
import ca.ulaval.glo2004.domain.Tool;
import ca.ulaval.glo2004.domain.cuts.BorderCut;
import ca.ulaval.glo2004.domain.cuts.HorizontalCut;
import ca.ulaval.glo2004.domain.cuts.LCut;
import ca.ulaval.glo2004.domain.cuts.RectangularCut;
import ca.ulaval.glo2004.domain.cuts.VerticalCut;
import ca.ulaval.glo2004.dto.CutDto;

/** Converts cuts between the domain and DTOs. */
public class CutMapper {
    private final ToolMapper toolMapper;

    public CutMapper(ToolMapper toolMapper) {
        this.toolMapper = toolMapper;
    }

    /** Rebuilds a cut with the given tool (the panel's own tool object, so tool changes reach the cut). */
    public Cut toDomain(CutDto dto, Tool tool) {
        Cut cut = switch (dto.type()) {
            case VERTICAL -> new VerticalCut(dto.origin(), dto.destination(), tool);
            case HORIZONTAL -> new HorizontalCut(dto.origin(), dto.destination(), tool);
            case RECTANGULAR -> new RectangularCut(dto.origin(), dto.width(), dto.height(), tool);
            case BORDER -> new BorderCut(dto.origin(), dto.width(), dto.height(), tool);
            case L -> new LCut(dto.origin(), dto.destination(), tool);
        };
        // Keep the identity and selection, so a cut can still be found after an undo or a load.
        cut.setId(dto.id());
        cut.setDepth(dto.depth());
        cut.setSelected(dto.selected());
        return cut;
    }

    public CutDto toDto(Cut cut) {
        return new CutDto(cut.getId(), cut.type(), cut.getOrigin(), cut.getDestination(), cut.getWidth(),
                cut.getHeight(), cut.getDepth(), toolMapper.toDto(cut.getTool()), cut.isSelected());
    }
}
