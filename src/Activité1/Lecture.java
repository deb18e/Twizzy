package Activité1;
import java.awt.image.BufferedImage;


import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;


public class Lecture {
	static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

	
	static Mat imag = null;

	public static void main(String[] args) {
	
	
				JFrame jframe = new JFrame("Detection de panneaux sur un flux vidéo");
				jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JLabel vidpanel = new JLabel();
				jframe.setContentPane(vidpanel);
				jframe.setSize(720, 480);
				jframe.setVisible(true);

				Mat frame = new Mat();
				VideoCapture camera = new VideoCapture("video1.avi");
				
				Mat PanneauAAnalyser = null;
				double [] scores=new double [6];
				while (camera.read(frame)) {
					//A completer
						//ImageIcon image = new ImageIcon(Function.Mat2bufferedImage(frame));
						ImageIcon image = new ImageIcon(Mat2bufferedImage(frame));
						vidpanel.setIcon(image);
						Mat trans_HSV=MaBibliothequeTraitementImageEtendue.transformeBGRversHSV(frame);
						Mat saturee=MaBibliothequeTraitementImage.seuillage(trans_HSV, 6, 170, 110);
						Mat objetrond = null;
						
					List<MatOfPoint> ListeContours= MaBibliothequeTraitementImageEtendue.ExtractContours(saturee);
						
						int i=0;
						int a =0;  //compteur de null

						//System.out.println(objetrond);
						
						
						
						for (MatOfPoint contour : ListeContours) {
			                objetrond = MaBibliothequeTraitementImageEtendue.DetectForm(frame, contour);

			                if (objetrond != null) {
			                    MaBibliothequeTraitementImage.afficheImage("Objet rond détecté", objetrond);
			                    scores[0] += MaBibliothequeTraitementImageEtendue.Similitude(objetrond, "ref30.jpg");
			                    scores[1] += MaBibliothequeTraitementImageEtendue.Similitude(objetrond, "ref50.jpg");
			                    scores[2] += MaBibliothequeTraitementImageEtendue.Similitude(objetrond, "ref70.jpg");
			                    scores[3] += MaBibliothequeTraitementImageEtendue.Similitude(objetrond, "ref90.jpg");
			                    scores[4] += MaBibliothequeTraitementImageEtendue.Similitude(objetrond, "ref110.jpg");
			                    scores[5] += MaBibliothequeTraitementImageEtendue.Similitude(objetrond, "refdouble.jpg");

			                    double scoremax = -1;
			                    int indexmax = 0;
			                    for (int j = 0; j < scores.length; j++) {
			                        if (scores[j] > scoremax) {
			                            scoremax = scores[j];
			                            indexmax = j;
			                        }
			                    }

			                    if (scoremax < 0) {
			                        System.out.println("Aucun Panneau détecté");
			                    } else {
			                        switch (indexmax) {
			                            case 0: System.out.println("Panneau 30 détecté"); break;
			                            case 1: System.out.println("Panneau 50 détecté"); break;
			                            case 2: System.out.println("Panneau 70 détecté"); break;
			                            case 3: System.out.println("Panneau 90 détecté"); break;
			                            case 4: System.out.println("Panneau 110 détecté"); break;
			                            case 5: System.out.println("Panneau interdiction de dépasser détecté"); break;
			                        }
			                    }
			                }
			                
							if(objetrond==null) {
								a++;
							}
							if(a>30) {
								scores[0]=0;
								scores[1]=0;
								scores[2]=0;
								scores[3]=0;
								scores[4]=0;
								scores[5]=0;
								a=0;	
							}
						}

						
					//ImageIcon image = new ImageIcon(Mat2bufferedImage(frame));
					vidpanel.setIcon(image);
					vidpanel.repaint();
				}
			}

			




			public static BufferedImage Mat2bufferedImage(Mat image) {
				MatOfByte bytemat = new MatOfByte();
				Highgui.imencode(".jpg", image, bytemat);
				byte[] bytes = bytemat.toArray();
				InputStream in = new ByteArrayInputStream(bytes);
				BufferedImage img = null;
				try {
					img = ImageIO.read(in);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return img;
			}



			public static int identifiepanneau(Mat objetrond){
				double [] scores=new double [6];
				 
				
				int indexmax=-1;
				if (objetrond!=null){
					scores[0]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref30.jpg");
					scores[1]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref50.jpg");
					scores[2]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref70.jpg");
					scores[3]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref90.jpg");
					scores[4]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref110.jpg");
					scores[5]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"refdouble.jpg");

					double scoremax=-1;

					for(int j=1;j<scores.length;j++){
						if (scores[j]>scoremax){scoremax=scores[j];indexmax=j;}}	


				}
				return indexmax;
			}

			
			public static BufferedImage Mat2BufferedImage(Mat m) {
				// Method converts a Mat to a Buffered Image
				int type = BufferedImage.TYPE_BYTE_GRAY;
				if (m.channels() > 1) {
					type = BufferedImage.TYPE_3BYTE_BGR;
				}
				int bufferSize = m.channels() * m.cols() * m.rows();
				byte[] b = new byte[bufferSize];
				m.get(0, 0, b); // get all the pixels
				BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
				final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
				System.arraycopy(b, 0, targetPixels, 0, b.length);
				return image;
			}
			


	
}
