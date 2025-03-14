package Activité1;
import javax.swing.*;
import java.awt.*;
public class Interface {

	public static void main(String[] args) {
		 		        // Créer une fenêtre
		        JFrame frame = new JFrame("Ma Première Interface");
		        frame.setSize(400, 300); // Taille de la fenêtre
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Fermer l'application quand la fenêtre est fermée

		        // Créer un bouton
		        JButton bouton = new JButton("Cliquez-moi !");

		        // Ajouter un écouteur d'événement au bouton
		        bouton.addActionListener(e -> {
		            JOptionPane.showMessageDialog(frame, "Vous avez cliqué sur le bouton !");
		        });

		        // Ajouter le bouton à la fenêtre
		        frame.getContentPane().add(bouton, BorderLayout.CENTER);

		        // Afficher la fenêtre
		        frame.setVisible(true);
		    }
		}

	
