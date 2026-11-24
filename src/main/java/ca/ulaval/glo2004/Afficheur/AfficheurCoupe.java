package ca.ulaval.glo2004.Afficheur;

import ca.ulaval.glo2004.dto.CoupeDTO;

import java.awt.*;

public class AfficheurCoupe extends Afficheur {
    private static final Color COULEUR_SELECTION = Color.BLUE;
    private static final Color COULEUR_NORMALE = Color.BLACK;

    // --- MÉTHODE POUR DESSINER UNE COUPE ---
    // Dessine une coupe sur le panneau :
    // - Si la coupe est sélectionnée, elle est affichée en rouge avec une bande épaisse.
    // - Sinon, elle est dessinée en noir avec une ligne fine.
    // - Gère les coupes verticales et horizontales différemment pour ajuster l'affichage.
    @Override
    public void dessiner(Graphics g, Object element) {
        if (element instanceof CoupeDTO coupeDTO) {
            Graphics2D g2d = (Graphics2D) g;

            float epaisseurOutil = (float) coupeDTO.outilDTO().largeur();

            // Configuration du style
            g2d.setColor(coupeDTO.estSelectionne() ? COULEUR_SELECTION : COULEUR_NORMALE);
            g2d.setStroke(new BasicStroke(
                    epaisseurOutil
            ));

            switch (coupeDTO.typeCoupe()) {
                case RECTANGULAR -> dessinerCoupeRectangulaire(g2d, coupeDTO);
                case VERTICAL, HORIZONTAL -> dessinerCoupeLineaire(g2d, coupeDTO);
                case BORDURE -> dessinerCoupeBordure(g2d, coupeDTO);
                case L -> dessinerCoupeL(g2d, coupeDTO);
            }
        }
    }

    private void dessinerCoupeRectangulaire(Graphics2D g2d, CoupeDTO coupeDTO) {
        Point origine = coupeDTO.origine();
        float epaisseurOutil = (float) coupeDTO.outilDTO().largeur();
        float offset = epaisseurOutil / 2;

        g2d.drawRect(
                (int) (origine.x + offset),
                (int) (origine.y + offset),
                (int) (coupeDTO.largeur() - epaisseurOutil),
                (int) (coupeDTO.longueur() - epaisseurOutil)
        );
    }

    private void dessinerCoupeLineaire(Graphics2D g2d, CoupeDTO coupeDTO) {
        g2d.drawLine(
                coupeDTO.origine().x,
                coupeDTO.origine().y,
                coupeDTO.destination().x,
                coupeDTO.destination().y
        );
    }

    private void dessinerCoupeBordure(Graphics2D g2d, CoupeDTO coupeDTO) {
        Point origine = coupeDTO.origine();
        float epaisseurOutil = (float) coupeDTO.outilDTO().largeur();

        // Dessiner le rectangle de la bordure
        g2d.setColor(coupeDTO.estSelectionne() ? COULEUR_SELECTION : COULEUR_NORMALE);
        g2d.setStroke(new BasicStroke(epaisseurOutil));
        g2d.drawRect(
                origine.x,
                origine.y,
                coupeDTO.largeur(),
                coupeDTO.longueur()
        );
    }

    private void dessinerCoupeL(Graphics2D g2d, CoupeDTO coupeDTO) {
        Point origine = coupeDTO.origine();
        Point destination = coupeDTO.destination();

        if (origine.x < destination.x && destination.y > origine.y) {
            g2d.drawLine(
                    origine.x,
                    destination.y,
                    destination.x,
                    destination.y
            );
            g2d.drawLine(
                    destination.x,
                    destination.y,
                    destination.x,
                    origine.y
            );
        } else if (origine.x < destination.x && destination.y < origine.y) {
            g2d.drawLine(
                    origine.x,
                    destination.y,
                    destination.x,
                    destination.y
            );
            g2d.drawLine(
                    destination.x,
                    destination.y,
                    destination.x,
                    origine.y
            );
        } else if (origine.x > destination.x && destination.y < origine.y) {
            g2d.drawLine(
                    destination.x,
                    origine.y,
                    destination.x,
                    destination.y
            );
            g2d.drawLine(
                    destination.x,
                    destination.y,
                    origine.x,
                    destination.y
            );
        } else {
            g2d.drawLine(
                    destination.x,
                    origine.y,
                    destination.x,
                    destination.y
            );
            g2d.drawLine(
                    destination.x,
                    destination.y,
                    origine.x,
                    destination.y
            );
        }
    }


}