package vue;

import modele.plateau.Plateau;

import java.util.Observable;

/**
 * Vue basique de l'échiquier - juste pour afficher le plateau sans interaction.
 * J'ai créé cette classe pour les cas où on a juste besoin d'afficher l'état
 * du jeu sans permettre de jouer (par exemple pour un replay ou une démo).
 */
public class VueEchiquier extends VueBase {

    /**
     * Constructeur - on initialise juste la vue avec le plateau.
     * J'ai choisi de ne pas ajouter de listeners ici puisqu'on veut
     * juste afficher, pas interagir.
     */
    public VueEchiquier(Plateau plateau) {
        // On s'abonne aux changements du plateau
        plateau.addObserver(this);
        
        // Même layout que la vue principale pour la cohérence
        setLayout(new java.awt.GridLayout(8, 8));
        
        // On initialise l'interface et on affiche l'état initial
        initUI();
        update(plateau, null);
    }

    /**
     * Met à jour l'affichage quand le plateau change.
     * C'est une version simplifiée de la mise à jour - on ne gère
     * que l'affichage des pièces, pas de sélection ni de surbrillance.
     */
    @Override
    public void update(Observable o, Object arg) {
        Plateau plateau = (Plateau) o;
        // On met juste à jour les pièces
        updatePieces(plateau);
        // Rafraîchissement de l'affichage
        revalidate();
        repaint();
    }
}
