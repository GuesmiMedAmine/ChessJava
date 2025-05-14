package modele.deco;

import modele.pieces.Pion;
import modele.pieces.PieceColor;
import modele.plateau.Case;

import java.util.ArrayList;
import java.util.List;

/**
 * Décorateur pour le Pion: mouvements spécifiques (avance d'une ou deux cases, prend en diagonale)
 */
public class DecoPion extends Deco {
    public DecoPion(Pion wrapped) {
        super(wrapped);
    }

    @Override
    public List<Case> getCasesAccessibles() {
        List<Case> result = new ArrayList<>();
        int x = getX();
        int y = getY();
        
        // Direction: les blancs montent (y augmente), les noirs descendent (y diminue)
        int dir = (getColor() == PieceColor.WHITE) ? 1 : -1;

        // Avancer d'une case
        Case next = getPlateau().getCase(x, y + dir);
        if (next != null && next.getPiece() == null) {
            result.add(next);
            
            // Avancer de deux cases depuis la position initiale
            boolean estPositionInitiale = (getColor() == PieceColor.WHITE && y == 1) || 
                                          (getColor() == PieceColor.BLACK && y == 6);
            if (estPositionInitiale) {
                Case next2 = getPlateau().getCase(x, y + 2 * dir);
                if (next2 != null && next2.getPiece() == null) {
                    result.add(next2);
                }
            }
        }

        // Prise en diagonale
        for (int dx : new int[]{ -1, 1 }) {
            Case diag = getPlateau().getCase(x + dx, y + dir);
            if (diag != null && diag.getPiece() != null && 
                diag.getPiece().getColor() != getColor()) {
                result.add(diag);
            }
        }

        // Prise en passant
        if ((getColor() == PieceColor.WHITE && y == 4) || 
            (getColor() == PieceColor.BLACK && y == 3)) {
            for (int dx : new int[]{ -1, 1 }) {
                Case adj = getPlateau().getCase(x + dx, y);
                if (adj != null && adj.getPiece() instanceof Pion) {
                    Pion pionAdj = (Pion) adj.getPiece();
                    if (pionAdj.getColor() != getColor() && pionAdj.isPriseEnPassantPossible()) {
                        Case ep = getPlateau().getCase(x + dx, y + dir);
                        if (ep != null) {
                            result.add(ep);
                        }
                    }
                }
            }
        }

        return result;
    }
}
