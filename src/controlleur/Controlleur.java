// controlleur/Controlleur.java
package controlleur;

import modele.jeu.Coup;
import modele.jeu.Jeu;
import modele.joueur.IJoueur;
import modele.joueur.JoueurHumain;
import modele.joueur.JoueurIA;
import modele.pieces.PieceColor;
import modele.pieces.PieceType;
import modele.plateau.Case;
import modele.plateau.Plateau;
import vue.VueControleur;

import javax.swing.*;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Observable;
import java.util.Observer;

/**
 * Le contrôleur - orchestre la logique de jeu et gère les interactions utilisateur
 */
public class Controlleur implements Observer {
    private Jeu jeu;
    private VueControleur vue;
    private final PropertyChangeSupport propertyChangeSupport;
    
    // État de l'interface
    private boolean interfaceVerrouillee = false;
    
    // Constantes pour les événements
    public static final String PROP_IA_THINKING = "ia_thinking";
    public static final String PROP_GAME_STATE = "game_state";
    public static final String PROP_GAME_OVER = "game_over";

    /**
     * Constructeur - initialise le jeu et la vue
     */
    public Controlleur() {
        this.jeu = new Jeu();
        this.vue = new VueControleur(jeu);
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        vue.addCaseClickListener(new GestionnaireClics());
        vue.setControlleur(this); // Important : associer le contrôleur à la vue
        
        // Observer le plateau pour détecter les changements après un coup de l'IA
        jeu.getPlateau().addObserver(this);
        
        // Par défaut, démarrer en mode 2 joueurs humains
        demarrerPartie(false, null);
    }

    /**
     * Démarre une nouvelle partie
     * @param avecIA true pour jouer contre l'IA
     * @param couleurIA la couleur de l'IA (peut être null si avecIA est false)
     */
    public void demarrerPartie(boolean avecIA, PieceColor couleurIA) {
        // Arrêter l'observation de l'ancien plateau
        if (jeu != null && jeu.getPlateau() != null) {
            jeu.getPlateau().deleteObserver(this);
        }
        
        // Créer un nouveau jeu avec un plateau frais
        this.jeu = new Jeu();
        
        // Observer le nouveau plateau
        jeu.getPlateau().addObserver(this);
        
        // Recréer la vue avec le nouveau jeu
        JPanel parent = (JPanel) vue.getParent();
        if (parent != null) {
            parent.remove(vue);
        }
        
        this.vue = new VueControleur(jeu);
        vue.setControlleur(this);
        vue.addCaseClickListener(new GestionnaireClics());
        
        if (parent != null) {
            parent.add(vue);
            parent.revalidate();
            parent.repaint();
        }
        
        // Configurer les joueurs
        if (avecIA && couleurIA != null) {
            // Mode joueur contre IA
            JoueurIA joueurIA = new JoueurIA(couleurIA);
            jeu.setModeIA(joueurIA);
        } else {
            // Mode 2 joueurs humains
            jeu.setModeHumain();
        }
        
        // Démarrer la partie
        jeu.demarrerPartie();
        
        // Signaler que la partie n'est pas terminée
        firePropertyChange(PROP_GAME_OVER, true, false);
        
        // Si l'IA commence, verrouiller l'interface
        if (jeu.joueurActuelEstIA()) {
            verrouillerInterface(true);
            firePropertyChange(PROP_IA_THINKING, false, true);
        } else {
            verrouillerInterface(false);
            firePropertyChange(PROP_IA_THINKING, true, false);
        }
    }

    /**
     * Verrouille ou déverrouille l'interface pendant le tour de l'IA
     * @param verrouille true pour verrouiller l'interface
     */
    private void verrouillerInterface(boolean verrouille) {
        interfaceVerrouillee = verrouille;
        // On ne désactive pas toute la vue, juste les interactions
        firePropertyChange(PROP_IA_THINKING, !verrouille, verrouille);
    }
    
    /**
     * Soumet un coup joué par un joueur humain
     * @param depart Case de départ
     * @param arrivee Case d'arrivée
     * @return true si le coup est valide
     */
    public boolean soumettreCoup(Case depart, Case arrivee) {
        // Vérifier qu'on peut jouer ce coup
        if (depart.getPiece() != null && 
            depart.getPiece().getColor() == jeu.getJoueurActuel()) {
            
            // Vérifier que c'est bien au joueur humain de jouer
            if (!jeu.joueurActuelEstIA()) {
                // Jouer directement le coup via le jeu - le jeu va notifier le prochain joueur
                boolean coupReussi = jeu.jouerCoup(depart, arrivee);
                
                if (!coupReussi) {
                    return false;
                }
                
                // Gestion de la promotion si nécessaire
                if (jeu.isPromotionEnCours()) {
                    PieceType choix = vue.demanderPromotion();
                    jeu.promouvoirPion(choix);
                }
                
                // Vérifier si la partie est terminée
                if (jeu.estPartieTerminee()) {
                    // Fin de la partie
                    afficherFinPartie();
                    // Déverrouiller l'interface et notifier que la partie est terminée
                    verrouillerInterface(false);
                    firePropertyChange(PROP_GAME_OVER, false, true);
                    return true;
                } 
                
                // Si c'est maintenant le tour de l'IA et que la partie n'est pas terminée
                if (jeu.joueurActuelEstIA()) {
                    verrouillerInterface(true);
                    firePropertyChange(PROP_IA_THINKING, false, true);
                }
                
                return true;
            }
        }
        return false;
    }

    /**
     * Cette méthode est appelée quand le plateau change (après un coup de l'IA ou du joueur)
     */
    @Override
    public void update(Observable o, Object arg) {
        // Vérifier d'abord si la partie est terminée
        if (jeu.estPartieTerminee()) {
            // Déverrouiller l'interface et notifier la fin de partie
            SwingUtilities.invokeLater(() -> {
                verrouillerInterface(false);
                firePropertyChange(PROP_IA_THINKING, true, false);
                firePropertyChange(PROP_GAME_OVER, false, true);
                afficherFinPartie();
            });
            return;
        }
        
        // Gérer le tour de jeu normal
        if (!jeu.joueurActuelEstIA() && interfaceVerrouillee) {
            SwingUtilities.invokeLater(() -> {
                verrouillerInterface(false);
                firePropertyChange(PROP_IA_THINKING, true, false);
            });
        }
        else if (jeu.joueurActuelEstIA() && !interfaceVerrouillee) {
            SwingUtilities.invokeLater(() -> {
                verrouillerInterface(true);
                firePropertyChange(PROP_IA_THINKING, false, true);
            });
        }
    }

    /**
     * Ajoute un listener pour les changements de propriétés
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Enlève un listener pour les changements de propriétés
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Déclenche un événement de changement de propriété
     */
    private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Getter pour la vue - utilisé par Main pour afficher la fenêtre
     */
    public VueControleur getVue() {
        return vue;
    }
    
    /**
     * Getter pour le jeu - utilisé par la vue console
     */
    public Jeu getJeu() {
        return jeu;
    }

    /**
     * Gère les clics sur les cases
     */
    private class GestionnaireClics extends MouseAdapter {
        private Case caseSelectionnee;

        @Override
        public void mouseClicked(MouseEvent e) {
            // Si l'interface est verrouillée (tour de l'IA), ignorer les clics
            if (interfaceVerrouillee || jeu.estPartieTerminee()) {
                return;
            }
            
            // Récupération de la case cliquée
            var label = (javax.swing.JLabel) e.getSource();
            Point position = (Point) label.getClientProperty("pos");
            Case caseCliquee = jeu.getPlateau().getCase(position.x, position.y);

            if (caseSelectionnee == null) {
                // Premier clic : sélection d'une pièce
                if (caseCliquee.getPiece() != null && 
                    caseCliquee.getPiece().getColor() == jeu.getJoueurActuel()) {
                    caseSelectionnee = caseCliquee;
                    vue.selectCase(caseSelectionnee, caseCliquee.getPiece().getCasesAccessibles());
                }
            } else {
                // Deuxième clic : tentative de jouer le coup
                boolean coupReussi = soumettreCoup(caseSelectionnee, caseCliquee);
                
                // Nettoyage de la sélection
                caseSelectionnee = null;
                vue.clearSelection();
                
                if (!coupReussi) {
                    // Coup invalide
                    JOptionPane.showMessageDialog(vue, 
                        "Coup invalide !", 
                        "Erreur", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }
    
    /**
     * Affiche le message de fin de partie
     */
    public void afficherFinPartie() {
        if (!jeu.estPartieTerminee()) {
            return; // Ne rien faire si la partie n'est pas terminée
        }
        
        PieceColor vainqueur = jeu.getVainqueur();
        String message;

        if (vainqueur != null) {
            String couleurGagnante = (vainqueur == PieceColor.WHITE ? "Blanc" : "Noir");
            message = "ÉCHEC ET MAT ! Les " + couleurGagnante + "s ont gagné la partie !";
        } else {
            message = "PAT ! La partie est nulle.";
        }

        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(vue, 
                message, 
                "Fin de partie", 
                JOptionPane.INFORMATION_MESSAGE)
        );
    }
}
