package Activité1;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

/**
 * Fenêtre de lecture vidéo + détection de panneau pour chaque frame.
 */
public class VideoResultFrame extends JFrame implements Runnable {
    private static final int SIDE_BAR_WIDTH = 250;   // largeur fixe pour panneau détecté
    private static final int PANE_HEIGHT    = 600;

    private final String videoPath;
    private final JLabel panneauLabel;
    private final JLabel frameLabel;

    public VideoResultFrame(String videoPath) {
        this.videoPath = videoPath;

        setTitle("Résultat - Vidéo");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, PANE_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Bande de gauche : panneau détecté ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(SIDE_BAR_WIDTH, PANE_HEIGHT));
        JLabel titrePaneau = new JLabel("Panneau détecté", SwingConstants.CENTER);
        titrePaneau.setFont(new Font("SansSerif", Font.BOLD, 16));
        panneauLabel = new JLabel("", SwingConstants.CENTER);
        leftPanel.add(titrePaneau, BorderLayout.NORTH);
        leftPanel.add(panneauLabel, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        // --- Bande centrale : vidéo ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        JLabel titreVideo = new JLabel("Vidéo", SwingConstants.CENTER);
        titreVideo.setFont(new Font("SansSerif", Font.BOLD, 16));
        frameLabel = new JLabel("", SwingConstants.CENTER);
        centerPanel.add(titreVideo, BorderLayout.NORTH);
        centerPanel.add(frameLabel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        setVisible(true);

        // Lance la lecture vidéo dans un thread séparé
        new Thread(this).start();
    }

    @Override
    public void run() {
        // 1) Charge la DLL FFmpeg correspondante (adapte le chemin & la version)
        System.load("C:\\Users\\hp\\Downloads\\open_cv249\\opencv\\build\\x64\\vc12\\bin\\opencv_ffmpeg249_64.dll");
        // 2) Charge la lib native Java
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 3) Ouvre la capture
        VideoCapture capture = new VideoCapture(videoPath);
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(
                this,
                "Impossible d'ouvrir la vidéo :\n" + videoPath,
                "Erreur", JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        Mat frame = new Mat();
        String tempImage = "frame_temp.jpg";

        while (capture.read(frame)) {
            // 4) Affiche la frame dans le JLabel
            BufferedImage buff = matToBufferedImage(frame);
            SwingUtilities.invokeLater(() -> frameLabel.setIcon(new ImageIcon(buff)));

            // 5) Sauvegarde la frame, détecte le panneau et met à jour l'UI
            Highgui.imwrite(tempImage, frame);
            try {
                String panneauPath = MainTraitementImage.processImage(tempImage);
                if (panneauPath != null) {
                    ImageIcon ic = new ImageIcon(panneauPath);
                    Image img = ic.getImage()
                                  .getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                    SwingUtilities.invokeLater(() -> panneauLabel.setIcon(new ImageIcon(img)));
                } else {
                    SwingUtilities.invokeLater(() -> panneauLabel.setIcon(null));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 6) Pause pour ~30 fps
            try {
                Thread.sleep(33);
            } catch (InterruptedException ignored) { }
        }

        capture.release();
    }

    /**
     * Convertit un Mat OpenCV en BufferedImage pour affichage Swing.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage img = new BufferedImage(mat.width(), mat.height(), type);
        byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);
        return img;
    }

    // Test rapide
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VideoResultFrame("video2.avi"));
    }
}
