package vue;

import controlleur.Controlleur;
import modele.jeu.Jeu;
import modele.pieces.PieceType;
import modele.plateau.Case;
import modele.plateau.Plateau;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Observable;

/**
 * Vue du plateau d'échecs - gère l'affichage graphique
 */
public class VueControleur extends VueBase implements PropertyChangeListener {
    // Le jeu en cours - on en a besoin pour vérifier les échecs
    private final Jeu jeu;
    private Controlleur controlleur;

    // Pour gérer la sélection et les coups possibles
    private Case caseSelectionnee;
    private List<Case> coupsPossibles;
    
    // Label pour l'état de l'IA
    private JLabel statusLabel;

    public VueControleur(Jeu jeu) {
        this.jeu = jeu;
        Plateau plateau = jeu.getPlateau();
        // On s'abonne aux changements du plateau
        plateau.addObserver(this);
        
        // On utilise un BorderLayout pour ajouter le label de statut en bas
        setLayout(new BorderLayout());
        
        // Création du panel d'échiquier
        JPanel echiquierPanel = new JPanel(new GridLayout(8, 8));
        initUI(echiquierPanel);
        
        // Création du panel de statut
        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel("Prêt");
        statusPanel.add(statusLabel);
        
        // Ajout des composants
        add(echiquierPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        update(plateau, null);
    }
    
    /**
     * Configure le contrôleur et s'abonne aux événements
     */
    public void setControlleur(Controlleur controlleur) {
        this.controlleur = controlleur;
        controlleur.addPropertyChangeListener(this);
    }

    /**
     * Met à jour l'affichage quand le plateau change.
     */
    @Override
    public void update(Observable o, Object arg) {
        Plateau plateau = (Plateau) o;

        // On met d'abord à jour les pièces
        updatePieces(plateau);

        // Puis on gère les surbrillances
        mettreAJourSurbrillances(plateau);

        // Force le rafraîchissement immédiat
        revalidate();
        repaint();
    }
    
    /**
     * Initialise l'interface utilisateur
     */
    protected void initUI(JPanel echiquierPanel) {
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
                echiquierPanel.add(caseLabel);
            }
        }
    }

    /**
     * Gère toutes les surbrillances : sélection, coups possibles, et rois en échec.
     */
    private void mettreAJourSurbrillances(Plateau plateau) {
        // On récupère les positions des rois une seule fois
        Case roiBlanc = plateau.getRoi(modele.pieces.PieceColor.WHITE);
        Case roiNoir = plateau.getRoi(modele.pieces.PieceColor.BLACK);

        // Vérification des échecs
        boolean blancEnEchec = jeu.estEnEchec(modele.pieces.PieceColor.WHITE);
        boolean noirEnEchec = jeu.estEnEchec(modele.pieces.PieceColor.BLACK);

        // Parcours de la grille
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                JLabel caseLabel = grille[x][y];
                Case caseCourante = plateau.getCase(x, y);

                // On commence avec la couleur de base
                caseLabel.setBackground(getCaseColor(x, y));

                // Case sélectionnée
                if (caseSelectionnee != null && caseSelectionnee.equals(caseCourante)) {
                    caseLabel.setBackground(new Color(255, 255, 0, 150));
                } 
                // Coups possibles
                else if (coupsPossibles != null && coupsPossibles.contains(caseCourante)) {
                    caseLabel.setBackground(new Color(144, 238, 144, 150));
                }

                // Roi en échec
                if ((blancEnEchec && caseCourante.equals(roiBlanc)) || 
                    (noirEnEchec && caseCourante.equals(roiNoir))) {
                    caseLabel.setBackground(new Color(255, 0, 0, 150));
                }
            }
        }
    }

    /**
     * Définit la case sélectionnée et les coups possibles.
     */
    public void selectCase(Case selection, List<Case> coups) {
        this.caseSelectionnee = selection;
        this.coupsPossibles = coups;
        mettreAJourSurbrillances(jeu.getPlateau());
        revalidate();
        repaint();
    }

    /**
     * Nettoie la sélection
     */
    public void clearSelection() {
        this.caseSelectionnee = null;
        this.coupsPossibles = null;
        mettreAJourSurbrillances(jeu.getPlateau());
        revalidate();
        repaint();
    }

    /**
     * Affiche une boîte de dialogue pour choisir la pièce de promotion
     */
    public PieceType demanderPromotion() {
        String[] options = {"Dame", "Tour", "Fou", "Cavalier"};
        int choix = JOptionPane.showOptionDialog(
            this,
            "Choisir une pièce pour la promotion :",
            "Promotion du pion",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        return switch (choix) {
            case 0 -> PieceType.DAME;
            case 1 -> PieceType.TOUR;
            case 2 -> PieceType.FOU;
            case 3 -> PieceType.CAVALIER;
            default -> PieceType.DAME;
        };
    }
    
    /**
     * Reçoit les événements de changement de propriétés du contrôleur
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Controlleur.PROP_IA_THINKING)) {
            boolean iaReflexion = (Boolean) evt.getNewValue();
            if (iaReflexion) {
                statusLabel.setText("L'IA réfléchit...");
            } else {
                statusLabel.setText("À votre tour");
            }
        }
    }
}
