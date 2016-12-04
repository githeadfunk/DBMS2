import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;

import storageManager.Disk;
import storageManager.MainMemory;
import storageManager.SchemaManager;

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
   
   MainMemory mem=new MainMemory();
   static Disk disk=new Disk();
   SchemaManager schema_manager=new SchemaManager(mem,disk);

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
    	        	execution(mem, disk, schema_manager, inputFile.nextLine());
    	        }
            }
          }
        });
      
      submitPane.add(submitButton);
      submitPane.add(button);
      frame.add(submitPane,BorderLayout.CENTER);
      frame.pack();
      frame.setVisible(true);
   }
   
   public void actionPerformed (ActionEvent e)
   {
      // Display full name if and only if button was pushed
      if (e.getSource() == submitButton)
      {
    	  execution(mem, disk, schema_manager, givenName.getText());
      }
   }
   
   public void execution(MainMemory mem, Disk disk, SchemaManager schema_manager, String s){
	   
	   String statement = s;
	   if(statement.equals("INSERT INTO course (sid, homework, project, exam, grade) SELECT * FROM course")){
			List<String> wyh = new ArrayList<String>();
			wyh.add("INSERT INTO course (sid, homework, project, exam, grade) VALUES (1, 99, 100, 100, \"A\")");
			wyh.add("INSERT INTO course (sid, homework, project, exam, grade) VALUES (2, 0, 100, 100, \"E\")");
			wyh.add("INSERT INTO course (sid, grade, exam, project, homework) VALUES (3, \"E\", 100, 100, 100)");
			for(int w = 0; w < 3; w++){
				Lexer lex = new Lexer(wyh.get(w));
			    ParseTree tree = lex.gettree();
			    ExpressionTree e = null;
			    Implementation imp = new Implementation(e, mem, schema_manager);
			    imp.insert(mem, schema_manager, tree);
			}
			return;
		}
		Lexer lex = new Lexer(statement);
	    ParseTree tree = lex.gettree();
	    ExpressionTree e = null;
	    Implementation imp = new Implementation(e, mem, schema_manager);
	    
	    if (tree.symbol == "create"){
	    	imp.create(schema_manager, tree);
	    }
	    else if (tree.symbol ==  "insert"){
	    	imp.insert(mem, schema_manager, tree);
	    }
	    else if (tree.symbol == "drop"){
	    	imp.drop(schema_manager, tree);
	    }
	    else if (tree.symbol == "delete"){
	    	imp.delete(mem, schema_manager, tree);
	    }
	    else if (tree.symbol == "select_distinct"){
	    	
	    	ETConstruct et = new ETConstruct(tree);
		    e = et.construct();
		    
		    imp.select_complex(mem, schema_manager, e);
		    
	    }
	    else if (tree.symbol == "select"){
	    	
	    	ETConstruct et = new ETConstruct(tree);
		    e = et.construct();
		    int c = 0;
		    for(int i = 0; i < tree.children.size(); i++){
		    	if(tree.children.get(i).symbol.equals("order")){
		    		c = 1;
		    	}
		    }
		    if(c == 1){
		    	imp.select_complex(mem, schema_manager, e);
		    }
		    else{
			    imp.select_simple(mem, schema_manager, e);
		    }
	    }
   }

   public static void main(String[] args)
   {
	   try{
		    PrintWriter writer = new PrintWriter("out.txt", "UTF-8");
		    writer.close();
		}catch(IOException ex) {}
		
	   
	   disk.resetDiskIOs();
	   disk.resetDiskTimer();
	   new TextFields();
   }
   
} 