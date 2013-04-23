package tree;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import machine.StackMachine;
import source.Errors;
import source.Position;
import source.Severity;
import syms.SymEntry;
import syms.SymbolTable;
import syms.Type;
import syms.Type.Field;
import tree.Coercion.IncompatibleTypes;
import tree.ExpNode.RecordEntryNode;
import tree.ExpNode.RecordFieldsNode;
import tree.Tree.*;

/** class StaticSemantics - Performs the static semantic checks on
 * the abstract syntax tree using a visitor pattern to traverse the tree.
 * @version $Id: StaticChecker.java 10 2013-04-15 23:26:08Z uqihayes $
 */
public class StaticChecker implements TreeVisitor, StatementVisitor, 
                                        ExpTransform<ExpNode> {

    /** The static checker maintains a symbol table reference.
     * Its current scope is that for the procedure 
     * currently being processed.
     */
    private SymbolTable symtab;
    /** Errors are reported through the error handler. */
    private Errors errors;

    /** Construct a static checker for PL0.
     * @param errors is the error message handler.
     */
    public StaticChecker( Errors errors ) {
        super();
        this.errors = errors;
    }
    /** The tree traversal starts with a call to visitProgramNode.
     * Then its descendants are visited using visit methods for each
     * node type, which are called using the visitor pattern accept
     * (or transform for expressions) method in the tree.
     */
    public void visitProgramNode(ProgramNode node) {
        // Set up the symbol table to be that for the main program.
        symtab = node.getBaseSymbolTable();
        // Set the current symbol table scope to that for the procedure.
        symtab.reenterScope( node.getBlock().getProcEntry().getLocalScope() );
        // resolve all references to identifiers with the declarations
        symtab.resolveCurrentScope();
        // Check the main program block.
        node.getBlock().accept( this );
        // Restore the symbol table to the parent scope.
        symtab.leaveScope();
    }
    /** Procedure, function or main program node */
    public void visitProcedureNode(DeclNode.ProcedureNode node) {
        SymEntry.ProcedureEntry procEntry = node.getProcEntry();
        // Set the current symbol table scope to that for the procedure.
        symtab.reenterScope( procEntry.getLocalScope() );
        // resolve all references to identifiers with the declarations
        symtab.resolveCurrentScope();
        // Check the block of the procedure.
        BlockNode block = node.getBlock();
        block.accept( this );
        // Restore the symbol table to the parent scope.
        symtab.leaveScope();
    }
    public void visitBlockNode(BlockNode node) {
        // Check the procedures, if any.
        node.getProcedures().accept( this );
        // Check the body of the block.
        node.getBody().accept( this );
    }
    public void visitStatementErrorNode(StatementNode.ErrorNode node) {
        // Nothing to check - already invalid.
    }

    public void visitAssignmentNode(StatementNode.AssignmentNode node) {
        // Check the left side left value.
        ExpNode left = node.getVariable().transform( this );
        node.setVariable( left );
        // Check the right side expression.
        ExpNode exp = node.getExp().transform( this );
        node.setExp( exp );
        // Validate that it is a true left value and not a constant.
        Type lvalType = left.getType();
        if( ! (lvalType instanceof Type.ReferenceType) ) {
            error( "variable (i.e., L-Value) expected", 
                    node.getVariable().getPosition() );
        } else {
            /* Validate that the right expression is assignment
             * compatible with the left value. This may require that the 
             * right side expression is coerced to the dereferenced
             * type of the left side LValue. */
            Type baseType = ((Type.ReferenceType)lvalType).getBaseType();
            node.setExp( Coercion.coerce( exp, baseType ) );
        }
    }

    public void visitWriteNode(StatementNode.WriteNode node) {
        // Check the expression being written.
        ExpNode exp = node.getExp().transform( this );
        // coerce expression to be of type integer,
        // or complain if not possible.
        node.setExp( Coercion.coerce( exp, Type.INTEGER_TYPE ) );
    }

    public void visitCallNode(StatementNode.CallNode node) {
        SymEntry.ProcedureEntry procEntry;
        Type.ProcedureType procType;
        // Look up the symbol table entry for the procedure.
        SymEntry entry = symtab.lookup( node.getId() );
        if( entry instanceof SymEntry.ProcedureEntry ) {
            procEntry = (SymEntry.ProcedureEntry)entry;
            node.setEntry( procEntry );
            procType = procEntry.getType();
        } else {
            error( "Procedure identifier required", node.getPosition() );
            return;
        }
    }

    public void visitStatementListNode( StatementNode.ListNode node ) {
        for( StatementNode s : node.getStatements() ) {
            s.accept( this );
        }
    }
    private ExpNode checkCondition( ExpNode cond ) {
        // Check and transform the condition
        cond = cond.transform( this );
        /* Validate that the condition is boolean, which may require
         * coercing the condition to be of type boolean. */     
        return Coercion.coerce( cond, Type.BOOLEAN_TYPE );
    }
    public void visitIfNode(StatementNode.IfNode node) {
        // Check the condition.
        node.setCondition( checkCondition( node.getCondition() ) );
        // Check the 'then' part.
        node.getThenStmt().accept( this );
        // Check the 'else' part.
        node.getElseStmt().accept( this );
    }

    public void visitWhileNode(StatementNode.WhileNode node) {
        // Check the condition.
        node.setCondition( checkCondition( node.getCondition() ) );
        // Check the body of the loop.
        node.getLoopStmt().accept( this );
    }
    public ExpNode visitErrorExpNode(ExpNode.ErrorNode node) {
        // Nothing to do - already invalid.
        return node;
    }

    public ExpNode visitConstNode(ExpNode.ConstNode node) {
        // type already set up
        return node;
    }

    public ExpNode visitReadNode(ExpNode.ReadNode node) {
        node.setType( Type.INTEGER_TYPE );
        return node;
    }
    
    public ExpNode visitBinaryOpNode( ExpNode.BinaryOpNode node ) {
        /* Check arguments and determine their types */
        ExpNode left = node.getLeft().transform( this );
        node.setLeft( left );
        ExpNode right = node.getRight().transform( this );
        node.setRight( right );
        BinaryOperator op = node.getOp();
        SymEntry.OperatorEntry opEntry = symtab.lookupOperator( op.getName() );
        Type opType = opEntry.getType();
        /* If the binary operator is overloaded it will have a union type,
         * i.e., multiple possible types, otherwise it will have a function
         * type, with its argument type being a product of two types.
         */
        if( opType instanceof Type.FunctionType ) {
            /* Just one type for this operator. Coerce each operand
             * to the argument type of the operator and report any
             * type mismatch.
             */
            Type.FunctionType fType = (Type.FunctionType)opType;
            Type.ProductType opArgType = (Type.ProductType)fType.getArgType();
            node.setLeft( Coercion.coerce(left, opArgType.getLeftType() ) );
            node.setRight( Coercion.coerce(right, opArgType.getRightType() ) );
            node.setType( fType.getResultType() );
        } else if( opType instanceof Type.UnionType ) {
            for( Type t : ((Type.UnionType)opType).getTypes() ) {
                Type.FunctionType fType = (Type.FunctionType)t;
                Type.ProductType opArgTypes = 
                    (Type.ProductType)fType.getArgType();
                try {
                    /* Coerce both arguments to the argument type for 
                     * this operator type. If either coercion fails an
                     * exception will be trapped and an alternative 
                     * function type within the union tried.
                     */
                    ExpNode newLeft = 
                        opArgTypes.getLeftType().coerce( left );
                    ExpNode newRight = 
                        opArgTypes.getRightType().coerce( right );
                    /* Both coercions succeeded if we get here */
                    node.setLeft( newLeft );
                    node.setRight( newRight );
                    node.setType( fType.getResultType() );
                    return node;
                } catch ( IncompatibleTypes ex ) {
                    // Allow "for" loop to try an alternative
                }
            }
            // no match in union
            error( "Type of arguments (" + left.getType() + "," + 
                    right.getType() + ")" + " do not match " + opType, 
                    node.getPosition() );
            node.setType( Type.ERROR_TYPE );
        } else {
            fatal( "Invalid operator type", node.getPosition() );
        }
        return node;
    }
    public ExpNode visitUnaryOpNode( ExpNode.UnaryOpNode node ) {
        /* Unary operators aren't overloaded */
        ExpNode subExp = node.getSubExp().transform( this );
        Type.FunctionType fType = 
            (Type.FunctionType)node.getOp().getType();
        node.setSubExp( Coercion.coerce( subExp, fType.getArgType() ) );
        node.setType( fType.getResultType() );
        return node;
    }
    public ExpNode visitArgumentsNode( ExpNode.ArgumentsNode node ) {
        List<ExpNode> newExps = new LinkedList<ExpNode>();
        List<Type> types = new LinkedList<Type>();
        for( ExpNode exp : node.getArgs() ) {
            newExps.add( exp.transform( this ) );
            types.add( exp.getType() );
        }
        node.setArgs( newExps );
        node.setType( new Type.ProductType( types ) );
        return node;
    }
    public ExpNode visitDereferenceNode(ExpNode.DereferenceNode node) {
        // Check the left value referred to by this right value.
        ExpNode lVal = node.getLeftValue().transform( this );
        node.setLeftValue( lVal );
        /* The type of the right value is the base type of the left value. */
        Type lValueType = lVal.getType();
        if( lValueType instanceof Type.ReferenceType ) {
            node.setType( ((Type.ReferenceType)lValueType).getBaseType() );
        } else if( lValueType != Type.ERROR_TYPE ) {
            error( "cannot dereference an expression which isn't a reference",
                    node.getPosition() );
        }
        return node;
    }
    /** When parsing an identifier within an expression one can't tell
     * whether it has been declared as a constant or an identifier.
     * Here we check which it is and return either a constant or 
     * a variable node.
     */
    public ExpNode visitIdentifierNode(ExpNode.IdentifierNode node) {
        // First we look up the identifier in the symbol table.
        ExpNode newNode;
        SymEntry entry = symtab.lookup( node.getId() );
        if( entry instanceof SymEntry.ConstantEntry ) {
            // Set up a new node which is a constant.
            SymEntry.ConstantEntry constEntry = 
                (SymEntry.ConstantEntry)entry;
            newNode = new ExpNode.ConstNode( node.getPosition(), 
                    constEntry.getType(), constEntry.getValue() );
        } else if( entry instanceof SymEntry.VarEntry ) {
            // Set up a new node which is a variable.
            SymEntry.VarEntry varEntry = (SymEntry.VarEntry)entry;
            newNode = new ExpNode.VariableNode(node.getPosition(), varEntry);
        } else {
            // Undefined identifier (or type or procedure identifier).
            // Set up new node to be an error node.
            newNode = new ExpNode.ErrorNode( node.getPosition() );
            error("Constant or variable identifier required", node.getPosition() );
        }
        // Check the created true node.
        newNode = newNode.transform( this );
        return newNode;
    }

    public ExpNode visitVariableNode(ExpNode.VariableNode node) {
        // Set the type of the node.
        node.setType( node.getVariable().getType() );
        return node;
    }
    public ExpNode visitNarrowSubrangeNode(ExpNode.NarrowSubrangeNode node) {
        // Nothing to do.
        return node;
    }

    public ExpNode visitWidenSubrangeNode(ExpNode.WidenSubrangeNode node) {
        // Nothing to do.
        return node;
    }
    
    public ExpNode visitRecordConstructorNode(ExpNode.RecordConstructorNode node) {
    	Type type = node.getType();
    	/*
    	Type refType = new Type.ReferenceType(type);
    	node.setType(refType);
    	*/
    	
    	/* To be dereferenced (using "^") an LValue, p, must be of type ref(PointerType(T))
    	 * for some type T */
    	if (type.getPointerType() == null) {
    		error("LValue must be of type ref(PointerType(" + type + "))", node.getPosition());
    	} else {
	    	node.setType(type.getPointerType());
    	}
    	
        return node;
    }
    
    /* Used when constructing a record with curlys */
	public ExpNode visitRecordFieldsNode(RecordFieldsNode node) {
		
		return null;
	}
	
	/* Used when accessing a record's fields */
	public ExpNode visitRecordEntryNode(RecordEntryNode node) {
		ExpNode record = node.getRecord().transform( this );
		Type.RecordType types = record.getType().getRecordType();
		boolean exists = false;
		
		
		/* Check field exists in record field */
		for (Field t : types.getFieldList()) {
			String recordField = t.getId();
			if (node.getField().equals(recordField)) {
				exists = true;
			}
		}
		
		if (!exists) {
			error("Field does not exist in record", node.getPosition());
		} else {
			node.setType(types.getFieldType(node.getField()));
		}
		
		return node;
	}

    /** Report a (semantic) error. */
    private void error(String message, Position pos) {
        errors.errorMessage( message, Severity.ERROR, pos );
    }
    /** Report a fatal error in static checker. */
    private void fatal(String message, Position pos) {
        errors.errorMessage( message, Severity.FATAL, pos );
    }
}
