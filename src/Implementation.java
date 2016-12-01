import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;

import storageManager.*;

public class Implementation {
	ExpressionTree exptree;
	SchemaManager s;
	MainMemory mem;
	
	public Implementation(ExpressionTree t, MainMemory mem, SchemaManager s){
		this.exptree = t;
		this.s = s;
		this.mem = mem;
	}
	
	private static void appendTupleToRelation(Relation relation_reference, MainMemory mem, int memory_block_index, Tuple tuple) {
	    Block block_reference;
	    if (relation_reference.getNumOfBlocks()==0) {
	      //System.out.print("The relation is empty" + "\n");
	      //System.out.print("Get the handle to the memory block " + memory_block_index + " and clear it" + "\n");
	      block_reference=mem.getBlock(memory_block_index);
	      block_reference.clear(); //clear the block
	      block_reference.appendTuple(tuple); // append the tuple
	      //System.out.print("Write to the first block of the relation" + "\n");
	      relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index);
	    } else {
	      //System.out.print("Read the last block of the relation into memory block 5:" + "\n");
	      relation_reference.getBlock(relation_reference.getNumOfBlocks()-1,memory_block_index);
	      block_reference=mem.getBlock(memory_block_index);

	      if (block_reference.isFull()) {
	        //System.out.print("(The block is full: Clear the memory block and append the tuple)" + "\n");
	        block_reference.clear(); //clear the block
	        block_reference.appendTuple(tuple); // append the tuple
	        //System.out.print("Write to a new block at the end of the relation" + "\n");
	        relation_reference.setBlock(relation_reference.getNumOfBlocks(),memory_block_index); //write back to the relation
	      } else {
	        //System.out.print("(The block is not full: Append it directly)" + "\n");
	        block_reference.appendTuple(tuple); // append the tuple
	        //System.out.print("Write to the last block of the relation" + "\n");
	        relation_reference.setBlock(relation_reference.getNumOfBlocks()-1,memory_block_index); //write back to the relation
	      }
	    }
	  }
	
	
	//--------separate implementation: projection, apply_con functions------
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
	
		public static void projection(Tuple tuple,List<String> attributes){
			String out = "";
			Schema s = tuple.getSchema();
			List<String> attri = new ArrayList<String>();
			if(attributes.get(0).equals("*")){
			for(int i = 0; i < s.getNumOfFields(); i++){
			attri.add(s.getFieldName(i));
			}
			}
			else{
			attri = attributes;
			}
			for(int i = 0; i < attri.size(); i++){
			out = out + tuple.getField(attri.get(i)) + " ";
			}
			System.out.println("######## output ######");
			System.out.println(out);
			}
	
	
	public Relation cross(Relation table1, Relation table2, Boolean naturaljoin){
		
	
		 	//-------------cross for 2 tables----------------------
		 	// get 2 tables

		    // get  field name and type for each table
		    //System.out.print("Creating a schema" + "\n");
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
		    			//System.out.println("repeat");
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
		    //System.out.println(cross_field_name);
		    // get schema for crossed intermediate table
		    Schema schema=new Schema(cross_field_name,cross_field_type);
		    // create relation for intermediate table
		    String relation_name="intermediate"+table1.getRelationName()+table2.getRelationName();
		    //System.out.print("&&&&&"+schema);
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
		    //System.out.println("%%%%%%%%%%%%%%%%%% "+ commonattributes);
		    
			// some basic parameters
			int num_block1 = table1.getNumOfBlocks();
			int num_busket1 = (int) Math.ceil(num_block1/5.0);
			int left1 = num_block1 % 5;
			int num_in_busket1 = 0;
			
			int num_block2 = table2.getNumOfBlocks();
			int num_busket2 = (int) Math.ceil(num_block2/5.0);
			int left2 = num_block2 % 5;
			int num_in_busket2 = 0;
			//System.out.println("test ceil"+num_busket);
			//System.out.println("tupleperblock "+table2.getNumOfTuples() +"num_block2 "+num_block2+"num_busket2 "+num_busket2);
			
			
			for (int i =0; i< num_busket1;i++){

				//System.out.println("i"+i);
				
				
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
					//System.out.println("j"+j);
				
					
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
					tuples2=mem.getTuples(5,num_in_busket1);
		    
				    
				    for (int m = 0; m< tuples1.size(); m++){
				    	//System.out.println("m"+m);
				    	for(int n =0; n < tuples2.size(); n++){
				    		//System.out.println("n"+n);
				    		// cross each tuple from one table with tuples in the other table
				    		Tuple t1 = tuples1.get(m);
						    Tuple t2 = tuples2.get(n);
						    //System.out.println(t1.getField("b").integer==t2.getField("b").integer);
						    
						    Boolean naturalcontain = true;
						    for (int c = 0; c < commonattributes.size(); c++ ){
						    	if (t1.getField(commonattributes.get(c)).integer != t2.getField(commonattributes.get(c)).integer){
						    		naturalcontain = false;
						    		
						    	}
						    	//System.out.println(t1.getField(commonattributes.get(c)).integer+" "+t2.getField(commonattributes.get(c)).integer+naturalcontain);
						    }
						    
						    
						    if (!(naturaljoin == true && naturalcontain == false)){
						    	
						    
						    	Tuple tuple = relation_reference.createTuple();
							    //System.out.println("tuple-0:"+tuple);
							    // first append values from tuple1 to the new tuple
							    for (int a =0; a< t1.getNumOfFields();a++){
							    	//convert to proper fieldType 
							    	if (field_types1.get(a).toString()=="INT"){
							    		tuple.setField(a, t1.getField(a).integer );
							    		 //System.out.println("tuple-1:"+tuple);
							    	}
							    	else{
							    		tuple.setField(a, t1.getField(a).str);
							    		 //System.out.println("tuple-2:"+tuple);
							    	}
							    }
							    // then append values from tuple2 to the new tuple
							    
							    for (int a =0; a< t2.getNumOfFields();a++){
							    	//System.out.println("getfield"+t2.getField(a));
							    	//convert to proper fieldType 
							    	 
							    	if (field_types2.get(a).toString()=="INT"){
							    		tuple.setField(a+t1.getNumOfFields(), Integer.parseInt(t2.getField(a).toString()));
							    		//System.out.println("tuple-3:"+tuple);
							    	}
							    	else{
							    		tuple.setField(a+t1.getNumOfFields(), t2.getField(a).toString());
							    		//System.out.println("tuple-4:"+tuple);
							    	}
							    }
							    //System.out.println(tuple);
							    // append new tuple to the crossed output relation
							    appendTupleToRelation(relation_reference, mem, 5,tuple);
							    //System.out.println(relation_reference);
							    //System.out.println("-----------for test--------");
							    //System.out.println(relation_reference);
						    }
						    
						    
						    
				    	}
				    }
				    
		    	}
		    }    	 
	        return relation_reference;
		
	}
	
	public void select_cross(MainMemory mem, SchemaManager s, ExpressionTree t){
		
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
		//System.out.println("table: "+tablelist);
		//System.out.println("attribute: "+attributelist);
		//System.out.println("condition : "+conditionlist);
	
		//-----------carry out cross process, nested with con_apply and pi function to get final result 
		int num = tablelist.size();
		if (num ==1){
			 //--------only one table, just return the table
			 //return s.getRelation(tablelist.get(0));
			 Relation table = s.getRelation(tablelist.get(0));
			 
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
						    	projection(tuple,attributelist);
						    }
						    else if (apply_cons(tuple,conditionlist)){
						    	projection(tuple,attributelist);
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
			    //System.out.print("Creating a schema" + "\n");
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
			    			//System.out.println("repeat");
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
			    //System.out.println(cross_field_name);
			    // get schema for crossed intermediate table
			    Schema schema=new Schema(cross_field_name,cross_field_type);
			    // create relation for intermediate table
			    String relation_name="intermediate";
			    //System.out.print("Creating table " + relation_name + "\n");
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
				    //System.out.println(mem);
				    
				    // inner loop for reading table2
			    	for (int j = 0; j < table2.getNumOfBlocks(); j++){

					    // read block j of talbe2 into memory block 1
					    table2.getBlock(j, 1);
					    block_reference=mem.getBlock(0);
					    block_reference1=mem.getBlock(1);
					    //System.out.println(block_reference.getTuple(0)+"<<<<<<<"); 
					    
					    //get how many tuples exist in a block for table 1 and table2
					    int num_tuple = block_reference.getNumTuples();
					    int num_tuple1 = block_reference1.getNumTuples();
					    
					    
					    
					    for (int m = 0; m< num_tuple; m++){
					    	for(int n =0; n < num_tuple1; n++){
					    		// cross each tuple from one table with tuples in the other table
					    		Tuple t1 = block_reference.getTuple(m);
							    Tuple t2 = block_reference1.getTuple(n);
							    
							    //System.out.println("i,j,m,n"+i+" "+j+" "+m+" "+n+" ");
							    // create crossed new tuple
							    Tuple tuple = relation_reference.createTuple();
							    //System.out.println("tuple-0:"+tuple);
							    // first append values from tuple1 to the new tuple
							    for (int a =0; a< t1.getNumOfFields();a++){
							    	//convert to proper fieldType 
							    	if (field_types1.get(a).toString()=="INT"){
							    		tuple.setField(a, t1.getField(a).integer );
							    		 //System.out.println("tuple-1:"+tuple);
							    	}
							    	else{
							    		tuple.setField(a, t1.getField(a).str);
							    		 //System.out.println("tuple-2:"+tuple);
							    	}
							    }
							    // then append values from tuple2 to the new tuple
							    
							    for (int a =0; a< t2.getNumOfFields();a++){
							    	//System.out.println("getfield"+t2.getField(a));
							    	//convert to proper fieldType 
							    	 
							    	if (field_types2.get(a).toString()=="INT"){
							    		tuple.setField(a+t1.getNumOfFields(), Integer.parseInt(t2.getField(a).toString()));
							    		//System.out.println("tuple-3:"+tuple);
							    	}
							    	else{
							    		tuple.setField(a+t1.getNumOfFields(), t2.getField(a).toString());
							    		//System.out.println("tuple-4:"+tuple);
							    	}
							    }
							    
							    // append new tuple to the crossed output relation
							    //appendTupleToRelation(relation_reference, mem, 5,tuple); 
							    //System.out.println("-----------for test--------");
							    //System.out.println(relation_reference);
							    
							    
							    // apply sigma and pi
							    if (conditionlist.size() == 0){
							    	projection(tuple,attributelist);
							    }
							    else if (apply_cons(tuple,conditionlist)){
							    	projection(tuple,attributelist);
							    }    
					    	}
					    }
					    
			    	}
			    }    	 
		 }
		 else{
			 // get list of relations sorted by size
			 //System.out.println("t1"+s.getRelation("t1"));
			 //System.out.println("#of blocks"+s.getRelation("t1").getNumOfBlocks());
			 //System.out.println("#of tuples"+s.getRelation("t1").getNumOfTuples());
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
					//System.out.println("relation"+relations);
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
						projection(tuple,attributelist);
					}
					else if (apply_cons(tuple,conditionlist)){
						projection(tuple,attributelist);
					}
				 }
					  			    
			}
			 
			 
			 
		 }
	}
	

		
	

	
}