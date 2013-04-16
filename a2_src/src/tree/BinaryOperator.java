package tree;

/**
 * enumeration BinaryOperator - Binary operators in abstract syntax tree.
 * @version $Id: BinaryOperator.java 8 2013-02-22 06:25:04Z ianh $
 */
public enum BinaryOperator {
    ADD_OP( "+" ),
    SUB_OP( "-" ),
    MUL_OP( "*" ),
    DIV_OP( "/" ),
    EQUALS_OP( "=" ),
    NEQUALS_OP( "!=" ),
    GREATER_OP( ">" ),
    LESS_OP( "<" ),
    LEQUALS_OP( "<=" ),
    GEQUALS_OP( ">=" ),

    INVALID_OP( "INVALID" );
    
    /** The name of the binary operator */
    String name;
    
    private BinaryOperator( String name ) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
