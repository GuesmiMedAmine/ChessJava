package vue;

import controlleur.Controlleur;
import modele.pieces.PieceColor;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static JFrame fenetre;
    private static VueConsole vueConsole;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Création de la fenêtre principale
            fenetre = new JFrame("Échecs MVC");
            fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Configuration du layout principal
            JPanel panelPrincipal = new JPanel(new BorderLayout());
            fenetre.setContentPane(panelPrincipal);
            
            // Création du contrôleur
            Controlleur controleur = new Controlleur();
            
            // Création du menu
            JMenuBar menuBar = new JMenuBar();
            JMenu menuJeu = new JMenu("Jeu");
            
            JMenuItem nouveauJeu = new JMenuItem("Nouvelle partie (2 joueurs)");
            nouveauJeu.addActionListener(e -> {
                controleur.demarrerPartie(false, null);
                mettreAJourVue(controleur);
            });
            
            JMenuItem iaBlanche = new JMenuItem("Nouvelle partie (IA: Blancs)");
            iaBlanche.addActionListener(e -> {
                controleur.demarrerPartie(true, PieceColor.WHITE);
                mettreAJourVue(controleur);
            });
            
            JMenuItem iaNoire = new JMenuItem("Nouvelle partie (IA: Noirs)");
            iaNoire.addActionListener(e -> {
                controleur.demarrerPartie(true, PieceColor.BLACK);
                mettreAJourVue(controleur);
            });
            
            JMenuItem quitter = new JMenuItem("Quitter");
            quitter.addActionListener(e -> System.exit(0));
            
            menuJeu.add(nouveauJeu);
            menuJeu.add(iaBlanche);
            menuJeu.add(iaNoire);
            menuJeu.addSeparator();
            menuJeu.add(quitter);
            
            menuBar.add(menuJeu);
            fenetre.setJMenuBar(menuBar);
            
            // Récupération de la vue et paramétrage
            VueControleur vue = controleur.getVue();
            vue.setControlleur(controleur); // Liaison bidirectionnelle
            
            // Ajout initial de la vue dans le panel principal
            panelPrincipal.add(vue, BorderLayout.CENTER);
            
            // Configuration de la fenêtre
            fenetre.setSize(600, 650); // Un peu plus grand pour le status
            fenetre.setLocationRelativeTo(null);
            fenetre.setVisible(true);
            
            // Lancement de l'interface console dans un thread séparé
            vueConsole = new VueConsole(controleur.getJeu());
            vueConsole.demarrer();
        });
    }
    
    /**
     * Met à jour la vue dans la fenêtre principale après une nouvelle partie
     */
    private static void mettreAJourVue(Controlleur controleur) {
        SwingUtilities.invokeLater(() -> {
            // Mettre à jour la vue console avec le nouveau jeu
            if (vueConsole != null) {
                vueConsole = new VueConsole(controleur.getJeu());
                vueConsole.demarrer();
            }
            
            // Rafraîchir la fenêtre
            Container contentPane = fenetre.getContentPane();
            if (contentPane instanceof JPanel) {
                JPanel panel = (JPanel) contentPane;
                panel.removeAll();
                panel.add(controleur.getVue(), BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }
        });
    }
}
