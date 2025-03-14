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
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


public class template_matching {

	public static void main(String[] args) {
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		        // Charger l'image "circles.jpg"
		        Mat image = LectureImage("Billard_Balls.jpg");

		        // Convertir l'image de l'espace colorimétrique BGR à HSV
		        Mat hsvImage = Mat.zeros(image.size(), image.type());
		        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
		        afficheImage("Cercles_hsv", hsvImage);
		        Mat thresholdImage1 = new Mat();
		        Mat thresholdImage2 = new Mat();
		        Mat thresholdImage = new Mat();
		        Core.inRange(hsvImage, new Scalar(0, 100, 100), new Scalar(10, 255, 255), thresholdImage1);
		        Core.inRange(hsvImage, new Scalar(160, 100, 100), new Scalar(179, 255, 255), thresholdImage2);
		        Core.bitwise_or(thresholdImage1, thresholdImage2, thresholdImage);
		        afficheImage("Cercles_hsv",thresholdImage );
		        
		        //Detection des cercles rouge 
		    
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
		        
		        
		        
		        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
		        float[] radius = new float[1];
		        Point center = new Point();

		        for (int c = 0; c < contours.size(); c++) {
		            MatOfPoint contour = contours.get(c);
		            double contourArea = Imgproc.contourArea(contour);
		            matOfPoint2f.fromList(contour.toList());
		            Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);

		            if ((contourArea / (Math.PI * radius[0] * radius[0])) >= 0.8) {
		                Core.circle(image, center, (int) radius[0], new Scalar(0, 255, 0), 2);
		                Rect rect = Imgproc.boundingRect(contour);
		                
		                Core.rectangle(image, new Point(rect.x, rect.y),
		                    new Point(rect.x + rect.width, rect.y + rect.height),
		                    new Scalar(0, 255, 0), 2);

		                Mat tmp = image.submat(rect.y, rect.y + rect.height, rect.x, rect.x + rect.width);
		                Mat ball = Mat.zeros(tmp.size(), tmp.type());
		                tmp.copyTo(ball);

		                afficheImage("Ball", ball);
		            }
		        }

		        afficheImage("Detection des cercles rouges", image);

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
		}


