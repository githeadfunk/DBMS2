import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
public class TextFields implements ActionListener
{
   JTextField givenName;
   JTextField familyName;
   JTextField fullName;
   JButton submitButton = new JButton("Submit");
   JButton button = new JButton("Select File");

   public TextFields()
   {
      JFrame frame = new JFrame("Text Fields");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      // Create fields for I/O
      givenName = new JTextField(10);
      // Add labelled input fields to display
      JPanel inFieldPane = new JPanel();
      inFieldPane.setLayout(new GridLayout(2,2));
      inFieldPane.add(new JLabel("Enter command"));
      inFieldPane.add(givenName);
      givenName.addActionListener(this);
      
      frame.add(inFieldPane,BorderLayout.NORTH);
      // Add submission button
      JPanel submitPane = new JPanel();
      submitPane.setLayout(new FlowLayout());
      submitPane.add(new JLabel("Press Submit to Enter"));
      submitButton.addActionListener(this);
      
      button.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
            	
    	        File selectedFile = fileChooser.getSelectedFile();
    	        Scanner inputFile = null;
    			try {
    				inputFile = new Scanner(selectedFile);
    			} catch (FileNotFoundException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    	        if (!selectedFile.exists()){
    		    	System.err.println("File doesn't exists!");
    		    	System.exit(0);
    		    }
    	        while (inputFile.hasNext()){
    	        	System.out.println(inputFile.nextLine());
    	        }
              System.out.println(selectedFile.getName());
            }
          }
        });
      
      submitPane.add(submitButton);
      submitPane.add(button);
      frame.add(submitPane,BorderLayout.CENTER);
      frame.pack();
      frame.setVisible(true);
      System.out.println("WYH333==".toLowerCase());
   }
   
   public void actionPerformed (ActionEvent e)
   {
      // Display full name if and only if button was pushed
      if (e.getSource() == submitButton)
      {
    	 System.out.println(givenName.getText());
      }
   }

   public static void main(String[] args)
   {
      new TextFields();
   }
} 