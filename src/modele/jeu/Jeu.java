// src/main/java/modele/jeu/Jeu.java
package modele.jeu;

import java.util.ArrayList;
import java.util.List;
import modele.joueur.IJoueur;
import modele.joueur.JoueurHumain;
import modele.plateau.Plateau;
import modele.plateau.Case;
import modele.pieces.*;
import modele.pieces.PieceColor;
import modele.pieces.PieceType;
import java.util.Observable;
/**
 * Logique de la partie : historique, tour de jeu, validation, etc.
 * L'initialisation des pièces se fait désormais dans Plateau.
 */
public class Jeu {
    private final Plateau plateau;
    private final List<Coup> historique;
    private PieceColor joueurActuel;
    private boolean promotionEnCours = false;
    private Case casePromotion;
    private boolean partieTerminee = false;
    
    // Nouveaux attributs pour les joueurs
    private IJoueur joueurBlanc;
    private IJoueur joueurNoir;
    private boolean modeIA = false;

    public Jeu() {
        this.plateau = new Plateau();
        this.historique = new ArrayList<>();
        this.joueurActuel = PieceColor.WHITE;
        
        // Par défaut, deux joueurs humains
        this.joueurBlanc = new JoueurHumain(PieceColor.WHITE);
        this.joueurNoir = new JoueurHumain(PieceColor.BLACK);
        this.joueurBlanc.setJeu(this);
        this.joueurNoir.setJeu(this);
    }

    /**
     * Configure le mode joueur contre IA
     * @param joueurIA Le joueur IA à utiliser
     */
    public void setModeIA(IJoueur joueurIA) {
        if (joueurIA.getCouleur() == PieceColor.WHITE) {
            this.joueurBlanc = joueurIA;
            this.joueurNoir = new JoueurHumain(PieceColor.BLACK);
        } else {
            this.joueurBlanc = new JoueurHumain(PieceColor.WHITE);
            this.joueurNoir = joueurIA;
        }
        this.joueurBlanc.setJeu(this);
        this.joueurNoir.setJeu(this);
        this.modeIA = true;
    }

    /**
     * Désactive le mode IA et remet en place deux joueurs humains
     */
    public void setModeHumain() {
        this.joueurBlanc = new JoueurHumain(PieceColor.WHITE);
        this.joueurNoir = new JoueurHumain(PieceColor.BLACK);
        this.joueurBlanc.setJeu(this);
        this.joueurNoir.setJeu(this);
        this.modeIA = false;
    }
    
    /**
     * Récupère le joueur actuel
     * @return Le joueur dont c'est le tour
     */
    public IJoueur getJoueurCourant() {
        return (joueurActuel == PieceColor.WHITE) ? joueurBlanc : joueurNoir;
    }
    
    /**
     * Vérifie si le joueur actuel est une IA
     * @return true si le joueur actuel est une IA
     */
    public boolean joueurActuelEstIA() {
        return getJoueurCourant().estIA();
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public PieceColor getJoueurActuel() {
        return joueurActuel;
    }

    /**
     * Vérifie si le joueur spécifié est en échec.
     * @param couleur Couleur du joueur à vérifier
     * @return true si le joueur est en échec
     */
    public synchronized boolean estEnEchec(PieceColor couleur) {
        return plateau.estEnEchec(couleur);
    }

    /**
     * Vérifie si la partie est terminée (échec et mat ou pat).
     * @return true si la partie est terminée
     */
    public synchronized boolean estPartieTerminee() {
        return estEchecEtMat(PieceColor.WHITE) || estEchecEtMat(PieceColor.BLACK) 
               || estPat(PieceColor.WHITE) || estPat(PieceColor.BLACK);
    }

    /**
     * Retourne le vainqueur de la partie, ou null si la partie n'est pas terminée ou est nulle.
     * @return La couleur du vainqueur, ou null
     */
    public PieceColor getVainqueur() {
        if (estEchecEtMat(PieceColor.WHITE)) return PieceColor.BLACK;
        if (estEchecEtMat(PieceColor.BLACK)) return PieceColor.WHITE;
        return null;
    }

    /**
     * Méthode pour démarrer une partie avec la boucle de jeu principale.
     * Cette méthode est bloquante et doit être appelée dans un thread séparé.
     */
    public void demarrerPartie() {
        // Réinitialiser l'état
        partieTerminee = false;
        joueurActuel = PieceColor.WHITE;
        
        // Notifier le premier joueur
        getJoueurCourant().notifierTour();
    }
    
    /**
     * Récupère un coup pour le joueur actuel.
     * Cette méthode sera implémentée par les différents types de joueurs (humain, IA, etc.)
     * 
     * @return le coup à jouer
     */
    public Coup getCoupJoueurActuel() {
        return getJoueurCourant().getCoup();
    }
    
    /**
     * Applique un coup au plateau de jeu.
     * 
     * @param coup le coup à appliquer
     * @return true si le coup a été appliqué avec succès
     */
    public synchronized boolean appliquerCoup(Coup coup) {
        Case depart = coup.getDepart();
        Case arrivee = coup.getArrivee();
        
        // Vérifier si le coup est valide
        if (!estCoupValide(depart, arrivee)) {
            return false;
        }

        Piece piece = depart.getPiece();

        // Gestion spéciale du roque
        if (piece instanceof Roi && Math.abs(arrivee.getX() - depart.getX()) == 2) {
            executerRoque((Roi) piece, arrivee);
        } else if (piece instanceof Pion) {
            if (estPriseEnPassant((Pion) piece, arrivee)) {
                executerPriseEnPassant((Pion) piece, arrivee);
            } else {
                // Marquer le pion comme pouvant être pris en passant s'il avance de 2 cases
                if (Math.abs(arrivee.getY() - depart.getY()) == 2) {
                    ((Pion) piece).setPriseEnPassantPossible(true);
                }
                arrivee.setPiece(piece);
                depart.setPiece(null);
                piece.setPosition(arrivee.getX(), arrivee.getY());

                // Vérifier la promotion
                if ((piece.getColor() == PieceColor.WHITE && arrivee.getY() == 7) ||
                    (piece.getColor() == PieceColor.BLACK && arrivee.getY() == 0)) {
                    promotionEnCours = true;
                    casePromotion = arrivee;
                }
            }
        } else {
            arrivee.setPiece(piece);
            depart.setPiece(null);
            piece.setPosition(arrivee.getX(), arrivee.getY());
        }

        // Ajouter le coup à l'historique
        historique.add(coup);

        // Changer le joueur actuel
        PieceColor previousPlayer = joueurActuel;
        joueurActuel = (joueurActuel == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        // Réinitialiser les drapeaux de prise en passant pour le JOUEUR ACTUEL (pas l'adversaire)
        for (Piece p : plateau.getPieces()) {
            if (p.getColor() == joueurActuel && p instanceof Pion) {
                ((Pion) p).setPriseEnPassantPossible(false);
            }
        }

        // Notifier les observateurs
        plateau.notifierObservers();
        return true;
    }

    /**
     * Joue un coup sur le plateau.
     * @param depart Case de départ
     * @param arrivee Case d'arrivée
     * @return true si le coup a été joué avec succès
     */
    public synchronized boolean jouerCoup(Case depart, Case arrivee) {
        Coup coup = new Coup(depart, arrivee);
        boolean resultat = appliquerCoup(coup);
        
        // Si le coup a été joué avec succès et que ce n'est pas une promotion en cours
        if (resultat && !promotionEnCours) {
            // Vérifier si la partie est terminée
            if (estPartieTerminee()) {
                partieTerminee = true;
            } else {
                // Notifier le prochain joueur
                getJoueurCourant().notifierTour();
            }
        }
        
        return resultat;
    }

    private void executerRoque(Roi roi, Case arrivee) {
        int dir = (arrivee.getX() == 6) ? 1 : -1;
        int rookStartX = (dir == 1) ? 7 : 0;
        int rookEndX   = (dir == 1) ? 5 : 3;
        Case rookCase = plateau.getCase(rookStartX, roi.getY());
        Tour rook = (Tour) rookCase.getPiece();

        arrivee.setPiece(roi);
        plateau.getCase(roi.getX(), roi.getY()).setPiece(null);
        roi.setPosition(arrivee.getX(), arrivee.getY());

        plateau.getCase(rookEndX, roi.getY()).setPiece(rook);
        rookCase.setPiece(null);
        rook.setPosition(rookEndX, roi.getY());
    }

    private void executerPriseEnPassant(Pion pion, Case arrivee) {
        int dir = (pion.getColor() == PieceColor.WHITE) ? 1 : -1;
        Case casePionAdverse = plateau.getCase(arrivee.getX(), arrivee.getY() - dir);
        
        // Capturer le pion adverse
        Piece pionAdverse = casePionAdverse.getPiece();
        casePionAdverse.setPiece(null);
        if (pionAdverse != null) {
            plateau.getPieces().remove(pionAdverse);
        }

        // Déplacer le pion attaquant
        Case caseDepart = pion.getCurrentCase(); // Récupérer la case de départ AVANT mise à jour
        arrivee.setPiece(pion);
        pion.setPosition(arrivee.getX(), arrivee.getY());
        caseDepart.setPiece(null); // Effacer l'ancienne position
    }

    public boolean estEchecEtMat(PieceColor couleur) {
        // Si le roi n'est pas en échec, ce n'est pas un échec et mat
        if (!plateau.estEnEchec(couleur)) return false;

        // Algorithme détaillé de détection d'échec et mat

        // 1. Vérifier si le roi peut s'échapper en se déplaçant
        if (roiPeutEchapper(couleur)) return false;

        // 2. Vérifier si les pièces attaquantes peuvent être capturées
        if (piecesAttaquantesPeuventEtreCapturees(couleur)) return false;

        // 3. Vérifier si l'échec peut être bloqué (sauf pour les cavaliers)
        if (echecPeutEtreBloque(couleur)) return false;

        // Si aucune des conditions ci-dessus n'est vraie, c'est un échec et mat
        return true;
    }

    public boolean estPat(PieceColor couleur) {
        if (plateau.estEnEchec(couleur)) return false;
        return !aDesMouvementsValides(couleur);
    }

    /**
     * Vérifie si le roi peut échapper à l'échec en se déplaçant.
     * @param couleur Couleur du joueur à vérifier
     * @return true si le roi peut échapper à l'échec
     */
    private boolean roiPeutEchapper(PieceColor couleur) {
        // Trouver le roi
        Case caseRoi = plateau.getRoi(couleur);
        if (caseRoi == null || caseRoi.getPiece() == null) return false;

        // Vérifier si le roi peut se déplacer vers une case sûre
        for (Case destination : caseRoi.getPiece().getCasesAccessibles()) {
            // Simuler le déplacement
            Piece roi = caseRoi.getPiece();
            Piece pieceCapturee = destination.getPiece();

            // Sauvegarder les coordonnées originales du roi
            int origX = roi.getX();
            int origY = roi.getY();

            // Déplacer le roi vers la nouvelle position
            destination.setPiece(roi);
            caseRoi.setPiece(null);
            roi.setPosition(destination.getX(), destination.getY());

            // Vérifier si le roi est toujours en échec après le déplacement
            boolean enEchec = plateau.estEnEchec(couleur);

            // Annuler la simulation
            roi.setPosition(origX, origY);
            caseRoi.setPiece(roi);
            destination.setPiece(pieceCapturee);

            // Si le roi n'est pas en échec après le déplacement, il peut s'échapper
            if (!enEchec) return true;
        }

        // Le roi ne peut pas s'échapper
        return false;
    }

    /**
     * Vérifie si les pièces attaquant le roi peuvent être capturées.
     * @param couleur Couleur du joueur à vérifier
     * @return true si au moins une pièce attaquante peut être capturée
     */
    private boolean piecesAttaquantesPeuventEtreCapturees(PieceColor couleur) {
        // Trouver le roi
        Case caseRoi = plateau.getRoi(couleur);
        if (caseRoi == null) return false;

        // Trouver les pièces attaquantes
        PieceColor couleurAdversaire = (couleur == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        for (Piece pieceAdversaire : plateau.getPieces()) {
            if (pieceAdversaire.getColor() == couleurAdversaire) {
                // Vérifier si cette pièce attaque le roi
                if (pieceAdversaire.getCasesAccessibles().contains(caseRoi)) {
                    Case caseAttaquant = pieceAdversaire.getCurrentCase();

                    // Vérifier si une pièce amie peut capturer l'attaquant
                    for (Piece pieceAmie : plateau.getPieces()) {
                        if (pieceAmie.getColor() == couleur) {
                            // Vérifier si la pièce amie peut capturer l'attaquant
                            for (Case destination : pieceAmie.getCasesAccessibles()) {
                                if (destination.equals(caseAttaquant)) {
                                    // Simuler la capture
                                    Case caseAmie = pieceAmie.getCurrentCase();

                                    caseAttaquant.setPiece(pieceAmie);
                                    caseAmie.setPiece(null);

                                    // Vérifier si le roi est toujours en échec après la capture
                                    boolean enEchec = plateau.estEnEchec(couleur);

                                    // Annuler la simulation
                                    caseAmie.setPiece(pieceAmie);
                                    caseAttaquant.setPiece(pieceAdversaire);

                                    // Si la capture résout l'échec, retourner vrai
                                    if (!enEchec) return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Vérifie si l'échec peut être bloqué en plaçant une pièce entre le roi et l'attaquant.
     * @param couleur Couleur du joueur à vérifier
     * @return true si l'échec peut être bloqué
     */
    private boolean echecPeutEtreBloque(PieceColor couleur) {
        // Trouver le roi
        Case caseRoi = plateau.getRoi(couleur);
        if (caseRoi == null) return false;

        // Trouver les pièces attaquantes
        PieceColor couleurAdversaire = (couleur == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        for (Piece pieceAdversaire : plateau.getPieces()) {
            if (pieceAdversaire.getColor() == couleurAdversaire) {
                // Vérifier si cette pièce attaque le roi
                if (pieceAdversaire.getCasesAccessibles().contains(caseRoi)) {
                    // Les cavaliers ne peuvent pas être bloqués
                    if (pieceAdversaire.getType() == PieceType.CAVALIER) continue;

                    // Trouver les cases entre le roi et l'attaquant
                    List<Case> casesEntreRoiEtAttaquant = getCasesEntreRoiEtAttaquant(caseRoi, pieceAdversaire.getCurrentCase());

                    // Vérifier si une pièce amie peut bloquer l'attaque
                    for (Piece pieceAmie : plateau.getPieces()) {
                        if (pieceAmie.getColor() == couleur && pieceAmie.getType() != PieceType.ROI) {
                            for (Case caseAccessible : pieceAmie.getCasesAccessibles()) {
                                if (casesEntreRoiEtAttaquant.contains(caseAccessible)) {
                                    // Simuler le blocage
                                    Case caseAmie = pieceAmie.getCurrentCase();

                                    caseAccessible.setPiece(pieceAmie);
                                    caseAmie.setPiece(null);

                                    // Vérifier si le roi est toujours en échec après le blocage
                                    boolean enEchec = plateau.estEnEchec(couleur);

                                    // Annuler la simulation
                                    caseAmie.setPiece(pieceAmie);
                                    caseAccessible.setPiece(null);

                                    // Si le blocage résout l'échec, retourner vrai
                                    if (!enEchec) return true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Trouve les cases entre le roi et la pièce attaquante.
     * @param caseRoi Case du roi
     * @param caseAttaquant Case de la pièce attaquante
     * @return Liste des cases entre le roi et l'attaquant
     */
    private List<Case> getCasesEntreRoiEtAttaquant(Case caseRoi, Case caseAttaquant) {
        List<Case> casesEntre = new ArrayList<>();

        int xRoi = caseRoi.getX();
        int yRoi = caseRoi.getY();
        int xAttaquant = caseAttaquant.getX();
        int yAttaquant = caseAttaquant.getY();

        // Déterminer la direction de l'attaque
        int dx = Integer.compare(xAttaquant - xRoi, 0);
        int dy = Integer.compare(yAttaquant - yRoi, 0);

        // Si l'attaquant n'est pas sur la même ligne, colonne ou diagonale que le roi
        if (dx != 0 && dy != 0 && Math.abs(xAttaquant - xRoi) != Math.abs(yAttaquant - yRoi)) {
            return casesEntre;
        }

        // Parcourir les cases entre le roi et l'attaquant
        int x = xRoi + dx;
        int y = yRoi + dy;

        while (x != xAttaquant || y != yAttaquant) {
            casesEntre.add(plateau.getCase(x, y));
            x += dx;
            y += dy;
        }

        return casesEntre;
    }

    private boolean aDesMouvementsValides(PieceColor couleur) {
        for (Piece p : plateau.getPieces()) {
            if (p.getColor() == couleur) {
                Case origine = p.getCurrentCase();
                for (Case dest : p.getCasesAccessibles()) {
                    Piece backup = dest.getPiece();
                    dest.setPiece(p);
                    origine.setPiece(null);
                    boolean valide = !plateau.estEnEchec(couleur);
                    origine.setPiece(p);
                    dest.setPiece(backup);
                    if (valide) return true;
                }
            }
        }
        return false;
    }

    /**
     * Convertit une case en notation algébrique (ex: "e4").
     * @param c La case à convertir
     * @return La notation algébrique de la case
     */
    private String notationAlgebrique(Case c) {
        char colonne = (char) ('a' + c.getX());
        int ligne = c.getY() + 1;
        return colonne + "" + ligne;
    }

    private boolean estCoupValide(Case depart, Case arrivee) {
        if (depart.getPiece() == null) return false;
        return MoveValidator.isValid(depart.getPiece(), arrivee, plateau);
    }

    private boolean estPriseEnPassant(Pion pion, Case arrivee) {
        return MoveValidator.validerPriseEnPassant(pion, arrivee, plateau);
    }

    public boolean isPromotionEnCours() {
        return promotionEnCours;
    }

    public Case getCasePromotion() {
        return casePromotion;
    }

    public void promouvoirPion(PieceType nouvellePiece) {
        Pion pion = (Pion) casePromotion.getPiece();
        Piece piece = switch (nouvellePiece) {
            case DAME -> new Dame(casePromotion.getX(), casePromotion.getY(), pion.getColor(), plateau);
            case TOUR -> new Tour(casePromotion.getX(), casePromotion.getY(), pion.getColor(), plateau);
            case FOU -> new Fou(casePromotion.getX(), casePromotion.getY(), pion.getColor(), plateau);
            case CAVALIER -> new Cavalier(casePromotion.getX(), casePromotion.getY(), pion.getColor(), plateau);
            default -> throw new IllegalArgumentException("Type invalide");
        };

        casePromotion.setPiece(piece);
        plateau.getPieces().remove(pion);
        plateau.getPieces().add(piece);
        promotionEnCours = false;
        plateau.notifierObservers();
    }
}
