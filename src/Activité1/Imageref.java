package Activité1;
import utilitaireAgreg.MaBibliothequeTraitementImage;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.highgui.Highgui;



public class Imageref {
	static int nbr;
	public static  String processImage(String args) throws Exception{
	//Ouverture le l'image et saturation des rouges
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			//System.loadLibrary("opencv_java249");
			Mat m=Highgui.imread(args,Highgui.CV_LOAD_IMAGE_COLOR);
			MaBibliothequeTraitementImageEtendue.afficheImage("Image testee", m);
			Mat transformee=MaBibliothequeTraitementImageEtendue.transformeBGRversHSV(m);
			MaBibliothequeTraitementImageEtendue.afficheImage("ImageHSV", transformee); 
			//la methode seuillage est ici extraite de l'archivage jar du meme nom 
			Mat saturee=MaBibliothequeTraitementImageEtendue.seuillage(transformee, 6, 170, 110);
			MaBibliothequeTraitementImageEtendue.afficheImage("Imagesaturee", saturee);
				Mat saturee1=TraitementImage.seuillage_exemple(transformee, 6);
	//	MaBibliothequeTraitementImageEtendue.afficheImage("Seuillageee 1 ", saturee1);
			MaBibliothequeTraitementImageEtendue.afficheImage("Seuillage", saturee);
		
			Mat objetrond = null;

			//Cr�ation d'une liste des contours � partir de l'image satur�e
			List<MatOfPoint> ListeContours= MaBibliothequeTraitementImageEtendue.ExtractContours(saturee);
			System.out.println("Nombre de contours détectés : " + ListeContours.size());
			int i=0;
			double [] scores=new double [7];
			//Pour tous les contours de la liste
			if (ListeContours.isEmpty()) {
			    System.out.println("Aucun contour détecté !");
			    return null;
			}
			for (MatOfPoint contour: ListeContours  ){
				i++;
				objetrond=MaBibliothequeTraitementImageEtendue.DetectFormim(m,contour);
				System.out.println("le nb de contour est "+ListeContours.size());
				if (objetrond == null) {
				    System.out.println("Le contour " + i + " n'a pas été reconnu comme un cercle.");
				    continue;
				}
				if (objetrond.empty()) {
				    System.out.println("Objet rond détecté mais l'image est vide !");
				    continue;
				}

				MaBibliothequeTraitementImageEtendue.afficheImage("Image_rond", objetrond);
			    
				if (objetrond!=null){
					MaBibliothequeTraitementImage.afficheImage("Objet rond det�ct�", objetrond);
					scores[0]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref30.jpg");
					scores[1]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref50.jpg");
					scores[2]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref70.jpg");
					scores[3]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref90.jpg");
					scores[4]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"ref110.jpg");
			//		scores[5]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"refdouble.jpg");
					//scores[6]=MaBibliothequeTraitementImageEtendue.Similitude(objetrond,"train1.jpg");

					//recherche de l'index du maximum et affichage du panneau detect�
					double scoremax=-1;
					int indexmax=0;
					for(int j=0;j<scores.length;j++){
						if (scores[j]>scoremax){scoremax=scores[j];indexmax=j;}}
					nbr=(int) scoremax;
					if(scoremax<0){System.out.println("Aucun Panneau d�t�ct�");}
					else{switch(indexmax){
					case -1:;break;
					case 0:
						 //MainMenu.textField.setText("C'est le panneau 30! le nombre de keypoints est :"+nbr);
						return "ref30.jpg";
					case 1:
						 ///MainMenu.textField.setText("C'est le panneau 50! le nombre de keypoints est :"+nbr);
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
            String result = processImage("70.jpg");
            System.out.println("Résultat : " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}









