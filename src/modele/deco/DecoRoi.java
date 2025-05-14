package modele.deco;

import modele.pieces.Piece;
import modele.pieces.Roi;
import modele.pieces.Tour;
import modele.plateau.Case;
import modele.plateau.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Décorateur pour le Roi: mouvements d'une case dans toutes les directions + roque
 */
public class DecoRoi extends Deco {
    public DecoRoi(Roi wrapped) {
        super(wrapped);
    }

    @Override
    public List<Case> getCasesAccessibles() {
        List<Case> result = new ArrayList<>();
        
        // Mouvements d'une case dans toutes les directions
        result.addAll(getAdjacentSquares());
        
        // Roque
        result.addAll(getRoqueMoves());

        // Filtrer les mouvements qui mettraient le roi en échec
        return filterSafeMoves(result);
    }

    /**
     * Retourne les cases adjacentes (une case dans chaque direction)
     * @return Liste des cases adjacentes accessibles
     */
    private List<Case> getAdjacentSquares() {
        List<Case> result = new ArrayList<>();
        int x = getX();
        int y = getY();
        
        // On vérifie chaque direction possible
        for (Direction dir : Direction.values()) {
            // On récupère la case relative
            Case caseRelative = getPlateau().getCaseRelative(
                    getPlateau().getCase(x, y),
                    dir.dx, dir.dy
            );
            
            // On ajoute la case seulement si elle existe et est accessible
            if (caseRelative != null && 
                (caseRelative.getPiece() == null || 
                 caseRelative.getPiece().getColor() != getColor())) {
                result.add(caseRelative);
            }
        }
        
        return result;
    }
    
    /**
     * Retourne les mouvements de roque possibles
     * @return Liste des cases accessibles par roque
     */
    private List<Case> getRoqueMoves() {
        List<Case> result = new ArrayList<>();
        Roi roi = (Roi) wrapped;
        
        // Si le roi a déjà bougé ou est en échec, pas de roque possible
        if (roi.hasMoved() || getPlateau().estEnEchec(getColor(), false)) {
            return result;
        }
        
        int y = getY();
        
        // Petit roque (côté roi)
        Case tourDroite = getPlateau().getCase(7, y);
        if (tourDroite.getPiece() instanceof Tour && !((Tour) tourDroite.getPiece()).hasMoved()) {
            if (getPlateau().getCase(5, y).getPiece() == null && 
                getPlateau().getCase(6, y).getPiece() == null) {
                
                // Vérifier que le roi ne traverse pas une case en échec
                if (!caseEnEchec(5, y) && !caseEnEchec(6, y)) {
                    result.add(getPlateau().getCase(6, y));
                }
            }
        }
        
        // Grand roque (côté dame)
        Case tourGauche = getPlateau().getCase(0, y);
        if (tourGauche.getPiece() instanceof Tour && !((Tour) tourGauche.getPiece()).hasMoved()) {
            if (getPlateau().getCase(1, y).getPiece() == null && 
                getPlateau().getCase(2, y).getPiece() == null && 
                getPlateau().getCase(3, y).getPiece() == null) {
                
                // Vérifier que le roi ne traverse pas une case en échec
                if (!caseEnEchec(1, y) && !caseEnEchec(2, y) && !caseEnEchec(3, y)) {
                    result.add(getPlateau().getCase(2, y));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Vérifie si une case serait en échec si le roi s'y trouvait
     * @param x Coordonnée x de la case
     * @param y Coordonnée y de la case
     * @return true si la case serait en échec
     */
    private boolean caseEnEchec(int x, int y) {
        Roi roi = (Roi) wrapped;
        Case caseOriginale = roi.getCurrentCase();
        Case caseTest = getPlateau().getCase(x, y);
        
        // Déplacer temporairement le roi
        caseTest.setPiece(roi);
        caseOriginale.setPiece(null);
        
        // Vérifier si le roi est en échec
        boolean enEchec = getPlateau().estEnEchec(getColor(), false);
        
        // Remettre le roi à sa place
        caseOriginale.setPiece(roi);
        caseTest.setPiece(null);
        
        return enEchec;
    }
    
    /**
     * Filtre les mouvements qui mettraient le roi en échec
     * @param mouvements Liste des mouvements à filtrer
     * @return Liste des mouvements valides
     */
    private List<Case> filterSafeMoves(List<Case> mouvements) {
        List<Case> mouvementsValides = new ArrayList<>();
        Roi roi = (Roi) wrapped;
        
        for (Case destination : mouvements) {
            Case origine = roi.getCurrentCase();
            Piece pieceCapturee = destination.getPiece();
            
            // Simuler le mouvement
            destination.setPiece(roi);
            origine.setPiece(null);
            
            // Vérifier si le roi est en échec après le mouvement
            if (!getPlateau().estEnEchec(getColor(), false)) {
                mouvementsValides.add(destination);
            }
            
            // Annuler le mouvement
            origine.setPiece(roi);
            destination.setPiece(pieceCapturee);
        }
        
        return mouvementsValides;
    }
}
