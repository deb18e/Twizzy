package Activité1;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.highgui.Highgui;
import javax.swing.JFrame;
import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.concurrent.Flow;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;



public class PanneauRecognitionGUI extends JFrame {
	public static void main(String[] args) {
	JFrame frame= new JFrame("ma première fenetre ");
	frame.setSize(400,300);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	JButton bouton= new JButton("cliquer ici");
	frame.add(bouton);
	bouton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Bouton cliué"); }
	});
	JPanel panel = new JPanel();
	panel.add(bouton);
	frame.getContentPane().add(panel);
	frame.setVisible(true);
	
	
	
	}
}
