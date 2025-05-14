package modele.jeu;

import modele.pieces.Piece;
import modele.plateau.Case;

/**
 * Représente un coup d'échecs - stocke la case de départ et celle d'arrivée
 */
public class Coup {
    private final Case depart;
    private final Case arrivee;
    private final Piece piece;
    private final Piece piecePrise;

    /**
     * Crée un nouveau coup
     * @param depart Case de départ
     * @param arrivee Case d'arrivée
     */
    public Coup(Case depart, Case arrivee) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.piece = depart.getPiece();
        this.piecePrise = arrivee.getPiece();
    }

    /**
     * Obtient la case de départ
     * @return La case de départ
     */
    public Case getDepart() {
        return depart;
    }

    /**
     * Obtient la case d'arrivée
     * @return La case d'arrivée
     */
    public Case getArrivee() {
        return arrivee;
    }

    /**
     * Obtient la pièce déplacée
     * @return La pièce déplacée
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Obtient la pièce capturée (le cas échéant)
     * @return La pièce capturée, ou null s'il n'y en a pas
     */
    public Piece getPiecePrise() {
        return piecePrise;
    }

    /**
     * Vérifie si ce coup est une capture
     * @return true si une pièce est capturée
     */
    public boolean estCapture() {
        return piecePrise != null;
    }

    @Override
    public String toString() {
        // Format simple: e2-e4
        char colonneDepart = (char) ('a' + depart.getX());
        char ligneDepart = (char) ('1' + depart.getY());
        char colonneArrivee = (char) ('a' + arrivee.getX());
        char ligneArrivee = (char) ('1' + arrivee.getY());
        
        return "" + colonneDepart + ligneDepart + "-" + colonneArrivee + ligneArrivee;
    }
}