package Activité1;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class LectureVideo {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Charger la bibliothèque native d'OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // Charger la DLL FFmpeg nécessaire pour la lecture vidéo (adaptée pour OpenCV 2.4.9)
        System.load("C:\\Users\\hp\\Downloads\\opencv\\build\\x64\\vc14\\bin\\opencv_ffmpeg2413_64.dll");

        // Chemin complet vers le fichier vidéo
        String filePath = "C:\\Users\\hp\\Downloads\\video1.avi";
        if (!Paths.get(filePath).toFile().exists()) {
            System.out.println("File " + filePath + " does not exist!");
            return;
        }

        // Ouvrir la vidéo
        VideoCapture camera = new VideoCapture(filePath);
        if (!camera.isOpened()) {
            System.out.println("Error! Camera can't be opened!");
            return;
        }

        // Lire une frame de la vidéo
        Mat frame = new Mat();
        while (true) {
            if (camera.read(frame)) {
                System.out.println("Frame Obtained");
                System.out.println("Captured Frame Width " + frame.width() + " Height " + frame.height());
                // Sauvegarder la frame dans un fichier image
                Highgui.imwrite("camera.jpg", frame);
                System.out.println("OK");
                break;
            }
        }

        // Convertir la Mat en BufferedImage et afficher l'image dans une fenêtre
        BufferedImage bufferedImage = matToBufferedImage(frame);
        showWindow(bufferedImage);

        // Libérer la capture vidéo
        camera.release();
    }

    // Méthode pour convertir une Mat (OpenCV) en BufferedImage (Java Swing)
    private static BufferedImage matToBufferedImage(Mat frame) {
        int type;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else {
            // Par défaut, on utilise une image couleur
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);
        return image;
    }

    // Méthode pour afficher une BufferedImage dans une fenêtre Swing
    private static void showWindow(BufferedImage img) {
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(img.getWidth(), img.getHeight() + 30);
        frame.setTitle("Image captured");
        frame.setVisible(true);
    }
    
    // Méthode alternative pour convertir une Mat en BufferedImage via encodage JPEG
    public static BufferedImage Mat2bufferedImage(Mat image) {
        MatOfByte bytemat = new MatOfByte();
        Highgui.imencode(".jpg", image, bytemat);
        byte[] bytes = bytemat.toArray();
        InputStream in = new ByteArrayInputStream(bytes);
        BufferedImage img = null;
        try {
            img = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }
}
