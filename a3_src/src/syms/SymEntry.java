package syms;

import source.ErrorHandler;
import source.Position;
import source.Severity;
import syms.Type.ReferenceType;
import tree.ConstExp;

/** This class provides the individual entries that go in a symbol table.
 * @version $Revision: 15 $  $Date: 2013-05-08 13:58:21 +1000 (Wed, 08 May 2013) $
 * The kinds of entries are constants, types, variables and procedures.
 * It provides subclasses for each of these kinds.
 * All entries have an identifier, the position (in the source input)
 * of the definition of the identifier, a static level and a type. 
 * Particular kinds of entries have additional fields. 
 */
public abstract class SymEntry {
    /** Name of the entry */
    protected String ident;
    /** position of declaration in source input */
    protected Position pos;
    /** scope in which declared */
    protected Scope scope;
    /** type of the identifier after type resolution */
    protected Type type;
    /** declared type of entry before resolving type identifier */
    protected Type declaredType;
    /** whether id has been resolved and space allocated, where necessary */
    protected boolean resolved;
    /* All entries have all the above fields. */
    
    /** Only subclasses of SymEntry have public constructors. */
    private SymEntry( String id, Position p, Scope s, Type t, boolean r ) {
        ident = id;
        pos = p;
        scope = s;
        type = t;
        declaredType = t;
        resolved = r;
    }
    public String getIdent() {
        return ident;
    }
    public Position getPosition() {
        return pos;
    }
    public void setScope( Scope scope ) {
        this.scope = scope;
    }
    public int getLevel() {
        return scope.getLevel();
    }
    /** @return the type of the entry.
     * May need to resolve type entry first.
     */
    public Type getType() {
        if( !resolved ) {
            resolve();
        }
        return type;
    }
    public void setType( Type t ) {
        this.type = t;
    }
    /** @return the declared type - may be a type identifier */
    public Type getDeclaredType() {
        return declaredType;
    }
    /** Resolve any references to type identifiers in supplied scope */
    public void resolve() {
        if( ! resolved ) {
            type = type.resolveType( pos );
            resolved = true;
        }
    }
    /** For debugging */
    @Override
    public String toString() {
        return ident + ": " + type;
    }
    
    /** Symbol table entry for a CONST identifier */
    public static class ConstantEntry extends SymEntry {
        /** The value of the constant represented as an integer. */
        int value;
        /** Expression tree for constant before it is evaluated */
        ConstExp tree;
        /** Status of constant for resolving references */
        private enum Status{ Unresolved, Resolving, Resolved } 
        /** Resolved if expression has been evaluated */
        protected Status status;
        /** Constructor if the constant value (and hence its type) is known */
        ConstantEntry(String id, Position p, Scope s, Type t, int val) {
            super( id, p, s, t, true );
            value = val;
        }
        /** Constructor when only an abstract syntax tree is available. */
        ConstantEntry(String id, Position p, Scope s, Type t, ConstExp val) {
            super( id, p, s, t, false );
            value  = 0x80808080;    // silly default value
            tree = val;
            status = Status.Unresolved;
        }
        /** Resolve references to constant identifiers and evaluate expression
         */
        @Override
        public void resolve() {
            switch ( status ) {
            case Unresolved:
                status = Status.Resolving;
                value = tree.getValue();
                type = tree.getType();
                status = Status.Resolved;
                resolved = true;
                break;
            case Resolving:
                error( "circular reference in constant expression", pos );
                status = Status.Resolved;
                resolved = true;
                break;
            case Resolved:
                break;
            }
        }
        public int getValue() {
            if( !resolved ) {
                resolve();
            }
            return value;
        }
        @Override
        public String toString() {
            return indent( scope.getLevel() ) + 
                "CONST " + super.toString() + " = " + value;
        }
    }

    /** Symbol table entry for a TYPE identifier */
    public static class TypeEntry extends SymEntry {

        TypeEntry( String id, Position p, Scope s, Type t ) {
            super( id, p, s, t, false );
        }
        @Override
        public String toString() {
            return indent( scope.getLevel() ) + "TYPE " + super.toString();
        }
    }

    /** Symbol table entry for a variable identifier */
    public static class VarEntry extends SymEntry {
        /** offset of variable starting from 0 */
        protected int offset;
        
        public VarEntry(String id, Position p, Scope s, ReferenceType t ) {
            super( id, p, s, t, false );
        }
        public ReferenceType getType() {
            return (ReferenceType)super.getType();
        }
        /** resolving a variable requires allocating space for it */
        @Override
        public void resolve() {
            if( ! resolved ) {
                // resolve the type of the variable
                super.resolve();
                /* Space is allocated for the variable and the address of that 
                 * location placed in the entry for the variable.
                 * The space allocated depends on the size of its type.
                 */
                Type baseType = ((Type.ReferenceType)type).getBaseType();
                offset = scope.allocVariableSpace( baseType.getSpace() );
            }
        }
        /** @requires resolved */
        public int getOffset() {
            assert resolved;
            return offset;
        }
        public void setOffset( int offset ) {
            this.offset = offset;
        }
        @Override
        public String toString() {
            return indent( scope.getLevel() ) + 
                "VAR " + super.toString() + " at " + offset + 
                " level " + scope.getLevel();
        }
    }
    /** Symbol table entry for a parameter.
     * Note that ParamEntry extends VarEntry so that parameters are 
     * treated as local variables when they are referenced. */
    public static class ParamEntry extends VarEntry {
        /** Constructor defaults to a value parameter */
        public ParamEntry(String id, Position p, Scope s, ReferenceType type){
            super( id, p, s, type );
        }
        /** resolving a parameter's type - this version is the same as the
         * default for Type but not for VarEntry which is the super type
         * of  ParamEntry */
        @Override
        public void resolve() {
            if( ! resolved ) {
                type = type.resolveType( pos );
                resolved = true;
            }
        }
        /** @requires offset <= 0 */
        @Override
        public void setOffset(int offset) {
            this.offset = offset;
        }
        public int getSpace() {
            return ((ReferenceType)getType()).getBaseType().getSpace();
        }
        @Override
        public String toString()
        {
            return indent( scope.getLevel() ) + 
                super.toString() + "PARAMETER ";
        }
    }

    public static class RefParamEntry extends ParamEntry {
        /** Offset of the descriptor for var parameter */
        
        public RefParamEntry( String id, Position p, Scope s,
                                ReferenceType type ) {
            super( id, p, s, type );
        }
        @Override
        public int getSpace() {
            return getType().getSpace();
        }
    }

    /** Symbol table entry for a procedure identifier */
    public static class ProcedureEntry extends SymEntry {
        /** start location of the procedure code */
        private int start;
        /** Scope of entries declared locally to the procedure */
        private Scope localScope;

        public ProcedureEntry( String id, Position p, Scope s, 
                                  Type.ProcedureType type ) {
            super( id, p, s, type, false );
        } 
        public ProcedureEntry( String id, Position p, Scope s ) {
            this( id, p, s, new Type.ProcedureType() );
        }
        @Override
        public Type.ProcedureType getType() {
            return (Type.ProcedureType)type;
        }
        public Scope getLocalScope() {
            return localScope;
        }
        public void setLocalScope( Scope symtab ) {
            localScope = symtab;
        }
       public int getStart() {
            return start;
        }
        public void setStart( int start ) {
            this.start = start;
        }
        @Override
        public String toString() {
            return indent( localScope.getLevel() ) + 
                "PROCEDURE " + super.toString() + " at " + start;
        }
    }

    /** Symbol table entry for an operator */
    public static class OperatorEntry extends SymEntry {

        OperatorEntry(String id, Position p, Scope s, Type optype ) {
            super( id, p, s, new Type.UnionType( optype ), true );
        }
        OperatorEntry(String id, Position p, Scope s, 
                Type oldType, Type... types ) {
            super( id, p, s, new Type.UnionType( types ), true );
            ((Type.UnionType)type).addType( oldType );
        }
        public void extendType( Type optype ) {
            ((Type.UnionType)type).addType( optype );
        }
        @Override
        public String toString() {
            return indent( scope.getLevel() ) + 
                "OPERATOR " + super.toString();
        }
    }

    /** For use in toString. Returns a string of blanks of length 2n. */
    private static String indent( int n ) {
       String ind = "";
       while( n > 0) {
           ind += "  ";
           n--;
       }
       return ind;
    }
    private static void error( String message, Position pos ) {
        ErrorHandler.getErrorHandler().errorMessage(message, 
                Severity.ERROR, pos);
    }
}
