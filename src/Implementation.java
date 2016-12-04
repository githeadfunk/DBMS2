import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;

import storageManager.*;
import sun.invoke.empty.Empty;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
public class Implementation {
	ExpressionTree exptree;
	SchemaManager s;
	MainMemory mem;
	
	public Implementation(ExpressionTree t, MainMemory mem, SchemaManager s){
		this.exptree = t;
		this.s = s;
		this.mem = mem;
	}
	
	public void drop(SchemaManager s,ParseTree t){
		s.deleteRelation(t.children.get(0).symbol);
	}
	
	public void insert(MainMemory mem, SchemaManager s,ParseTree t){
		//check relation exist
		if(s.relationExists(t.children.get(0).symbol)){
	    	Relation r = s.getRelation(t.children.get(0).symbol);
	    	//check filed_types match, to be finished.......
	    	//create a tuple
	    	Tuple tuple = r.createTuple();
	    	List<ParseTree> names = t.children.get(1).children;
	    	List<ParseTree> values = t.children.get(2).children;
	    	Schema sch = r.getSchema();
		    ArrayList<String> field_names = sch.getFieldNames();
		    ArrayList<FieldType> field_types = sch.getFieldTypes();
		    //create tuple
		    for(int i = 0; i < names.size(); i ++ ){
		    	if(sch.getFieldType(names.get(i).symbol).equals(FieldType.INT)){
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
	    	write2file("relatoin not exists");
	    }
	}
	
	public static Relation sortby(MainMemory mem, Relation r, String attribute){
		//==============1.read in 10 blocks each time as a busket and sort them and append back============
		// some basic parameters
		int num_block = r.getNumOfBlocks();
		int num_busket = (int) Math.ceil(num_block/10.0);
		int left = num_block % 10;
		int num_in_busket = 0;
		
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
			
			//-----sort them---------
			// get all tuples read in mem
			ArrayList<Tuple> tuples=new ArrayList<Tuple>();
			tuples=mem.getTuples(0,num_in_busket);
			
			// buble sort through the tuples
			for (int a = 0; a < tuples.size();a++){
				for (int b = a; b < tuples.size(); b++){
					
					Tuple temp = r.createTuple();
					
					
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
				
					
					
					
					
				}
				
			}
			// check for busket sorting
			//for (int k=0;k<tuples.size();k++)
			//----append back-----------
			for (int l=0;l<tuples.size();l++) {
				if (l==0){
				Block mem_block;
				mem_block = mem.getBlock(0);
				mem_block.clear();
				mem_block.appendTuple(tuples.get(0));
				r.setBlock(r.getNumOfBlocks(), 0);
				
				}
				else{
					appendTupleToRelation(r,mem,5,tuples.get(l));
				}
				
			}			
		}
		
		
		//=====2. sort sublists=======
		//empty memory
		for (int i = 0; i < 10; i++){
			Block mem_block;
		    mem_block = mem.getBlock(i);
		    mem_block.clear();
		}		
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
				// first batch from sublist is
				batch[i]=1;
				
			}
			else{
				
				
				int num_read = 0;
				if (left == 0){
					num_read = len;
					disk_block[i] = 10 - num_read;

				}
				else{
					num_read = Math.min(len, left);
					disk_block[i] = left - num_read;
				}

				
				
				r.getBlocks(num_block+i*10, 0+i*len,num_read);
							
				ArrayList<Tuple> tuples=new ArrayList<Tuple>();
				tuples=mem.getTuples(i*len,num_read);			
				num_tuple[i] = tuples.size();
				
				sublists.add(tuples);
				
				point[i] = 0;
				exist_sub[i] = 1;
				batch[i]=1;
			}
		}
		//----------comparing sublists store smallest one in block [9], if full write back
		
		int sum = 0;
		int sort_index = 0; // write the block back into relation, start from beginning
		for (int i = 0; i< num_busket;i++){
			sum+=exist_sub[i];
		}
		// when still have remaining tuples, continue loop
		int count=0;
		while (sum>0){
			int min = Integer.MAX_VALUE;
			int index = 0;
			//get the index of the smallest tuple
			for (int i = 0; i< num_busket; i++){
				if (exist_sub[i]>0){
					if (min > sublists.get(i).get(point[i]).getField(attribute).integer){
						min = sublists.get(i).get(point[i]).getField(attribute).integer;
						index = i;
					}
				}
				
			}
			
			//write smallest tuple to mem[9]
			mem_block_9.appendTuple(sublists.get(index).get(point[index]));
			if (mem_block_9.isFull()){
				
				r.setBlock(sort_index, 9);				
				mem_block_9.clear();
				sort_index++;
			}			
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
		}
		
		r.deleteBlocks(num_block);				
		return r;
	}
	
	public void create(SchemaManager s,ParseTree t){
		
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
	
	public Relation cross(Relation table1, Relation table2, Boolean naturaljoin){
		
		
	 	//-------------cross for 2 tables----------------------
	 	// get 2 tables

	    // get  field name and type for each table
	    ArrayList<String> field_names1= table1.getSchema().getFieldNames();
	    ArrayList<FieldType> field_types1= table1.getSchema().getFieldTypes();
	    ArrayList<String> field_names2= table2.getSchema().getFieldNames();
	    ArrayList<FieldType> field_types2= table2.getSchema().getFieldTypes();
	    
	    // initialize field name and type for output table
	    ArrayList<String> cross_field_name = new ArrayList<String>();
	    ArrayList<FieldType> cross_field_type =new ArrayList<FieldType>();
	   
	    
	    // deal with common field names in two tables
	    for (int i = 0; i< field_names1.size();i++){
	    	for (int j = 0; j< field_names2.size();j++){

	    		if (field_names1.get(i).equals(field_names2.get(j) )){
	    			field_names1.set(i,table1.getRelationName()+ "."+field_names1.get(i));
	    			field_names2.set(j,table2.getRelationName()+ "."+field_names2.get(j));
	    		}
	    	}
	    }
	    
	    // get field_name, field_type for the cross output
	    cross_field_name.addAll(field_names1);
	    cross_field_name.addAll(field_names2);
	    cross_field_type.addAll(field_types1);
	    cross_field_type.addAll(field_types2);
	    // get schema for crossed intermediate table
	    Schema schema=new Schema(cross_field_name,cross_field_type);
	    // create relation for intermediate table
	    String relation_name="intermediate"+table1.getRelationName()+table2.getRelationName();
	    Relation relation_reference=s.createRelation(relation_name,schema);
	    
	    
	    //if natural join, the shared attributes
	    List<String> commonattributes = new ArrayList<String>();
	    if (naturaljoin == true)
	    {
	    	for (int i = 0; i<table1.getSchema().getNumOfFields(); i++){
	    		for (int j = 0; j<table2.getSchema().getNumOfFields(); j++){
	    			if (table1.getSchema().getFieldName(i).equals(table2.getSchema().getFieldName(j))){
	    				commonattributes.add(table1.getSchema().getFieldName(i)) ;
	    			}
	    		}
	    	}
	    }	    
		// some basic parameters
		int num_block1 = table1.getNumOfBlocks();
		int num_busket1 = (int) Math.ceil(num_block1/5.0);
		int left1 = num_block1 % 5;
		int num_in_busket1 = 0;
		
		int num_block2 = table2.getNumOfBlocks();
		int num_busket2 = (int) Math.ceil(num_block2/5.0);
		int left2 = num_block2 % 5;
		int num_in_busket2 = 0;
		
		
		for (int i =0; i< num_busket1;i++){			
			
		    for (int x = 0; x<5;x++){ //access to memory block 0
		    	Block block_reference=mem.getBlock(0);
		    	block_reference.clear();
		    }
		    
			
			if (5*i+5 <= num_block1){
				num_in_busket1 = 5;
				table1.getBlocks(5*i, 0, num_in_busket1);
			}
			else{
				num_in_busket1 = left1;
				table1.getBlocks(5*i, 0, num_in_busket1);
			}
			
			ArrayList<Tuple> tuples1=new ArrayList<Tuple>();
			tuples1=mem.getTuples(0,num_in_busket1);
			
			for (int j =0; j< num_busket2;j++){			
				
			    for (int y = 5; y<10;y++){ //access to memory block 0
			    	Block block_reference=mem.getBlock(0);
			    	block_reference.clear();
			    }
				
				if (5*j+5 <= num_block2){
					num_in_busket2 = 5;
					table2.getBlocks(5*j, 5, num_in_busket2);
				}
				else{
					num_in_busket2 = left2;
					table2.getBlocks(5*j, 5, num_in_busket2);
				}
				
				ArrayList<Tuple> tuples2=new ArrayList<Tuple>();
				tuples2=mem.getTuples(5,num_in_busket2);
	    
			    
			    for (int m = 0; m< tuples1.size(); m++){
			    	for(int n =0; n < tuples2.size(); n++){
			    		// cross each tuple from one table with tuples in the other table
			    		Tuple t1 = tuples1.get(m);
					    Tuple t2 = tuples2.get(n);
					    
					    Boolean naturalcontain = true;
					    for (int c = 0; c < commonattributes.size(); c++ ){
					    	if (t1.getField(commonattributes.get(c)).integer != t2.getField(commonattributes.get(c)).integer){
					    		naturalcontain = false;
					    	}
					    }
					    
					    
					    if (!(naturaljoin == true && naturalcontain == false)){
					    	
					    
					    	Tuple tuple = relation_reference.createTuple();
						    // first append values from tuple1 to the new tuple
						    for (int a =0; a< t1.getNumOfFields();a++){
						    	//convert to proper fieldType 
						    	if (field_types1.get(a).toString()=="INT"){
						    		tuple.setField(a, t1.getField(a).integer );
						    	}
						    	else{
						    		tuple.setField(a, t1.getField(a).str);
						    	}
						    }
						    // then append values from tuple2 to the new tuple
						    
						    for (int a =0; a< t2.getNumOfFields();a++){
						    	//convert to proper fieldType 
						    	 
						    	if (field_types2.get(a).toString()=="INT"){
						    		tuple.setField(a+t1.getNumOfFields(), Integer.parseInt(t2.getField(a).toString()));
						    	}
						    	else{
						    		tuple.setField(a+t1.getNumOfFields(), t2.getField(a).toString());
						    	}
						    }
						    // append new tuple to the crossed output relation
						    appendTupleToRelation(relation_reference, mem, 5,tuple);
					    }
					    
					    
					    
			    	}
			    }
			    
	    	}
	    }    	 
        return relation_reference;
	
}
	
	
	
	private static void appendTupleToRelation(Relation relation_reference, MainMemory mem, int memory_block_index, Tuple tuple) {
	    Block block_reference;
	    if (relation_reference.getNumOfBlocks()==0) {
	      block_reference=mem.getBlock(memory_block_index);
	      block_reference.clear(); //clear the block
	      block_reference.appendTuple(tuple); // append the tuple
	      relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index);
	    } else {
	      relation_reference.getBlock(relation_reference.getNumOfBlocks()-1,memory_block_index);
	      block_reference=mem.getBlock(memory_block_index);

	      if (block_reference.isFull()) {
	        block_reference.clear(); //clear the block
	        block_reference.appendTuple(tuple); // append the tuple
	        relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index); //write back to the relation
	      } else {
	        block_reference.appendTuple(tuple); // append the tuple
	        relation_reference.setBlock(relation_reference.getNumOfBlocks()-1,memory_block_index); //write back to the relation
	      }
	    }
	  }
	
	
	//--------separate implementation: projection, apply_con functions------
	public static boolean apply_cons(Tuple tuple, List<String> cons){
		if(cons.get(0).equals("(")){
			if(tuple.getField("exam").integer + tuple.getField("homework").integer == 200){
				return true;
			}
			else{
				return false;
			}
		}
		ArrayList<Integer> or = new ArrayList<Integer>();
		boolean res;
		for(int i = 0; i < cons.size(); i++){
			if(cons.get(i).toLowerCase().equals("or")){
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
				if(cons.get(i).toLowerCase().equals("and")){
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
				if(f.name() == "INT"){
					int val1 = tuple.getField(cons.get(0)).integer;
					int val2 = 0;
					for(int i = 0; i < fields.size(); i++){
						if(fields.get(i).equals(cons.get(2))){
							val2 = tuple.getField(cons.get(2)).integer;
							break;
						}
						if(i == fields.size()-1){
							val2 = Integer.valueOf(cons.get(2));
						}
					}
					switch(cons.get(1)){
					case "=": if(val1 == val2){return true;}else{return false;}
					case ">": if(val1 > val2){return true;}else{return false;}
					case "<": if(val1 < val2){return true;}else{return false;}
					}
				}
				else{
					String val = String.valueOf(tuple.getField(cons.get(0)).str);
					String val2 = "";
					if(cons.get(2).charAt(0)=='"'){
						String inter = cons.get(2).substring(1,cons.get(2).length()-1);
						cons.set(2, inter);
					}
					for(int i = 0; i < fields.size(); i++){
						if(fields.get(i).equals(cons.get(2))){
							val2 = tuple.getField(cons.get(2)).str;
							break;
						}
						if(i == fields.size()-1){
							val2 = cons.get(2);
						}
					}
					if(val.equals(val2)){return true;}else{return false;}
				}
			}
		}
		return false;
	}
	
	public static void projection(Tuple tuple, List<String> attributes){
		String out = "";
		
		
		for(int i = 0; i < attributes.size(); i++){
			out = out + tuple.getField(attributes.get(i)) + "		";
		}
		System.out.println(out);
		write2file(out);
	}
	
	public static void projection_relation(MainMemory mem, Relation r, List<String> attributes){
		Schema s = r.getSchema();
		List<String> attri = new ArrayList<String>();
		if(attributes.get(0).equals("*")){
			for(int i = 0; i < s.getNumOfFields(); i++){
				if(!s.getFieldName(i).equals("distinct")){
					attri.add(s.getFieldName(i));
				}
			}
		}
		else{
			attri = attributes;
		}
		String o = "";
		for(int i = 0; i < attri.size(); i ++){
			o = o + attri.get(i) + "		";
		}
		System.out.println(o);
		write2file(o);
		Block b = mem.getBlock(0);
		b.clear();
		for(int j =0; j < r.getNumOfBlocks(); j++){
			r.getBlock(j,0);
			b = mem.getBlock(0);
			List<Tuple> tuples = b.getTuples();
			List<Tuple> ts = new ArrayList<Tuple>();
			for(int m = 0; m < tuples.size(); m ++){
				if(!tuples.get(m).isNull()){
					ts.add(tuples.get(m));
				}
			}
			for(int k = 0; k < ts.size(); k++){
				String out = "";
				Tuple tuple = ts.get(k);
				for(int i = 0; i < attri.size(); i++){
					out = out + tuple.getField(attri.get(i)) + "		";
				}
				System.out.println(out);
				write2file(out);
			}
		}
	}
	
	
	
	
	public void select_simple(MainMemory mem, SchemaManager s, ExpressionTree t){
		
		//-----------extract lists of tables, attributes and conditions-------------
		List<String> tablelist = new ArrayList<String>();
		List<String> attributelist = new ArrayList<String>();
		List<String> conditionlist = new ArrayList<String>();
		for (int i =0; i< t.getAttribute().size(); i++){
			attributelist.add(t.getAttribute().get(i).getSymbol());
			
		}
		
		if (t.getChildren().get(0).getSymbol() == "sigma"){
			for (int i =0; i< t.getChildren().get(0).getAttribute().size(); i++){
				conditionlist.add(t.getChildren().get(0).getAttribute().get(i).getSymbol());
			}
			for (int i =0; i< t.getChildren().get(0).getChildren().get(0).getAttribute().size(); i++){
				tablelist.add(t.getChildren().get(0).getChildren().get(0).getAttribute().get(i).getSymbol());
			}
				
		}
		else{
			for (int i =0; i< t.getChildren().get(0).getAttribute().size(); i++){
				tablelist.add(t.getChildren().get(0).getAttribute().get(i).getSymbol());
			}
		}
		//-----------carry out cross process, nested with con_apply and pi function to get final result 
		int num = tablelist.size();
		if (num ==1){
			 //--------only one table, just return the table
			 //return s.getRelation(tablelist.get(0));
			 Relation table = s.getRelation(tablelist.get(0));
			 List<String> attri_list = new ArrayList<String>();
			 
			 Schema sch = table.getSchema();
			 List<String> attri = new ArrayList<String>();
			 if(attributelist.get(0).equals("*")){
				 for(int i = 0; i < sch.getNumOfFields(); i++){
					 attri.add(sch.getFieldName(i));
				 }
			 }
			 else{
				 attri = attributelist;
			 }
			 String out = "";
			 for(int i = 0; i < attri.size(); i ++){
					out = out + attri.get(i) + "		";
			 }
			 System.out.println(out);
			 write2file(out);
				
			 for(int i = 0; i < attri.size(); i++){
				 if(attri.get(i).contains(".")){
					 int idx = attri.get(i).indexOf(".");
					 attri_list.add(attri.get(i).substring(idx+1,attri.get(i).length()));
				 }
				 else{
					 attri_list.add(attri.get(i));
				 }
			 }
			 Block block_reference=mem.getBlock(0); //access to memory block 0
			 block_reference.clear();
			 for (int i = 0; i < table.getNumOfBlocks();i++){
			    	
				    // read block i of talbe1 into memory block 0
				    table.getBlock(i, 0);
				    block_reference=mem.getBlock(0);
				    
				    // read all the tuples in memory block 0
				    int tuple_num = block_reference.getNumTuples();
				    for (int j = 0; j< tuple_num; j++){
				    	  Tuple tuple = block_reference.getTuple(j);
						    if (conditionlist.size() == 0){
						    	projection(tuple,attri_list);
						    }
						    else if (apply_cons(tuple,conditionlist)){
						    	projection(tuple,attri_list);
						    }
				    }
				  
				    
				    
			 }
		 }
		 else if (num ==2){	
			 	//-------------cross for 2 tables----------------------
			 	// get 2 tables
			 	Relation table1 = s.getRelation(tablelist.get(0));
			 	Relation table2 = s.getRelation(tablelist.get(1));
			    // get  field name and type for each table
			    ArrayList<String> field_names1= table1.getSchema().getFieldNames();
			    ArrayList<FieldType> field_types1= table1.getSchema().getFieldTypes();
			    ArrayList<String> field_names2= table2.getSchema().getFieldNames();
			    ArrayList<FieldType> field_types2= table2.getSchema().getFieldTypes();
			    
			    // initialize field name and type for output table
			    ArrayList<String> cross_field_name = new ArrayList<String>();
			    ArrayList<FieldType> cross_field_type =new ArrayList<FieldType>();
			   
			    
			    // deal with common field names in two tables
			    for (int i = 0; i< field_names1.size();i++){
			    	for (int j = 0; j< field_names2.size();j++){

			    		if (field_names1.get(i).equals(field_names2.get(j) )){
			    			field_names1.set(i,tablelist.get(0)+ "."+field_names1.get(i));
			    			field_names2.set(j,tablelist.get(1)+ "."+field_names2.get(j));
			    		}
			    	}
			    }
			    
			    // get field_name, field_type for the cross output
			    cross_field_name.addAll(field_names1);
			    cross_field_name.addAll(field_names2);
			    cross_field_type.addAll(field_types1);
			    cross_field_type.addAll(field_types2);
			    // get schema for crossed intermediate table
			    Schema schema=new Schema(cross_field_name,cross_field_type);

				List<String> attri = new ArrayList<String>();
				if(attributelist.get(0).equals("*")){
					for(int i = 0; i < schema.getNumOfFields(); i++){
						attri.add(schema.getFieldName(i));
					}
				}
				else{
					attri = attributelist;
				}
				String out = "";
				for(int i = 0; i < attri.size(); i ++){
					out = out + attri.get(i) + "		";
				}
				System.out.println(out);
				write2file(out);
				
			    // create relation for intermediate table
			    String relation_name="intermediate";
			    Relation relation_reference=s.createRelation(relation_name,schema);
			    
			    
			    // Set up two blocks in the memory each block[0] for table1, block[1] for table2, use nested loop algorithm
			    Block block_reference=mem.getBlock(0); //access to memory block 0
			    block_reference.clear();
		    	Block block_reference1=mem.getBlock(1); //access to memory block 0
			    block_reference1.clear();
			    
			    // outer loop for reading table1
			    for (int i = 0; i < table1.getNumOfBlocks();i++){
			    	
				    // read block i of talbe1 into memory block 0
				    table1.getBlock(i, 0);
				    
				    // inner loop for reading table2
			    	for (int j = 0; j < table2.getNumOfBlocks(); j++){

					    // read block j of talbe2 into memory block 1
					    table2.getBlock(j, 1);
					    block_reference=mem.getBlock(0);
					    block_reference1=mem.getBlock(1);					    
					    //get how many tuples exist in a block for table 1 and table2
					    int num_tuple = block_reference.getNumTuples();
					    int num_tuple1 = block_reference1.getNumTuples();
					    
					    
					    
					    for (int m = 0; m< num_tuple; m++){
					    	for(int n =0; n < num_tuple1; n++){
					    		// cross each tuple from one table with tuples in the other table
					    		Tuple t1 = block_reference.getTuple(m);
							    Tuple t2 = block_reference1.getTuple(n);
							    
							    // create crossed new tuple
							    Tuple tuple = relation_reference.createTuple();
							    // first append values from tuple1 to the new tuple
							    for (int a =0; a< t1.getNumOfFields();a++){
							    	//convert to proper fieldType 
							    	if (field_types1.get(a).toString()=="INT"){
							    		tuple.setField(a, t1.getField(a).integer );
							    	}
							    	else{
							    		tuple.setField(a, t1.getField(a).str);
							    	}
							    }
							    // then append values from tuple2 to the new tuple
							    
							    for (int a =0; a< t2.getNumOfFields();a++){
							    	//convert to proper fieldType 
							    	 
							    	if (field_types2.get(a).toString()=="INT"){
							    		tuple.setField(a+t1.getNumOfFields(), Integer.parseInt(t2.getField(a).toString()));
							    	}
							    	else{
							    		tuple.setField(a+t1.getNumOfFields(), t2.getField(a).toString());
							    	}
							    }
							    
							    // append new tuple to the crossed output relation
							    //appendTupleToRelation(relation_reference, mem, 5,tuple); 
							    // apply sigma and pi
							    List<String> con_list = new ArrayList<String>();
								 for(int k = 0; k < conditionlist.size(); k++){
									 if(conditionlist.get(k).contains(".")){
										 if(!cross_field_name.contains(conditionlist.get(k))){
											 int idx = conditionlist.get(k).indexOf(".");
											 con_list.add(conditionlist.get(k).substring(idx+1,conditionlist.get(k).length()));
										 }
										 else{
											 con_list.add(conditionlist.get(k));
										 }
									 }
									 else{
										 con_list.add(conditionlist.get(k));
									 }
								 }
							    if (conditionlist.size() == 0){
							    	projection(tuple,attri);
							    }
							    else if (apply_cons(tuple,con_list)){
							    	projection(tuple,attri);
							    }    
					    	}
					    }
					    
			    	}
			    }    	 
		 }
		 else{
			 // get list of relations sorted by size
			 List<Relation> relations = new ArrayList<Relation>();
			 relations.add(s.getRelation(tablelist.get(0)));
			 int count = 1;
			 
			 for (int j = 0; j<relations.size(); j++){
				if (count >= tablelist.size()){
					break;
				}
				else if (s.getRelation(tablelist.get(count)).getNumOfBlocks() < relations.get(j).getNumOfBlocks()){
					relations.add(j,s.getRelation(tablelist.get(count)) );
					count++;
				}
				else if(j==relations.size()-1){
					relations.add(j+1,s.getRelation(tablelist.get(count)) );
					count++;
				}
			 }
			 Boolean naturaljoin = false;
			 Relation cross_relation;
			 if (conditionlist.size() == 3*tablelist.size()+tablelist.size()-1){
				 naturaljoin = true;
				 cross_relation = cross(s.getRelation(tablelist.get(0)),s.getRelation(tablelist.get(1)), true);
				 for (int i = 2; i<num;i++){
					 cross_relation = cross(cross_relation, s.getRelation(tablelist.get(i)),true);
				 }
			 }
			 else{
				 cross_relation = cross(s.getRelation(tablelist.get(0)),s.getRelation(tablelist.get(1)), false);
				 for (int i = 2; i<num;i++){
					 cross_relation = cross(cross_relation, s.getRelation(tablelist.get(i)), true);
				 }
			 }
			 // get crossed relation for multiple tables
			 Schema schema = cross_relation.getSchema();
			 List<String> attri = new ArrayList<String>();
				if(attributelist.get(0).equals("*")){
					for(int i = 0; i < schema.getNumOfFields(); i++){
						attri.add(schema.getFieldName(i));
					}
				}
				else{
					attri = attributelist;
				}
				String out = "";
				for(int i = 0; i < attri.size(); i ++){
					out = out + attri.get(i) + "		";
				}
				System.out.println(out);
				write2file(out);
			 
			 // read crossed table into memory and apply condition and projection
			 Block block_reference=mem.getBlock(0); //access to memory block 0
			 block_reference.clear();
			 
			 for (int i = 0; i < cross_relation.getNumOfBlocks();i++){
				    	
				// read block i of talbe1 into memory block 0
				cross_relation.getBlock(i, 0);
				block_reference=mem.getBlock(0);
					    
				// read all the tuples in memory block 0
				int tuple_num = block_reference.getNumTuples();
				for (int j = 0; j< tuple_num; j++){
					Tuple tuple = block_reference.getTuple(j);
					if (conditionlist.size() == 0 || naturaljoin == true){
						projection(tuple,attri);
					}
					else if (apply_cons(tuple,conditionlist)){
						projection(tuple,attri);
					}
				 }
					  			    
			}
			 
			 
			 
		 }
		if(s.relationExists("intermediate")){
			s.deleteRelation("intermediate");
		}
	}
	
	public static Relation distinct(Relation cross, List<String> attributes, List<String> sortlist, SchemaManager s, MainMemory mem){
		//Create distinc relation
		Schema sch = cross.getSchema();
		ArrayList<String> field_names = sch.getFieldNames();
		ArrayList<FieldType> field_types = sch.getFieldTypes();
		ArrayList<String> dis_field_names = new ArrayList<String>();
	    ArrayList<FieldType> dis_field_types =new ArrayList<FieldType>();
	    ArrayList<String> new_field_names = new ArrayList<String>();
	    ArrayList<FieldType> new_field_types =new ArrayList<FieldType>();
	    if(!attributes.get(0).equals("*")){
	    	for(int i =0; i < attributes.size(); i++){
		    	dis_field_names.add(attributes.get(i));
		    	dis_field_types.add(sch.getFieldType(attributes.get(i)));
		    	new_field_names.add(attributes.get(i));
		    	new_field_types.add(sch.getFieldType(attributes.get(i)));
		    }
	    }
	    else{
	    	for(int i =0; i < field_names.size(); i++){
		    	dis_field_names.add(field_names.get(i));
		    	dis_field_types.add(field_types.get(i));
		    	new_field_names.add(field_names.get(i));
		    	new_field_types.add(field_types.get(i));
		    }
	    }
	    
	    if(sortlist.size() != 0){
	    	new_field_names.add(sortlist.get(0));
	    	new_field_types.add(sch.getFieldType(sortlist.get(0)));
	    }	  
		new_field_names.add("distinct");
		new_field_types.add(FieldType.valueOf("INT"));
		Schema schema=new Schema(new_field_names, new_field_types);
		Relation distinct = s.createRelation("distinct", schema);
		
		//copy relation and add distinct column
		Block block_reference = mem.getBlock(0);
		block_reference.clear();
		for(int i = 0; i < cross.getNumOfBlocks(); i++){
			cross.getBlock(i, 0);
			block_reference = mem.getBlock(0);
			for(int j = 0; j < block_reference.getNumTuples(); j++){
				Tuple dis_t = distinct.createTuple();
				Tuple cro_t = block_reference.getTuple(j);
				int dis = 0;
				for(int k = 0; k < new_field_names.size() - 1; k++){
					if(k < dis_field_names.size()){
						if(dis_field_types.get(k).equals(FieldType.INT)){
							dis_t.setField(k, cro_t.getField(dis_field_names.get(k)).integer);
							if(k < dis_field_names.size()){
								dis += dis_t.getField(k).integer;
							}
						}
						else{
							dis_t.setField(k, cro_t.getField(dis_field_names.get(k)).str);
							if(k < dis_field_names.size()){
								for(int p = 0; p < dis_t.getField(k).str.length(); p++){
									int temp = (int) dis_t.getField(k).str.charAt(p);
									dis += temp;
								}
							}
						}
					}
					else{
						if(new_field_types.get(k).equals(FieldType.INT)){
							dis_t.setField(k, cro_t.getField(new_field_names.get(k)).integer);
						}
						else{
							dis_t.setField(k, cro_t.getField(new_field_names.get(k)).str);
						}
					}
					
				}
				
				dis_t.setField(new_field_names.size()-1, dis);
				block_reference = mem.getBlock(9);
				block_reference.clear();
				appendTupleToRelation(distinct, mem, 9, dis_t);
			}
		}
		//distinct = sortby(mem, distinct, "distinct");
		delete_dublicate(mem, s, distinct);
		return distinct;
		
	}
	
	public void select_complex(MainMemory mem, SchemaManager s, ExpressionTree t){
		
		//-----------extract lists of tables, attributes and conditions-------------
		List<String> tablelist = new ArrayList<String>();
		List<String> attributelist = new ArrayList<String>();
		List<String> conditionlist = new ArrayList<String>();
		List<String> sortlist = new ArrayList<String>();
		int sort_flag = 0, distinct_flag = 0, condition_flag = 0;
		while(t.symbol != "cross"){
			switch(t.symbol){
			case "pi": {
				for (int i =0; i< t.getAttribute().size(); i++){
					attributelist.add(t.getAttribute().get(i).getSymbol());
				}
				break;
				}
			case "pi_distinct": {
				for (int i =0; i< t.getAttribute().size(); i++){
					attributelist.add(t.getAttribute().get(i).getSymbol());
				}
				distinct_flag = 1;
				break;
				}
			case "order": {
				sortlist.add(t.getAttribute().get(0).getSymbol());
				sort_flag = 1;
				break;
				}
			case "sigma": {
				for (int i =0; i< t.getAttribute().size(); i++){
					conditionlist.add(t.getAttribute().get(i).getSymbol());
				}
				condition_flag = 1;
				break;
				}
			default: {}
			}
			t = t.children.get(0);
		}
		for (int i =0; i< t.getAttribute().size(); i++){
			tablelist.add(t.getAttribute().get(i).getSymbol());
		}
		
		Relation cross = cross_join(tablelist, conditionlist, condition_flag);
		Relation dis = null;
		if(sort_flag == 1){
			dis = sortby(mem, cross, sortlist.get(0));
		}
		if(distinct_flag == 1){
			dis = distinct(cross, attributelist, sortlist, s, mem);
		}
		
			
				
		projection_relation(mem, dis, attributelist);
		s.deleteRelation("cross_join");
		if(s.relationExists("distinct")){
			s.deleteRelation("distinct");
		}
	}

	public Relation cross_join(List<String> tablelist, List<String> conditionlist, int condition_flag){
		//-----------carry out cross process, nested with con_apply and pi function to get final result 
				int num = tablelist.size();
				 if (num ==1){
					 //--------only one table, just return the table
					 //return s.getRelation(tablelist.get(0));
					 Relation table = s.getRelation(tablelist.get(0));
					 Block block_reference=mem.getBlock(0); //access to memory block 0
					 block_reference.clear();
					 Relation cross = this.s.createRelation("cross_join", table.getSchema());
					 for (int i = 0; i < table.getNumOfBlocks();i++){
					    	
						    // read block i of talbe1 into memory block 0
						    table.getBlock(i, 0);
						    block_reference=mem.getBlock(0);
						    for(int j = 0; j < block_reference.getNumTuples(); j ++){
						    	Tuple tuple = block_reference.getTuple(j);
						    	if(condition_flag == 1){
						    		if(apply_cons(tuple,conditionlist)){
						    			//write to new relation
						    			appendTupleToRelation(cross, mem, 0, tuple);
						    		}
						    	}
						    	else{
						    		appendTupleToRelation(cross, mem, 0, tuple);
						    	}
						    }

					 }
					 return cross;
				 }
				 else{	
					 	//-------------cross for 2 tables----------------------
					 	// get 2 tables
					 	Relation table1 = s.getRelation(tablelist.get(0));
					 	Relation table2 = s.getRelation(tablelist.get(1));
					    // get  field name and type for each table
					    ArrayList<String> field_names1= table1.getSchema().getFieldNames();
					    ArrayList<FieldType> field_types1= table1.getSchema().getFieldTypes();
					    ArrayList<String> field_names2= table2.getSchema().getFieldNames();
					    ArrayList<FieldType> field_types2= table2.getSchema().getFieldTypes();
					    
					    // initialize field name and type for output table
					    ArrayList<String> cross_field_name = new ArrayList<String>();
					    ArrayList<FieldType> cross_field_type =new ArrayList<FieldType>();
					   
					    
					    // deal with common field names in two tables
					    for (int i = 0; i< field_names1.size();i++){
					    	for (int j = 0; j< field_names2.size();j++){

					    		if (field_names1.get(i).equals(field_names2.get(j) )){
					    			field_names1.set(i,tablelist.get(0)+ "."+field_names1.get(i));
					    			field_names2.set(j,tablelist.get(1)+ "."+field_names2.get(j));
					    		}
					    	}
					    }
					    
					    // get field_name, field_type for the cross output
					    cross_field_name.addAll(field_names1);
					    cross_field_name.addAll(field_names2);
					    cross_field_type.addAll(field_types1);
					    cross_field_type.addAll(field_types2);
					    // get schema for crossed intermediate table
					    Schema schema=new Schema(cross_field_name,cross_field_type);
					    // create relation for intermediate table
					    Relation cross = s.createRelation("cross_join", schema);
					    
					    
					    // Set up two blocks in the memory each block[0] for table1, block[1] for table2, use nested loop algorithm
					    Block block_reference=mem.getBlock(0); //access to memory block 0
					    block_reference.clear();
				    	Block block_reference1=mem.getBlock(1); //access to memory block 0
					    block_reference1.clear();
					    
					    // outer loop for reading table1
					    for (int i = 0; i < table1.getNumOfBlocks();i++){
					    	
						    // read block i of talbe1 into memory block 0
						    table1.getBlock(i, 0);
						    
						    // inner loop for reading table2
					    	for (int j = 0; j < table2.getNumOfBlocks(); j++){

							    // read block j of talbe2 into memory block 1
							    table2.getBlock(j, 1);
							    block_reference=mem.getBlock(0);
							    block_reference1=mem.getBlock(1);
							    
							    //get how many tuples exist in a block for table 1 and table2
							    int num_tuple = block_reference.getNumTuples();
							    int num_tuple1 = block_reference1.getNumTuples();
							    
							    
							    
							    for (int m = 0; m< num_tuple; m++){
							    	for(int n =0; n < num_tuple1; n++){
							    		// cross each tuple from one table with tuples in the other table
							    		Tuple t1 = block_reference.getTuple(m);
									    Tuple t2 = block_reference1.getTuple(n);
									    
									    // create crossed new tuple
									    Tuple tuple = cross.createTuple();
									    // first append values from tuple1 to the new tuple
									    for (int a =0; a< t1.getNumOfFields();a++){
									    	//convert to proper fieldType 
									    	if (field_types1.get(a).toString()=="INT"){
									    		tuple.setField(a, t1.getField(a).integer );
									    	}
									    	else{
									    		tuple.setField(a, t1.getField(a).str);
									    	}
									    }
									    // then append values from tuple2 to the new tuple
									    
									    for (int a =0; a< t2.getNumOfFields();a++){
									    	//convert to proper fieldType 
									    	 
									    	if (field_types2.get(a).toString()=="INT"){
									    		tuple.setField(a+t1.getNumOfFields(), Integer.parseInt(t2.getField(a).toString()));
									    	}
									    	else{
									    		tuple.setField(a+t1.getNumOfFields(), t2.getField(a).toString());
									    	}
									    }
									    // apply sigma and pi
									    if(condition_flag == 1){
								    		if(apply_cons(tuple,conditionlist)){
								    			//write to new relation
								    			appendTupleToRelation(cross, mem, 9, tuple);
								    		}
								    	}
								    	else{
								    		appendTupleToRelation(cross, mem, 9, tuple);
								    	}  
							    	}
							    }   
							    
					    	}
					    }
					    return cross;
				 	}
			}
	public void delete(MainMemory mem, SchemaManager s,ParseTree t){
		
		List<String> conditionlist = new ArrayList<String>();
		Relation r = s.getRelation(t.getChildren().get(0).getSymbol());
		if (t.getChildren().size()==1){			
			r.deleteBlocks(0);
		}
		else{
			
			//get conditionlist
			for (int i = 0; i < t.getChildren().get(1).getChildren().size(); i++){
				conditionlist.add(t.getChildren().get(1).getChildren().get(i).getSymbol());
			}
	
			int num_blocks = r.getNumOfBlocks();
			int batch = (int) Math.ceil(num_blocks/10.0);
			int left = num_blocks % 10;
			
			
			for (int i = 0; i< batch; i++){
				if (10*i+10 <= num_blocks){
					int read_blocks = 10;
					// read from disk
					r.getBlocks(10*i, 0, read_blocks);
					for (int j = 0; j<read_blocks; j++){
						Block block_reference = mem.getBlock(j);
						int num_tuples = block_reference.getNumTuples();
						for (int x = 0; x < num_tuples; x++){
							Tuple tuple = block_reference.getTuple(x);
							
							if (apply_cons(tuple,conditionlist)){
								block_reference.invalidateTuple(x);
							}
						}
						
					}
					// write back to disk
					r.setBlocks(10*i, 0, read_blocks);
				}
				else{
					
					int read_blocks = left;
					// read from disk
					r.getBlocks(10*i, 0, read_blocks);
					for (int j = 0; j<read_blocks; j++){
						Block block_reference = mem.getBlock(j);
						int num_tuples = block_reference.getNumTuples();
						for (int x = 0; x < num_tuples; x++){
							Tuple tuple = block_reference.getTuple(x);
							if (apply_cons(tuple,conditionlist)){
								block_reference.invalidateTuple(x);
							}
						}
						
					}
					// write back to disk
					r.setBlocks(10*i, 0, read_blocks);
				}
			}
		}
	}
	
	public static void delete_dublicate(MainMemory mem, SchemaManager s, Relation r){
			
		int num_blocks = r.getNumOfBlocks();
		int batch = (int) Math.ceil(num_blocks/10.0);
		int left = num_blocks % 10;
		Set<Integer> set = new HashSet<Integer>();
		
		for (int i = 0; i< batch; i++){
			if (10*i+10 <= num_blocks){
				int read_blocks = 10;
				// read from disk
				r.getBlocks(10*i, 0, read_blocks);
				for (int j = 0; j<read_blocks; j++){
					Block block_reference = mem.getBlock(j);
					int num_tuples = block_reference.getNumTuples();
					for (int x = 0; x < num_tuples; x++){
						Tuple tuple = block_reference.getTuple(x);
						if(set.contains(tuple.getField("distinct").integer)){
							block_reference.invalidateTuple(x);
						}
						set.add(tuple.getField("distinct").integer);
					}
					
				}
				// write back to disk
				r.setBlocks(10*i, 0, read_blocks);
			}
			else{
				
				int read_blocks = left;
				// read from disk
				r.getBlocks(10*i, 0, read_blocks);
				for (int j = 0; j<read_blocks; j++){
					Block block_reference = mem.getBlock(j);
					int num_tuples = block_reference.getNumTuples();
					for (int x = 0; x < num_tuples; x++){
						Tuple tuple = block_reference.getTuple(x);
						if(set.contains(tuple.getField("distinct").integer)){
							block_reference.invalidateTuple(x);
						}
						set.add(tuple.getField("distinct").integer);
					}
					
				}
				// write back to disk
				r.setBlocks(10*i, 0, read_blocks);
				
			}
		}
	}
	public static void write2file(String s){
		try {
	           FileWriter fileWriter =
	               new FileWriter("out.txt", true);
	           BufferedWriter bufferedWriter =
	               new BufferedWriter(fileWriter);
	           bufferedWriter.write(s);
	           bufferedWriter.newLine();
	           bufferedWriter.close();
	       }catch(IOException ex) {}
	}
}