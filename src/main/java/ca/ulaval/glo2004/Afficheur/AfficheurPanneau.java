package ca.ulaval.glo2004.Afficheur;

import ca.ulaval.glo2004.dto.CoupeDTO;
import ca.ulaval.glo2004.dto.PanneauDTO;

import java.awt.Color;
import java.awt.Graphics;

public class AfficheurPanneau extends Afficheur {
    private static final double FACTEUR_ECHELLE = 1.0; // Conversion mm -> pixels


    // --- MÉTHODE POUR DESSINER LE PANNEAU ---
    // Dessine un panneau à partir de ses dimensions et de ses coupes
    // - Affiche le panneau en gris clair
    // - Ajoute une bordure noire autour du panneau
    // - Utilise l'AfficheurCoupe pour dessiner chaque coupe
    @Override
    public void dessiner(Graphics g, Object element) {
        if (element instanceof PanneauDTO panneauDTO) {

            // Dimensions du panneau en pixels
            int largeurPixel = mmEnPixels(panneauDTO.largeurPanneau());
            int longueurPixel = mmEnPixels(panneauDTO.longueurPanneau());

            // Dessiner le panneau
            g.setColor(Color.LIGHT_GRAY); // Couleur du panneau
            g.fillRect(0, 0, largeurPixel, longueurPixel);

            g.setColor(Color.BLACK); // Bordure du panneau
            g.drawRect(0, 0, largeurPixel, longueurPixel);

            // Dessiner les coupes
            AfficheurCoupe afficheurCoupe = new AfficheurCoupe();
            for (CoupeDTO coupe : panneauDTO.coupesPanneauDTO()) {
                afficheurCoupe.dessiner(g, coupe);
            }
        }
    }

    // --- MÉTHODE UTILITAIRE ---
    // Convertit une dimension donnée en millimètres (mm) en pixels
    private int mmEnPixels(double mm) {
        return (int) (mm * FACTEUR_ECHELLE);
    }
}
