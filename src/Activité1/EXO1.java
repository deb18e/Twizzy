

//afficher	les	canaux	couleur	d’unel’image






package Activité1;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;  // Correct pour OpenCV 2.4.9
import org.opencv.core.Core;


public class EXO1 {

    public static void main(String[] args) {
        // Charger la bibliothèque OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Lire l'image
        Mat image = LectureImage("bgr.png");
      
        // Vérifier si l'image a été correctement chargée
        if (image.empty()) {
            System.out.println("Erreur : Impossible de charger l'image.");
            return;
        }

        // Afficher l'image originale
        afficheImage("Image originale", image);

        // Séparer les canaux (bleu, vert, rouge)
        List<Mat> channels = new ArrayList<>();
        Core.split(image, channels);
        for(int i= 0;i<channels.size();i++) {
        	afficheImage(Integer.toString(i),channels.get(i));
        }

        // Noms des canaux
       String[] canalNames = {"Bleu", "Vert", "Rouge"};

        // Afficher chaque canal
        for (int i = 0; i < channels.size(); i++) {
            // Créer une image noire avec 3 canaux (RGB)
            Mat colorChannel = new Mat(image.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));

            // Liste pour stocker les nouveaux canaux
            List<Mat> newChannels = new ArrayList<>();
            newChannels.add(Mat.zeros(image.size(), CvType.CV_8UC1)); // Bleu
            newChannels.add(Mat.zeros(image.size(), CvType.CV_8UC1)); // Vert
            newChannels.add(Mat.zeros(image.size(), CvType.CV_8UC1)); // Rouge

            // Ajouter le canal actuel
            newChannels.set(i, channels.get(i));

            // Fusionner les canaux pour reconstruire l'image
            Core.merge(newChannels, colorChannel);

            // Afficher l'image
            afficheImage("Canal " + canalNames[i], colorChannel);
        }

       
    }

    // Méthode pour lire une image
    public static Mat LectureImage(String fichier) {
        File file = new File(fichier);
        Mat img = Highgui.imread(file.getAbsolutePath());

        if (img.empty()) {
            System.err.println("Erreur : Image introuvable à " + file.getAbsolutePath());
        }
        return img;
    }

    // Méthode pour afficher une image
    public static void afficheImage(String title, Mat img) {
        MatOfByte matOfByte = new MatOfByte(); //matOfByte contient  une version compressée de l’image sous forme de tableau de bytes.
        Highgui.imencode(".png", img, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;

        try {
            InputStream in = new ByteArrayInputStream(byteArray);
             bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();
            frame.setTitle(title);
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
