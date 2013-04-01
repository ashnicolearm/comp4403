package parser;

import source.Position;

/**
 * class IdentifierToken - Identifier token needs an identifier name
 * @version $Id: IdentifierToken.java 8 2013-02-22 06:25:04Z ianh $
 */ 
public class IdentifierToken extends LexicalToken {

    private String name;

    /** Construct a token with the given type, position and string value. 
     * @param type should normally be IDENTIFIER.
     */
    public IdentifierToken( Token type, Position posn, String name ) {
        super(type,posn);
        this.name = name;
    }
    /** Extract name of IDENTIFIER token */
    public String getName( ) {
        return name;
    }
    /** @return a human readable string representation of the token */
    public String toString() {
        return name;
    }
}
