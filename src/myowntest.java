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
	
	public static void select(MainMemory mem, SchemaManager s,ExpressionTree t){
		//get tables,conditions and attributes from et
		List<String> attributes = new ArrayList<String>();
		List<String> conditions = new ArrayList<String>();
		List<String> tables = new ArrayList<String>();
		for(int i = 0; i < t.attribute.size(); i++){
			attributes.add(t.attribute.get(i).symbol);
			//System.out.println(attributes.get(i));
		}
		for(int i = 0; i < t.children.get(0).attribute.get(0).children.size(); i++){
			conditions.add(t.children.get(0).attribute.get(0).children.get(i).symbol);
			//System.out.println(conditions.get(i));
		} 
		for(int i = 0; i < t.children.get(0).children.get(0).children.size(); i++){
			tables.add(t.children.get(0).children.get(0).children.get(0).symbol);
			//System.out.println(tables.get(i));
		}
		String head = "";
		for(int i = 0; i < attributes.size(); i++){
			head = head + attributes.get(i) + "		";
		}
		System.out.println(head);
		//process conditions
		
		//simple case, only one table
		
		if(t.children.get(0).children.get(0).children.size() == 1){
			Relation r = s.getRelation(tables.get(0));
			Block block;
		    block=mem.getBlock(0);
		    block.clear(); //clear the block
		    for(int i = 0; i < r.getNumOfBlocks(); i++){
			    r.getBlock(i, 0);
			    for(int j = 0; j < mem.getBlock(0).getTuples().size(); j++){
			    	if(apply_cons(mem.getBlock(0).getTuple(i), conditions)){
			    		//System.out.print("satisfied");
			    		projection(mem.getBlock(0).getTuple(i), attributes);
			    	}
			    	else{
			    		//System.out.print("not satisfied");
			    	}
			    }
		    }
			
		}
	}
	
	public static boolean apply_cons(Tuple tuple, List<String> cons){
		ArrayList<Integer> or = new ArrayList<Integer>();
		boolean res;
		for(int i = 0; i < cons.size(); i++){
			if(cons.get(i).equals("or")){
				or.add(i);
			}
		}
		if(or.size() != 0){
			int start = 0;
			int end = 0;
			res = false; 
			for(int i = 0; i < or.size() + 1; i++){
				if(i == or.size()){end = cons.size();}else{end = or.get(i);}
				res = res | apply_cons(tuple, cons.subList(start, end));
				if(i != or.size()){start = or.get(i) + 1;}
			}
			return res;
		}
		else{
			ArrayList<Integer> and = new ArrayList<Integer>();
			for(int i = 0; i < cons.size(); i++){
				if(cons.get(i).equals("and")){
					and.add(i);
				}
			}
			if(and.size() != 0){
				int start = 0;
				int end = 0;
				res = true;
				for(int i = 0; i < and.size() + 1; i++){
					if(i == and.size()){end = cons.size();}else{end = and.get(i);}
					res = res & apply_cons(tuple, cons.subList(start, end));
					if(i != and.size()){start = and.get(i) + 1;}
				}
				return res;
			}
			else{
				//simple case, only attribute name;
				Schema s = tuple.getSchema();
				FieldType f = s.getFieldType(cons.get(0));
				//System.out.println(cons);
				if(f.name() == "INT"){
					int val = tuple.getField(cons.get(0)).integer;
					switch(cons.get(1)){
					case "=": if(val == Integer.valueOf(cons.get(2))){return true;}else{return false;}
					case ">": if(val > Integer.valueOf(cons.get(2))){return true;}else{return false;}
					case "<": if(val < Integer.valueOf(cons.get(2))){return true;}else{return false;}
					}
				}
				else{
					String val = String.valueOf(tuple.getField(cons.get(0)).str);
					//System.out.println(cons.get(2).substring(0,1));
					if(cons.get(2).substring(0,1).equals("\"")){
						//System.out.println(cons.get(2).substring(1,cons.get(2).length()-1));
						//System.out.println(val);
						if(val.equals(cons.get(2).substring(1,cons.get(2).length()-1))){return true;}else{return false;}
					}
				}
			}
		}
		return false;
	}
	
	public static void projection(Tuple tuple,List<String> attributes){
		String out = "";
		for(int i = 0; i < attributes.size(); i++){
			out = out + tuple.getField(attributes.get(i)) + "		";
		}
		System.out.println(out);
	}
	

	
	public static void main(String[] args) throws IOException{
		//-----Initialize-----
		MainMemory mem=new MainMemory();
		Disk disk=new Disk();
		SchemaManager schema_manager=new SchemaManager(mem,disk);
		disk.resetDiskIOs();
	    disk.resetDiskTimer();
	    //--------------------
	    
	    File file = new File("H:/Courses/2016_Fall/Database/Project_2/Projects/my/src/test.txt");
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
				    //Implementation imp = new Implementation(e, mem, schema_manager);
				    //imp.iterativeProcess(e,mem, schema_manager);
			    }
	    	}
	    }
	    inputFile.close();
	    Relation r1 = schema_manager.getRelation("course");
	    System.out.println(r1.toString());
	    Relation r2 = schema_manager.getRelation("course2");
	    System.out.println(r2.toString());
	    
	    /*
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
	    //drop(schema_manager, drop_tree);
	    
		//String select = "SELECT wyh,atm FROM c, course2 WHERE course.sid = course2.sid AND course.exam > course2.exam;";
		String select = "select sid, homework, grade from course where project = 200 or grade = \"E\'  f\";";
	    Lexer lex3 = new Lexer(select);
	    ParseTree sel_tree = lex3.gettree();
	    ETConstruct etc = new ETConstruct(sel_tree);
	    ExpressionTree et = etc.select();
	    select(mem, schema_manager, et);
	    
	    
	    System.out.println("Enter your name");  
	    String name=br.readLine();  
	    System.out.println("Welcome "+name);  
	    
		switch(tree.symbol){
		case "create": create(schema_manager, tree);break;
		case "insert": insert(mem, schema_manager, tree);break;
		case "drop": drop(schema_manager, tree);break;
		}
		*/
	}
}
