package ca.ulaval.glo2004.infra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Stack;

import ca.ulaval.glo2004.dto.PanneauDTO;

public class UndoRedoManager {
  private final Stack<PanneauDTO> undoStack = new Stack<>();
  private final Stack<PanneauDTO> redoStack = new Stack<>();

  public void saveState(PanneauDTO state) {
    undoStack.push(deepCopy(state));
    redoStack.clear();
  }

  public PanneauDTO undo(PanneauDTO currentState) {
    if (!undoStack.isEmpty()) {
      redoStack.push(deepCopy(currentState));
      return undoStack.pop();
    }
    return null;
  }

  /**
   * Bug corrigé : l'ancienne version empilait l'état rétabli (au lieu de l'état courant) dans la pile
   * d'annulation, si bien que la première annulation après un rétablissement ne faisait rien.
   */
  public PanneauDTO redo(PanneauDTO currentState) {
    if (!redoStack.isEmpty()) {
      undoStack.push(deepCopy(currentState));
      return redoStack.pop();
    }
    return null;
  }

  private PanneauDTO deepCopy(PanneauDTO panneau) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bos);
      out.writeObject(panneau);

      ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
      ObjectInputStream in = new ObjectInputStream(bis);
      return (PanneauDTO) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("Erreur lors de la copie", e);
    }
  }

  public void saveToFile(PanneauDTO panneau, String filePath) throws IOException {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
      oos.writeObject(panneau);
    }
  }

  public PanneauDTO loadFromFile(String filePath) throws IOException, ClassNotFoundException {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
      return (PanneauDTO) ois.readObject();
    }
  }
}