package modele.pieces;

import modele.deco.Deco;
import modele.plateau.Case;
import modele.plateau.Plateau;
import java.util.List;

/**
 * Classe de base pour toutes les pièces
 */
public abstract class Piece {
    protected int x, y;
    protected final PieceColor color;
    protected final PieceType type;
    protected final Plateau plateau;
    protected Deco decorateur;
    protected String imagePath;

    public Piece(int x, int y, PieceColor color, Plateau plateau, PieceType type) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.plateau = plateau;
        this.type = type;
        initDecorateur();
        setImagePath();
    }

    /** Instancie le décorateur approprié dans chaque sous-classe */
    protected abstract void initDecorateur();

    /** Chemin de l'image selon couleur/type */
    protected void setImagePath() {
        this.imagePath = "/Images/"
                + (color == PieceColor.WHITE ? "w" : "b")
                + type.getLetter()
                + ".png";
    }

    /** Délégation au décorateur pour les mouvements */
    public List<Case> getCasesAccessibles() {
        return decorateur.getCasesAccessibles();
    }

    // Accesseurs
    public int getX() { return x; }
    public int getY() { return y; }
    public PieceColor getColor() { return color; }
    public PieceType getType() { return type; }
    public String getImagePath() { return imagePath; }
    public Plateau getPlateau() { return plateau; }

    /**
     * Retourne la case sur laquelle se trouve la pièce
     */
    public Case getCurrentCase() {
        return plateau.getCase(x, y);
    }

    /**
     * Définit la nouvelle position de la pièce
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
