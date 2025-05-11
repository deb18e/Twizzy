//Seuillage	d’une	image	par	couleur




package Activité1;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class EXO2 {
	

	    public static void main(String[] args) {
	        // Charger la bibliothèque native OpenCV
	        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

	        // Charger l'image "circles.jpg"
	        Mat image = LectureImage("p8.jpg");

	        // Convertir l'image de l'espace colorimétrique BGR à HSV
	        Mat hsvImage = Mat.zeros(image.size(), image.type());
	        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

	        // Appliquer un seuillage pour détecter les cercles rouges
	        Mat thresholdImage1 = new Mat();
	        Mat thresholdImage2 = new Mat();
	        Mat thresholdImage = new Mat();
	        Core.inRange(hsvImage, new Scalar(0, 100, 100), new Scalar(10, 255, 255), thresholdImage1);//Borne inférieure pour Hue (0), Saturation (100), Value (100) Hue (Teinte) : Définit la couleur (0 = rouge, 120 = vert, 240 = bleu).

//Saturation : Définit la pureté de la couleur (0 = gris, 255 = couleur pure).

//Value (Valeur) : Définit la luminosité (0 = noir, 255 = blanc).
	        Core.inRange(hsvImage, new Scalar(160, 100, 100), new Scalar(179, 255, 255), thresholdImage2);//Borne supérieure pour Hue (10), Saturation (255), Value (255).
	        Core.bitwise_or(thresholdImage1, thresholdImage2, thresholdImage);

	        // Appliquer un flou gaussien pour lisser l'image seuillée
	        Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(9, 9), 2, 2);

	        // Afficher l'image résultante
	        afficheImage("Cercles rouges", thresholdImage);
	    
	}
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
