import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;

import storageManager.*;

public class myowntest {
	public static void create(SchemaManager s,ParseTree t){
		
		ArrayList<String> field_names=new ArrayList<String>();
	    ArrayList<FieldType> field_types=new ArrayList<FieldType>();
	    List<ParseTree> names = t.children.get(1).children;
	    List<ParseTree> types = t.children.get(2).children;
	    for(int i = 0; i < names.size(); i ++){
	    	field_names.add(names.get(i).symbol);
	    	field_types.add(FieldType.valueOf(types.get(i).symbol.toUpperCase()));
	    }
	    
	    Schema schema=new Schema(field_names,field_types);
	    String relation_name = t.children.get(0).symbol;
	    Relation relation_reference=s.createRelation(relation_name,schema);
	    //------print relation-------
	    //System.out.print("After Create table the relation contains: " + "\n");
	   // System.out.print(relation_reference + "\n" + "\n");
	}
	public static void insert(MainMemory mem, SchemaManager s,ParseTree t){
		if(s.relationExists(t.children.get(0).symbol)){
	    	Relation r = s.getRelation(t.children.get(0).symbol);
	    	//System.out.println("relation exists");
	    	//create a tuple
	    	Tuple tuple = r.createTuple();
	    	List<ParseTree> names = t.children.get(1).children;
	    	List<ParseTree> values = t.children.get(2).children;
		    Schema sche = r.getSchema();
		    ArrayList<FieldType> field_type = sche.getFieldTypes();
		    for(int i = 0; i < t.children.get(1).children.size(); i ++ ){
	    		if(field_type.get(i).equals(FieldType.INT)){
		    		tuple.setField(names.get(i).symbol, Integer.parseInt(values.get(i).symbol));
	    		}
	    		else{
		    		tuple.setField(names.get(i).symbol, values.get(i).symbol);
	    		}
	    	}
		    Block block_reference;
		    block_reference=mem.getBlock(0);
		    block_reference.clear(); //clear the block
		    block_reference.appendTuple(tuple); // append the tuple
		    r.setBlock(r.getNumOfBlocks(), 0);
		    
		    //------print relation-------
		    //System.out.print("After insertion the relation contains: " + "\n");
		   // System.out.print(r + "\n" + "\n");
		    
		}
	    else{
	    	System.err.println("relatoin not exists");
	    	//throwexception relation not exist and go back to command input or stop
	    }
	}
	
	public static void drop(SchemaManager s,ParseTree t){
		
	}

	
	public static void main(String[] args) throws IOException{
		//-----Initialize-----
		MainMemory mem=new MainMemory();
		Disk disk=new Disk();
		SchemaManager schema_manager=new SchemaManager(mem,disk);
		disk.resetDiskIOs();
	    disk.resetDiskTimer();
	    //--------------------
	    
	    //String[] statements = new String[4];
	    //statements[0]="CREATE TABLE course (sid INT, homework INT, project INT, exam INT, grade STR20);";
	    //statements[1]="INSERT INTO course (sid, homework, project, exam, grade) VALUES (12, 0, 100, 100, \"E\'  f\");";
	    //statements[2]="INSERT INTO course (sid, homework, project, exam, grade) VALUES (12, 0, 90, 90, \"E\'  f\");";
	    //statements[3]="SELECT exam FROM course;";
	    //--------------------
	    //String raw_statement = "DROP TABLE course";
		//String raw_statement = "SELECT wyh,atm FROM c, course2 WHERE course.sid = course2.sid AND course.exam > course2.exam;";
	    //String raw_statement = "INSERT INTO course (sid, homework, project, exam, grade) VALUES (12, 0, 100, 100, \"E\'  f\")";
		//String raw_statement = "INSERT INTO course VALUES (\"2 d d\", 0, 100, 100, \"E\'  f\")";
	    //String raw_statement = "CREATE TABLE course (sid INT, homework INT, project INT, exam INT, grade STR20)";
	    
	    //System.out.print("Starting, the memory contains: " + "\n");
	    //System.out.print(mem + "\n");

	    //System.out.print("Starting, Current schemas and relations: " + "\n");
	    //System.out.print(schema_manager + "\n");
	    
	    
	    File file = new File("/Users/zy/Desktop/Course/CSCE608_Database/Project2/DBMS2/src/test.txt");
	    Scanner inputFile = new Scanner(file);
	    if (!file.exists()){
	    	System.err.println("File doesn't exists!");
	    	System.exit(0);
	    }
	    else{
	    	while (inputFile.hasNext()){
	    		String statement = inputFile.nextLine();
	    		Lexer lex = new Lexer(statement);
			    ParseTree tree = lex.gettree();
			    if (tree.symbol == "create"){
			    	create(schema_manager, tree);
			    }
			    else if (tree.symbol ==  "insert"){
			    	insert(mem, schema_manager, tree);
			    }
			    else if (tree.symbol == "drop"){
			    	drop(schema_manager, tree);
			    }
			    else if (tree.symbol == "select"){
			    	ETConstruct et = new ETConstruct(tree);
				    ExpressionTree e;
				    e = et.construct();
				    Implementation imp = new Implementation(e, mem, schema_manager);
				    imp.select_cross(mem, schema_manager,e);
			    }
	    	}
	    }
	    inputFile.close();
	    
	    
		
		
	   System.out.print("After, the memory contains: " + "\n");
	    //System.out.print(mem + "\n");

	    //System.out.print("After, Current schemas and relations: " + "\n");
	    //System.out.print(schema_manager + "\n");
		
		
	   
		 //------select test-------
		
		
	}
}
