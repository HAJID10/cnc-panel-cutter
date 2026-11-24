package ca.ulaval.glo2004.Afficheur;

import java.awt.Graphics;

public abstract class Afficheur {

    // --- MÉTHODE ABSTRAITE POUR DESSINER UN ÉLÉMENT ---
    // Cette méthode doit être implémentée par les sous-classes.
    // Elle permet de dessiner un élément donné sur un objet Graphics.
    // Paramètres :
    // - g : L'objet Graphics utilisé pour dessiner.
    // - element : L'élément à dessiner (peut être un panneau, une coupe, etc.).
    public abstract void dessiner(Graphics g, Object element);
}
