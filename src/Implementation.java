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
	
	
	//--------seperate implementation: pi, sigma, union------
	public Relation pi(MainMemory mem, SchemaManager s, Relation r, List<ParseTree> attributes){
		System.out.println("enter pi");
		System.out.println(r);
		
		System.out.print(mem + "\n" + "\n");
		// used for output
		//System.out.print(block_reference.getTuple(0)）；
		//ArrayList<Tuple> tuples=block_reference.getTuples();
		//for (int i=0;i<tuples.size();i++)
		//    System.out.print(tuples.get(i).toString()+"\n");
		return r;
	}
	
	public Relation sigma(MainMemory mem, SchemaManager s, Relation r, List<ParseTree> attributes){
		return r;
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
		System.out.println("table: "+tablelist);
		System.out.println("attribute: "+attributelist);
		System.out.println("condition : "+conditionlist);
	
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
				    Tuple tuple = block_reference.getTuple(0);
				    
				    
			 }
		 }
		 else{	
			 	//-------------cross for 2 tables----------------------
			 	// get 2 tables
			 	Relation table1 = s.getRelation(tablelist.get(0));
			 	Relation table2 = s.getRelation(tablelist.get(1));
			    // get  field name and type for each table
			    System.out.print("Creating a schema" + "\n");
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
			    			System.out.println("repeat");
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
			    System.out.println(cross_field_name);
			    // get schema for crossed intermediate table
			    Schema schema=new Schema(cross_field_name,cross_field_type);
			    // create relation for intermediate table
			    String relation_name="intermediate";
			    System.out.print("Creating table " + relation_name + "\n");
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
					    
					    //get 2 tuples from each table from memory
					    Tuple t1 = block_reference.getTuple(0);
					    Tuple t2 = block_reference1.getTuple(0);
					    
					    
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
					    	//System.out.println("getfield"+t2.getField(a));
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
					    //System.out.println("-----------for test--------");
					    //System.out.println(relation_reference);
					    
					    //break;
					    
					    
			    	}
			    	//break;
			    }    
			    
			 
		 }
	}
	
	//-----implement the logic query from buttom to the top------------------------
	
}