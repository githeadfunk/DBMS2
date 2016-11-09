import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
public class PTConstruct {
	
	ArrayList<String> statement;
	
	public PTConstruct(ArrayList<String> s){
		this.statement = s;
	}
	
	public ParseTree construct(){
		ParseTree t;
		switch(this.statement.get(0).toLowerCase()){
		case "select": t = select();break;
		case "create": t = create();break;
		case "insert": t = insert();break;
		default: t = create();
		}
		return t;
	}
	/*
	public ParseTree select(){
		int count = 0;
		int from_index = 0, where_index = 0;
		for(String s : this.statement){
	    	if(s.toLowerCase().equals("from")){
	    		from_index = count;
	    	}
	    	if(s.toLowerCase().equals("where")){
	    		where_index = count;
	    	}
	    	count += 1;
	    }
		//construct attributes list
		ArrayList<ParseTree> attributes = new ArrayList<ParseTree>();
		for(int i = 1; i < from_index; i++){
			if(!this.statement.get(i).equals(",")){
				attributes.add(new ParseTree(this.statement.get(i)));
			}
		}
		ParseTree attri_list = new ParseTree("attri_list", attributes);
		
		//construct tables list
		ArrayList<ParseTree> tables = new ArrayList<ParseTree>();
		for(int i = from_index + 1; i < where_index; i++){
			if(!this.statement.get(i).equals(",")){
				tables.add(new ParseTree(this.statement.get(i)));
			}
		}
		ParseTree table_list = new ParseTree("table_list", tables);
		
		//construct conditions list
		ArrayList<ParseTree> conditions = new ArrayList<ParseTree>();
		for(int i = where_index + 1; i < this.statement.size(); i++){
			if(!this.statement.get(i).equals(",")){
				conditions.add(new ParseTree(this.statement.get(i)));
			}
		}
		ParseTree condition_list = new ParseTree("condition_list", conditions);
		
		//construct whole ParseTree
		ArrayList<ParseTree> root_list = new ArrayList<ParseTree>();
		root_list.add(attri_list);
		root_list.add(table_list);
		root_list.add(condition_list);
		ParseTree root = new ParseTree("select", root_list);
		return root;
	}
	*/
	
	public ParseTree select(){
		int count = 0;
		int from_index = 0, where_index = 0, order_index = 0;
		for(String s : this.statement){
	    	if(s.toLowerCase().equals("from")){
	    		from_index = count;
	    	}
	    	if(s.toLowerCase().equals("where")){
	    		where_index = count;
	    	}
	    	if(s.toLowerCase().equals("order")){
	    		order_index = count;
	    	}
	    	count += 1;
	    }
		//construct attributes list
		List<ParseTree> attributes = new ArrayList<ParseTree>();
		
		// zy: avoid disruption of ",", "(", ")"
		List valid = Arrays.asList(",", "(", ")");
		for(int i = 1; i < from_index; i++){
			if(!valid.contains(this.statement.get(i))){
				attributes.add(new ParseTree(this.statement.get(i)));
				//System.out.println("arraylist element:"+this.statement.get(i));
			}
		}
		
		ParseTree attri_list = new ParseTree("attri_list", attributes);
		
		//construct tables list
		List<ParseTree> tables = new ArrayList<ParseTree>();
		int end_index = 0;
		if (where_index == 0){
			end_index = this.statement.size()-1;
		}
		else{
			end_index = where_index;
		}
		for(int i = from_index + 1; i < end_index; i++){
			if(!this.statement.get(i).equals(",")){
				tables.add(new ParseTree(this.statement.get(i)));
			}
		}
		ParseTree table_list = new ParseTree("table_list", tables);
		
		//construct conditions list
		ParseTree condition_list = new ParseTree("null");
		if ( where_index != 0){
			List<ParseTree> conditions = new ArrayList<ParseTree>();				
			List valid1 = Arrays.asList("AND","OR");		
			// index for the condition loop
			int searchindex = 0;
			if (order_index == 0){
				searchindex = this.statement.size();
			}
			else{
				searchindex = order_index;
			}		
			// zy: indicate the index of the last "AND", "OR"
			int last =  where_index ; 		
			// zy: when meet "AND" "OR" extract concessitive 3 elements as a tree of subcondition, "AND""OR" goes to condition tree
			for(int i = where_index + 1; i < searchindex; i++){
				if(valid1.contains(this.statement.get(i))){
					List<ParseTree> subcondition = new ArrayList<ParseTree>(); 
					for (int j = last+1; j < i; j++ ){					
						subcondition.add(new ParseTree(this.statement.get(j)));
						System.out.println("subcondtion:"+ this.statement.get(j));				
					}
					last = i;
					ParseTree subcondition_list = new ParseTree("subcondition_list", subcondition);
					conditions.add(subcondition_list);
					conditions.add(new ParseTree(this.statement.get(i)));							
				}
			}	
			// add the last subcondition after the last AND OR
			List<ParseTree> subcondition = new ArrayList<ParseTree>(); 
			for (int k = last+1; k< searchindex-1; k++){
				subcondition.add(new ParseTree(this.statement.get(k)));
				System.out.println("subcondtion:"+ this.statement.get(k));		
			}
			ParseTree subcondition_list = new ParseTree("subcondition_list", subcondition);
			conditions.add(subcondition_list);
			
			// construct condition_list ParseTree, if 5 children, then 2 AND OR, 2 subcondition
			condition_list = new ParseTree("condition_list", conditions);
		}
		
		
		//construct whole ParseTree
		List<ParseTree> root_list = new ArrayList<ParseTree>();
		ParseTree root = new ParseTree("null");
		if (where_index !=0){
			root_list.add(attri_list);
			root_list.add(table_list);
			root_list.add(condition_list);
            root = new ParseTree("select", root_list);
		}
		else{
			root_list.add(attri_list);
			root_list.add(table_list);
			root = new ParseTree("select", root_list);
		}
		return root;
	}
	
	public ParseTree create(){
		List<String> s = new ArrayList<String>();
		for(int i = 0; i < this.statement.size(); i++){
			switch(this.statement.get(i).toLowerCase()){
			case "create": {break;}
			case "table": {break;}
			case "(": {break;}
			case ")": {break;}
			case ";": {break;}
			case ",": {break;}
			case "\"": {break;}
			default: s.add(this.statement.get(i));
			}
		}
		ParseTree rel_name = new ParseTree(s.get(0));
		ParseTree field_name = new ParseTree("field_name");
		ParseTree field_type = new ParseTree("field_type");
		for(int i = 1; i < s.size(); i++){
			if(i % 2 == 1){
				field_name.children.add(new ParseTree(s.get(i)));
			}
			else{
				field_type.children.add(new ParseTree(s.get(i)));
			}
		}
		ParseTree t = new ParseTree("create");
		t.children.add(rel_name);
		t.children.add(field_name);
		t.children.add(field_type);
		return t;
	}
	
	
	
	public ParseTree insert(){
		int no_attribute = 0; 
		if(this.statement.get(3).toLowerCase().equals("values")){
			no_attribute = 1;
		}
		List<String> s = new ArrayList<String>();
		for(int i = 0; i < this.statement.size(); i++){
			switch(this.statement.get(i).toLowerCase()){
			case "insert": {break;}
			case "into": {break;}
			case "values": {break;}
			case "(": {break;}
			case ")": {break;}
			case ";": {break;}
			case ",": {break;}
			case "\"": {break;}
			default: s.add(this.statement.get(i));
			}
		}
		ParseTree rel_name = new ParseTree(s.get(0));
		ParseTree field_name = new ParseTree("field_name");
		ParseTree field_type = new ParseTree("field_type");
		for(int i = 1; i < s.size(); i++){
			if(no_attribute == 0 && i <= s.size() / 2 ){
				field_name.children.add(new ParseTree(s.get(i)));
			}
			else{
				field_type.children.add(new ParseTree(s.get(i)));
			}
		}
		ParseTree t = new ParseTree("insert");
		t.children.add(rel_name);
		t.children.add(field_name);
		t.children.add(field_type);
		return t;
	}
}
