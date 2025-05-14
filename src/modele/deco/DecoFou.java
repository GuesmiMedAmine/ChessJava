package modele.deco;

import modele.pieces.Fou;
import modele.plateau.Direction;

import java.util.List;

/**
 * Décorateur pour le Fou: mouvements diagonaux uniquement
 */
public class DecoFou extends DecoDirectionnel {
    public DecoFou(Fou wrapped) {
        super(wrapped);
    }

    @Override
    protected List<Direction> getDirections() {
        return List.of(Direction.UP_LEFT, Direction.UP_RIGHT, 
                      Direction.DOWN_LEFT, Direction.DOWN_RIGHT);
    }
}

