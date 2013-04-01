package parser;

import source.Position;

/**
 * LexicalToken - Defines the basic tokens returned from the lexical analyser.
 * @version $Id: LexicalToken.java 8 2013-02-22 06:25:04Z ianh $
 *   Arguably should be an inner class of Scanner, but is a little
 *   easier this way. Also gets heavily used by Parser.
 */

public class LexicalToken {

    private Token kind;
    private Position posn; /* The position of the first char of the token */

/****************** Constructors ********************/

    /** Construct a token with the given type and position. 
     * @param kind Type of the lexical token
     * @param posn Position in the source input file
     */
    public LexicalToken( Token kind, Position posn ) {
        this.kind = kind;
        this.posn = posn;
    }

/****************** Public Methods ******************/

    /** Extract the type of a token */
    public Token getKind( ) {
        return kind;
    }

    /** Extract the position of a token */
    public Position getPosn( ) {
        return posn;
    }
    
    /** Test if the type of the token matches the argument 
     * @param expected Type to be matched
     */ 
    public boolean isMatch( Token kind ) {
        return this.kind == kind;
    }
    /** Don't want to accidentally use equals instead of isMatch */
    public boolean equals( Object o ) {
        System.out.println( 
            "Fatal internal error: Use isMatch to compare token kind" );
        System.exit(1);
        return false;
    }
    
    /** Test if the token is contained in the given set of token types
     * @param tokenTypes set of tokens to test against
     * @return true iff in the set
     */
    public boolean isIn( TokenSet tokenTypes ) {
        return tokenTypes.contains( this.kind );
    }

    /* Virtual extract integer value of INTEGER token */
    public int getIntValue( ) {
        System.out.println("Internal error: call on getIntValue on a Token");
        System.exit(1);
        return 0;
    }

    /* Virtual extract name of IDENTIFIER token */
    public String getName( ) {
        System.out.println("Internal error: call on getName on a Token");
        System.exit(1);
        return null;
    }

    /* return a human readable string representation of the token */
    public String toString() {
        return kind.toString();
    }

}
