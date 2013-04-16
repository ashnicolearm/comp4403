package tree;
import syms.Type;

/**
 * enumeration UnaryOperator - Unary operators in abstract syntax tree.
 * @version $Id: UnaryOperator.java 10 2013-04-15 23:26:08Z uqihayes $
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
    @Override
    public String toString() {
        return name;
    }
}
