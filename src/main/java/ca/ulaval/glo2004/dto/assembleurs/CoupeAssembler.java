package ca.ulaval.glo2004.dto.assembleurs;


import ca.ulaval.glo2004.domain.Coupe;
import ca.ulaval.glo2004.domain.ordinaire.CoupeHorizontale;
import ca.ulaval.glo2004.domain.TypeCoupe;
import ca.ulaval.glo2004.domain.irregulieres.CoupeRectangulaire;
import ca.ulaval.glo2004.domain.irregulieres.CoupeBordure;
import ca.ulaval.glo2004.domain.irregulieres.CoupeEnL;
import ca.ulaval.glo2004.domain.ordinaire.CoupeVerticale;
import ca.ulaval.glo2004.domain.Outil;
import ca.ulaval.glo2004.dto.CoupeDTO;

public class CoupeAssembler {
    private final OutilAssembler outilAssembler;

    public CoupeAssembler(OutilAssembler outilAssembler) {
        this.outilAssembler = outilAssembler;
    }

    public Coupe creerCoupe(CoupeDTO dto) {
        Outil outil = outilAssembler.creerOutil(dto.outilDTO());

        Coupe coupe = switch (dto.typeCoupe()) {
            case VERTICAL -> new CoupeVerticale(
                    dto.origine(),
                    dto.destination(),
                    outil
            );
            case HORIZONTAL -> new CoupeHorizontale(
                    dto.origine(),
                    dto.destination(),
                    outil
            );
            case RECTANGULAR -> new CoupeRectangulaire(
                    dto.origine(),
                    dto.largeur(),
                    dto.longueur(),
                    outil
            );
            case BORDURE -> new CoupeBordure(
                    dto.origine(),
                    dto.largeur(),
                    dto.longueur(),
                    outil
            );
            case L -> new CoupeEnL(
                    dto.origine(),
                    dto.destination(),
                    outil
            );
        };
        // Bug corrigé : chaque conversion créait un nouvel identifiant et perdait la sélection, si bien
        // qu'après une annulation ou un chargement les coupes ne pouvaient plus être retrouvées.
        coupe.setId(dto.id());
        coupe.setEstSelectionne(dto.estSelectionne());
        return coupe;
    }

    public CoupeDTO creerCoupeDTO(Coupe coupe) {
        TypeCoupe typeCoupe;
        int largeur = 0;
        int longueur = 0;

        if (coupe instanceof CoupeVerticale) {
            typeCoupe = TypeCoupe.VERTICAL;
            longueur = CoupeUtils.calculerLongueur(coupe.getOrigineDeCoupe(), coupe.getDestinationDeCoupe());
        } else if (coupe instanceof CoupeHorizontale) {
            typeCoupe = TypeCoupe.HORIZONTAL;
            largeur = CoupeUtils.calculerLargeur(coupe.getOrigineDeCoupe(), coupe.getDestinationDeCoupe());
        } else if (coupe instanceof CoupeRectangulaire rect) {
            typeCoupe = TypeCoupe.RECTANGULAR;
            largeur = rect.getLargeur();
            longueur = rect.getHauteur();
        } else if (coupe instanceof CoupeBordure bord) {
            typeCoupe = TypeCoupe.BORDURE;
            largeur = bord.getLargeur();
            longueur = bord.getLongueur();
        } else if (coupe instanceof CoupeEnL l) {
            typeCoupe = TypeCoupe.L;
            largeur = l.getLargeur();
            longueur = l.getLongueur();
        } else {
            throw new IllegalArgumentException("Type de coupe non supporté");
        }

        return new CoupeDTO(
                coupe.getId(),
                coupe.getOrigineDeCoupe(),
                coupe.getDestinationDeCoupe(),
                coupe.getProfondeurDeCoupe(),
                outilAssembler.creerOutilDTO(coupe.getOutilDeCoupe()),
                coupe.estSelectionne(),
                typeCoupe,
                largeur,
                longueur
        );
    }
}