package modele.deco;

import modele.pieces.Tour;
import modele.plateau.Direction;

import java.util.List;

/**
 * Décorateur pour la Tour: mouvements orthogonaux uniquement
 */
public class DecoTour extends DecoDirectionnel {
    public DecoTour(Tour wrapped) {
        super(wrapped);
    }

    @Override
    protected List<Direction> getDirections() {
        return List.of(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
    }
}
