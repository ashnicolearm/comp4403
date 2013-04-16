package tree;

import java.util.LinkedList;
import java.util.List;

import source.ErrorHandler;
import source.Severity;
import syms.SymEntry.ProcedureEntry;

/** 
 * class Procedures - code for each procedure and start and finish
 * addresses. Handles a stack trace back for the stack machine
 * in the event of a runtime error.
 * @version $Id: Procedures.java 12 2013-04-15 23:38:49Z uqihayes $
 */
public class Procedures {
    
    private class ProcedureStart {
        ProcedureEntry proc;
        int start, finish;
        
        ProcedureStart( ProcedureEntry proc, int start ) {
            super();
            this.proc = proc;
            this.start = start;
        }
        @Override
        public String toString() {
            return proc.getIdent() + " " + start + " : " + finish;
        }
    }
    private List<ProcedureStart> procStarts;
    
    public Procedures() {
        procStarts = new LinkedList<ProcedureStart>();
    }
    public void addProcedureStart( ProcedureEntry proc, int start ) {
        if( ! procStarts.isEmpty() ) {
            /* check last finish location is current location */
            ProcedureStart previous = procStarts.get( procStarts.size() - 1 );
            assert previous.finish == start;
        }
        procStarts.add( new ProcedureStart( proc, start ) );
    }
    public void addProcedureFinish( int finish ) {
        assert ! procStarts.isEmpty() &&
            procStarts.get( procStarts.size() - 1 ).start <= finish;
        procStarts.get( procStarts.size() -1 ).finish = finish;
    }
    public ProcedureEntry getProcedure( int pc ) {
        if( pc < procStarts.get(0).start ||
            procStarts.get(procStarts.size()-1).finish <= pc ) {
            // Must be in main program setup or finalization code
            return null;
        }
        for( ProcedureStart ps : procStarts ) {
            if( pc < ps.finish ) {
                return ps.proc;
            }
        }
        // Can't get here
        ErrorHandler.getErrorHandler().errorMessage(
                "getProcedure failed assertion 2", Severity.FATAL );
        return null;
    }
    @Override
    public String toString() {
        String s = "";
        for( ProcedureStart start : procStarts ) {
            s += start.toString() + "\n";
        }
        return s;
    }
}
