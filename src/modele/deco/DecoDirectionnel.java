package modele.deco;

import modele.pieces.Piece;
import modele.plateau.Case;
import modele.plateau.Direction;

import java.util.List;

/**
 * Classe abstraite pour les décorateurs de pièces qui se déplacent dans des directions spécifiques
 */
public abstract class DecoDirectionnel extends Deco {
    
    /**
     * Constructeur
     * @param wrapped Pièce à décorer
     */
    public DecoDirectionnel(Piece wrapped) {
        super(wrapped);
    }
    
    /**
     * Retourne la liste des directions dans lesquelles la pièce peut se déplacer
     * @return Liste des directions
     */
    protected abstract List<Direction> getDirections();
    
    @Override
    public List<Case> getCasesAccessibles() {
        return DecoratorMoveUtils.slideInDirections(this, getDirections());
    }
} 