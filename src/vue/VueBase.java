package vue;

import modele.plateau.Case;
import modele.plateau.Plateau;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

/**
 * Classe de base pour toutes nos vues d'échiquier.
 * J'ai choisi d'utiliser l'héritage ici pour éviter de dupliquer le code de la grille
 * et de la gestion des pièces. C'est peut-être un peu rigide, mais ça simplifie
 * la maintenance.
 */
public abstract class VueBase extends JPanel implements Observer {
    // La grille de l'échiquier - j'ai choisi JLabel pour sa simplicité
    // même si on pourrait faire plus sophistiqué avec des composants personnalisés
    protected final JLabel[][] grille = new JLabel[8][8];
    
    // État d'activation des interactions
    protected boolean interactionsActivees = true;

    /**
     * Initialise l'interface - c'est ici qu'on crée notre grille de cases.
     * J'ai gardé ça simple avec des JLabels, même si on pourrait faire
     * des composants personnalisés plus élaborés.
     */
    protected void initUI() {
        initUI(this);
    }
    
    /**
     * Initialise l'interface avec un conteneur spécifique
     * @param container Le conteneur où ajouter les cases
     */
    protected void initUI(Container container) {
        // On crée la grille case par case
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                JLabel caseLabel = new JLabel();
                caseLabel.setOpaque(true);
                // Taille fixe pour l'instant - on pourrait rendre ça dynamique
                caseLabel.setPreferredSize(new Dimension(80, 80));
                caseLabel.setHorizontalAlignment(SwingConstants.CENTER);
                caseLabel.setVerticalAlignment(SwingConstants.CENTER);
                // On stocke la position dans les propriétés du label
                // C'est un peu un hack, mais c'est pratique pour les événements
                caseLabel.putClientProperty("pos", new Point(x, y));
                grille[x][y] = caseLabel;
                container.add(caseLabel);
            }
        }
    }

    /**
     * Ajoute un listener de clics sur toutes les cases.
     * J'aurais pu faire ça dans le constructeur, mais c'est plus flexible
     * de le laisser à la classe fille.
     */
    public void addCaseClickListener(MouseListener listener) {
        // On parcourt toute la grille pour ajouter le listener
        // J'aurais pu utiliser un stream, mais c'est moins lisible ici
        for (var ligne : grille) {
            for (var caseLabel : ligne) {
                caseLabel.addMouseListener(listener);
            }
        }
    }
    
    /**
     * Active ou désactive les interactions avec la vue
     * @param enabled true pour activer, false pour désactiver
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.interactionsActivees = enabled;
        // On garde l'appel à super pour maintenir la compatibilité
        super.setEnabled(enabled);
    }
    
    /**
     * Vérifie si les interactions sont activées
     * @return true si les interactions sont activées
     */
    public boolean isInteractionsEnabled() {
        return interactionsActivees;
    }

    /**
     * Retourne la couleur d'une case.
     * J'ai choisi ces couleurs spécifiques car elles sont plus douces
     * que le noir et blanc classique.
     */
    protected Color getCaseColor(int x, int y) {
        // Alternance simple des couleurs
        return (x + y) % 2 == 0
                ? new Color(238, 238, 210)  // Beige clair - plus agréable que le blanc
                : new Color(118, 150, 86);  // Vert olive - plus doux que le noir
    }

    /**
     * Met à jour l'affichage des pièces.
     * Cette méthode est appelée à chaque changement du plateau.
     * J'ai choisi de redessiner toutes les cases à chaque fois plutôt que
     * de faire des mises à jour partielles - c'est plus simple à maintenir.
     */
    protected void updatePieces(Plateau plateau) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                JLabel caseLabel = grille[x][y];
                Case caseCourante = plateau.getCase(x, y);
                
                // On commence par le fond
                caseLabel.setBackground(getCaseColor(x, y));
                
                // Puis on gère la pièce
                if (caseCourante.getPiece() != null) {
                    // Chargement et redimensionnement de l'image
                    // J'ai choisi 70x70 pour laisser une petite marge
                    ImageIcon icone = new ImageIcon(
                            getClass().getResource(caseCourante.getPiece().getImagePath())
                    );
                    Image image = icone.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                    caseLabel.setIcon(new ImageIcon(image));
                } else {
                    // Pas de pièce = pas d'icône
                    caseLabel.setIcon(null);
                }
            }
        }
    }
}