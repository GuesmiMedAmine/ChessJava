package modele.deco;

import modele.pieces.Piece;
import modele.plateau.Case;
import modele.plateau.Direction;
import modele.plateau.Plateau;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire qui contient des méthodes réutilisables pour les mouvements des pièces.
 * Évite la duplication de code entre les décorateurs.
 */
public class DecoratorMoveUtils {

    /**
     * Calcule les mouvements orthogonaux (horizontaux et verticaux)
     * @param piece La pièce qui se déplace
     * @return Liste des cases accessibles
     */
    public static List<Case> getOrthogonalMoves(Piece piece) {
        return slideInDirections(piece, 
                List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT));
    }

    /**
     * Calcule les mouvements diagonaux
     * @param piece La pièce qui se déplace
     * @return Liste des cases accessibles
     */
    public static List<Case> getDiagonalMoves(Piece piece) {
        return slideInDirections(piece, 
                List.of(Direction.UP_LEFT, Direction.UP_RIGHT, Direction.DOWN_LEFT, Direction.DOWN_RIGHT));
    }

    /**
     * Glissement générique dans des directions spécifiées
     * @param piece La pièce qui se déplace
     * @param dirs Les directions dans lesquelles se déplacer
     * @return Liste des cases accessibles
     */
    public static List<Case> slideInDirections(Piece piece, List<Direction> dirs) {
        List<Case> result = new ArrayList<>();
        int x = piece.getX();
        int y = piece.getY();
        Plateau plateau = piece.getPlateau();

        for (Direction dir : dirs) {
            int step = 1;
            while (true) {
                Case c = plateau.getCaseRelative(
                        plateau.getCase(x, y),
                        dir.dx * step, dir.dy * step
                );
                if (c == null) break;
                if (c.getPiece() == null || c.getPiece().getColor() != piece.getColor()) {
                    result.add(c);
                }
                if (c.getPiece() != null) break;
                step++;
            }
        }
        return result;
    }
} 