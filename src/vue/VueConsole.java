package vue;

import modele.jeu.Coup;
import modele.jeu.Jeu;
import modele.pieces.PieceColor;
import modele.pieces.PieceType;
import modele.plateau.Case;
import modele.plateau.Plateau;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

/**
 * Vue console pour le jeu d'échecs - permet de jouer dans la console
 * tout en conservant la vue graphique.
 */
public class VueConsole implements Observer {
    private final Jeu jeu;
    private final Scanner scanner;
    private boolean running = true;
    private boolean afficherMisesAJour = false; // Option pour désactiver l'affichage automatique

    public VueConsole(Jeu jeu) {
        this.jeu = jeu;
        this.scanner = new Scanner(System.in);
        
        // S'abonner aux changements du plateau
        jeu.getPlateau().addObserver(this);
    }

    /**
     * Démarre l'interface console dans un thread séparé
     */
    public void demarrer() {
        // Créer un thread séparé pour la console
        Thread consoleThread = new Thread(this::boucleConsole);
        consoleThread.setDaemon(true); // Pour que le thread se termine quand l'application se ferme
        consoleThread.start();
    }

    /**
     * Boucle principale de l'interface console
     */
    private void boucleConsole() {
        afficherTutoriel();
        afficherPlateau();
        
        while (running && !jeu.estPartieTerminee()) {
            System.out.println("\nTour des " + 
                (jeu.getJoueurActuel() == PieceColor.WHITE ? "Blancs" : "Noirs"));
            System.out.print("Entrez votre coup (ex: e2 e4) ou 'aide' pour l'aide: ");
            
            String input = scanner.nextLine().trim().toLowerCase();
            
            switch (input) {
                case "exit", "quitter" -> {
                    running = false;
                    System.out.println("Au revoir!");
                }
                case "aide", "help" -> afficherTutoriel();
                case "afficher", "show" -> afficherPlateau();
                case "silencieux", "quiet" -> {
                    afficherMisesAJour = false;
                    System.out.println("Mode silencieux activé : les mises à jour du plateau ne seront plus affichées automatiquement.");
                }
                case "verbeux", "verbose" -> {
                    afficherMisesAJour = true;
                    System.out.println("Mode verbeux activé : les mises à jour du plateau seront affichées automatiquement.");
                }
                default -> traiterCoup(input);
            }
        }
        
        if (jeu.estPartieTerminee()) {
            afficherFinPartie();
        }
    }

    /**
     * Traite un coup entré par l'utilisateur
     */
    private void traiterCoup(String input) {
        try {
            String[] parts = input.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Format incorrect. Utilisez: e2 e4");
                return;
            }
            
            String depart = parts[0];
            String arrivee = parts[1];
            
            // Conversion notation algébrique -> coordonnées
            int departX = depart.charAt(0) - 'a';
            int departY = Character.getNumericValue(depart.charAt(1)) - 1;
            int arriveeX = arrivee.charAt(0) - 'a';
            int arriveeY = Character.getNumericValue(arrivee.charAt(1)) - 1;
            
            // Vérification des indices
            if (departX < 0 || departX > 7 || departY < 0 || departY > 7 ||
                arriveeX < 0 || arriveeX > 7 || arriveeY < 0 || arriveeY > 7) {
                System.out.println("Coordonnées invalides. Les valeurs doivent être entre a1 et h8.");
                return;
            }
            
            Case caseDepart = jeu.getPlateau().getCase(departX, departY);
            Case caseArrivee = jeu.getPlateau().getCase(arriveeX, arriveeY);
            
            // Vérifier si la pièce appartient au joueur actuel
            if (caseDepart.getPiece() == null || 
                caseDepart.getPiece().getColor() != jeu.getJoueurActuel()) {
                System.out.println("Vous devez sélectionner une de vos pièces.");
                return;
            }
            
            boolean coupReussi = jeu.jouerCoup(caseDepart, caseArrivee);
            
            if (!coupReussi) {
                System.out.println("Coup invalide !");
            } else if (jeu.isPromotionEnCours()) {
                // Gestion de la promotion
                PieceType choix = demanderPromotion();
                jeu.promouvoirPion(choix);
            }
            
            // La mise à jour de l'affichage se fait via update()
        } catch (Exception e) {
            System.out.println("Erreur: " + e.getMessage());
        }
    }

    /**
     * Demande à l'utilisateur quelle pièce il souhaite pour la promotion
     */
    private PieceType demanderPromotion() {
        System.out.println("Promotion du pion ! Choisissez une pièce:");
        System.out.println("1. Dame");
        System.out.println("2. Tour");
        System.out.println("3. Fou");
        System.out.println("4. Cavalier");
        System.out.print("Votre choix (1-4): ");
        
        int choix;
        try {
            choix = Integer.parseInt(scanner.nextLine().trim());
            if (choix < 1 || choix > 4) {
                System.out.println("Choix invalide, Dame sélectionnée par défaut.");
                return PieceType.DAME;
            }
        } catch (NumberFormatException e) {
            System.out.println("Entrée invalide, Dame sélectionnée par défaut.");
            return PieceType.DAME;
        }
        
        return switch (choix) {
            case 2 -> PieceType.TOUR;
            case 3 -> PieceType.FOU;
            case 4 -> PieceType.CAVALIER;
            default -> PieceType.DAME;
        };
    }

    /**
     * Affiche le tutoriel
     */
    private void afficherTutoriel() {
        System.out.println("\n===== TUTORIEL DU JEU D'ÉCHECS EN CONSOLE =====");
        System.out.println("Pour jouer un coup, entrez la position de départ et d'arrivée comme ceci: e2 e4");
        System.out.println("Les commandes disponibles:");
        System.out.println("  afficher : Affiche l'état actuel du plateau");
        System.out.println("  aide     : Affiche ce tutoriel");
        System.out.println("  silencieux : Désactive l'affichage automatique du plateau après chaque coup");
        System.out.println("  verbeux    : Active l'affichage automatique du plateau après chaque coup");
        System.out.println("  quitter  : Quitte le jeu console (la vue graphique reste active)");
        System.out.println("Notations:");
        System.out.println("  P: Pion blanc     p: Pion noir");
        System.out.println("  R: Tour blanche   r: Tour noire");
        System.out.println("  N: Cavalier blanc n: Cavalier noir");
        System.out.println("  B: Fou blanc      b: Fou noir");
        System.out.println("  Q: Dame blanche   q: Dame noire");
        System.out.println("  K: Roi blanc      k: Roi noir");
        System.out.println("===============================================");
    }

    /**
     * Affiche le plateau d'échecs dans la console
     */
    private void afficherPlateau() {
        Plateau plateau = jeu.getPlateau();
        System.out.println("\n  a b c d e f g h");
        System.out.println(" +-+-+-+-+-+-+-+-+");
        
        for (int y = 7; y >= 0; y--) {
            System.out.print(y+1 + "|");
            for (int x = 0; x < 8; x++) {
                Case c = plateau.getCase(x, y);
                if (c.getPiece() != null) {
                    // Déterminer le type de la pièce
                    char lettre;
                    switch (c.getPiece().getType()) {
                        case ROI -> lettre = 'K';
                        case DAME -> lettre = 'Q';
                        case TOUR -> lettre = 'R';
                        case FOU -> lettre = 'B';
                        case CAVALIER -> lettre = 'N';
                        default -> lettre = 'P'; // Pion
                    }
                    
                    // Ajuster la casse selon la couleur
                    if (c.getPiece().getColor() == PieceColor.BLACK) {
                        lettre = Character.toLowerCase(lettre);
                    }
                    
                    System.out.print(lettre + "|");
                } else {
                    System.out.print(" |");
                }
            }
            System.out.println(y+1);
            System.out.println(" +-+-+-+-+-+-+-+-+");
        }
        System.out.println("  a b c d e f g h");
        
        // Afficher les informations supplémentaires
        if (jeu.estEnEchec(PieceColor.WHITE)) {
            System.out.println("Les blancs sont en ÉCHEC !");
        }
        if (jeu.estEnEchec(PieceColor.BLACK)) {
            System.out.println("Les noirs sont en ÉCHEC !");
        }
    }

    /**
     * Affiche le message de fin de partie
     */
    private void afficherFinPartie() {
        PieceColor vainqueur = jeu.getVainqueur();
        
        if (vainqueur != null) {
            String couleurGagnante = (vainqueur == PieceColor.WHITE ? "Blancs" : "Noirs");
            System.out.println("\nÉCHEC ET MAT ! Les " + couleurGagnante + " ont gagné la partie !");
        } else {
            System.out.println("\nPAT ! La partie est nulle.");
        }
    }

    /**
     * Appelé quand le plateau change
     */
    @Override
    public void update(Observable o, Object arg) {
        // Rafraîchir l'affichage du plateau seulement si le mode verbeux est activé
        if (afficherMisesAJour) {
            System.out.println("\n--- Mise à jour du plateau ---");
            afficherPlateau();
        }
    }
} 