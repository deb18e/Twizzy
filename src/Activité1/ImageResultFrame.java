package Activité1;

import javax.swing.*;
import java.awt.*;

public class ImageResultFrame extends JFrame {
    private final JLabel panneauLabel;
    private final JLabel imageLabel;

    /**
     * @param originalPath chemin vers l'image d'origine
     * @param panneauPath  chemin vers l'image du panneau détecté
     */
    public ImageResultFrame(String originalPath, String panneauPath) {
        // Configuration de la fenêtre
        setTitle("Résultat - Image");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Bande de gauche : panneau détecté (largeur fixe) ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(250, getHeight()));
        JLabel titrePanneau = new JLabel("Panneau détecté", SwingConstants.CENTER);
        titrePanneau.setFont(new Font("SansSerif", Font.BOLD, 16));
        panneauLabel = new JLabel("", SwingConstants.CENTER);
        leftPanel.add(titrePanneau, BorderLayout.NORTH);
        leftPanel.add(panneauLabel, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        // --- Bande centrale : image d'origine (remplit tout le reste) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        JLabel titreOrigine = new JLabel("Image d'origine", SwingConstants.CENTER);
        titreOrigine.setFont(new Font("SansSerif", Font.BOLD, 16));
        imageLabel = new JLabel("", SwingConstants.CENTER);
        centerPanel.add(titreOrigine, BorderLayout.NORTH);
        centerPanel.add(imageLabel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // On met visible avant de dimensionner pour avoir getWidth()/getHeight() valides
        setVisible(true);

        // --- Redimensionnements des images ---

        // 1) Panneau détecté fixe à 200×200 px
        ImageIcon icP = new ImageIcon(panneauPath);
        Image imgP = icP.getImage()
                       .getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        panneauLabel.setIcon(new ImageIcon(imgP));

        // 2) Image d'origine occupe la zone centrale
        //    largeur dispo = largeur totale - largeur leftPanel - marges
        int availableW = getContentPane().getWidth() - leftPanel.getPreferredSize().width - 20;
        int availableH = getContentPane().getHeight() - titreOrigine.getPreferredSize().height - 20;
        ImageIcon icO = new ImageIcon(originalPath);
        Image imgO = icO.getImage()
                       .getScaledInstance(availableW, availableH, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(imgO));
    }
}
