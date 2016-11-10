import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
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
		if(s.relationExists(t.children.get(0).symbol)){
	    	Relation r = s.getRelation(t.children.get(0).symbol);
	    	System.out.println("relation exists");
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
		    
		}
	    else{
	    	System.err.println("relatoin not exists");
	    	//throwexception relation not exist and go back to command input or stop
	    }
	}
	
	public static void main(String[] args){
		//-----Initialize-----
		MainMemory mem=new MainMemory();
		Disk disk=new Disk();
		SchemaManager schema_manager=new SchemaManager(mem,disk);
		disk.resetDiskIOs();
	    disk.resetDiskTimer();
	    //--------------------
	    
	    //-----create relation-----
		//String raw_statement = "INSERT INTO course VALUES (\"2 d d\", 0, 100, 100, \"E\'  f\")";
	    String raw_statement = "CREATE TABLE course (sid INT, homework INT, project INT, exam INT, grade STR20)";
	    Lexer lex = new Lexer(raw_statement);
	    ParseTree tree = lex.gettree();
	    if(tree.symbol.equals("create")){
		    create(schema_manager, tree);
	    }
	    //-------------------------
	    
	    //select test
	    
	    //-----insert tuples-----
	    String insert = "INSERT INTO course (sid, homework, project, exam, grade) VALUES (12, 0, 100, 100, \"E\'  f\")";
	    Lexer ins =new Lexer(insert);
	    ParseTree ins_tree = ins.gettree();
	    insert(mem, schema_manager, ins_tree);
	    //-------------------------
	    
	    System.out.println("done");
	    
	}
}
