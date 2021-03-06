package tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import source.Position;
import syms.Scope;
import syms.SymEntry;
import syms.Type;

/** 
 * class StatementNode - Abstract syntax tree representation of statements. 
 * @version $Id: StatementNode.java 10 2013-04-15 23:26:08Z uqihayes $
 * All statements have a position within the original source code.
 */
public abstract class StatementNode {
    /** Position in the input source program */
    private Position pos;

    /** Constructor */
    protected StatementNode( Position pos )
    {
        this.pos = pos;
    }
    protected Position getPosition() {
        return pos;
    }
    /** All statement nodes provide an accept method to implement the visitor
     * pattern to traverse the tree.
     * @param visitor class implementing the details of the particular
     *  traversal.
     */
    public abstract void accept( StatementVisitor visitor );
    
    public abstract Code genCode( StatementTransform<Code> visitor );

    /** Statement node representing an erroneous statement. */
    public static class ErrorNode extends StatementNode {
        public ErrorNode( Position pos ) {
            super( pos );
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitStatementErrorNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitStatementErrorNode( this );
        }
        @Override
        public String toString() {
            return "ERROR";
        }
    }

    /** Tree node representing an assignment statement. */
    public static class AssignmentNode extends StatementNode {
        /** Tree node for expression on left hand side of an assignment. */
        private ExpNode variable;
        /** Tree node for the expression to be assigned. */
        private ExpNode exp;

        public AssignmentNode( Position pos, ExpNode variable, 
                ExpNode exp ) {
            super( pos );
            this.variable = variable;
            this.exp = exp;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitAssignmentNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitAssignmentNode( this );
        }
        public ExpNode getVariable() {
            return variable;
        }
        public void setVariable( ExpNode variable ) {
            this.variable = variable;
        }
        public void setExp(ExpNode exp) {
            this.exp = exp;
        }
        public ExpNode getExp() {
            return exp;
        }
        public String getVariableName() {
            if( variable instanceof ExpNode.VariableNode ) {
                return ((ExpNode.VariableNode)variable).getId();
            } else {
                return "<noname>";
            }
        }
        @Override
        public String toString( ) {
            return variable.toString() + " := " + exp.toString();
        }
    }

    /** Tree node representing a "write" statement. */
    public static class WriteNode extends StatementNode {
        private ExpNode exp;

        public WriteNode( Position pos, ExpNode exp ) {
            super( pos );
            this.exp = exp;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitWriteNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitWriteNode( this );
        }
        public ExpNode getExp() {
            return exp;
        }
        public void setExp( ExpNode exp ) {
            this.exp = exp;
        }
        @Override
        public String toString( ) {
            return "WRITE " + exp.toString();
        }
    }
    
    /** Tree node representing a "call" statement. */
    public static class CallNode extends StatementNode {
        private String id;
        private SymEntry.ProcedureEntry procEntry;

        public CallNode( Position pos, String id ) {
            super( pos );
            this.id = id;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitCallNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitCallNode( this );
        }
        public String getId() {
            return id;
        }
        public void setEntry(SymEntry.ProcedureEntry entry) {
            this.procEntry = entry;
        }
        public SymEntry.ProcedureEntry getEntry() {
            return procEntry;
        }
        @Override
        public String toString( ) {
            String s = "CALL " + procEntry.getIdent() + "(";
            return s + ")";
        }
    }
    /** Tree node representing a statement list. */
    public static class ListNode extends StatementNode {
        private List<StatementNode> statements;
        
        public ListNode( Position pos ) {
            super( pos );
            this.statements = new ArrayList<StatementNode>();
        }
        public void addStatement( StatementNode s ) {
            statements.add( s );
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitStatementListNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitStatementListNode( this );
        }
        public List<StatementNode> getStatements() {
            return statements;
        }
        @Override
        public String toString() {
            String result = "";
            String sep = "";
            for( StatementNode s : statements ) {
                result += sep + s.toString();
                sep = "; ";
            }
            return result;
        }
    }
    /** Tree node representing an "if" statement. */
    public static class IfNode extends StatementNode {
        private ExpNode condition;
        private StatementNode thenStmt;
        private StatementNode elseStmt;

        public IfNode( Position pos, ExpNode condition, 
                StatementNode thenStmt, StatementNode elseStmt ) {
            super( pos );
            this.condition = condition;
            this.thenStmt = thenStmt;
            this.elseStmt = elseStmt;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitIfNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitIfNode( this );
        }
        public ExpNode getCondition() {
            return condition;
        }
        public void setCondition( ExpNode cond ) {
            this.condition = cond;
        }
        public StatementNode getThenStmt() {
            return thenStmt;
        }
        public StatementNode getElseStmt() {
            return elseStmt;
        }
        @Override
        public String toString( ) {
            return "IF " + condition.toString() + " THEN " + thenStmt +
                " ELSE " + elseStmt;
        }
    }

    /** Tree node representing a "while" statement. */
    public static class WhileNode extends StatementNode {
        private ExpNode condition;
        private StatementNode loopStmt;

        public WhileNode( Position pos, ExpNode condition, 
              StatementNode loopStmt ) {
            super( pos );
            this.condition = condition;
            this.loopStmt = loopStmt;
        }
        @Override
        public void accept( StatementVisitor visitor ) {
            visitor.visitWhileNode( this );
        }
        @Override
        public Code genCode( StatementTransform<Code> visitor ) {
            return visitor.visitWhileNode( this );
        }
        public ExpNode getCondition() {
            return condition;
        }
        public void setCondition( ExpNode cond ) {
            this.condition = cond;
        }
        public StatementNode getLoopStmt() {
            return loopStmt;
        }
        @Override
        public String toString( ) {
            return "WHILE " + condition.toString() + " DO " +
                loopStmt.toString();
        }
    }
}

