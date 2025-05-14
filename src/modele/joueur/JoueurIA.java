package modele.joueur;

import modele.jeu.Coup;
import modele.jeu.Jeu;
import modele.pieces.Piece;
import modele.pieces.PieceColor;
import modele.pieces.PieceType;
import modele.plateau.Case;
import modele.plateau.Plateau;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implémentation d'un joueur contrôlé par une intelligence artificielle simple
 */
public class JoueurIA implements IJoueur {
    private Jeu jeu;
    private final PieceColor couleur;
    private final AtomicBoolean estEnTrain = new AtomicBoolean(false);
    private final Random random = new Random();
    
    // Pondération simple pour l'évaluation des pièces
    private static final Map<PieceType, Integer> VALEUR_PIECES = Map.of(
        PieceType.PION, 1,
        PieceType.CAVALIER, 3,
        PieceType.FOU, 3,
        PieceType.TOUR, 5,
        PieceType.DAME, 9,
        PieceType.ROI, 100
    );
    
    // Pool de threads pour exécuter l'IA sans bloquer l'interface utilisateur
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Crée une nouvelle instance de JoueurIA
     * @param couleur La couleur des pièces du joueur IA
     */
    public JoueurIA(PieceColor couleur) {
        this.couleur = couleur;
    }

    @Override
    public Coup getCoup() {
        // Vérification simple pour éviter les appels concurrents
        if (estEnTrain.getAndSet(true)) {
            return null;
        }
        
        try {
            // Attendre un court délai pour simuler la "réflexion"
            Thread.sleep(500);
            
            // Trouver le meilleur coup selon notre stratégie simple
            return trouverMeilleurCoup();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            estEnTrain.set(false);
        }
    }

    /**
     * Trouve le "meilleur" coup selon une stratégie simple
     * @return Le coup choisi par l'IA
     */
    private Coup trouverMeilleurCoup() {
        Plateau plateau = jeu.getPlateau();
        List<Piece> mesPieces = new ArrayList<>();
        
        // Récupérer toutes les pièces de l'IA
        for (Piece piece : plateau.getPieces()) {
            if (piece.getColor() == couleur) {
                mesPieces.add(piece);
            }
        }
        
        // Mélanger les pièces pour ajouter de la variété
        Collections.shuffle(mesPieces);
        
        // Structure pour stocker les coups possibles avec leur score
        List<CoupEvalue> coupsPossibles = new ArrayList<>();
        
        // Examiner chaque pièce et ses mouvements possibles
        for (Piece piece : mesPieces) {
            Case caseDepart = piece.getCurrentCase();
            List<Case> casesAccessibles = piece.getCasesAccessibles();
            
            for (Case caseArrivee : casesAccessibles) {
                // Évaluer ce coup
                int score = evaluerCoup(caseDepart, caseArrivee);
                coupsPossibles.add(new CoupEvalue(new Coup(caseDepart, caseArrivee), score));
            }
        }
        
        if (coupsPossibles.isEmpty()) {
            // Aucun coup possible, situation d'échec et mat ou de pat
            return null;
        }
        
        // Trier par score décroissant
        coupsPossibles.sort(Comparator.comparingInt(CoupEvalue::getScore).reversed());
        
        // Prendre un des meilleurs coups (avec un peu d'aléatoire parmi les bons coups)
        int limit = Math.min(3, coupsPossibles.size());
        int index = random.nextInt(limit);
        return coupsPossibles.get(index).getCoup();
    }
    
    /**
     * Classe interne pour associer un coup à son score
     */
    private static class CoupEvalue {
        private final Coup coup;
        private final int score;
        
        public CoupEvalue(Coup coup, int score) {
            this.coup = coup;
            this.score = score;
        }
        
        public Coup getCoup() {
            return coup;
        }
        
        public int getScore() {
            return score;
        }
    }
    
    /**
     * Évalue un coup selon des critères simples
     * @param depart La case de départ
     * @param arrivee La case d'arrivée
     * @return Un score pour ce coup
     */
    private int evaluerCoup(Case depart, Case arrivee) {
        int score = 0;
        
        // Bonus pour les captures (basé sur la valeur de la pièce capturée)
        if (arrivee.getPiece() != null) {
            score += 10 * VALEUR_PIECES.getOrDefault(arrivee.getPiece().getType(), 1);
        }
        
        // Bonus pour avancer les pions vers la promotion
        if (depart.getPiece().getType() == PieceType.PION) {
            int direction = (couleur == PieceColor.WHITE) ? 1 : -1;
            if (arrivee.getY() - depart.getY() == direction) {
                score += 1;
            }
            
            // Bonus supplémentaire pour les pions proches de la promotion
            int distancePromotion = (couleur == PieceColor.WHITE) ? 
                                    (7 - arrivee.getY()) : arrivee.getY();
            if (distancePromotion <= 2) {
                score += (3 - distancePromotion) * 2;
            }
        }
        
        // Petit malus pour déplacer le roi (sauf en cas de capture)
        if (depart.getPiece().getType() == PieceType.ROI && arrivee.getPiece() == null) {
            score -= 1;
        }
        
        // Petite composante aléatoire pour éviter la prévisibilité
        score += random.nextInt(2);
        
        return score;
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
        // Exécuter l'IA dans un thread séparé pour ne pas bloquer l'interface
        executorService.submit(() -> {
            try {
                // Petit délai pour que l'interface ait le temps de se mettre à jour
                Thread.sleep(200);
                
                Coup coup = getCoup();
                if (coup != null) {
                    boolean reussi = jeu.jouerCoup(coup.getDepart(), coup.getArrivee());
                    
                    if (!reussi) {
                        return;
                    }
                    
                    // Gérer la promotion si nécessaire
                    if (jeu.isPromotionEnCours()) {
                        // L'IA choisit toujours la dame pour la promotion
                        jeu.promouvoirPion(PieceType.DAME);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean estIA() {
        return true;
    }
} 