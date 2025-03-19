package Activité1;

import org.opencv.features2d.DescriptorMatcher;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Reconnaissance_de_cercles {

	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
      
        // Charger l'image "circles.jpg"
        Mat image = LectureImage("70.jpg");

        // Convertir l'image de l'espace colorimétrique BGR à HSV
        Mat hsvImage = Mat.zeros(image.size(), image.type());
        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
        afficheImage("Cercles_hsv", hsvImage);
        //seuillage 
        Mat thresholdImage1 = new Mat();
        Mat thresholdImage2 = new Mat();
        Mat thresholdImage = new Mat();
        Core.inRange(hsvImage, new Scalar(0, 100, 100), new Scalar(10, 255, 255), thresholdImage1);
        Core.inRange(hsvImage, new Scalar(160, 100, 100), new Scalar(179, 255, 255), thresholdImage2);
        Core.bitwise_or(thresholdImage1, thresholdImage2, thresholdImage);
        afficheImage("Cercles_seuillage_sans_lissage",thresholdImage );
        //avce lissage à l'aide  un flou gaussien
        Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(9, 9), 2, 2);
        afficheImage("Cercles_seuillé_lissé",thresholdImage );
        
        
        //Detection des cercles rouge /des contours avec la Méthode	:	filtre	de	Canny
    
        int thresh = 100;
        Mat canny_output = new Mat(); 
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfInt4 hierarchy = new MatOfInt4();
        Imgproc.Canny(thresholdImage, canny_output, thresh, thresh * 2);
        Imgproc.findContours(canny_output, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat drawing = Mat.zeros(canny_output.size(), CvType.CV_8UC3);
        Random rand = new Random();
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(rand.nextInt(255 - 0 + 1), rand.nextInt(255 - 0 + 1), rand.nextInt(255 - 0 + 1));
            Imgproc.drawContours(drawing, contours, i, color, 1, 8, hierarchy, 0, new Point());
        }
        System.out.println("Nombre de contours détectés : " + contours.size());
        afficheImage("Contours", drawing);

        
       //Reconnaître	les	formes	de	contours que ça soit cercle / triangle/rectangle 
       MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        float[] radius = new float[1];
        Point center = new Point();

        for (int c = 0; c < contours.size(); c++) {
            MatOfPoint contour = contours.get(c);
            double contourArea = Imgproc.contourArea(contour);
            matOfPoint2f.fromList(contour.toList());
            Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);

            // Détection des cercles
            if ((contourArea / (Math.PI * radius[0] * radius[0])) >= 0.8) {
                Core.circle(image, center, (int) radius[0], new Scalar(0, 255, 0), 2);
                Rect rect = Imgproc.boundingRect(contour);
                Core.rectangle(image, new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(255, 0, 0), 2);

                Mat tmp = image.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
                Mat ball = Mat.zeros(tmp.size(), tmp.type());
                tmp.copyTo(ball);

                afficheImage("detection_forme", ball);
             // Enregistrer l'image de la balle dans un fichier
                Highgui.imwrite("ball_detected.png", ball);
            } else {
                // Approximer le contour en un polygone
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                double epsilon = 0.2 * Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
                Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), approxCurve, epsilon, true);

                // Convertir le résultat en MatOfPoint
                MatOfPoint approxContour = new MatOfPoint();
                approxCurve.convertTo(approxContour, CvType.CV_32S);

                // Récupérer le nombre de segments du polygone
                int numSegments = approxContour.toArray().length;

                // Détection des triangles
                if (numSegments == 3) {
                    // Triangle
                    Imgproc.drawContours(image, Arrays.asList(approxContour), -1, new Scalar(0, 255, 255), 2);
                    System.out.println("c'est un triangle ");
                } else if (numSegments == 4) {
                    // Détection des rectangles
                    Point[] points = approxContour.toArray();

                    // Calcul des angles entre les segments
                    double angle1 = angle(points[0], points[1], points[2]);
                    double angle2 = angle(points[1], points[2], points[3]);
                    double angle3 = angle(points[2], points[3], points[0]);
                    double angle4 = angle(points[3], points[0], points[1]);
                    System.out.println("c'est un rectangle ");
                    // Vérification si les angles sont proches de 90 degrés
                    if (Math.abs(angle1 - 90) < 10 && Math.abs(angle2 - 90) < 10 &&
                        Math.abs(angle3 - 90) < 10 && Math.abs(angle4 - 90) < 10) {
                        // Dessiner le rectangle
                        Rect rect = Imgproc.boundingRect(approxContour);
                        Core.rectangle(image, new Point(rect.x, rect.y),
                                new Point(rect.x + rect.width, rect.y + rect.height),
                                new Scalar(0, 255, 0), 2);
                    }
                } else if (numSegments >= 5 && numSegments <= 6) {
                    // Polygones (pentagones, hexagones, etc.)
                    Imgproc.drawContours(image, Arrays.asList(approxContour), -1, new Scalar(255, 0, 0), 2);
                }
            }
        }
        
        // Afficher l'image finale
      afficheImage("Detection des formes", image);
        Mat savedBall = LectureImage("ball_detected.png");
       afficheImage("Balle Enregistrée", savedBall);
        
       Mat inputImage = LectureImage("ball_detected.png"); // Image d'entrée
      Mat referenceImage = LectureImage("ref110.jpg"); // Image de référence 
      //changement en niveau de gris
      
 /*     Mat object = LectureImage("ball_detected.png");
      Mat sroadSign = Highgui.imread("ref30.jpg");      
      Mat sObject = new Mat();
      Imgproc.resize(object, sObject, sroadSign.size());
      Mat grayObject = new Mat(sObject.rows(), sObject.cols(), sObject.type());
      Imgproc.cvtColor(sObject, grayObject, Imgproc.COLOR_BGRA2GRAY);
      Core.normalize(grayObject, grayObject, 0, 255, Core.NORM_MINMAX);

      Mat graySign = new Mat(sroadSign.rows(), sroadSign.cols(), sroadSign.type());
      Imgproc.cvtColor(sroadSign, graySign, Imgproc.COLOR_BGRA2GRAY);
      Core.normalize(graySign, graySign, 0, 255, Core.NORM_MINMAX);
      

   // Extraction des descripteurs et keypoints  
   FeatureDetector orbDetector = FeatureDetector.create(FeatureDetector.ORB);  
   DescriptorExtractor orbExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

   MatOfKeyPoint objectKeypoints = new MatOfKeyPoint();  
   orbDetector.detect(grayObject, objectKeypoints);

   MatOfKeyPoint signKeypoints = new MatOfKeyPoint();  
   orbDetector.detect(graySign, signKeypoints);

   Mat objectDescriptor = new Mat(object.rows(), object.cols(), object.type());
   orbExtractor.compute(grayObject, objectKeypoints, objectDescriptor);

   Mat signDescriptor = new Mat(sroadSign.rows(), sroadSign.cols(), sroadSign.type());
   orbExtractor.compute(graySign, signKeypoints, signDescriptor);
// **Faire le matching**  
   
	
	   

	   

	    // Faire le matching des descripteurs
	    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
	    MatOfDMatch matches = new MatOfDMatch();
	    matcher.match(objectDescriptor, signDescriptor, matches);

	    // Convertir les matches en liste pour filtrer
	    List<DMatch> matchesList = matches.toList();
	    List<DMatch> goodMatches = new ArrayList<>();

	    // Filtrer les correspondances avec une distance inférieure à un seuil
	    double maxDistance = 50.0; // Seuil à ajuster
	    for (DMatch match : matchesList) {
	        if (match.distance < maxDistance) {
	            goodMatches.add(match);
	        }
	    }

	    // Calculer un score de similarité
	    double similarityScore =(double) goodMatches.size() / matchesList.size() * 100;
	    System.out.println("Score de similarité : " + similarityScore + "%");

	    // Déterminer si les images sont similaires
	    double similarityThreshold = 60.0; // Seuil de similarité (à ajuster)
	    if (similarityScore >= similarityThreshold) {
	        System.out.println("Les images sont similaires.");
	    } else {
	        System.out.println("Les images ne sont pas similaires.");
	    }

	    // (Optionnel) Dessiner les correspondances
	    Mat matchedImage = new Mat();
	    Features2d.drawMatches(inputImage, objectKeypoints, referenceImage, signKeypoints,
	            new MatOfDMatch(goodMatches.toArray(new DMatch[0])), matchedImage);

	    // Afficher ou sauvegarder l'image résultante
	    Highgui.imwrite("matches.jpg", matchedImage);
	    afficheImage("Correspondances", matchedImage);
	
}
   */
  //      if (inputImage.empty() || referenceImage.empty()) {
    //        System.err.println("Erreur : Une des images n'a pas pu être chargée.");
      //      return;} /*
      
        
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
        
        
		afficheImage("Image_gris_objet",grayInput);
		afficheImage("Image_gris_reference",grayReference);
        
      

        // Détection des points clés et calcul des descripteurs
        FeatureDetector orbDetector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor orbExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        MatOfKeyPoint inputKeypoints = new MatOfKeyPoint();
        Mat inputKeypointsImage = new Mat();
        
        
        
        orbDetector.detect(grayInput, inputKeypoints);
        Features2d.drawKeypoints(inputImage, inputKeypoints, inputKeypointsImage);
        afficheImage("Points clés de l'image d'entrée", inputKeypointsImage);

        MatOfKeyPoint referenceKeypoints = new MatOfKeyPoint();
        orbDetector.detect(grayReference, referenceKeypoints);
        Mat referenceKeypointsImage = new Mat();
        Features2d.drawKeypoints(referenceImage, referenceKeypoints, referenceKeypointsImage);
        afficheImage("Points clés de l'image de référence", referenceKeypointsImage);

        Mat inputDescriptor = new Mat();
        orbExtractor.compute(grayInput, inputKeypoints, inputDescriptor);

        Mat referenceDescriptor = new Mat();
        orbExtractor.compute(grayReference, referenceKeypoints, referenceDescriptor);
        System.out.println("Taille du descripteur de l'image d'entrée : " + inputDescriptor.size());
        System.out.println("Taille du descripteur de l'image de référence : " + referenceDescriptor.size());

        // Correspondance des descripteurs
        MatOfDMatch matches = new MatOfDMatch();
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        matcher.match(inputDescriptor, referenceDescriptor, matches);
        System.out.println("Matrice"+matches.dump());
        System.out.println("taille_Matrice"+matches.size());

        // Afficher les distances des correspondances
        for (DMatch match : matches.toArray()) {
            System.out.println("Distance de correspondance : " + match.distance);
        }

        // Calculer un score de correspondance
        double matchScore = calculateMatchScore(matches);
        System.out.println("Score de correspondance : " + matchScore);

        // Si la correspondance est bonne, retourner le nombre de l'image de référence
        if (matchScore > 50) { // Seuil ajusté
            System.out.println("Correspondance détectée ! Le nombre dans l'image de référence est : 110");
        } else {
            System.out.println("Aucune correspondance détectée.");
        }

        // Afficher les correspondances (optionnel)
        Mat matchedImage = new Mat();
        Features2d.drawMatches(inputImage, inputKeypoints, referenceImage, referenceKeypoints, matches, matchedImage);
        afficheImage("Correspondances", matchedImage);
	}
	
 
	public static Mat LectureImage(String fichier) {
        File file = new File(fichier);
        Mat img = Highgui.imread(file.getAbsolutePath());

        if (img.empty()) {
            System.err.println("Erreur : Image introuvable à " + file.getAbsolutePath());
        }
        return img;
    }
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

	

	

	public static double angle(Point a, Point b, Point c) {
		Point ab = new Point( b.x - a.x, b.y - a.y );
		Point cb = new Point( b.x - c.x, b.y - c.y );
		double dot = (ab.x * cb.x + ab.y * cb.y); // dot product
		double cross = (ab.x * cb.y - ab.y * cb.x); // cross product
		double alpha = Math.atan2(cross, dot);
		return Math.floor(alpha * 180. / Math.PI + 0.5);
	}
	   // Méthode pour calculer un score de correspondance
	  public static double calculateMatchScore(MatOfDMatch matches) {
	        double totalDistance = 0;
	        int taille = (int) matches.total();

	        
	        for (DMatch match : matches.toArray()) {
	            totalDistance += match.distance;
	        }
	        System.out.println(totalDistance);
	        System.out.println(matches.rows());
	        if(totalDistance>9000 ) {
	        	return ( totalDistance /  150);
	        }
	        else 
	     
	    //   return ( totalDistance / matches.rows() ); // Score entre 0 et 1
	        return ( totalDistance /  100);
	    }

	    public static double compareHistograms(Mat img1, Mat img2) {
	        Mat hist1 = new Mat();
	        Mat hist2 = new Mat();
	        Imgproc.calcHist(Arrays.asList(img1), new MatOfInt(0), new Mat(), hist1, new MatOfInt(256), new MatOfFloat(0, 256));
	        Imgproc.calcHist(Arrays.asList(img2), new MatOfInt(0), new Mat(), hist2, new MatOfInt(256), new MatOfFloat(0, 256));
	        return Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL);
	    }
}
