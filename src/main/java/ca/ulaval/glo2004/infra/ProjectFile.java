package ca.ulaval.glo2004.infra;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ca.ulaval.glo2004.dto.PanelDto;

/** Saves and opens projects. */
public class ProjectFile {

    public void save(PanelDto panel, String path) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(panel);
        }
    }

    public PanelDto load(String path) throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            return (PanelDto) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("This file is not a project.", e);
        }
    }
}
