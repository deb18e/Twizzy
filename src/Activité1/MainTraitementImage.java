package Activité1;
import java.nio.file.Path;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.highgui.Highgui;

public class MainTraitementImage {
	static Path currentDirPath = Paths.get("");
	public static String currentDir = currentDirPath.toAbsolutePath().toString().replace("\\", "/");
	 static List<Integer> found;

	public static List<Integer> img(Mat m) {

		// Ouverture le l'image et saturation des rouges
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//Mat m = Highgui.imread(currentDir + "/res/images/p10.jpg",Highgui.CV_LOAD_IMAGE_COLOR);
		// Mat m = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_COLOR);
		found = new ArrayList();
		// TraitementImage.afficheImage("Image teste", m);
		Mat transformee = MaBibliothequeTraitementImage.transformeBGRversHSV(m);
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
				scores[0] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref30.jpg");
				scores[1] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref50.jpg");
				scores[2] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref70.jpg");
				scores[3] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref90.jpg");
				scores[4] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref110.jpg");
				scores[5] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/refdouble.jpg");

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
	public static List<Integer> img(String path) {
		
		// Ouverture le l'image et saturation des rouges
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		//Mat m = Highgui.imread(currentDir + "/res/images/p10.jpg",Highgui.CV_LOAD_IMAGE_COLOR);
		Mat m = Highgui.imread(path, Highgui.CV_LOAD_IMAGE_COLOR);
		found = new ArrayList();
		// TraitementImage.afficheImage("Image teste", m);
		Mat transformee = MaBibliothequeTraitementImage.transformeBGRversHSV(m);
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
				scores[0] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref30.jpg");
				scores[1] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref50.jpg");
				scores[2] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref70.jpg");
				scores[3] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref90.jpg");
				scores[4] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/ref110.jpg");
				scores[5] = MaBibliothequeTraitementImage.Similitude(objetrond, currentDir + "/res/images/refdouble.jpg");

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


}
