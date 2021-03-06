package pl0;
import java.io.IOException;

import parser.Parser;
import parser.Scanner;
import source.ErrorHandler;
import source.Errors;
import source.Source;
import tree.CodeGenerator;
import tree.StaticChecker;
import tree.Tree;
import tree.Code;
import machine.StackMachine;

/** 
 * class PL0-RD - PL0 Compiler with recursive descent parser.
 * @version $Id: PL0_RD.java 8 2013-02-22 06:25:04Z ianh $
 * Parses the command line arguments, and then compiles and/or executes any
 * available code.
 */
public class PL0_RD {

    public static void usage() {
        System.out.println(
            "PL0 Compiler\n" +
            "Usage: java pl0.PL0_RD [-cdhptv] <filename>\n"+
            "  -c  =  compile only (no execution)\n" +
            "  -d  =  debug parse\n" +
            "  -h  =  output this usage information\n" +
            "  -p  =  parse only (implies -c)\n" +
            "  -t  =  trace execution of resulting code\n" +
            "  -v  =  verbose output of generated code\n" +
            " <filename> is compiled, and if no errors the generated code" +
            " is executed unless -c or -p is given." );
    }
    public static String SourceSuffix = ".pl0";

    /** PL0 main procedure */
    public static void main( String args[] ) throws java.lang.Exception {
        /** Name of the input source program file. */
        String srcFile = null;
        /** Error handler for reporting error messages */
        Errors errors;
        /** Input source stream */
        Source src;
        /** Generated code for the program */
        Code code = null;
        /** Perform a static check */
        boolean staticCheck = true;
        /** Execute after successful compile */
        boolean executing = true;
        /** Detailed trace of execution */
        boolean tracing = false;
        /** Verbose output of code generation */
        boolean verbose = false;
        /** Debug mode for parser - quite verbose */
        boolean debugParse = false;

        /* Parse command line */
        for( int i=0; i<args.length; i++ ) {
            if( args[i].charAt(0) == '-' ) { /* Option */
                switch( args[i].charAt(1) ) {
                case 'c': /* Compile only */
                    executing = false;
                    break;
                case 'd': /* Debug parse */
                    debugParse = true;
                    break;
                case 'h': /* Help */
                    usage();
                    System.exit(0);
                    break;
                case 'p': /* Parse only */
                    staticCheck = false;
                    executing = false;
                    break;
                case 't': /* Trace program at runtime. */
                    tracing = true;
                    break;
                case 'v': /* Verbose output (of generated code) */
                    verbose = true;
                    break;
                }
            } else { /* ( args[i].charAt(0) != '-' ) Not Option */
                srcFile = args[i];
            }
        }
        try {
            /* Set up the input source stream for the source file */
            if( srcFile == null ) {
                System.out.println( "No source file specified." );
                System.exit( 1 );
            }
            src = new Source( srcFile );
            /* Set up the error handler reference */
            errors = new ErrorHandler( System.out, src );
            /* Compile the program */
            code = compile( src, errors, verbose, staticCheck, debugParse );
            if( executing && code != null ) { /* run it if possible */
                execute( errors, code, tracing, verbose );
            }
        } catch( IOException e ) {
            System.out.println( "Got IOException: " + e + "... Aborting" );
            System.exit(1);
        }
    }

    /**
     * execute the code
     * @param errors error message handler
     * @param code sequence of instructions to be loaded
     * @param tracing trace instruction execution
     * @param verbose listing of instructions loaded
     */
    private static void execute( Errors errors, Code code, boolean tracing, 
            boolean verbose ) {
        StackMachine machine;
        machine = new StackMachine( errors, verbose, code );
        System.out.println( "Running ..." );
        machine.setTracing( tracing ? StackMachine.TRACE_ALL 
                         : StackMachine.TRACE_NONE );
        machine.run();
    }

    /** Compile the program
     * 
     * @param src program source
     * @param errors handler for errors
     * @param verbose generate more messages during compilation
     * @param staticCheck do the static checking
     * @param debugParse debugging messages during parsing 
     * @return generated code and procedure addresses table
     */
    private static Code compile( Source src, Errors errors, 
            boolean verbose, boolean staticCheck, boolean debugParse ) 
        throws IOException, Exception
    {
        /** Abstract syntax tree returned by parser */
        Tree.ProgramNode tree = null;
        /** Generated code and procedure table (if any) */
        Code code = null;
        /** Object returned by parser */
        Object parseResult; 
        
        System.out.println( "Compiling " + src.getFileName() );
        try {
            /* Set up the lexical analyser using the source program stream */
            Scanner lex = new Scanner( src );
            /** Recursive descent parser */
            Parser parser;
            /* Set up the parser using the lexical analyser */
            parser = new Parser( lex, debugParse );
            parseResult = parser.parse();
            /* Flush any error messages from the parse */
            errors.flush();
            System.out.println( "Parsing complete" );
            if( staticCheck && parseResult != null && 
                    parseResult instanceof Tree.ProgramNode ) {
                tree = (Tree.ProgramNode)parseResult;
                /* Perform the static semantics analysis */
                StaticChecker staticSemantics = 
                    new StaticChecker( ErrorHandler.getErrorHandler() );
                staticSemantics.visitProgramNode( tree );           
                /* Don't generate any code if there are any errors. */
                if( ErrorHandler.getErrorHandler().hadErrors() ) {
                    /* Skip code generation if there were errors */
                    tree = null;
                }
                errors.flush();
                System.out.println( "Static semantic analysis complete" );
            }
        } catch (IOException e) {
            System.out.println( "Exception: " + e + "... Aborting" );
            System.exit(1);
        }
        if( tree != null ) {
            /* Generate the stack machine code */
            CodeGenerator codeGen = new CodeGenerator( errors, verbose );
            code = codeGen.generateCode( (Tree.ProgramNode) tree );
            System.out.println( "Code generation complete" );
        }
        errors.flush();
        errors.errorSummary();
        return code;
    }
}
