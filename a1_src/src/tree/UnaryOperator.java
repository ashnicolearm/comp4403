package tree;
import syms.Type;

/**
 * enumeration UnaryOperator - Unary operators in abstract syntax tree.
 * @version $Id: UnaryOperator.java 8 2013-02-22 06:25:04Z ianh $
 */

public enum UnaryOperator {
    NOT_OP( "!", Type.LOGICAL_UNARY ),
    NEG_OP( "-", Type.ARITH_UNARY );

    /** The name of the unary operator. */
    String name;
    Type type;

    private UnaryOperator( String name, Type type ) {
        this.name = name;
        this.type = type;
    }
    public Type getType() {
        return type;
    }
    public String toString() {
        return name;
    }
}
