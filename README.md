# CNC Panel Cutter

[![build](https://github.com/HAJID10/cnc-panel-cutter/actions/workflows/build.yml/badge.svg)](https://github.com/HAJID10/cnc-panel-cutter/actions/workflows/build.yml)

A Java 21 / Swing desktop app to plan the cuts on a wooden panel for a CNC router, and export them as G-code.
You set the panel size, place cuts with the mouse or with exact values, pick a tool (router bit) for each cut,
mark the areas the bit must never touch, and the app writes the program for the machine.

![Screenshot: a panel with a border, straight cuts, a rectangle, an L-shaped groove and two clamp zones](docs/capture.png)

## Features

- **Five kinds of cut**: vertical, horizontal, rectangular, L-shaped (any direction) and border (trims the panel
  to a final size, always centred, exactly the size you type).
- **Mouse or exact values**: click on the panel, or type positions in the side panel. Any cut can be selected,
  dragged, or edited by values.
- **Depth per cut**: through cuts (the default) or grooves; shallow grooves are drawn lighter.
- **Tools**: several bits with their own width. Changing a width updates every cut that uses it, and a tool still
  in use can't be deleted.
- **Forbidden zones** (clamps, screws): a cut that would touch one is refused. A zone placed over existing cuts
  shows them in red, and G-code export waits until they are fixed.
- **Checks**: cuts must stay on the panel; duplicates and rectangles smaller than the bit are refused, with a
  clear message.
- **Millimetres or inches** (View menu). The model and the files always stay in millimetres.
- **Undo / redo** (last 100 steps), **projects saved as readable JSON**, **G-code export**, grid with snapping,
  zoom and pan.

## Using it

| Action | How |
|---|---|
| Add a cut | Pick its type in the *Cuts* tab, then click on the panel (two clicks for a rectangle, an L or a zone), or fill in the side panel and press *Add*. |
| Edit a cut | *Select / move*, click the cut, change the values, *Apply*. Or drag it. |
| Delete | Select it, then *Delete selected* or the Delete key. |
| Zoom / pan | Mouse wheel / drag an empty spot. *Fit panel to window* resets the view. |
| Tools | *Tools* tab: add a bit, change its width, choose the one used for new cuts. |
| Files | *File*: new panel, open / save (`.json`), export G-code (`.nc`). Ctrl+Z / Ctrl+Y undo and redo. |

### G-code conventions

- Millimetres, absolute coordinates (`G21 G90`).
- Origin at the **bottom-left corner** of the panel, **Z0 on the top surface**. The screen shows y going down,
  so machine Y = panel length − y.
- One pass per cut at its full depth. Cuts are grouped by tool, with an `M0` stop to change the bit.
- Fixed defaults: feed 1000 mm/min, plunge 300 mm/min, safe height 5 mm, spindle 18 000 rpm.
  **Check them for your machine and material before running the program.**

## Build and run

Requirements: JDK 21. Maven is not needed, the wrapper downloads it.

```bash
./mvnw package                      # compiles and runs the 86 tests (mvnw.cmd on Windows)
java -jar target/cnc-panel-cutter.jar
```

## Architecture

```
gui (Swing)  ──>  controller.CncController  ──>  domain: Panel, Cut + 5 subclasses, Tool, ForbiddenZone
                         │
                         ├── dto + mappers   read-only copies for the screen, the undo history and files
                         └── infra           History (undo/redo), ProjectFile + Json, GCodeWriter
```

- The interface only receives DTOs, so it can't change the model behind the controller's back.
- Each cut describes the **path** followed by the centre of the bit (`Line2D`, `Path2D`, `Rectangle2D`). The
  removed material, the click area, the drawing, the zone checks and the G-code are all derived from that path,
  so they always agree.
- Every change goes through one method of the controller: if the domain refuses it, the panel is put back as it
  was and no empty undo step is left.
- No dependencies apart from JUnit 5 (JSON reading and writing is a small class of its own).

The original UML model (Visual Paradigm) is in [`docs/modele-uml.vpp`](docs/modele-uml.vpp).

## Changelog

**2026, after the course**

- Code, interface and documentation translated to English; the cut types now carry their own behaviour (no more
  chains of `instanceof`).
- New: depth per cut, forbidden zones, L-cut editor, mm / inch switch, G-code export, decimal positions.
- Projects saved as JSON instead of Java serialization (files survive code changes, and opening one can't run
  code).
- Fixed: empty undo steps after a refused action, deleting a tool still in use, negative sizes for L cuts drawn
  backwards, duplicate cuts, borders 4 mm too small, depth equal to the bit width, rounding to whole millimetres.
- Maven wrapper, GitHub Actions build, 86 tests (there were 30).

**Earlier fixes**: tests that no longer compiled, moving a cut through the interface that moved a copy, cut ids
that changed after undo or loading, redo that broke the history, border and half of the L that couldn't be
selected, cut areas twice too wide, rectangles stretched when moved, `.jar` that wouldn't start.

## Credits

Projet d'équipe réalisé dans le cadre du cours GLO-2004 / IFT-2007 (Génie logiciel orienté objet, Université
Laval, automne 2024) par **Ismail Regragui, Taha-Yassine Rouissi, Bilal Draa, Yassine Tabet et Hajid Jeddi**.
Reprise et améliorations en 2026 par Hajid Jeddi.

MIT License, see [`LICENSE`](LICENSE).
