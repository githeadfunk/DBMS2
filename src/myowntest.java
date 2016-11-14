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
	}
	public static void insert(MainMemory mem, SchemaManager s,ParseTree t){
		//check relation exist
		if(s.relationExists(t.children.get(0).symbol)){
	    	Relation r = s.getRelation(t.children.get(0).symbol);
	    	System.out.println("relation exists");
	    	//check filed_types match, to be finished.......
	    	List<ParseTree> names = t.children.get(1).children;
	    	List<ParseTree> values = t.children.get(2).children;
		    ArrayList<String> field_names = r.getSchema().getFieldNames();
		    ArrayList<FieldType> field_types = r.getSchema().getFieldTypes();
		    //create tuple
	    	Tuple tuple = r.createTuple();
		    for(int i = 0; i < t.children.get(1).children.size(); i ++ ){
	    		if(field_types.get(i).equals(FieldType.INT)){
		    		tuple.setField(names.get(i).symbol, Integer.parseInt(values.get(i).symbol));
	    		}
	    		else{
		    		tuple.setField(names.get(i).symbol, values.get(i).symbol.substring(1, values.get(i).symbol.length()-1));
	    		}
	    	}
		    //insert tuple
		    Block block_reference;
		    block_reference=mem.getBlock(0);
		    block_reference.clear(); //clear the block
		    block_reference.appendTuple(tuple); // append the tuple
		    r.setBlock(r.getNumOfBlocks(), 0);
		    
		}
	    else{
	    	System.err.println("relatoin not exists");
	    	//throwexception relation not exist and go back to command input or stop
	    }
	}
	
	public static void drop(SchemaManager s,ParseTree t){
		s.deleteRelation(t.symbol);
	}
	
	public static void select(MainMemory mem, SchemaManager s,ParseTree t){
		
	}

	
	public static void main(String[] args) throws IOException{
		//-----Initialize-----
		MainMemory mem=new MainMemory();
		Disk disk=new Disk();
		SchemaManager schema_manager=new SchemaManager(mem,disk);
		disk.resetDiskIOs();
	    disk.resetDiskTimer();
	    //--------------------
	    
	    
	    //--------------------
	    //String raw_statement = "DROP TABLE course";
		//String raw_statement = "SELECT wyh,atm FROM c, course2 WHERE course.sid = course2.sid AND course.exam > course2.exam;";
	    //String raw_statement = "INSERT INTO course (sid, homework, project, exam, grade) VALUES (12, 0, 100, 100, \"E\'  f\")";
		//String raw_statement = "INSERT INTO course VALUES (\"2 d d\", 0, 100, 100, \"E\'  f\")";
	    String create = "CREATE TABLE course (sid INT, homework INT, project INT, exam INT, grade STR20)";
	    Lexer lex = new Lexer(create);
	    ParseTree cre_tree = lex.gettree();
	    create(schema_manager, cre_tree);
	    String insert = "INSERT INTO course (sid, homework, project, exam, grade) VALUES (12, 0, 100, 100, \"E\'  f\")";
	    Lexer lex1 = new Lexer(insert);
	    ParseTree ins_tree = lex1.gettree();
	    insert(mem, schema_manager, ins_tree);
	    
	    String drop = "DROP TABLE course";
	    Lexer lex2 = new Lexer(drop);
	    ParseTree drop_tree = lex2.gettree();
	    drop(schema_manager, drop_tree);
	    
		String select = "SELECT wyh,atm FROM c, course2";
		Lexer lex3 = new Lexer(select);
	    ParseTree sel_tree = lex3.gettree();
	    select(mem, schema_manager, sel_tree);
	    
	    InputStreamReader r=new InputStreamReader(System.in);  
	    BufferedReader br=new BufferedReader(r);  
	      
	    System.out.println("Enter your name");  
	    String name=br.readLine();  
	    System.out.println("Welcome "+name);  
	    /*
		switch(tree.symbol){
		case "create": create(schema_manager, tree);break;
		case "insert": insert(mem, schema_manager, tree);break;
		case "drop": drop(schema_manager, tree);break;
		}
		*/
	}
}
