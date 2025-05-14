package modele.joueur;

import modele.jeu.Coup;
import modele.jeu.Jeu;
import modele.pieces.PieceColor;

/**
 * Interface pour les joueurs (humain ou IA)
 */
public interface IJoueur {
    /**
     * Obtient le prochain coup à jouer
     * @return Le coup à jouer
     */
    Coup getCoup();
    
    /**
     * Définit le jeu auquel le joueur participe
     * @param jeu Le jeu d'échecs
     */
    void setJeu(Jeu jeu);
    
    /**
     * Obtient la couleur des pièces du joueur
     * @return La couleur du joueur
     */
    PieceColor getCouleur();
    
    /**
     * Indique au joueur que c'est à son tour de jouer
     */
    void notifierTour();
    
    /**
     * Vérifie si le joueur est humain ou IA
     * @return true si le joueur est contrôlé par une IA
     */
    boolean estIA();
} 