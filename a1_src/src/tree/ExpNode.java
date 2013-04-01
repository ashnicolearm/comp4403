package tree;

import java.util.ArrayList;
import java.util.List;

import source.Position;
import syms.SymEntry;
import syms.Type;

/** 
 * class ExpNode - Abstract syntax Tree representation of expressions.
 * @version $Id: ExpNode.java 8 2013-02-22 06:25:04Z ianh $
 * Abstract class representing expressions.
 * All expression nodes have a position and a type.
 */
public abstract class ExpNode {
    /** Position in the source code of the expression */
    private Position pos;
    /** Type of the expression (determined by static checker) */
    private Type type;
    
    /** Constructor when type is known */
    protected ExpNode( Position pos, Type type) {
        this.pos = pos;
        this.type = type;
    }
    /** Constructor when type as yet unknown */
    protected ExpNode( Position pos ) {
        this( pos, Type.ERROR_TYPE );
    }
    public Type getType() {
//      if( type == null ) {
//          fatal( "type not set for node in static checking", pos );
//      }
        return type;
    }
    public void setType( Type type ) {
        this.type = type;
    }
    public Position getPosition() {
        return pos;
    }
    
    /** Each subclass of ExpNode must provide a transform method
     * to do type checking and transform the expression node to 
     * handle type coercions, etc.
     * @param visitor object that implements a traversal.
     * @return transformed expression node
     */
    public abstract ExpNode transform( ExpTransform<ExpNode> visitor );

    /** Each subclass of ExpNode must provide a genCode method
     * to visit the expression node to handle code generation.
     * @param visitor object that implements a traversal.
     */
    public abstract Code genCode( ExpTransform<Code> visitor );
    
    /** Each subclass of ExpNode must provide an accept method 
     * to visit the expression node
     * @param visitor object that implements a traversal.
     */
    public abstract void accept( ExpVisitor visitor );
    
    /** Tree node representing an erroneous expression. */
    public static class ErrorNode extends ExpNode {
        
        public ErrorNode( Position pos, Type type ) {
            super( pos, type );
        }
        public ErrorNode( Position pos ) {
            this( pos, Type.ERROR_TYPE );
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitErrorExpNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitErrorExpNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitErrorExpNode( this );
        }
        public String toString() {
            return "ErrorNode";
        }
    }

    /** Tree node representing a constant within an expression. */
    public static class ConstNode extends ExpNode {
        /** constant's value */
        private int value;

        public ConstNode( Position pos, Type type, int value ) {
            super( pos, type );
            this.value = value;
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitConstNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitConstNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitConstNode( this );
        }
        public int getValue() {
            return value;
        }
        public String toString( ) {
            return Integer.toString(value);
        }
    }

    /** Identifier node is used until the identifier can be resolved 
     * to be either a constant or a variable during the static 
     * semantics check phase. 
     */
    public static class IdentifierNode extends ExpNode {
        private String id;
        
        public IdentifierNode( Position pos, String id ) {
            super( pos );
            this.id = id;
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitIdentifierNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitIdentifierNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitIdentifierNode( this );
        }
        public String getId() {
            return id;
        }
        public String toString() {
            return id;
        }
    }
    /** Tree node representing a variable. */
    public static class VariableNode extends ExpNode {
        protected SymEntry.VarEntry variable;
    
        public VariableNode( Position pos, SymEntry.VarEntry variable ) {
            super( pos );
            this.variable = variable;
        }
        public String getId() {
            return variable.getIdent();
        }
        public Type getType() {
            return variable.getType();
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitVariableNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitVariableNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitVariableNode( this );
        }
        public SymEntry.VarEntry getVariable() {
            return variable;
        }
        public String toString( ) {
            return "VariableNode(" + variable + ")";
        }
    }
    /** Tree node representing a "read" expression. */
    public static class ReadNode extends ExpNode {

        public ReadNode( Position pos ) {
            super( pos );
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitReadNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitReadNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitReadNode( this );
        }
        public String toString( ) {
            return "Read";
        }
    }
    /** Tree node for a binary operator. */
    public static class BinaryOpNode extends ExpNode {
        private BinaryOperator op;
        private ExpNode left, right;
        
        public BinaryOpNode( Position pos, BinaryOperator op,
                ExpNode left, ExpNode right ) {
            /* For a binary operator with an overloaded type we can't 
             * determine type of result until we know type of its
             * arguments during static checking
             */
            super( pos );
            this.op = op;
            this.left = left;
            this.right = right;
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitBinaryOpNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitBinaryOpNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitBinaryOpNode( this );
        }
        public BinaryOperator getOp() {
            return op;
        }
        public void setOp( BinaryOperator op ) {
            this.op = op;
        }
        public ExpNode getLeft() {
            return left;
        }
        public void setLeft( ExpNode left ) {
            this.left = left;
        }
        public ExpNode getRight() {
            return right;
        }
        public void setRight( ExpNode right ) {
            this.right = right;
        }
        public String toString() {
            return op + "(" + left + ", " + right + ")";
        }
    }
    
    /** Tree node for a unary operator. */
    public static class UnaryOpNode extends ExpNode {
        private UnaryOperator op;
        private ExpNode subExp;
        
        public UnaryOpNode( Position pos, UnaryOperator op, ExpNode subExp ) {
            super( pos );
            this.op = op;
            this.subExp = subExp;
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitUnaryOpNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitUnaryOpNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitUnaryOpNode( this );
        }
        public UnaryOperator getOp() {
            return op;
        }
        public ExpNode getSubExp() {
            return subExp;
        }
        public void setSubExp( ExpNode subExp ) {
            this.subExp = subExp;
        }
        public String toString() {
            return op.toString() + subExp;
        }
    }
    /** Tree node for a list of arguments */
    public static class ArgumentsNode extends ExpNode {
        private List<ExpNode> args;
        
        public ArgumentsNode( Position pos, Type.ProductType t, 
                List<ExpNode> args ) {
            super( pos, t );
            this.args = args;
        }
        public ArgumentsNode( Position pos, List<ExpNode> args ) {
            super( pos );
            this.args = args;
        }
        public ArgumentsNode( ExpNode e1, ExpNode... rest ) {
            super( e1.getPosition() );
            args = new ArrayList<ExpNode>();
            args.add( e1 );
            for( ExpNode e : rest ) {
                args.add( e );
            }
        }
        public ExpNode transform(ExpTransform<ExpNode> visitor ) {
            return visitor.visitArgumentsNode( this );
        }
        public Code genCode(ExpTransform<Code> visitor ) {
            return visitor.visitArgumentsNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitArgumentsNode( this );
        }
        public List<ExpNode> getArgs() {
            return args;
        }
        public void setArgs( List<ExpNode> args ) {
            this.args = args;
        }
        public String toString() {
            return "[" + args + "]";
        }
    }

    /** Tree node for dereferencing an LValue.
     * A Dereference node references an ExpNode node and represents the
     * dereferencing of the "address" given by the leftValue to give
     * the value at that address.
     */
    public static class DereferenceNode extends ExpNode {
        private ExpNode leftValue;

        public DereferenceNode( Position pos, ExpNode lval ) {
            super( pos );
            this.leftValue = lval;
        }
        public DereferenceNode( Type type, ExpNode exp ) {
            super( exp.getPosition(), type );
            this.leftValue = exp;
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitDereferenceNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitDereferenceNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitDereferenceNode( this );
        }
        public ExpNode getLeftValue() {
            return leftValue;
        }
        public void setLeftValue( ExpNode leftValue ) {
            this.leftValue = leftValue;
        }
        public String toString( ) {
            return "Dereference(" + leftValue + ")";
        }
    }

    /** Tree node representing a coerce that narrows a subrange.
     * This will require a bounds check at runtime.
     */
    public static class NarrowSubrangeNode extends ExpNode {
        private ExpNode exp;

        /* @requires type instance of Type.SubrangeType */
        public NarrowSubrangeNode( Position pos, Type type, 
                ExpNode exp )
        {
            super( pos, type );
            this.exp = exp;
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitNarrowSubrangeNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitNarrowSubrangeNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitNarrowSubrangeNode( this );
        }
        public Type.SubrangeType getSubrangeType() {
            return (Type.SubrangeType)getType();
        }
        public ExpNode getExp() {
            return exp;
        }
        public String toString() {
            return "NarrowSubrange(" + exp + ":" + getType() + ")";
        }
    }
    
    /** Tree node representing a widening of a subrange.
     * This won't require any runtime action.
     */
    public static class WidenSubrangeNode extends ExpNode {
        private ExpNode exp;

        /* @requires type instance of Type.SubrangeType */
        public WidenSubrangeNode( Position pos, Type type, 
                ExpNode exp )
        {
            super( pos, type );
            this.exp = exp;
        }
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitWidenSubrangeNode( this );
        }
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitWidenSubrangeNode( this );
        }
        public void accept( ExpVisitor visitor ) {
            visitor.visitWidenSubrangeNode( this );
        }
        public ExpNode getExp() {
            return exp;
        }
        public String toString() {
            return "WidenSubrange(" + exp + ":" + getType() + ")";
        }
    }
}
