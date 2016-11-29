import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;
import java.io.*;


import storageManager.*;

public class myowntest {
	private static void appendTupleToRelation(Relation relation_reference, MainMemory mem, int memory_block_index, Tuple tuple) {
	    Block block_reference;
	    if (relation_reference.getNumOfBlocks()==0) {
	      System.out.print("The relation is empty" + "\n");
	      System.out.print("Get the handle to the memory block " + memory_block_index + " and clear it" + "\n");
	      block_reference=mem.getBlock(memory_block_index);
	      block_reference.clear(); //clear the block
	      block_reference.appendTuple(tuple); // append the tuple
	      System.out.print("Write to the first block of the relation" + "\n");
	      relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index);
	    } else {
	      System.out.print("Read the last block of the relation into memory block 5:" + "\n");
	      relation_reference.getBlock(relation_reference.getNumOfBlocks()-1,memory_block_index);
	      block_reference=mem.getBlock(memory_block_index);

	      if (block_reference.isFull()) {
	        System.out.print("(The block is full: Clear the memory block and append the tuple)" + "\n");
	        block_reference.clear(); //clear the block
	        block_reference.appendTuple(tuple); // append the tuple
	        System.out.print("Write to a new block at the end of the relation" + "\n");
	        relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index); //write back to the relation
	      } else {
	        System.out.print("(The block is not full: Append it directly)" + "\n");
	        block_reference.appendTuple(tuple); // append the tuple
	        System.out.print("Write to the last block of the relation" + "\n");
	        relation_reference.setBlock(relation_reference.getNumOfBlocks()-1,memory_block_index); //write back to the relation
	      }
	    }
	  }
	
	
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
		//check relation exist
		if(s.relationExists(t.children.get(0).symbol)){
	    	Relation r = s.getRelation(t.children.get(0).symbol);
	    	//check filed_types match, to be finished.......
	    	//System.out.println("relation exists");
	    	//create a tuple
	    	Tuple tuple = r.createTuple();
	    	List<ParseTree> names = t.children.get(1).children;
	    	List<ParseTree> values = t.children.get(2).children;
		    ArrayList<String> field_names = r.getSchema().getFieldNames();
		    ArrayList<FieldType> field_types = r.getSchema().getFieldTypes();
		    //create tuple
		    for(int i = 0; i < t.children.get(1).children.size(); i ++ ){
	    		if(field_types.get(i).equals(FieldType.INT)){
		    		tuple.setField(names.get(i).symbol, Integer.parseInt(values.get(i).symbol));
	    		}
	    		else{
		    		tuple.setField(names.get(i).symbol, values.get(i).symbol.substring(1, values.get(i).symbol.length()-1));
	    		}
	    	}
		    //insert tuple
		    Block mem_block;
		    mem_block = mem.getBlock(0);
		    mem_block.clear(); //clear the block
		    if(r.getNumOfBlocks() == 0){
			    mem_block.appendTuple(tuple); // append the tuple
		    	r.setBlock(r.getNumOfBlocks(), 0);
		    }
		    else{
		    	r.getBlock(r.getNumOfBlocks()-1, 0);
		    	mem_block = mem.getBlock(0);
		    	if(mem_block.isFull()){
		    		mem_block.clear();
		    		mem_block.appendTuple(tuple); // append the tuple
			    	r.setBlock(r.getNumOfBlocks(), 0);
		    	}
		    	else{
		    		mem_block.appendTuple(tuple); // append the tuple
			    	r.setBlock(r.getNumOfBlocks()-1, 0);
		    	}
		    }
		    
		    
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
				Schema s = tuple.getSchema();
				FieldType f = s.getFieldType(cons.get(0));
				ArrayList<String> fields = tuple.getSchema().getFieldNames();
				int val2 = 0;
				for(int i = 0; i < fields.size(); i++){
					if(fields.get(0).equals(cons.get(2))){
						val2 = tuple.getField(cons.get(2)).integer;
						break;
					}
					else{
						val2 = Integer.valueOf(cons.get(2));
					}
				}
				if(f.name() == "INT"){
					int val1 = tuple.getField(cons.get(0)).integer;
					switch(cons.get(1)){
					case "=": if(val1 == val2){return true;}else{return false;}
					case ">": if(val1 > val2){return true;}else{return false;}
					case "<": if(val1 < val2){return true;}else{return false;}
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
	
	
	public static Relation sortby(MainMemory mem, Relation r, String attribute){
		//==============1.read in 10 blocks each time as a busket and sort them and append back============
		// some basic parameters
		int num_block = r.getNumOfBlocks();
		int num_busket = (int) Math.ceil(num_block/10.0);
		int left = num_block % 10;
		int num_in_busket = 0;
		System.out.println("test ceil"+num_busket);
		
		
		for (int i =0; i< num_busket;i++){
			
			//---- read in 10 blocks----
			if (10*i+9 <= num_block-1){
				num_in_busket = 10;
				r.getBlocks(10*i, 0, num_in_busket);
			}
			else{
				num_in_busket = left;
				r.getBlocks(10*i, 0, num_in_busket);
			}
			System.out.println(mem);
			
			//-----sort them---------
			// get all tuples read in mem
			ArrayList<Tuple> tuples=new ArrayList<Tuple>();
			tuples=mem.getTuples(0,num_in_busket);
			
			// buble sort through the tuples
			for (int a = 0; a < tuples.size();a++){
				for (int b = a; b < tuples.size(); b++){
					
					Tuple temp = r.createTuple();
					
					//System.out.println(tuples.get(a).getField(attribute));
					
					// if sorting attribute is integer type
					if (temp.getField(attribute).type == FieldType.INT){
						int x = tuples.get(a).getField(attribute).integer;
						int y = tuples.get(b).getField(attribute).integer;
						
						// swap
						if (x>y){
							temp = tuples.get(a);
							tuples.set(a,tuples.get(b));
							tuples.set(b, temp);
						}
					}
					// if sorting attribute is string----
					// left to be done
					//else{
					//	String x = tuples.get(a).getField(attribute).str;
					//	String y = tuples.get(b).getField(attribute).str;
					
					//	if ( x>y){
				    //	
					//	}
					
					
					
					
				}
				
			}
			// check for busket sorting
			//for (int k=0;k<tuples.size();k++)
		    	//System.out.print(tuples.get(k).toString()+"\n");
			//System.out.println(r);
			//----append back-----------
		    for (int l=0;l<tuples.size();l++) {
		        appendTupleToRelation(r,mem,5,tuples.get(l));
		    }
			System.out.println(r);
			
		}
		
		//=====2. sort sublists=======
		//empty memory
		for (int i = 0; i < 10; i++){
			Block mem_block;
		    mem_block = mem.getBlock(i);
		    mem_block.clear();
		}
		System.out.println(mem);
		
		//mem_block_9 used for storing smallest tuple
	    Block mem_block_9 = mem.getBlock(9);
	    
		
		// len: at most blocks for each busket to read into memory
		int len = 9/num_busket;
		
		// disk_block[]: # of blocks remain in disk to be ordered for each busket
		int disk_block[]= new int[num_busket];
		
		
		// num_tuple[]: # of tuples remain in memory for each busket
		int num_tuple[] = new int[num_busket];
		
	
		
		// exist_sub[]: which buskets exist for ordering
		int exist_sub[] = new int[num_busket];
		
		// ith batch from certain sublist 
		int batch[] = new int[num_busket];
		
		// sublists: array of tuples for each busket in mem
		ArrayList<ArrayList<Tuple>> sublists=new ArrayList<ArrayList<Tuple>>(num_busket);
		
		// point to top tuple of each sublists
		int point[] = new int[num_busket];
		
		//----------read in sublists from each buskets------
		for (int i = 0; i< num_busket;i++){
			if(i< num_busket-1){
				
				disk_block[i]=10-len;
				// read len blocks for busket i into memory
				r.getBlocks(num_block+i*10, 0+i*len, len);
				
				// get tuples for the blocks in memory
				ArrayList<Tuple> tuples=new ArrayList<Tuple>();
				tuples=mem.getTuples(i*len,len);				
				num_tuple[i] = tuples.size();
				
				sublists.add(tuples);
				
				// first point to the first tuple
				point[i] = 0;
				// sublist all exist at beginning
				exist_sub[i] = 1;
				// first batch from sublist i
				batch[i]=1;
				
			}
			else{
				
				disk_block[i] = left - Math.min(len, left);
				
				r.getBlocks(num_block+i*10, 0+i*len,Math.min(len, left));
				
				ArrayList<Tuple> tuples=new ArrayList<Tuple>();
				tuples=mem.getTuples(i*len,Math.min(len, left));			
				num_tuple[i] = tuples.size();
				
				sublists.add(tuples);
				
				point[i] = 0;
				exist_sub[i] = 1;
				batch[i]=1;
			}
		}
		System.out.println(mem);
		//----------comparing sublists store smallest one in block [9], if full write back
		
		int sum = 0;
		int sort_index = 0; // write the block back into relation, start from beginning
		for (int i = 0; i< num_busket;i++){
			sum+=exist_sub[i];
		}
		System.out.println("busket"+num_busket);
		// when still have remaining tuples, continue loop
		int count=0;
		while (sum>0){
			System.out.println("----------round-"+count+"--------");
			int min = Integer.MAX_VALUE;
			int index = 0;
			//get the index of the smallest tuple
			for (int i = 0; i< num_busket; i++){
				if (exist_sub[i]>0){
					System.out.println("sublistsize"+i+" "+sublists.get(i));
					if (min > sublists.get(i).get(point[i]).getField(attribute).integer){
						min = sublists.get(i).get(point[i]).getField(attribute).integer;
						index = i;
					}
				}
				
			}
			
			//write smallest tuple to mem[9]
			//System.out.println("********"+index+point[index]+num_tuple[index]);
			mem_block_9.appendTuple(sublists.get(index).get(point[index]));
			if (mem_block_9.isFull()){
				r.setBlock(sort_index, 9);
				mem_block_9.clear();
				sort_index++;
			}
			System.out.println("index"+index+" "+"point"+point[index]+"left blocks in disk"+disk_block[index]);
			System.out.println(r);
			
			
			// deal with the changed sublist
			point[index]++;
			
			num_tuple[index]--;
			
			
			
			// when tuples of a sublist in memory are used up, read more from disk
			if (num_tuple[index]==0){
				
				// if disk still have more tuples
				if(disk_block[index]>0){		
						
						// tuples in disk is more than len
						if(disk_block[index] > len){
							
							// read the following # of len tuples from that sublist 
							r.getBlocks(num_block+index*10+batch[index]*len, index*len,len);
							// update the corresponding sublist of tuples
							ArrayList<Tuple> tuples=new ArrayList<Tuple>();
							tuples=mem.getTuples(index*len,len);
							num_tuple[index] = tuples.size();
							sublists.set(index, tuples);
							
							// decrease the blocks left in disk by len
							disk_block[index]-=len;
							// record how many batches are read from disk for a sublist
							batch[index]++;
						}
						// tuples in disk is less than len
						else{
							r.getBlocks(num_block+index*10+batch[index]*len, index*len,disk_block[index]);
							ArrayList<Tuple> tuples=new ArrayList<Tuple>();
							tuples=mem.getTuples(index*len,disk_block[index]);
							num_tuple[index] = tuples.size();
							sublists.set(index, tuples);
							disk_block[index]=0;
							batch[index]++;
						}
						
					
				point[index]=0;	
				}
				// a sublist is all sorted, then indicate in exist_sub
				else{
					exist_sub[index]=0;
				}
			}
		
			sum = 0;
			for (int i = 0; i< num_busket;i++){
				sum+=exist_sub[i];
			}
			count++;
			//System.out.println("sum"+sum);
		
		}
		
		r.deleteBlocks(num_block);
		System.out.println(r);
				
		return r;
	}

	
	public static void main(String[] args) throws IOException{
		//-----Initialize-----
		MainMemory mem=new MainMemory();
		Disk disk=new Disk();
		SchemaManager schema_manager=new SchemaManager(mem,disk);
		disk.resetDiskIOs();
	    disk.resetDiskTimer();
	    //--------------------
	    /*
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
				    Implementation imp = new Implementation(e, mem, schema_manager);
				    imp.select_cross(mem, schema_manager, e);
				    
			    }
			    
	    	}
	    	
	    }
	    inputFile.close();
	    Relation r1 = schema_manager.getRelation("course");
	    System.out.println(r1.toString());
	    Relation r2 = schema_manager.getRelation("course2");
	    System.out.println(r2.toString());
	    
	    //-------test sortby----------------------------
	    sortby(mem, schema_manager.getRelation("course"), "homework");
	    
		
		
	   System.out.print("After, the memory contains: " + "\n");
	    //System.out.print(mem + "\n");

	    //System.out.print("After, Current schemas and relations: " + "\n");
	    //System.out.print(schema_manager + "\n");
		
		
	   
		 //------select test-------
		
		String s = "SELECT DISTINCT course.grade, course2.grade FROM course, course2 WHERE course.sid = course2.sid AND course.grade = \"A\" AND course2.grade = \"A\" ORDER BY course.exam";
		Lexer l = new Lexer(s);
		ParseTree t = l.gettree();
		*/
	    String s = "CREATE TABLE course (sid INT, homework INT, project INT, exam INT, grade STR20)";
	    Lexer l = new Lexer(s);
		ParseTree t = l.gettree();
		create(schema_manager, t);


		//r.getSchema().addcolumn("distinct", FieldType.valueOf("INT"));
		String s1 = "INSERT INTO course (sid, homework, project, exam, grade) VALUES (1, 99, 100, 100, \"A\")";
		Lexer l1 = new Lexer(s1);
		ParseTree t1 = l1.gettree();
		insert(mem, schema_manager, t1);
		
		
		
	}
}
