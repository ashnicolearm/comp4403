package parser;

import source.Position;

/**
 * class NumberToken - Number token needs an integer value
 * @version $Id: NumberToken.java 8 2013-02-22 06:25:04Z ianh $
 */ 
public class NumberToken extends LexicalToken {

    private int intValue;

    /** Construct a token with the given type, position and integer value. 
     * @param type should always be NUMBER.
     */
    public NumberToken( Token type, Position posn, int intValue ) {
        super(type,posn);
        this.intValue = intValue;
    }
    /** Extract integer value of NUMBER token */
    public int getIntValue( ) {
    return intValue;
    }
    /** @return a human readable string representation of the token */
    public String toString() {
        return ((Integer)intValue).toString();
    }
}
