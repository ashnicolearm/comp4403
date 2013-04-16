package source;

/** 
 * enumeration Severity - Error message severity types 
 * @version $Id: Severity.java 8 2013-02-22 06:25:04Z ianh $
 */
public enum Severity {
    FATAL( "Fatal" ),
    RESTRICTION( "Restriction" ),
    ERROR( " Error" ),
    WARNING( "Warning" ),
    REPAIR( "Repair" ),
    NOTE( "Note" ),
    INFORMATION( "Information" );
   
    String message;

    private Severity( String message ) {
        this.message = message;
    }
    public String toString() {
        return message;
    }
}
