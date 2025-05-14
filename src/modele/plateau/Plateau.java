// src/main/java/modele/plateau/Plateau.java
package modele.plateau;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import modele.pieces.*;

/**
 * Représente l'échiquier, stocke les cases et la liste des pièces,
 * et définit les directions via un enum interne.
 */
public class Plateau extends Observable {
    public static final int SIZE = 8;

    private final Case[][] cases;
    private final List<Piece> pieces;

    public Plateau() {
        // Création du damier
        cases = new Case[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                cases[x][y] = new Case(x, y);
            }
        }

        // Initialisation de la liste de pièces
        pieces = new ArrayList<>();
        initPieces();
    }

    /**
     * Place toutes les pièces sur le plateau au démarrage.
     * (Déplacé depuis Jeu.initialiserPieces())
     */
    private void initPieces() {
        // Pièces blanches
        pieces.add(new Tour    (0, 0, PieceColor.WHITE, this));
        pieces.add(new Cavalier(1, 0, PieceColor.WHITE, this));
        pieces.add(new Fou     (2, 0, PieceColor.WHITE, this));
        pieces.add(new Dame    (3, 0, PieceColor.WHITE, this));
        pieces.add(new Roi     (4, 0, PieceColor.WHITE, this));
        pieces.add(new Fou     (5, 0, PieceColor.WHITE, this));
        pieces.add(new Cavalier(6, 0, PieceColor.WHITE, this));
        pieces.add(new Tour    (7, 0, PieceColor.WHITE, this));
        for (int i = 0; i < SIZE; i++) {
            pieces.add(new Pion(i, 1, PieceColor.WHITE, this));
        }

        // Pièces noires
        pieces.add(new Tour    (0, 7, PieceColor.BLACK, this));
        pieces.add(new Cavalier(1, 7, PieceColor.BLACK, this));
        pieces.add(new Fou     (2, 7, PieceColor.BLACK, this));
        pieces.add(new Dame    (3, 7, PieceColor.BLACK, this));
        pieces.add(new Roi     (4, 7, PieceColor.BLACK, this));
        pieces.add(new Fou     (5, 7, PieceColor.BLACK, this));
        pieces.add(new Cavalier(6, 7, PieceColor.BLACK, this));
        pieces.add(new Tour    (7, 7, PieceColor.BLACK, this));
        for (int i = 0; i < SIZE; i++) {
            pieces.add(new Pion(i, 6, PieceColor.BLACK, this));
        }

        // Placement sur les cases
        for (Piece p : pieces) {
            Case c = getCase(p.getX(), p.getY());
            c.setPiece(p);
        }
    }

    /**
     * Retourne la case aux coordonnées spécifiées.
     * @param x Coordonnée x
     * @param y Coordonnée y
     * @return La case aux coordonnées spécifiées, ou null si hors limites
     */
    public synchronized Case getCase(int x, int y) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) return null;
        return cases[x][y];
    }

    public Case getCaseRelative(Case origine, int dx, int dy) {
        return getCase(origine.getX() + dx, origine.getY() + dy);
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    /**
     * Vérifie si le joueur spécifié est en échec.
     * @param couleur Couleur du joueur à vérifier
     * @param checkKingMoves Si true, vérifie aussi les mouvements des rois adverses
     * @return true si le joueur est en échec
     */
    public synchronized boolean estEnEchec(PieceColor couleur, boolean checkKingMoves) {
        // Trouver le roi
        Case roiCase = null;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Case c = getCase(x, y);
                if (c.getPiece() != null
                        && c.getPiece().getType() == PieceType.ROI
                        && c.getPiece().getColor() == couleur) {
                    roiCase = c;
                    break;
                }
            }
            if (roiCase != null) break;
        }

        // Vérifier si une pièce adverse peut atteindre le roi
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Case c = getCase(x, y);
                if (c.getPiece() != null && c.getPiece().getColor() != couleur) {
                    // Si on ne vérifie pas les mouvements des rois ou si la pièce n'est pas un roi
                    if (!checkKingMoves && c.getPiece().getType() == PieceType.ROI) {
                        continue;
                    }
                    if (c.getPiece().getCasesAccessibles().contains(roiCase)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Vérifie si le joueur spécifié est en échec.
     * @param couleur Couleur du joueur à vérifier
     * @return true si le joueur est en échec
     */
    public synchronized boolean estEnEchec(PieceColor couleur) {
        return estEnEchec(couleur, true);
    }

    /**
     * Notifie les observateurs du plateau.
     */
    public synchronized void notifierObservers() {
        setChanged();
        notifyObservers();
    }

    public Case getRoi(PieceColor couleur) {
        for (Piece p : pieces) {
            if (p.getType() == PieceType.ROI && p.getColor() == couleur) {
                return getCase(p.getX(), p.getY());
            }
        }
        return null;
    }
}
