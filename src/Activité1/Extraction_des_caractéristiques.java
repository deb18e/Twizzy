package Activité1;


import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

public class Extraction_des_caractéristiques {

    public static void main(String[] args) {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Charger les images
        Mat inputImage = LectureImage("p88.jpg"); // Image d'entrée
        Mat referenceImage = LectureImage("p89.jpg"); // Image de référence (balle avec le nombre 3)

        // Redimensionner les images
        Imgproc.resize(inputImage, inputImage, new Size(300, 300));
        Imgproc.resize(referenceImage, referenceImage, new Size(300, 300));

        // Appliquer un flou gaussien
        Imgproc.GaussianBlur(inputImage, inputImage, new Size(5, 5), 0);
        Imgproc.GaussianBlur(referenceImage, referenceImage, new Size(5, 5), 0);

        // Convertir en niveaux de gris et normaliser
        Mat grayInput = new Mat(inputImage.rows(), inputImage.cols(), inputImage.type());
        Imgproc.cvtColor(inputImage, grayInput, Imgproc.COLOR_BGRA2GRAY);
        Core.normalize(grayInput, grayInput, 0, 255, Core.NORM_MINMAX);

        Mat grayReference = new Mat(referenceImage.rows(), referenceImage.cols(), referenceImage.type());
        Imgproc.cvtColor(referenceImage, grayReference, Imgproc.COLOR_BGRA2GRAY);
        Core.normalize(grayReference, grayReference, 0, 255, Core.NORM_MINMAX);
        afficheImage("image1",grayReference);
		afficheImage("image2",grayInput);

        // Détection des points clés et calcul des descripteurs
        FeatureDetector orbDetector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor orbExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        MatOfKeyPoint inputKeypoints = new MatOfKeyPoint();
        orbDetector.detect(grayInput, inputKeypoints);

        MatOfKeyPoint referenceKeypoints = new MatOfKeyPoint();
        orbDetector.detect(grayReference, referenceKeypoints);

        Mat inputDescriptor = new Mat(inputImage.rows(), inputImage.cols(), inputImage.type());
        orbExtractor.compute(grayInput, inputKeypoints, inputDescriptor);

        Mat referenceDescriptor = new Mat(referenceImage.rows(), referenceImage.cols(), referenceImage.type());
        orbExtractor.compute(grayReference, referenceKeypoints, referenceDescriptor);

        // Correspondance des descripteurs
        MatOfDMatch matches = new MatOfDMatch();
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        matcher.match(inputDescriptor, referenceDescriptor, matches);
        System.out.println(matches.dump());

        // Afficher les distances des correspondances
        for (DMatch match : matches.toArray()) {
            System.out.println("Distance de correspondance : " + match.distance);
        }

        // Calculer un score de correspondance
        double matchScore = calculateMatchScore(matches);
        System.out.println("Score de correspondance : " + matchScore);

        // Si la correspondance est bonne, retourner le nombre de l'image de référence
        if (matchScore > 0.1) { // Seuil ajusté
            System.out.println("Correspondance détectée ! Le nombre dans l'image de référence est : 3");
        } else {
            System.out.println("Aucune correspondance détectée.");
        }

        // Afficher les correspondances (optionnel)
        Mat matchedImage = new Mat(inputImage.rows(), inputImage.cols() * 2, inputImage.type());
        Features2d.drawMatches(inputImage, inputKeypoints, referenceImage, referenceKeypoints, matches, matchedImage);
        afficheImage("Correspondances", matchedImage);
    
    }

    // Méthode pour calculer un score de correspondance
    public static double calculateMatchScore(MatOfDMatch matches) {
        double totalDistance = 0;
        for (DMatch match : matches.toArray()) {
            totalDistance += match.distance;
        }
        return 1.0 / (1.0 + totalDistance / matches.rows()); // Score entre 0 et 1
    }

    // Méthode pour charger une image
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
        MatOfByte matOfByte = new MatOfByte();
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
