package ca.ulaval.glo2004.dto.mappers;

import ca.ulaval.glo2004.domain.Tool;
import ca.ulaval.glo2004.dto.ToolDto;

/** Converts tools between the domain and DTOs. */
public class ToolMapper {

    public Tool toDomain(ToolDto dto) {
        Tool tool = new Tool(dto.name(), dto.width());
        tool.setId(dto.id());
        tool.setDefault(dto.isDefault());
        return tool;
    }

    public ToolDto toDto(Tool tool) {
        if (tool == null) {
            return null;
        }
        return new ToolDto(tool.getId(), tool.getName(), tool.getWidth(), tool.isDefault());
    }
}
