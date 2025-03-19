package Activité1;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.KeyPoint;
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

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import utilitaireAgreg.MaBibliothequeTraitementImage;

public class MaBibliothequeTraitementImageEtendue {
	
	public static Mat LectureImage(String fichier) {
		File f = new File (fichier);
		Mat m = Highgui.imread(f.getAbsolutePath());
		return m;
	}

	
	
	//Contient toutes les méthodes necessaires à la transformation des images


	//Methode qui permet de transformer une matrice intialement au  format BGR au format HSV
	public static Mat transformeBGRversHSV(Mat matriceBGR){
		Mat matriceHSV=new Mat(matriceBGR.height(),matriceBGR.cols(),matriceBGR.type());
		Imgproc.cvtColor(matriceBGR,matriceHSV,Imgproc.COLOR_BGR2HSV);
		return matriceHSV;

	}

	//Methode qui convertit une matrice avec 3 canaux en un vecteur de 3 matrices monocanal (un canal par couleur)
	public static Vector<Mat> splitHSVChannels(Mat input) {
		Vector<Mat> channels = new Vector<Mat>(); 
		Core.split(input, channels);
		return channels;
	}

	//Methode qui permet d'afficher une image sur un panel
	public static void afficheImage(String title, Mat img){
		MatOfByte matOfByte=new MatOfByte();
		Highgui.imencode(".png",img,matOfByte);
		byte[] byteArray=matOfByte.toArray();
		BufferedImage bufImage=null;
		try{
			InputStream in=new ByteArrayInputStream(byteArray);
			bufImage=ImageIO.read(in);
			JFrame frame=new JFrame();
			frame.setTitle(title);
			frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
			frame.pack();
			frame.setVisible(true);

		}
		catch(Exception e){
			e.printStackTrace();
		}


	}

	

	//Methode qui permet d'extraire les contours d'une image donnee
	public static List<MatOfPoint> ExtractContours(Mat input) {
		// Detecter les contours des formes trouvées
		int thresh = 100;
		Mat canny_output = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		MatOfInt4 hierarchy = new MatOfInt4();
		Imgproc.Canny( input, canny_output, thresh, thresh*2);


		/// Find extreme outer contours
		Imgproc.findContours( canny_output, contours, hierarchy,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		Mat drawing = Mat.zeros( canny_output.size(), CvType.CV_8UC3 );
		Random rand = new Random();
		for( int i = 0; i< contours.size(); i++ )
		{
			Scalar color = new Scalar( rand.nextInt(255 - 0 + 1) , rand.nextInt(255 - 0 + 1),rand.nextInt(255 - 0 + 1) );
			Imgproc.drawContours( drawing, contours, i, color, 1, 8, hierarchy, 0, new Point() );
		}
		//afficheImage("Contours",drawing);

		return contours;
	
	}	public static Mat seuillage(Mat input, int seuilRougeOrange, int seuilRougeViolet, int seuilSaturation) {
		Vector<Mat> channels = splitHSVChannels(input);
		Scalar rougeviolet = new Scalar(seuilRougeViolet);
		Scalar rougeorange = new Scalar(seuilRougeOrange);
		Scalar seuilsaturation = new Scalar(seuilSaturation);

		Mat rouges_orange = new Mat();
		Mat rouges_violet = new Mat();
		Mat rouges_sat = new Mat();
		Mat Image_sortierouge = new Mat();
		Mat Image_sortie = new Mat();

		Core.compare(channels.get(0), rougeviolet, rouges_violet, Core.CMP_GT);
		Core.compare(channels.get(0), rougeorange, rouges_orange, Core.CMP_LT);
		Core.compare(channels.get(1), seuilsaturation, rouges_sat, Core.CMP_GT);

		Core.bitwise_or(rouges_violet, rouges_orange, Image_sortierouge);
		Core.bitwise_and(Image_sortierouge, rouges_sat, Image_sortie);

		return Image_sortierouge;

	}

	public static Mat DetectForm(Mat img, MatOfPoint contour) {
		MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		float[] radius = new float[1];
		Point center = new Point();
		Rect rect = Imgproc.boundingRect(contour);
		double contourArea = Imgproc.contourArea(contour);

		matOfPoint2f.fromList(contour.toList());
		// Cherche le plus petit cercle entourant le contour
		Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);
		// System.out.println(contourArea+" "+Math.PI*radius[0]*radius[0]);
		// on dit que c'est un cercle si l'aire occup� par le contour est � sup�rieure �
		// 80% de l'aire occup�e par un cercle parfait
		if ((contourArea / (Math.PI * radius[0] * radius[0])) >= 0.8) {
			// System.out.println("Cercle");
			Core.circle(img, center, (int) radius[0], new Scalar(255, 0, 0), 2);
			Core.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(0, 255, 0), 2);
			Mat tmp = img.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
			Mat sign = Mat.zeros(tmp.size(), tmp.type());
			tmp.copyTo(sign);
			afficheImage("detection_forme", sign);
			return sign;
		} else {

			Imgproc.approxPolyDP(matOfPoint2f, approxCurve, Imgproc.arcLength(matOfPoint2f, true) * 0.02, true);
			long total = approxCurve.total();
			if (total == 3) { // is triangle
				// System.out.println("Triangle");
				Point[] pt = approxCurve.toArray();
				Core.line(img, pt[0], pt[1], new Scalar(255, 0, 0), 2);
				Core.line(img, pt[1], pt[2], new Scalar(255, 0, 0), 2);
				Core.line(img, pt[2], pt[0], new Scalar(255, 0, 0), 2);
				Core.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
						new Scalar(0, 255, 0), 2);
				Mat tmp = img.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
				Mat sign = Mat.zeros(tmp.size(), tmp.type());
				tmp.copyTo(sign);
				return null;
			}
			if (total >= 4 && total <= 6) {
				List<Double> cos = new ArrayList<>();
				Point[] points = approxCurve.toArray();
				for (int j = 2; j < total + 1; j++) {
					cos.add(angle(points[(int) (j % total)], points[j - 2], points[j - 1]));
				}
				Collections.sort(cos);
				Double minCos = cos.get(0);
				Double maxCos = cos.get(cos.size() - 1);
				boolean isRect = total == 4 && minCos >= -0.1 && maxCos <= 0.3;
				boolean isPolygon = (total == 5 && minCos >= -0.34 && maxCos <= -0.27)
						|| (total == 6 && minCos >= -0.55 && maxCos <= -0.45);
				if (isRect) {
					double ratio = Math.abs(1 - (double) rect.width / rect.height);
					// drawText(rect.tl(), ratio <= 0.02 ? "SQU" : "RECT");
					// System.out.println("Rectangle");
					Core.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
							new Scalar(0, 255, 0), 2);
					Mat tmp = img.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
					Mat sign = Mat.zeros(tmp.size(), tmp.type());
					tmp.copyTo(sign);
					return null;
				}
				
			}
		}
		return null;

	}

	public static double angle(Point a, Point b, Point c) {
		Point ab = new Point(b.x - a.x, b.y - a.y);
		Point cb = new Point(b.x - c.x, b.y - c.y);
		double dot = (ab.x * cb.x + ab.y * cb.y); // dot product
		double cross = (ab.x * cb.y - ab.y * cb.x); // cross product
		double alpha = Math.atan2(cross, dot);
		return Math.floor(alpha * 180. / Math.PI + 0.5);
	}
public static Mat DetectFormim(Mat img,MatOfPoint contour) {
		
		
		MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		float[] radius = new float[1];
		Point center = new Point();
		//System.out.println(contour);
		Rect rect = Imgproc.boundingRect(contour);
		double contourArea = Imgproc.contourArea(contour);
		int contourSize = (int)contour.total();
		matOfPoint2f.fromList(contour.toList());
		// Cherche le plus petit cercle entourant le contour
		Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);
		//System.out.println(contourArea+" "+Math.PI*radius[0]*radius[0]);
		//on dit que c'est un cercle si l'aire occupé par le contour est à supérieure à  80% de l'aire occupée par un cercle parfait
		if ((contourArea / (Math.PI*radius[0]*radius[0])) >=0.8 ) {
			//System.out.println("Cercle");
			Core.circle(img, center, (int)radius[0], new Scalar(255, 0, 0), 2);
			Core.rectangle(img, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar (0, 255, 0), 2);
			Mat tmp = img.submat(rect.y,rect.y+rect.height,rect.x,rect.x+rect.width);
			Mat sign = Mat.zeros(tmp.size(),tmp.type());
			tmp.copyTo(sign);
			return sign;
		
		}else {

			Imgproc.approxPolyDP(matOfPoint2f, approxCurve, Imgproc.arcLength(matOfPoint2f, true) * 0.02, true);
			long total = approxCurve.total();
			if (total == 3  && contourSize>10 ) { // is triangle
				//System.out.println("Triangle");
				Point [] pt = approxCurve.toArray();
				Core.line(img, pt[0], pt[1], new Scalar(255,0,0),2);
				Core.line(img, pt[1], pt[2], new Scalar(255,0,0),2);
				Core.line(img, pt[2], pt[0], new Scalar(255,0,0),2);
				Core.rectangle(img, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar (0, 255, 0), 3);
				Mat tmp = img.submat(rect.y,rect.y+rect.height,rect.x,rect.x+rect.width);
				Mat sign = Mat.zeros(tmp.size(),tmp.type());
				tmp.copyTo(sign);
				return null;
			}
			if (total >= 4 && total <= 6 && contourSize>60) {
				//System.out.println("rectangle**");
				List<Double> cos = new ArrayList<>();
				Point[] points = approxCurve.toArray();
		        if (total==4 ) {
		        	Core.rectangle(img, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar (0, 255, 0), 2);
					Mat tmp = img.submat(rect.y,rect.y+rect.height,rect.x,rect.x+rect.width);
					Mat sign = Mat.zeros(tmp.size(),tmp.type());
					tmp.copyTo(sign);
					return null;
		        	
		        }
				
			}			
		}
		System.out.println("Aucune forme détectée pour ce contour.");

		return null;

	}
	

	

	
	
	public static double Similitude(Mat src,String objectfile) {

			// Conversion du signe de reference en niveaux de gris et normalisation
					Mat sroadSign= Highgui.imread(objectfile);
					Mat sObject=new Mat();
					Mat object = new Mat();
					Imgproc.resize( src, object, sroadSign.size() );
					Imgproc.resize(object, sObject, sroadSign.size());
					//Conversion du panneau extrait de l'image en gris et normalisation et redimensionnement à la taille du panneau de réference
					Mat grayObject = new Mat(sObject.rows(), sObject.cols(), sObject.type());
					Imgproc.cvtColor(sObject, grayObject, Imgproc.COLOR_BGRA2GRAY);
					Core.normalize(grayObject, grayObject, 0, 255, Core.NORM_MINMAX);
					
					Mat graySign = new Mat(sroadSign.rows(),  sroadSign.cols(), sroadSign.type());
					Imgproc.cvtColor(sroadSign,graySign,Imgproc.COLOR_RGB2GRAY);
					Core.normalize(graySign, graySign, 0, 255, Core.NORM_MINMAX);
					//Extraction des descripteurs et keypoints
					FeatureDetector orbDetector= FeatureDetector.create(FeatureDetector.ORB);
					DescriptorExtractor orbExtractor=DescriptorExtractor.create(DescriptorExtractor.ORB);
					
					MatOfKeyPoint objectKeypoints=new MatOfKeyPoint();
					orbDetector.detect(graySign, objectKeypoints);
					
					MatOfKeyPoint signKeypoints=new MatOfKeyPoint();
					orbDetector.detect(graySign, signKeypoints);

					Mat objectDescriptor=new Mat(object.rows(),object.cols(),object.type());
					orbExtractor.compute(grayObject, objectKeypoints,objectDescriptor);
					
					Mat signDescriptor=new Mat(sroadSign.rows(),sroadSign.cols(),sroadSign.type());
					orbExtractor.compute(graySign, signKeypoints,signDescriptor);
					//System.out.println(signDescriptor);
					//la modif
					//faire le matching
					List<MatOfDMatch> matchs = new ArrayList<MatOfDMatch>();
					DescriptorMatcher matcher=DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT 	);
					int k=2;
					Mat matchedImage=new Mat(sroadSign.rows(),sroadSign.cols()*2,sroadSign.type());
					matcher.knnMatch(objectDescriptor, signDescriptor, matchs, k);
				    LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
				    for (Iterator<MatOfDMatch> iterator = matchs.iterator(); iterator.hasNext();) {
				        MatOfDMatch matOfDMatch = (MatOfDMatch) iterator.next();
				        if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 0.85) {
				            good_matches.add(matOfDMatch.toArray()[0]);
				        }
				    }
				    // get keypoint coordinates of good matches to find homography and remove outliers using ransac
				    List<Point> pts1 = new ArrayList<Point>();
				    List<Point> pts2 = new ArrayList<Point>();
				    for(int i = 0; i<good_matches.size(); i++){
				        pts1.add(objectKeypoints.toList().get(good_matches.get(i).queryIdx).pt);
				        pts2.add(signKeypoints.toList().get(good_matches.get(i).trainIdx).pt);
				    }
				    // convertion of data types - there is maybe a more beautiful way
				    Mat outputMask = new Mat();
				    MatOfPoint2f pts1Mat = new MatOfPoint2f();
				    pts1Mat.fromList(pts1);
				    MatOfPoint2f pts2Mat = new MatOfPoint2f();
				    pts2Mat.fromList(pts2);
				    // Find homography - here just used to perform match filtering with RANSAC, but could be used to e.g. stitch images
				    // the smaller the allowed reprojection error (here 15), the more matches are filtered 
				    Mat Homog = Calib3d.findHomography(pts1Mat, pts2Mat, Calib3d.RANSAC,15, outputMask);

				    // outputMask contains zeros and ones indicating which matches are filtered
				    LinkedList<DMatch> better_matches = new LinkedList<DMatch>();
				    for (int i = 0; i < good_matches.size(); i++) {
				        if (outputMask.get(i, 0)[0] != 0.0) {
				            better_matches.add(good_matches.get(i));
				        }
				    }

				    // DRAWING OUTPUT
				    Mat outputImg = new Mat();
				    // this will draw all matches, works fine
				    MatOfDMatch better_matches_mat = new MatOfDMatch();
				    better_matches_mat.fromList(better_matches);
				    Features2d.drawMatches(object, objectKeypoints, sroadSign, signKeypoints, better_matches_mat, outputImg);
				    //MaBibliothequeTraitementImage.afficheImage("similitude", outputImg);
				    //System.out.println(better_matches_mat.size(),better_matches_mat.pt.x);
					return better_matches_mat.size().height;
						
		}
	
	public static void main(String[] args) {
        try {
        	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			//System.loadLibrary("opencv_java249");
			Mat m=Highgui.imread("p0.jpg",Highgui.CV_LOAD_IMAGE_COLOR);
			MaBibliothequeTraitementImageEtendue.afficheImage("Image testee", m);
			Mat transformee=MaBibliothequeTraitementImageEtendue.transformeBGRversHSV(m);
			MaBibliothequeTraitementImageEtendue.afficheImage("ImageHSV", transformee); 
			//la methode seuillage est ici extraite de l'archivage jar du meme nom 
			Mat saturee=MaBibliothequeTraitementImage.seuillage(transformee, 6, 170, 110);
			MaBibliothequeTraitementImageEtendue.afficheImage("Imagesaturee", saturee);
				Mat saturee1=TraitementImage.seuillage_exemple(transformee, 6);
	//	MaBibliothequeTraitementImageEtendue.afficheImage("Seuillageee 1 ", saturee1);
			MaBibliothequeTraitementImageEtendue.afficheImage("Seuillage", saturee);
			List<MatOfPoint> ListeContours= MaBibliothequeTraitementImageEtendue.ExtractContours(saturee);
			Mat objetrond = null;

			for(MatOfPoint contour :ListeContours) {
				objetrond=MaBibliothequeTraitementImageEtendue.DetectForm(m,contour);
				
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}