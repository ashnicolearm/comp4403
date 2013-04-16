package source;

/**
 * interface Errors - interface to allow reporting of compilation
 *      errors and other messages. Use flush() to cause output.
 * @version $Id: Errors.java 8 2013-02-22 06:25:04Z ianh $
 */
public interface Errors {

    /** report an error with the given severity and source position */
    public void errorMessage( CompileError e );
    
    /** report an error with the given severity and source position */
    public void errorMessage( String message, Severity severity, Position pos );

    /** report an error with the given severity. */
    public void errorMessage(String message, Severity severity);
    
    /** report an error */
    public void errorMessage( String message);

    /** Print all pending messages immediately */
    public void listMessages();

    /** Print immediately a summary of all errors reported */
    public void errorSummary();

    /** List impending error messages, and clear accumulated errors. */
    public void flush();

    /** Return whether any errors have been reported at all */
    public boolean hadErrors();
    
}
