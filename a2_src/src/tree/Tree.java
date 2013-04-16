package tree;

import syms.SymEntry;
import syms.SymbolTable;
/** 
 * class Tree - Abstract syntax tree nodes and support functions.
 * @version $Id: Tree.java 10 2013-04-15 23:26:08Z uqihayes $
 * Uses visitor pattern in order to separate the static semantic checks
 * and code generation from tree building. 
 * The accept method for each type of tree node, calls the corresponding
 * visit method of the tree visitor. 
 */
public abstract class Tree {

    /** Tree node representing the main program. */
    public static class ProgramNode {
        private SymbolTable baseSymbolTable;
        private BlockNode mainProc;

        public ProgramNode( SymbolTable baseSyms, BlockNode mainProc ) {
            this.baseSymbolTable = baseSyms;
            this.mainProc = mainProc;
        }
        public void accept( TreeVisitor visitor ) {
            visitor.visitProgramNode( this );
        }
        public Code accept( TreeTransform<Code> visitor ) {
            return visitor.visitProgramNode( this );
        }
        public SymbolTable getBaseSymbolTable() {
            return baseSymbolTable;
        }
        public BlockNode getBlock() {
            return mainProc;
        }
        @Override
        public String toString() {
            return getBlock().toString();
        }
    }

    /** Node representing a Block consisting of declarations and
     * body of a procedure, function, or the main program. */
    public static class BlockNode {
        protected DeclNode.DeclListNode procedures;
        protected StatementNode body;
        protected SymEntry.ProcedureEntry procEntry;

        /** Constructor for a block within a procedure */
        public BlockNode(DeclNode.DeclListNode procedures, StatementNode body) {
            this.procedures = procedures;
            this.body = body;
        }
        public void accept( TreeVisitor visitor ) {
            visitor.visitBlockNode( this );
        }
        public Code accept( TreeTransform<Code> visitor ) {
            return visitor.visitBlockNode( this );
        }
        public DeclNode.DeclListNode getProcedures() {
            return procedures;
        }
        public StatementNode getBody() {
            return body;
        }
        public SymEntry.ProcedureEntry getProcEntry() {
            return procEntry;
        }
        public void setProcEntry( SymEntry.ProcedureEntry procEntry ) {
            this.procEntry = procEntry;
        }
        @Override
        public String toString() {
            return "BLOCK " + getProcedures() + " BEGIN" + body + " END";
        }
    }

}
