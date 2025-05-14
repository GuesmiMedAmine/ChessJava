package modele.deco;

import modele.pieces.Dame;
import modele.plateau.Direction;

import java.util.Arrays;
import java.util.List;

/**
 * Décorateur pour la Dame: combine les mouvements diagonaux et orthogonaux
 */
public class DecoDame extends DecoDirectionnel {
    public DecoDame(Dame wrapped) {
        super(wrapped);
    }

    @Override
    protected List<Direction> getDirections() {
        return Arrays.asList(Direction.values());
    }
}
