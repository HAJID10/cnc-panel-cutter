package ca.ulaval.glo2004.domain;

import java.io.Serializable;
import java.util.UUID;

public class Outil  implements Serializable {
    private UUID id;
    private String nom;  // Nom de l'outil
    private double largeurOutil;  // Largeur de la coupe en mm
    private boolean utiliseParDefaut;  // Indicateur si l'outil est utilisé par défaut

    public Outil(String nom, double largeurOutil) {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'outil ne peut pas être null ou vide.");
        }
        this.nom = nom;
        if (largeurOutil <= 0) {
            throw new IllegalArgumentException("La largeur de l'outil doit être positive.");
        }
        this.largeurOutil = largeurOutil;
        this.utiliseParDefaut = false;
        this.id = UUID.randomUUID();

    }

    // --- MÉTHODES D'ACCÈS ---

    // Retourne l'identifiant unique de l'outil
    public UUID getId() {
        return id;
    }

    // Définit l'identifiant unique de l'outil
    public void setId(UUID id) {
        this.id = id;
    }

    // Retourne le nom de l'outil
    public String getNomOutil() {
        return nom;
    }


    // Retourne la largeur de coupe de l'outil
    public double getLargeur() {
        return largeurOutil;
    }

    public void setLargeur(double largeurOutil) {
        if (largeurOutil <= 0) {
            throw new IllegalArgumentException("La largeur de l'outil doit être positive.");
        }
        this.largeurOutil = largeurOutil;
    }

    // Vérifie si l'outil est utilisé par défaut
    public boolean estParDefault() {
        return utiliseParDefaut;
    }

    // Active l'outil comme outil par défaut
    public void activerOutilParDefaut() {
        this.utiliseParDefaut = true;
    }

    // Désactive l'outil comme outil par défaut
    public void desactiverOutilParDefaut() {
        this.utiliseParDefaut = false;
    }


}