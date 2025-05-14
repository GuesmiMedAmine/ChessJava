package modele.joueur;

import modele.jeu.Coup;
import modele.jeu.Jeu;
import modele.pieces.PieceColor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Représente un joueur humain qui interagit via l'interface utilisateur
 */
public class JoueurHumain implements IJoueur {
    private Jeu jeu;
    private final PieceColor couleur;
    private CompletableFuture<Coup> futureInput;

    public JoueurHumain(PieceColor couleur) {
        this.couleur = couleur;
        this.futureInput = new CompletableFuture<>();
    }

    @Override
    public Coup getCoup() {
        try {
            // Attend que le joueur humain joue via l'interface
            return futureInput.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setJeu(Jeu jeu) {
        this.jeu = jeu;
    }

    @Override
    public PieceColor getCouleur() {
        return couleur;
    }

    @Override
    public void notifierTour() {
        // Pour un joueur humain, la notification se fait via l'interface graphique
        // Le contrôleur est responsable de l'affichage
        this.futureInput = new CompletableFuture<>();
    }

    /**
     * Cette méthode est appelée par le contrôleur quand le joueur humain a choisi son coup
     */
    public void soumettreCoup(Coup coup) {
        if (!futureInput.isDone()) {
            futureInput.complete(coup);
        }
    }

    @Override
    public boolean estIA() {
        return false;
    }
} 