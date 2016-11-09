import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExpressionTree implements Iterable<ExpressionTree> {
    /** The grammar symbol represented by this node of the tree. */
    public final String symbol;

    /** The children of this parse tree node, in the order in which they appear */
    public final List<ExpressionTree> children;
    
    public final List<ParseTree> attribute;

    /**
     * Constructs a new parse tree wrapping the given symbol with the given
     * children.
     *
     * @param symbol The symbol at this parse tree node.
     * @param children The children of this parse tree node.
     */
    public ExpressionTree(String symbol, List<ExpressionTree> children, List<ParseTree> attribute) {
        if (symbol == null || children == null )
            throw new NullPointerException();

        this.symbol = symbol;
        this.children = children;
        this.attribute = attribute;
    }

    /**
     * Constructs a new parse tree node holding the given symbol, but with
     * no children.
     *
     * @param symbol The symbol held by this parse tree node.
     */
    public ExpressionTree(String symbol) {
        this(symbol, new ArrayList<ExpressionTree>(), new ArrayList<ParseTree>());
    }

    /**
     * Returns the symbol held by this parse tree node.
     *
     * @return The symbol held by this parse tree node.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns a mutable view of the children of this parse tree node.
     *
     * @return A mutable view of the children of this parse tree node.
     */
    public List<ExpressionTree> getChildren() {
        return children;
    }
    
    public List<ParseTree> getAttribute() {
        return attribute;
    }

    /**
     * Returns a mutable iterator to traverse the children of this parse tree.
     *
     * @return A mutable iterator traversing the children of this node.
     */
    public Iterator<ExpressionTree> iterator() {
        return getChildren().iterator();
    }
}