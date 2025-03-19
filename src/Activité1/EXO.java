// passage	de	BGR	à	HSV



package Activité1;
import utilitaireAgreg.MaBibliothequeTraitementImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.highgui.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


public class EXO {

	public static void main(String[] args) {
		 System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	     Mat m = Highgui.imread("p0.jpg", Highgui.CV_LOAD_IMAGE_COLOR);

			MaBibliothequeTraitementImage.afficheImage("Image originale", m);
			// Charger l'image en couleur
			Mat image = LectureImage("p0.jpg");

			// Créer une matrice de sortie pour stocker l'image convertie en HSV
			Mat hsvImage = Mat.zeros(image.size(), image.type());

			// Convertir l'image de l'espace colorimétrique BGR à HSV
			Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);

			// Afficher l'image HSV
			afficheImage("HSV Image", hsvImage);

			// Séparer les canaux HSV
			Vector<Mat> hsvChannels = new Vector<Mat>();
			Core.split(hsvImage, hsvChannels);

			// Définir les valeurs HSV pour chaque canal (Hue, Saturation, Value)
			double[][] hsvValues = {
			    {1, 255, 255},   // Canal H (Hue)
			    {179, 1, 255},   // Canal S (Saturation)
			    {179, 0, 1}      // Canal V (Value)
			};

			// Parcourir chaque canal HSV
			for (int i = 0; i < 3; i++) {
			    // Afficher le canal HSV actuel
				afficheImage(i + "-HSV Channel", hsvChannels.get(i));

			    // Créer un tableau pour stocker les canaux modifiés
			    Mat[] modifiedChannels = new Mat[3];

			    // Parcourir chaque canal pour appliquer les modifications
			    for (int j = 0; j < 3; j++) {
			        // Créer une matrice remplie de 1
			        Mat onesMatrix = Mat.ones(image.size(), CvType.CV_8UC1);

			        // Créer une matrice pour stocker le résultat de la multiplication
			        Mat resultMatrix = Mat.ones(image.size(), CvType.CV_8UC1);

			        // Multiplier la matrice de 1 par la valeur HSV correspondante
			        Scalar scalarValue = new Scalar(hsvValues[i][j]);
			        Core.multiply(onesMatrix, scalarValue, resultMatrix);

			        // Stocker le résultat dans le tableau des canaux modifiés
			        modifiedChannels[j] = resultMatrix;
			    }

			    // Remplacer le canal actuel par le canal HSV original
			    modifiedChannels[i] = hsvChannels.get(i);

			    // Fusionner les canaux modifiés pour reconstruire l'image HSV
			    Mat mergedImage = Mat.zeros(hsvImage.size(), hsvImage.type());
			    Core.merge(Arrays.asList(modifiedChannels), mergedImage);

			    // Convertir l'image HSV modifiée en BGR pour l'affichage
			    Mat resultImage = Mat.ones(mergedImage.size(), mergedImage.type());
			    Imgproc.cvtColor(mergedImage, resultImage, Imgproc.COLOR_HSV2BGR);

			    // Afficher l'image résultante
			    afficheImage(i + "-Modified HSV to BGR", resultImage);
			}

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


