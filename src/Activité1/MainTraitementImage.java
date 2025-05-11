package Activité1;

import java.nio.file.Path;




import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.highgui.Highgui;
import utilitaireAgreg.MaBibliothequeTraitementImage;
public class MainTraitementImage {
	
	 static List<Integer> found;

	
	public static List<Integer> img(String path) {
		
		// Ouverture le l'image et saturation des rouges
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//Mat m = Highgui.imread(currentDir + "/res/images/p10.jpg",Highgui.CV_LOAD_IMAGE_COLOR);
		Mat m = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_COLOR);
		found = new ArrayList();
		// TraitementImage.afficheImage("Image teste", m);
		Mat transformee = TraitementImage.transformeBGRversHSV(m);
		// la methode seuillage est ici extraite de l'archivage jar du meme nom
		Mat saturee = MaBibliothequeTraitementImage.seuillage(transformee, 6, 170, 110);
		TraitementImage.afficheImage("Seuillage ", saturee);
		Mat objetrond = null;

		// Cr�ation d'une liste des contours � partir de l'image satur�e
		List<MatOfPoint> ListeContours = MaBibliothequeTraitementImage.ExtractContours(saturee);
		int i = 0;
		double[] scores = new double[6];
		// Pour tous les contours de la liste
		for (MatOfPoint contour : ListeContours) {
			i++;
			objetrond = MaBibliothequeTraitementImage.DetectForm(m, contour);
			if (objetrond != null) {
				// TraitementImage.afficheImage("Objet rond détecté", objetrond);
				scores[0] = TraitementImage.Similitude(objetrond,  "ref30.jpg");
				scores[1] = TraitementImage.Similitude(objetrond,  "ref50.jpg");
				scores[2] = TraitementImage.Similitude(objetrond,  "ref70.jpg");
				scores[3] = TraitementImage.Similitude(objetrond,  "ref90.jpg");
				scores[4] = TraitementImage.Similitude(objetrond,  "ref110.jpg");
				scores[5] = TraitementImage.Similitude(objetrond,  "refdouble.jpg");

				// recherche de l'index du maximum et affichage du panneau detect�
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
					return null;
				} else {
					found.add(indexmax);
				}
			}

		}

		return found;
	}
	public static  String processImage(String args) throws Exception{
		// Ouverture le l'image et saturation des rouges
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//Mat m = Highgui.imread(currentDir + "/res/images/p10.jpg",Highgui.CV_LOAD_IMAGE_COLOR);
		Mat m = Highgui.imread(args, Highgui.CV_LOAD_IMAGE_COLOR);
		

		found = new ArrayList();
		// TraitementImage.afficheImage("Image teste", m);
		Mat transformee = TraitementImage.transformeBGRversHSV(m);
		// la methode seuillage est ici extraite de l'archivage jar du meme nom
		Mat saturee = MaBibliothequeTraitementImage.seuillage(transformee, 6, 170, 110);
		Mat objetrond = null;

		// Cr�ation d'une liste des contours � partir de l'image satur�e
		List<MatOfPoint> ListeContours = MaBibliothequeTraitementImage.ExtractContours(saturee);
		int i = 0;
		double[] scores = new double[6];
		// Pour tous les contours de la liste
		for (MatOfPoint contour : ListeContours) {
			i++;
			objetrond = MaBibliothequeTraitementImage.DetectForm(m, contour);
			if (objetrond != null) {
				// TraitementImage.afficheImage("Objet rond détecté", objetrond);
				scores[0] = TraitementImage.Similitude(objetrond,  "ref30.jpg");
				scores[1] = TraitementImage.Similitude(objetrond,  "ref50.jpg");
				scores[2] = TraitementImage.Similitude(objetrond,  "ref70.jpg");
				scores[3] = TraitementImage.Similitude(objetrond,  "ref90.jpg");
				scores[4] = TraitementImage.Similitude(objetrond,  "ref110.jpg");
				scores[5] = TraitementImage.Similitude(objetrond,  "refdouble.jpg");
				

				// recherche de l'index du maximum et affichage du panneau detect�
				double scoremax = -1;
				int indexmax = 0;
				for (int j = 0; j < scores.length; j++) {

					if (scores[j] > scoremax) {
						scoremax = scores[j];
						indexmax = j;
					}
				}

				if (scoremax < 0) {
					System.out.println("Aucun Panneau détecté");}
					else{switch(indexmax){
					case -1:;break;
					case 0:
						 //MainMenu.textField.setText("C'est le panneau 30! le nombre de keypoints est :"+nbr);
						return "ref30.jpg";
					case 1:
						 //MainMenu.textField.setText("C'est le panneau 50! le nombre de keypoints est :"+nbr);
						return "ref50.jpg";
					case 2:
						// MainMenu.textField.setText("C'est le panneau 70!le nombre de keypoints est :"+nbr);
						return "ref70.jpg";
					case 3:
						// MainMenu.textField.setText("C'est le panneau 90! le nombre de keypoints est :"+nbr);
						return "ref90.jpg";
					case 4:
						// MainMenu.textField.setText("C'est le panneau 110! le nombre de keypoints est :"+nbr);
						return "ref110.jpg";
					
					case 5:
						// MainMenu.textField.setText("C'est le panneau d'interdiction de passer! le nombre de keypoints est :"+nbr);
						return "refdouble.jpg";
					//case 6:System.out.println("Panneau passage de train");break;
					
						
					}}

				}
			}
			return null;	
	}
	
	public static void main(String[] args) {
        try {
            String result = processImage("p8.jpg");
            System.out.println("Résultat : " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
			}
