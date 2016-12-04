import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;


import storageManager.*;

public class myowntest {
	public static void main(String[] args) throws IOException{
		//-----Initialize-----
		MainMemory mem=new MainMemory();
		Disk disk=new Disk();
		SchemaManager schema_manager=new SchemaManager(mem,disk);
		disk.resetDiskIOs();
	    disk.resetDiskTimer();
	    //--------------------
	    
	    File file = new File("H:/Courses/2016_Fall/Database/Project_2/Projects/my/src/test_simple.txt");
	    Scanner inputFile = new Scanner(file);
	    if (!file.exists()){
	    	System.err.println("File doesn't exists!");
	    	System.exit(0);
	    }
	    else{
	    	while (inputFile.hasNext()){
	    		String statement = inputFile.nextLine();
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
	    			continue;
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
	    	
	    }
	    inputFile.close();
	}
}
