import java.util.ArrayList;
import java.util.List;

public class ETConstruct {
	ParseTree parsetree;
	
	public ETConstruct(ParseTree s){
		this.parsetree = s;
	}
	public ExpressionTree construct(){
		//if(statement.get(0).toLowerCase().equals("select")){
			ExpressionTree t;
			t = select();
			return t;
		//}
	}
	
	//zy: add the order_index
	public ExpressionTree select(){
		//node of "cross"
		ExpressionTree pi = new ExpressionTree("null");
		List<ParseTree> tables_for_cross = new ArrayList<ParseTree>();
		// get the number of children for "table_list"
		int count = parsetree.getChildren().get(1).getChildren().size();
		//System.out.println(parsetree.getChildren().get(1).getChildren().get(0).getSymbol());
		for(int i = 0; i < count; i++){	
			tables_for_cross.add(new ParseTree(parsetree.getChildren().get(1).getChildren().get(i).getSymbol()));
		}
		ExpressionTree cross = new ExpressionTree("cross",null,tables_for_cross);

		
		int n = parsetree.getChildren().size();
		// no order no condition
		if (n ==2){
			// node of "pi"
			List<ParseTree> names_for_select = new ArrayList<ParseTree>();
			// get the number of children for "select_list"
			int num = parsetree.getChildren().get(0).getChildren().size();
			//System.out.println(parsetree.getChildren().get(1).getChildren().get(0).getSymbol());
			for(int i = 0; i < num; i++){	
				names_for_select.add(parsetree.getChildren().get(0).getChildren().get(i));
			}
			List<ExpressionTree> list1 = new ArrayList<ExpressionTree>();
		    list1.add(cross);
		    if (parsetree.getSymbol() == "select"){
		    	pi = new ExpressionTree("pi",list1,names_for_select);
		    }
		    else{
		    	pi = new ExpressionTree("pi_distinct",list1,names_for_select);
		    }
			
		}
		// no order but condition
		else if (n == 3 && parsetree.getChildren().get(2).getSymbol() == "condition_list"){
			// make "cross" into an array, preparing to be a child of upper node
			List<ExpressionTree> list = new ArrayList<ExpressionTree>();
		    list.add(cross);
		    // make "cross" a child of "sigma"
			ExpressionTree sigma = new ExpressionTree("sigma",list,parsetree.getChildren().get(2).getChildren());
			
			// attribute list for "pi"
			List<ParseTree> names_for_select = new ArrayList<ParseTree>();
			// get the number of children for "select_list"
			int num = parsetree.getChildren().get(0).getChildren().size();
			//System.out.println(parsetree.getChildren().get(1).getChildren().get(0).getSymbol());
			for(int i = 0; i < num; i++){	
				names_for_select.add(parsetree.getChildren().get(0).getChildren().get(i));
			}
			// make "sigma" into an array, preparing to be a child of upper node
			List<ExpressionTree> list1 = new ArrayList<ExpressionTree>();
		    list1.add(sigma);
		    if (parsetree.getSymbol() == "select"){
		    	// make "sigma" a child of "pi"
		    	pi = new ExpressionTree("pi",list1,names_for_select);
		    }
		    else{
		    	pi = new ExpressionTree("pi_distinct",list1,names_for_select);
		    }
			System.out.println("*****");
		}
		// no condition but order
		else if (n == 3 && parsetree.getChildren().get(2).getSymbol() == "order"){
			// attribute list for node "pi"
			List<ParseTree> names_for_select = new ArrayList<ParseTree>();
			// get the number of children for "select_list"
			int num = parsetree.getChildren().get(0).getChildren().size();
			//System.out.println(parsetree.getChildren().get(1).getChildren().get(0).getSymbol());
			for(int i = 0; i < num; i++){	
				names_for_select.add(parsetree.getChildren().get(0).getChildren().get(i));
			}
			// construct tree from buttom up, append "cross" first
			List<ExpressionTree> list = new ArrayList<ExpressionTree>();
		    list.add(cross);
		    // make cross a child for "order"
		    ExpressionTree order = new ExpressionTree("order",list,parsetree.getChildren().get(2).getChildren());
			
		    // make "order" into an array (children are an array)
			List<ExpressionTree> list1 = new ArrayList<ExpressionTree>();
		    list1.add(order);
		    
		    if (parsetree.getSymbol() == "select"){
		    	// make "order" children of "pi"
		    	pi = new ExpressionTree("pi",list1,names_for_select);
		    }
		    else{
		    	pi = new ExpressionTree("pi_distinct",list1,names_for_select);
		    }
		}
		// both order and condition
		else{
			
			// make "cross" into an array, preparing to be a child of upper node
			List<ExpressionTree> list = new ArrayList<ExpressionTree>();
		    list.add(cross);
		    // make "cross" a child of "sigma"
			ExpressionTree sigma = new ExpressionTree("sigma",list,parsetree.getChildren().get(2).getChildren());
			
			List<ExpressionTree> list1 = new ArrayList<ExpressionTree>();
		    list1.add(sigma);
		    // make cross a child for "order"
		    ExpressionTree order = new ExpressionTree("order",list1,parsetree.getChildren().get(3).getChildren());
			
		    // make "order" into an array (children are an array)
			List<ExpressionTree> list2 = new ArrayList<ExpressionTree>();
		    list2.add(order);
			
			
			// attribute list for "pi"
			List<ParseTree> names_for_select = new ArrayList<ParseTree>();
			// get the number of children for "select_list"
			int num = parsetree.getChildren().get(0).getChildren().size();
			//System.out.println(parsetree.getChildren().get(1).getChildren().get(0).getSymbol());
			for(int i = 0; i < num; i++){	
				names_for_select.add(parsetree.getChildren().get(0).getChildren().get(i));
			}

			
		    if (parsetree.getSymbol() == "select"){
		    	pi = new ExpressionTree("pi",list2,names_for_select);
		    }
		    else{
		    	pi = new ExpressionTree("pi_distinct",list2,names_for_select);
		    }
		}
		
		return pi;
	}
	
}