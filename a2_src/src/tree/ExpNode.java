package tree;

import java.util.ArrayList;
import java.util.List;

import source.Position;
import syms.SymEntry;
import syms.Type;

/** 
 * class ExpNode - Abstract syntax Tree representation of expressions.
 * @version $Id: ExpNode.java 10 2013-04-15 23:26:08Z uqihayes $
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
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitErrorExpNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitErrorExpNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitErrorExpNode( this );
        }
        @Override
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
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitConstNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitConstNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitConstNode( this );
        }
        public int getValue() {
            return value;
        }
        @Override
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
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitIdentifierNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitIdentifierNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitIdentifierNode( this );
        }
        public String getId() {
            return id;
        }
        @Override
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
        @Override
        public Type getType() {
            return variable.getType();
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitVariableNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitVariableNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitVariableNode( this );
        }
        public SymEntry.VarEntry getVariable() {
            return variable;
        }
        @Override
        public String toString( ) {
            return "VariableNode(" + variable + ")";
        }
    }
    /** Tree node representing a "read" expression. */
    public static class ReadNode extends ExpNode {

        public ReadNode( Position pos ) {
            super( pos );
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitReadNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitReadNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitReadNode( this );
        }
        @Override
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
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitBinaryOpNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitBinaryOpNode( this );
        }
        @Override
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
        @Override
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
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitUnaryOpNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitUnaryOpNode( this );
        }
        @Override
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
        @Override
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
        @Override
        public ExpNode transform(ExpTransform<ExpNode> visitor ) {
            return visitor.visitArgumentsNode( this );
        }
        @Override
        public Code genCode(ExpTransform<Code> visitor ) {
            return visitor.visitArgumentsNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitArgumentsNode( this );
        }
        public List<ExpNode> getArgs() {
            return args;
        }
        public void setArgs( List<ExpNode> args ) {
            this.args = args;
        }
        @Override
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
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitDereferenceNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitDereferenceNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitDereferenceNode( this );
        }
        public ExpNode getLeftValue() {
            return leftValue;
        }
        public void setLeftValue( ExpNode leftValue ) {
            this.leftValue = leftValue;
        }
        @Override
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
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitNarrowSubrangeNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitNarrowSubrangeNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitNarrowSubrangeNode( this );
        }
        public Type.SubrangeType getSubrangeType() {
            return (Type.SubrangeType)getType();
        }
        public ExpNode getExp() {
            return exp;
        }
        @Override
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
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitWidenSubrangeNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitWidenSubrangeNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitWidenSubrangeNode( this );
        }
        public ExpNode getExp() {
            return exp;
        }
        @Override
        public String toString() {
            return "WidenSubrange(" + exp + ":" + getType() + ")";
        }
    }
    
    /** Tree node representing a type identifier. */
    public static class RecordConstructorNode extends ExpNode {
    	ExpNode fields;
    	Type type;
        public RecordConstructorNode( Position pos, Type type, ExpNode fields )
        {
            super( pos, type );
            this.type = type;
            this.fields = fields;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitRecordConstructorNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitRecordConstructorNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitRecordConstructorNode( this );
        }
        public Type getType() {
        	return this.type;
        }
        public void setType(Type t) {
        	this.type = t;
        }
        public ExpNode getFields() {
        	return this.fields;
        }
        @Override
        public String toString() {
        	return "RECORD CONSTRUCTOR( " + fields + " )";
        }
    }
    
    /** Tree node representing a RecordFields node. */
    public static class RecordFieldsNode extends ExpNode {
    	private List<ExpNode> fields;
        public RecordFieldsNode()
        {
            super( Position.NO_POSITION );
        }
        public RecordFieldsNode(Position pos)
        {
            super( pos );
            fields = new ArrayList<ExpNode>();
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitRecordFieldsNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitRecordFieldsNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitRecordFieldsNode( this );
        }
        public List<ExpNode> getFields() {
        	return fields;
        }
        public void add(ExpNode e) {
        	fields.add(e);
        }
        @Override
        public String toString() {
            String result = "";
            String sep = "";
            for( ExpNode e : fields ) {
                result += sep + e.toString();
                sep = "; ";
            }
            return result;
        }
    }

    /** Tree node representing a record entry. */
    public static class RecordEntryNode extends ExpNode {
    	ExpNode record;
    	String field;
    	
        public RecordEntryNode( Position pos, ExpNode record, String field )
        {
            super( pos );
            this.record = record;
            this.field = field;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitRecordEntryNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitRecordEntryNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitRecordEntryNode( this );
        }
        public ExpNode getRecord() {
        	return this.record;
        }
		public void setRecord(ExpNode record) {
			this.record = record;
		}
        public String getField() {
        	return this.field;
        }
        @Override
        public String toString() {
            return "RecordEntry(" + getType() + ")";
        }
    }
    /** Tree node representing a pointer constructor. */
    public static class PointerConstructorNode extends ExpNode {
    	Type pointerType;
    	
        public PointerConstructorNode( Position pos, Type pointerType )
        {
        	super( pos );
            this.pointerType = pointerType;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitPointerConstructorNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitPointerConstructorNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitPointerConstructorNode( this );
        }
        public Type getPointerType() {
        	return this.pointerType;
        }
        public void setPointerType(Type pointerType) {
        	this.pointerType = pointerType;
        }
        @Override
        public String toString() {
            return "new Pointer(" + getType() + ")";
        }
    }
    /** Tree node representing a pointer constructor. */
    public static class PointerNode extends ExpNode {
    	ExpNode value;
    	
        public PointerNode( Position pos, ExpNode value )
        {
            super( pos );
            this.value = value;
        }
        @Override
        public ExpNode transform( ExpTransform<ExpNode> visitor ) {
            return visitor.visitPointerNode( this );
        }
        @Override
        public Code genCode( ExpTransform<Code> visitor ) {
            return visitor.visitPointerNode( this );
        }
        @Override
        public void accept( ExpVisitor visitor ) {
            visitor.visitPointerNode( this );
        }
        public ExpNode getValue() {
        	return this.value;
        }
        public void setValue(ExpNode value) {
        	this.value = value;
        }
        @Override
        public String toString() {
            return "Pointer(" + getType() + ")";
        }
    }
}
