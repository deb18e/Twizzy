package Activité1;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class HomePage extends JFrame {

    public HomePage() {
        setTitle("Projet Twizzy : Reconnaissance de panneaux de signalisation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        /* --------- Titre --------- */
        JLabel titre = new JLabel(
            "Projet Twizzy : Reconnaissance de panneaux de signalisation",
            SwingConstants.CENTER);
        titre.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(titre, BorderLayout.NORTH);

        /* --------- Illustration centrale --------- */
        JLabel illustration = new JLabel(
            new ImageIcon("interface3.PNG"),
            SwingConstants.CENTER);
        add(illustration, BorderLayout.CENTER);

        /* --------- Boutons Images / Vidéo --------- */
        JPanel boutonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 60, 20));
        JButton btnImage = new JButton("Images");
        JButton btnVideo = new JButton("Vidéo");
        boutonsPanel.add(btnImage);
        boutonsPanel.add(btnVideo);
        add(boutonsPanel, BorderLayout.SOUTH);

        /* === Action : Images === */
        btnImage.addActionListener(e -> {
            // Dossier racine du projet
            File projectDir = new File(System.getProperty("user.dir"));
            JFileChooser chooser = new JFileChooser(projectDir);
            chooser.setDialogTitle("Choisir une image…");
            chooser.setFileFilter(new FileNameExtensionFilter(
                "Images (jpg, png)", "jpg", "jpeg", "png"));

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File imgFile = chooser.getSelectedFile();
                try {
                    String panneauPath =
                        MainTraitementImage.processImage(imgFile.getAbsolutePath());
                    if (panneauPath != null) {
                        new ImageResultFrame(
                            imgFile.getAbsolutePath(),
                            panneauPath
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Aucun panneau détecté sur l'image sélectionnée.",
                            "Information",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                        this,
                        "Erreur : " + ex.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        /* === Action : Vidéo (lance directement) === */
        btnVideo.addActionListener(e -> {
            // Chemin fixe de ta vidéo dans le projet
            String videoPath =
                System.getProperty("user.dir")
                + File.separator
                + "video2.avi";
            new VideoResultFrame(videoPath);
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HomePage::new);
    }
}
