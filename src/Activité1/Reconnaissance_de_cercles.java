package Activité1;
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
        Mat image = LectureImage("p7.jpg");

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

                afficheImage("Ball", ball);
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
        Mat referenceImage = LectureImage("ref50.jpg"); // Image de référence (balle avec le nombre 3)

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
        for (DMatch match : matches.toArray()) {
            totalDistance += match.distance;
        }
        return 1.0 / (1.0 + totalDistance / matches.rows()); // Score entre 0 et 1
    }

}
