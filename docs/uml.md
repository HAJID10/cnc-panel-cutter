# UML model

Class and sequence diagrams of the current code, written in [Mermaid](https://mermaid.js.org/) so GitHub draws
them on this page and they can be updated with the code. Only the main members are shown. Overloads that add an
optional `depth` and simple getters/setters are left out.

The original course model (Visual Paradigm, autumn 2024) is still in [`modele-uml.vpp`](modele-uml.vpp). It
shows the French design from before the 2026 rewrite.

## 1. Layers

The interface only talks to the controller and only receives DTOs (read-only copies). The domain knows nothing
about Swing, files or G-code.

```mermaid
flowchart LR
    gui["gui<br/>MainWindow, DrawingPanel,<br/>side panels, Units"]
    controller["controller<br/>CncController"]
    domain["domain<br/>Panel, Cut + 5 types,<br/>Tool, ForbiddenZone"]
    dto["dto + mappers<br/>PanelDto, CutDto,<br/>ToolDto, ZoneDto"]
    infra["infra<br/>History, ProjectFile, Json,<br/>GCodeWriter"]

    gui -->|calls| controller
    gui -.->|reads| dto
    controller -->|changes| domain
    controller -->|builds| dto
    controller -->|uses| infra
    infra -.->|reads/writes| dto
```

## 2. Domain

Each cut describes the **path** followed by the centre of the bit. The removed material (`getCutArea`), the click
area, the drawing, the forbidden-zone check and the G-code (`pathPoints`) are all derived from that one path.
Each subclass decides how it moves, resizes and checks itself, so `Panel` never tests the concrete type.

```mermaid
classDiagram
    direction TB

    class Panel {
        -UUID id
        -double length
        -double width
        -double thickness
        +addTool(Tool)
        +removeTool(UUID toolId)
        +setDefaultTool(UUID toolId)
        +setToolWidth(UUID toolId, double width)
        +addCut(Cut, Double depth)
        +addVerticalCut(double x, UUID toolId)
        +addHorizontalCut(double y, UUID toolId)
        +addRectangularCut(Point2D corner1, Point2D corner2, UUID toolId)
        +addLCut(Point2D origin, Point2D corner, UUID toolId)
        +addBorderCut(double finalWidth, double finalLength, UUID toolId)
        +moveCut(UUID cutId, Point2D newOrigin)
        +resizeCut(UUID cutId, double width, double height)
        +reshapeCut(UUID cutId, Point2D origin, Point2D destination)
        +setCutTool(UUID cutId, UUID toolId)
        +setCutDepth(UUID cutId, double depth)
        +removeCut(UUID cutId)
        +addZone(Point2D corner1, Point2D corner2)
        +editZone(UUID zoneId, Point2D topLeft, double width, double height)
        +removeZone(UUID zoneId)
        +isInForbiddenZone(Cut) boolean
        +findCutAt(Point2D) Optional~Cut~
        +findZoneAt(Point2D) Optional~ForbiddenZone~
        +selectCut(UUID cutId)
        -checkDepth(double depth)
        -checkNotDuplicate(Cut)
        -checkZones(Cut)
        -isInside(Point2D) boolean
    }

    class Tool {
        -UUID id
        -String name
        -double width
        -boolean isDefault
    }

    class ForbiddenZone {
        -UUID id
        -Rectangle2D bounds
        +setBounds(Point2D topLeft, double width, double height)
        +touches(Cut) boolean
        +contains(Point2D) boolean
    }

    class Cut {
        <<abstract>>
        #UUID id
        #Point2D origin
        #Point2D destination
        #double depth
        #boolean selected
        +type()* CutType
        #path()* Shape
        +referencePoint()* Point2D
        #selectableInside() boolean
        +isMovable() boolean
        #constrainOrigin(Point2D requested) Point2D
        +resize(double width, double height)
        +reshape(Point2D origin, Point2D destination)
        +checkToolFits()
        +sameAs(Cut other) boolean
        +getWidth() double
        +getHeight() double
        +getCutArea() Shape
        +pathPoints() List~Point2D~
        +contains(Point2D) boolean
        +translate(double dx, double dy)
        +setDepth(double depth)
        +setTool(Tool)
    }

    class VerticalCut {
        #constrainOrigin(Point2D) Point2D
    }
    class HorizontalCut {
        #constrainOrigin(Point2D) Point2D
    }
    class RectangularCut {
        +resize(double width, double height)
        +checkToolFits()
        #selectableInside() boolean
    }
    class BorderCut {
        +resize(double width, double length)
        +checkToolFits()
        +isMovable() boolean
    }
    class LCut {
        +reshape(Point2D origin, Point2D corner)
    }

    class CutType {
        <<enumeration>>
        VERTICAL
        HORIZONTAL
        RECTANGULAR
        BORDER
        L
    }

    Panel "1" *-- "0..*" Cut : cuts
    Panel "1" *-- "1..*" Tool : tools
    Panel "1" *-- "0..*" ForbiddenZone : zones
    Cut "0..*" --> "1" Tool : tool
    Cut <|-- VerticalCut
    Cut <|-- HorizontalCut
    Cut <|-- RectangularCut
    Cut <|-- BorderCut
    Cut <|-- LCut
    Cut ..> CutType
```

Rules enforced by the domain (every refusal is an `IllegalArgumentException` with a message for the user):

| Rule | Where |
|---|---|
| A cut stays on the panel | `Panel.isInside` on add, move, resize, reshape |
| No exact duplicate | `Cut.sameAs`, `Panel.checkNotDuplicate` |
| A rectangle or border is not smaller than its bit | `checkToolFits` in `RectangularCut` and `BorderCut` |
| An L has two arms | `LCut` constructor and `reshape` |
| 0 < depth ≤ panel thickness | `Panel.checkDepth` |
| No new or moved cut touches a forbidden zone | `ForbiddenZone.touches`, `Panel.checkZones` |
| A tool used by cuts can't be removed; there is always a default tool | `Panel.removeTool` |
| A vertical cut only slides sideways, a horizontal one up and down, a border never moves | `constrainOrigin`, `isMovable` |

## 3. Controller, DTOs and infrastructure

```mermaid
classDiagram
    direction LR

    class CncController {
        -Panel panel
        -change(Runnable action)
        +getPanel() PanelDto
        +newPanel(PanelDto)
        +undo()
        +redo()
        +save(String path)
        +load(String path)
        +gCode() String
        +exportGCode(String path)
        +addVerticalCut(double x, UUID toolId, Double depth)
        +addRectangularCut(Point2D c1, Point2D c2, UUID toolId, Double depth)
        +moveCut(UUID cutId, Point2D newOrigin)
        +editCut(UUID cutId, Point2D origin, Double width, Double height, UUID toolId, Double depth)
        +editLCut(UUID cutId, Point2D origin, Point2D corner, UUID toolId, Double depth)
        +deleteCut(UUID cutId)
        +getCutArea(UUID cutId) Optional~Shape~
        +findCutAt(Point2D) Optional~CutDto~
        +addZone(Point2D c1, Point2D c2)
        +editZone(UUID zoneId, Point2D topLeft, double width, double height)
        +addTool(ToolDto)
        +removeTool(UUID toolId)
        +setToolWidth(UUID toolId, double width)
    }

    class PanelDto {
        <<record>>
        UUID id
        double length
        double width
        double thickness
        List~CutDto~ cuts
        List~ToolDto~ tools
        List~ZoneDto~ zones
    }
    class CutDto {
        <<record>>
        UUID id
        CutType type
        Point2D origin
        Point2D destination
        double width
        double height
        double depth
        ToolDto tool
        boolean selected
        boolean inForbiddenZone
    }
    class ToolDto {
        <<record>>
        UUID id
        String name
        double width
        boolean isDefault
    }
    class ZoneDto {
        <<record>>
        UUID id
        Point2D origin
        double width
        double height
    }

    class PanelMapper {
        +toDomain(PanelDto) Panel
        +toDto(Panel) PanelDto
    }
    class CutMapper {
        +toDomain(CutDto, Tool) Cut
        +toDto(Cut, boolean inForbiddenZone) CutDto
    }
    class ToolMapper {
        +toDomain(ToolDto) Tool
        +toDto(Tool) ToolDto
    }

    class History {
        +MAX_STEPS = 100$
        +record(PanelDto before)
        +undo(PanelDto current) PanelDto
        +redo(PanelDto current) PanelDto
    }
    class ProjectFile {
        +save(PanelDto, String path)
        +load(String path) PanelDto
        +toJson(PanelDto) String
        +fromJson(String) PanelDto
    }
    class Json {
        <<utility>>
        +write(Object value)$ String
        +read(String text)$ Object
    }
    class GCodeWriter {
        +write(double width, double length, double thickness, List~Job~ jobs) String
    }
    class Job {
        <<record>>
        String label
        String toolName
        double toolWidth
        double depth
        List~Point2D~ path
    }

    CncController --> PanelMapper
    CncController --> History
    CncController --> ProjectFile
    CncController --> GCodeWriter
    PanelMapper --> CutMapper
    PanelMapper --> ToolMapper
    CutMapper --> ToolMapper
    ProjectFile ..> Json
    GCodeWriter ..> Job
    PanelDto *-- CutDto
    PanelDto *-- ToolDto
    PanelDto *-- ZoneDto
    CutDto --> ToolDto
    History ..> PanelDto
    ProjectFile ..> PanelDto
```

## 4. User interface

```mermaid
classDiagram
    direction TB

    class MainWindow {
        +refresh()
        +panelReplaced()
        +unitsChanged()
        -updateSidePanel(PanelDto, Optional~CutDto~)
    }
    class AppMenuBar {
        -newPanel()
        -open()
        -save()
        -exportGCode()
    }
    class DrawingPanel {
        -Mode mode
        -UUID selectedZone
        +setMode(Mode)
        +fitToWindow()
        +getSelectedZone() Optional~ZoneDto~
        -onPress(MouseEvent)
        -onDrag(MouseEvent)
        -onRelease()
    }
    class Mode {
        <<enumeration>>
        NONE
        SELECT
        VERTICAL
        HORIZONTAL
        RECTANGULAR
        BORDER
        L
        ZONE
    }
    class PanelRenderer {
        +draw(Graphics2D, PanelDto, ...)
        ~colourForDepth(double depth, double thickness)$ Color
    }
    class Units {
        <<utility>>
        +format(double mm)$ String
        +parse(String text)$ double
        +setCurrent(Unit)$
    }
    class CutEditorPanel {
        <<abstract>>
        +showCreate()
        +showEdit(CutDto)
        #create()*
        #update(CutDto)*
        #fillFields(CutDto)*
        #clearFields()*
    }
    class StraightCutPanel
    class RectangularCutPanel
    class BorderCutPanel
    class LCutPanel
    class ZonePanel {
        +showCreate()
        +showEdit(ZoneDto)
    }
    class ToolPanel {
        +refresh()
    }

    MainWindow *-- DrawingPanel
    MainWindow *-- AppMenuBar
    MainWindow *-- ToolPanel
    MainWindow *-- "4..*" CutEditorPanel : side panel
    MainWindow *-- ZonePanel : side panel
    DrawingPanel --> PanelRenderer
    DrawingPanel ..> Mode
    CutEditorPanel <|-- StraightCutPanel
    CutEditorPanel <|-- RectangularCutPanel
    CutEditorPanel <|-- BorderCutPanel
    CutEditorPanel <|-- LCutPanel
    MainWindow ..> Units
    CutEditorPanel ..> Units
```

`StraightCutPanel` serves both vertical and horizontal cuts. All of these classes call `CncController`, which is
left out of this diagram to keep it readable.

## 5. A change, step by step

Every change goes through `CncController.change`. The state before the change goes into the undo history only if
the domain accepts the change. If it is refused, the panel is rebuilt as it was, so a refused action leaves no
trace and a multi-part edit is all-or-nothing.

```mermaid
sequenceDiagram
    actor User
    participant Editor as RectangularCutPanel
    participant C as CncController
    participant P as Panel
    participant H as History

    User->>Editor: types new values, presses Apply
    Editor->>C: editCut(id, origin, width, height, tool, depth)
    C->>C: before = getPanel() (DTO snapshot)
    C->>P: setCutTool / setCutDepth / resizeCut / moveCut
    alt every step accepted
        P-->>C: ok
        C->>H: record(before)
        C-->>Editor: ok
        Editor->>Editor: MainWindow.refresh() redraws
    else a rule is broken (outside the panel, forbidden zone...)
        P-->>C: IllegalArgumentException(message)
        C->>C: panel = toDomain(before)
        C-->>Editor: exception
        Editor-->>User: message box, nothing changed
    end
```
