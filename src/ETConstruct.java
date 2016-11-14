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
		if (n == 3){
			//node of "sigma"
			List<ExpressionTree> list = new ArrayList<ExpressionTree>();
		    list.add(cross);
			ExpressionTree sigma = new ExpressionTree("sigma",list,parsetree.getChildren().get(2).getChildren());
			
			// node of "pi"
			List<ParseTree> names_for_select = new ArrayList<ParseTree>();
			// get the number of children for "table_list"
			int num = parsetree.getChildren().get(0).getChildren().size();
			//System.out.println(parsetree.getChildren().get(1).getChildren().get(0).getSymbol());
			for(int i = 0; i < num; i++){	
				names_for_select.add(parsetree.getChildren().get(0).getChildren().get(i));
			}
			List<ExpressionTree> list1 = new ArrayList<ExpressionTree>();
		    list1.add(sigma);
			pi = new ExpressionTree("pi",list1,names_for_select);
			System.out.println("*****");
		}
		else{
			// node of "pi"
			List<ParseTree> names_for_select = new ArrayList<ParseTree>();
			// get the number of children for "table_list"
			int num = parsetree.getChildren().get(0).getChildren().size();
			//System.out.println(parsetree.getChildren().get(1).getChildren().get(0).getSymbol());
			for(int i = 0; i < num; i++){	
				names_for_select.add(parsetree.getChildren().get(0).getChildren().get(i));
			}
			List<ExpressionTree> list1 = new ArrayList<ExpressionTree>();
		    list1.add(cross);
			pi = new ExpressionTree("pi",list1,names_for_select);
			System.out.println("&&&&&");
		}		
		return pi;
	}
	
}
