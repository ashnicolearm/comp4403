package parser;

/** enumeration Token - Defines the basic tokens returned from the lexical analyzer.
 * @version $Id: Token.java 8 2013-02-22 06:25:04Z ianh $
 */
public enum Token {
    EOF( "End-of-file"),
    PLUS( "+" ),
    MINUS( "-" ),
    TIMES( "*"),
    DIVIDE( "/" ),
    LPAREN( "(" ),
    RPAREN( ")" ),
    SEMICOLON( ";" ),
    COLON( ":" ),
    ASSIGN( ":=" ),
    COMMA( "," ),
    RANGE( ".." ),
    EQUALS( "=" ),
    NEQUALS( "!=" ),
    LEQUALS( "<=" ),
    LESS( "<" ),
    GEQUALS( ">=" ),
    GREATER( ">" ),
    LOG_AND( "&&" ),
    LOG_OR( "||" ),
    LOG_NOT( "!" ),
    BAR( "|" ),
    SEPARATOR( "[]"),
    KW_BEGIN( "begin" ),
    KW_CALL( "call" ),
    KW_CONST( "const" ),
    KW_DO( "do" ),
    KW_ELSE( "else" ),
    KW_END( "end" ),
    KW_EXIT( "exit" ),
    KW_IF( "if" ),
    KW_OD( "od" ),
    KW_PROCEDURE( "procedure" ),
    KW_READ( "read" ),
    KW_SKIP( "skip" ),
    KW_THEN( "then" ),
    KW_TYPE( "type" ),
    KW_VAR( "var" ),
    KW_WHILE( "while" ),
    KW_WRITE( "write" ),
    IDENTIFIER( "identifier" ),
    NUMBER( "number" ),
    ILLEGAL( "illegal" );
    
    /** The name of the token */
    String name;
    
    private Token( String name ) {
        this.name = name;
    }
    public String toString() {
        return name;
    }
}
