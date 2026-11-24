# CNC Panel Cutter — Java / Swing

> *A Java 21 Swing desktop app to plan cuts on a wooden panel for a CNC router: straight, rectangular, L-shaped and border cuts, multiple tools, selection, move/resize, undo/redo and save/load. Layered architecture (domain / DTO / controller / GUI) with a JUnit 5 test suite.*

Application de bureau qui rend la CNC aussi simple qu'une scie à panneaux : on définit les dimensions du
panneau, on place les coupes à la souris ou par valeurs exactes, et on choisit l'outil (largeur de coupe).

![Capture d'écran](docs/capture.png)

## Fonctionnalités

- Coupes **verticales**, **horizontales**, **rectangulaires**, **en L** et **de bordure** (ramène le panneau à une
  taille finale, toujours centrée).
- Plusieurs **outils** avec leur largeur de coupe; changer la largeur d'un outil met à jour ses coupes.
- **Sélection** au clic (avec une tolérance de 3 mm), **déplacement**, **redimensionnement** et changement d'outil.
- **Annuler / Rétablir**, **enregistrement / ouverture** de projets, grille, zoom.

## Architecture

```
GUI (Swing)  ──>  CNC_Controller  ──>  domain (Panneau, Coupe et sous-classes, Outil)
                        │
                        └── dto + assembleurs (états immuables pour l'affichage, l'historique et les fichiers)
infra/UndoRedoManager   historique et sérialisation
```

Chaque coupe décrit le **trajet** suivi par le centre de l'outil (`Line2D`, `Path2D`, `Rectangle2D`); la zone coupée
et la zone de sélection en sont déduites avec un `BasicStroke` de la largeur de l'outil.

## Compiler et lancer

Prérequis : JDK 21+ et Maven 3.9+.

```bash
mvn package                          # compile et exécute les 30 tests
java -jar target/cnc-panel-cutter.jar
```

## Conception

Le modèle UML (Visual Paradigm) est dans [`docs/modele-uml.vpp`](docs/modele-uml.vpp).

## Crédits

Projet d'équipe réalisé dans le cadre du cours GLO-2004 / IFT-2007 (Génie logiciel orienté objet, Université Laval,
automne 2024) par **Ismail Regragui, Taha-Yassine Rouissi, Bilal Draa, Yassine Tabet et Hajid Jeddi**.

Corrections apportées ensuite : les tests étaient tous en commentaire et ne compilaient plus (réécrits : 30 tests);
déplacer ou redimensionner une coupe via l'interface agissait sur une copie; les identifiants des coupes changeaient
après chaque annulation ou chargement; « Rétablir » corrompait l'historique; la sélection remplissait l'historique;
coupes de bordure impossibles à sélectionner et créées avec largeur et longueur inversées; une moitié de la coupe en
L non sélectionnable; zones de coupe deux fois trop larges; rectangle étiré lors d'un déplacement; `.jar` non
exécutable.
