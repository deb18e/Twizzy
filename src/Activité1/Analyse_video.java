package Activité1;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.*;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class Analyse_video {
	static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

	public static void main(String[] args) {
		JFrame jframe = new JFrame("Detection de panneaux sur un flux vidéo");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel vidpanel = new JLabel();
        jframe.setContentPane(vidpanel);
        jframe.setSize(720, 480);
        jframe.setVisible(true);

        Mat frame = new Mat();
        VideoCapture camera = new VideoCapture("C:/Users/hp/Downloads/video1.avi");
        if (!camera.isOpened()) {
            System.err.println("Erreur : Impossible d'ouvrir la vidéo. Vérifiez le chemin et le format.");
            return;
        } else {
            System.out.println("Vidéo chargée avec succès.");
        }

        String dernierPanneauDetecte = "";
        while (camera.read(frame)) {
        	  if (frame.empty()) {
        	        System.err.println("Erreur : Frame vide.");
        	        continue;
        	    }
            // Détection des panneaux
            List<Mat> panneauxDetectes = detecterPanneaux(frame);

            // Affichage des panneaux détectés dans des fenêtres indépendantes
            for (Mat panneau : panneauxDetectes) {
                afficherPanneau(panneau);
            }

            // Affichage du nom du dernier panneau détecté dans la console
            if (!panneauxDetectes.isEmpty()) {
                String nomPanneau = identifierPanneau(panneauxDetectes.get(0)); // Identifier le panneau
                if (!nomPanneau.equals(dernierPanneauDetecte)) {
                    System.out.println("Panneau détecté : " + nomPanneau);
                    dernierPanneauDetecte = nomPanneau;
                }
            }

            // Affichage de la frame originale dans la fenêtre principale
            ImageIcon image = new ImageIcon(Mat2bufferedImage(frame));
            vidpanel.setIcon(image);
            vidpanel.repaint();
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
	
	private static List<Mat> detecterPanneaux(Mat frame) {
	    List<Mat> panneaux = new ArrayList<>();
	    Mat hsvImage = Mat.zeros(frame.size(), frame.type());

	    // Conversion de BGR à HSV
	    Imgproc.cvtColor(frame, hsvImage, Imgproc.COLOR_BGR2HSV);

	    // Seuillage pour détecter la couleur rouge
	    Mat thresholdImage1 = new Mat();
	    Mat thresholdImage2 = new Mat();
	    Mat thresholdImage = new Mat();
	    Core.inRange(hsvImage, new Scalar(0, 100, 100), new Scalar(10, 255, 255), thresholdImage1);
	    Core.inRange(hsvImage, new Scalar(160, 100, 100), new Scalar(179, 255, 255), thresholdImage2);
	    Core.bitwise_or(thresholdImage1, thresholdImage2, thresholdImage);

	    // Appliquer un flou gaussien pour lisser l'image
	    Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(9, 9), 2, 2);

	    // Détection des contours avec Canny
	    Mat canny_output = new Mat();
	    List<MatOfPoint> contours = new ArrayList<>();
	    Mat hierarchy = new Mat();
	    Imgproc.Canny(thresholdImage, canny_output, 100, 200);
	    Imgproc.findContours(canny_output, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

	    // Filtrage des contours pour détecter les panneaux
	    for (MatOfPoint contour : contours) {
	        Rect rect = Imgproc.boundingRect(contour);
	        if (rect.width > 50 && rect.height > 50) { // Filtre pour éliminer les petits contours
	            Mat panneau = new Mat(frame, rect); // Extraire la ROI
	            panneaux.add(panneau);
	        }
	    }

	    return panneaux;
	}
	private static void afficherPanneau(Mat panneau) {
	    JFrame frame = new JFrame("Panneau détecté");
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Fermer la fenêtre sans quitter l'application
	    JLabel label = new JLabel(new ImageIcon(Mat2bufferedImage(panneau)));
	    frame.setContentPane(label);
	    frame.pack();
	    frame.setVisible(true);
	}
	private static String identifierPanneau(Mat panneau) {
	    // Convertir l'image en niveaux de gris
	    Mat grayPanneau = new Mat();
	    Imgproc.cvtColor(panneau, grayPanneau, Imgproc.COLOR_BGR2GRAY);

	    // Détection des contours
	    Mat canny_output = new Mat();
	    List<MatOfPoint> contours = new ArrayList<>();
	    Mat hierarchy = new Mat();
	    Imgproc.Canny(grayPanneau, canny_output, 100, 200);
	    Imgproc.findContours(canny_output, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

	    // Reconnaissance des formes
	    for (MatOfPoint contour : contours) {
	        MatOfPoint2f approxCurve = new MatOfPoint2f();
	        double epsilon = 0.02 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
	        Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approxCurve, epsilon, true);

	        // Récupérer le nombre de segments du polygone
	        int numSegments = approxCurve.toArray().length;

	        if (numSegments == 3) {
	            return "Triangle";
	        } else if (numSegments == 4) {
	            // Vérifier si c'est un rectangle
	            Point[] points = approxCurve.toArray();
	            double angle1 = angle(points[0], points[1], points[2]);
	            double angle2 = angle(points[1], points[2], points[3]);
	            double angle3 = angle(points[2], points[3], points[0]);
	            double angle4 = angle(points[3], points[0], points[1]);

	            if (Math.abs(angle1 - 90) < 10 && Math.abs(angle2 - 90) < 10 &&
	                Math.abs(angle3 - 90) < 10 && Math.abs(angle4 - 90) < 10) {
	                return "Rectangle";
	            }
	        } else if (numSegments > 4) {
	            return "Polygone";
	        }
	    }

	    return "Forme inconnue";
	}
	private static double angle(Point a, Point b, Point c) {
	    Point ab = new Point(b.x - a.x, b.y - a.y);
	    Point cb = new Point(b.x - c.x, b.y - c.y);
	    double dot = (ab.x * cb.x + ab.y * cb.y); // Produit scalaire
	    double cross = (ab.x * cb.y - ab.y * cb.x); // Produit vectoriel
	    double alpha = Math.atan2(cross, dot);
	    return Math.floor(alpha * 180. / Math.PI + 0.5);
	}
	private static BufferedImage Mat2bufferedImage(Mat mat) {
	    int type = BufferedImage.TYPE_BYTE_GRAY;
	    if (mat.channels() > 1) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    }
	    BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
	    mat.get(0, 0, ((java.awt.image.DataBufferByte) image.getRaster().getDataBuffer()).getData());
	    return image;
	}

}
