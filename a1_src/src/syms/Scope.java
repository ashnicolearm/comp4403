package syms;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import machine.StackMachine;
import syms.SymEntry;
import syms.SymEntry.ProcedureEntry;

/** A Scope represents a static scope for a procedure, main program or 
 * the predefined scope. 
 * @version $Id: Scope.java 8 2013-02-22 06:25:04Z ianh $
 * It provides operations to add identifiers, look up identifiers. 
 * Searching for an identifier in a scope starts at the current scope, 
 * but then if it is not found, the search proceeds to the next outer 
 * (parent) scope, and so on. 
 */
public class Scope {
    /** Symbol table entry for the procedure
     *  (or main program or predefined scope). */
    private SymEntry.ProcedureEntry entry;
    /** Parent Scope */
    private Scope parent;
    /** Static level of this scope */
    private int level;
    /** Symbol table entries */
    private Map<String, SymEntry> entries;
    /** space allocated for local variables */
    private int variableSpace;

    /** This constructs a single scope within a symbol table
     * that is linked to the parent scope, which may be null to
     * indicate that there is no parent. */
    public Scope( ProcedureEntry entry, Scope parent, int level ) {
        this.entry = entry;
        this.parent = parent;
        this.level = level;
        /* Initially empty */
        this.entries = new HashMap<String, SymEntry>();
        variableSpace = 0;
    }
    public ProcedureEntry getEntry() {
        return entry;
    }
    public Scope getParent() {
        return parent;
    }
    public int getLevel() {
        return level;
    }
    /** @return the set of entries in this scope */
    public Collection<SymEntry> getEntries() {
        return entries.values();
    }
    /** Lookup id starting in the current scope and 
     * thence in the parent scope and so on.
     * @param id Identifier to search for.
     * @return symbol table entry for the id, or null if not found.
     */
    public SymEntry lookup(String id) {
        if( entries.containsKey( id ) ) {
            return entries.get( id );
        }
        if( parent != null ) {
            return parent.lookup( id );
        }
        return null;
    }
    /** Add an entry to the scope unless an entry for the same name exists.
     * @return the entry added or null otherwise. */
    public SymEntry addEntry( SymEntry entry ) {
        if( entries.containsKey( entry.getIdent() ) ) {
            return null;
        } else {
            entries.put( entry.getIdent(), entry );
            return entry;
        }
    }
    /** Return the amount of space allocated to local variables
     * within the current scope. */
    public int getVariableSpace() {
            return variableSpace;
    }
    /** Allocate space for a local variable and return its address (offset).
     * @param size is the amount of space required for the variable. */
    public int allocVariableSpace( int size ) {
            int base = variableSpace;
            variableSpace += size;
            return StackMachine.LOCALS_BASE + base;
    }

    // TODO The formatting produced here could be better
    public String toString() {
        return "\nScope " + entry.getIdent() + "\n" + entries +
            (parent == null ? "" : parent);
    }
}
