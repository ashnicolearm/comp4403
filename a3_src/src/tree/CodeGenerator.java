package tree;
import java.util.List;
import java.util.ListIterator;

import machine.Operation;
import machine.StackMachine;
import source.Errors;
import source.Position;
import source.Severity;
import syms.SymEntry;
import syms.Type;
import tree.Tree.*;

/** class CodeGenerator implements code generation using the
 * visitor pattern to traverse the abstract syntax tree.
 * @version $Revision: 16 $  $Date: 2013-05-08 14:06:51 +1000 (Wed, 08 May 2013) $ 
 */
public class CodeGenerator 
        implements TreeTransform<Code>, StatementTransform<Code>, 
                    ExpTransform<Code> {
    /** Current static level of nesting into procedures. */
    private int staticLevel;
    /** Current code location for tracking start addresses of procedures */
    private int start;
    /** Table of procedure start and finish addresses */
    private Procedures procStarts;
    /** Error message handler */
    Errors errors;
    
    public CodeGenerator(Errors errors) {
        super();
        this.errors = errors;
    }

    /*-------------------- Main Method to start code generation --------*/

    /** Main generate code for this tree. */
    public CodePlusProcedures generateCode( ProgramNode node ) {
        /* Main program is at static level 1. */
        staticLevel = 1;
        start  = StackMachine.CODE_START;
        procStarts = new Procedures();
        Code code = this.visitProgramNode( node );
        return new CodePlusProcedures( code, procStarts );
    }
    

    /* -------------------- Visitor methods ----------------------------*/
    
    /** Generate the code for the main program. */
    public Code visitProgramNode( ProgramNode node ) {
        Code code = new Code();
        /* Place dummy static and dynamic links on stack.
         * The stack machine begins execution with the frame pointer
         * equal to the stack pointer (both 0).
         * Hence the first value pushed is at the location
         * addressed by the frame pointer (fp). */
        code.generateOp( Operation.ZERO );  /* Push dummy static link */
        code.generateOp( Operation.ZERO );  /* Push dummy dynamic link */
        /* place return address from main program on stack:
         * a return address of 0 will terminate stack machine execution. */
        code.generateOp( Operation.ZERO );
        /* Update the current location in code generation */
        start += code.size(); 
        procStarts.addProcedureStart( "<main>", 
                node.getBlock().getBlockLocals(), start );
        /* generate code for body of program */
        code.append( node.getBlock().accept( this ) );
        return code;
    }

    /** Generate code for a block. */
    public Code visitBlockNode( BlockNode node ) {
        /** Generate code to allocate space for local variables on
         * procedure entry.
         */
        Code code = new Code();
        code.genAllocStack( node.getBlockLocals().getVariableSpace() );
        /* Generate the code for the body */
        code.append( node.getBody().genCode( this ) );
        code.generateOp( Operation.RETURN );
        /* Save finish address of procedure */
        start += code.size();
        procStarts.addProcedureFinish( start );
        /** Generate code for local procedures. */
        /* Static level is one greater for the procedures. */
        staticLevel++;
        code.append( node.getProcedures().accept(this) );
        staticLevel--;
        return code;
    }

    
    /** Code generation for a declaration list */
    public Code visitDeclListNode( DeclNode.DeclListNode node ) {
        Code code = new Code();
        for( DeclNode decl : node.getDeclarations() ) {
            code.append( decl.accept( this ) );
        }
        return code;
    }

    /** Generate code for a single procedure. */
    public Code visitProcedureNode( DeclNode.ProcedureNode node ) {
        SymEntry.ProcedureEntry proc = node.getProcEntry();
        /* Save start address of procedure */
        proc.setStart( start );
        procStarts.addProcedureStart( proc.getIdent(), 
                proc.getLocalScope(), start );
        // Generate code for the block
        return node.getBlock().accept( this );
    }
    /*************************************************
     *  Statement node code generation visit methods
     *************************************************/
    /** Code generation for an erroneous statement should not be attempted. */
    public Code visitStatementErrorNode( StatementNode.ErrorNode node ) {
        fatal( "PL0 Internal error: generateCode for ErrorStatementNode",
                node.getPosition() );
        return null;
    }

    /** Code generation for an assignment statement. */
    public Code visitAssignmentNode(StatementNode.AssignmentNode node) {
        /* Generate code to evaluate the expression */
        Code code = node.getExp().genCode( this );
        /* Generate the code to load the address of the variable */
        code.append( node.getVariable().genCode( this ) );
        Type.ReferenceType refType = 
            (Type.ReferenceType)node.getVariable().getType();
        int size = refType.getBaseType().getSpace();
        if (size == 1) {
            /* For an expression that can fit in a single word,
             *  store that into the variable.
             */
            code.generateOp( Operation.STORE_FRAME );
        } else {
            /* For the assignment of one multi-word variable to another 
             * generate a STORE_MULTI instruction to store the entire value.
             */
            code.genLoadConstant(size);
            code.generateOp(Operation.STORE_MULTI);
        }
        return code;
    }
 
    /** Generate code for a "write" statement. */
    public Code visitWriteNode( StatementNode.WriteNode node ) {
        Code code = node.getExp().genCode( this );
        code.generateOp( Operation.WRITE );
        return code;
    }

    /** Generate code for a "call" statement. */
    public Code visitCallNode( StatementNode.CallNode node ) {
        Code code = new Code();
        // Call the procedure
        SymEntry.ProcedureEntry proc = node.getEntry();
        List<ExpNode.ActualParamNode> actualParams = node.getActualParams().getActualParams();
        
        for (int i = actualParams.size() - 1; i >= 0; i--) {
        	code.append(actualParams.get(i).genCode(this));
        	
        	if (proc.getType().getParams().get(i) instanceof SymEntry.RefParamEntry) {
        		code.generateOp(Operation.TO_GLOBAL);
        	}
        }
        
        /* Generate the call instruction */
        code.genCall( staticLevel - proc.getLevel(), proc );
        
        /* Clean up stack */
        int paramSpace = proc.getLocalScope().getParameterSpace();
        code.genDeallocStack(paramSpace);
        
        return code;
    }
    /** Generate code for a statement list */
    public Code visitStatementListNode( StatementNode.ListNode node ) {
        Code code = new Code();
        for( StatementNode s : node.getStatements() ) {
            code.append( s.genCode( this ) );
        }
        return code;
    }

    /** Generate code for an "if" statement. */
    public Code visitIfNode(StatementNode.IfNode node) {
        /* Generate code to evaluate the condition and then and else parts */
        Code code = node.getCondition().genCode( this );
        Code thenCode = node.getThenStmt().genCode( this );
        Code elseCode = node.getElseStmt().genCode( this );
        /* Append a branch over then part to condition code */
        code.genJumpIfFalse( thenCode.size() + Code.SIZE_JUMP_ALWAYS );
        /* Next append the code for the then part */
        code.append( thenCode );
        /* Append branch over the else part */
        code.genJumpAlways( elseCode.size() );
        /* Finally append the code for the else part */
        code.append( elseCode );
        return code;
    }
 
    /** Generate code for a "while" statement. */
    public Code visitWhileNode(StatementNode.WhileNode node) {
        /* Generate the code to evaluate the condition. */
        Code code = node.getCondition().genCode( this );
        /* Generate the code for the loop body */
        Code bodyCode = node.getLoopStmt().genCode( this );
        /* Add a branch over the loop body on false.
         * The offset is the size of the loop body code plus 
         * the size of the branch to follow the body.
         */
        code.genJumpIfFalse( bodyCode.size() + Code.SIZE_JUMP_ALWAYS );
        /* Append the code for the body */
        code.append( bodyCode );
        /* Add a branch back to the condition.
         * The offset is the total size of the current code plus the
         * size of a Jump Always.
         */
        code.genJumpAlways( -(code.size() + Code.SIZE_JUMP_ALWAYS) );
        return code;
    }
    /*************************************************
     *  Expression node code generation visit methods
     *************************************************/
    /** Code generation for an erroneous expression should not be attempted. */
    public Code visitErrorExpNode( ExpNode.ErrorNode node ) { 
        fatal( "PL0 Internal error: generateCode for ErrorExpNode",
                node.getPosition() );
        return null;
    }

    /** Generate code for a constant expression. */
    public Code visitConstNode( ExpNode.ConstNode node ) {
        Code code = new Code();
        code.genLoadConstant( node.getValue() );
        return code;
    }

    /** Generate code for a "read" expression. */
    public Code visitReadNode( ExpNode.ReadNode node ) {
        Code code = new Code();
        code.generateOp( Operation.READ );
        return code;
    }
    /** Generate binary operator code with operands loaded in order */
    private Code genBinaryInOrder( ExpNode.BinaryOpNode node ) {
        Code code = node.getLeft().genCode( this );
        code.append( node.getRight().genCode( this ) );
        return code;
    }
    /** Generate binary operator operands in reverse order */
    private Code genBinaryInReverse( ExpNode.BinaryOpNode node ) {
        Code code = node.getRight().genCode( this );
        code.append( node.getLeft().genCode( this ) );
        return code;
    }

    /** Generate code for a binary expression. */
    public Code visitBinaryOpNode( ExpNode.BinaryOpNode node ) {
        Code code;
        switch ( node.getOp() ) {
        case ADD_OP:
            code = genBinaryInOrder( node );
            code.generateOp(Operation.ADD);
            break;
        case SUB_OP:
            code = genBinaryInOrder( node );
            code.generateOp(Operation.NEGATE);
            code.generateOp(Operation.ADD);
            break;
        case MUL_OP:
            code = genBinaryInOrder( node );
            code.generateOp(Operation.MPY);
            break;
        case DIV_OP:
            code = genBinaryInOrder( node );
            code.generateOp(Operation.DIV);
            break;
        case EQUALS_OP:
            code = genBinaryInOrder( node );
            code.generateOp(Operation.EQUAL);
            break;
        case LESS_OP:
            code = genBinaryInOrder( node );
            code.generateOp(Operation.LESS);
            break;
        case NEQUALS_OP:
            code = genBinaryInOrder( node );
            code.generateOp(Operation.EQUAL);
            code.genBoolNot();
            break;
        case LEQUALS_OP:
            code = genBinaryInOrder( node );
            code.generateOp(Operation.LESSEQ);
            break;
        case GREATER_OP:
            /* Generate argument values in reverse order and use LESS */
            code = genBinaryInReverse( node );
            code.generateOp(Operation.LESS);
            break;
        case GEQUALS_OP:
            /* Generate argument values in reverse order and use LESSEQ */
            code = genBinaryInReverse( node );
            code.generateOp(Operation.LESSEQ);
            break;
        default:
            fatal("PL0 Internal error: Unknown binary operator",
                    node.getPosition() );
            code = null;
        }
        return code;
    }
    /** Generate the code to load arguments (in order) */
    public Code visitArgumentsNode( ExpNode.ArgumentsNode node ) {
        Code code = new Code();
        for( ExpNode exp : node.getArgs() ) {
            code.append( exp.genCode( this ) );
        }
        return code;
    }
    /** Generate the code to load arguments (in reverse order) */
    public Code reverseArgGenerate( ExpNode.ArgumentsNode node ) {
        List<ExpNode> args = node.getArgs();
        Code code = new Code();
        for( int i = args.size()-1; 0 <= i; i-- ) {
            code.append( args.get(i).genCode( this ) );
        }
        return code;
    }
 
    /** Generate code for a unary expression. */
    public Code visitUnaryOpNode(ExpNode.UnaryOpNode node) {
        Code code = node.getSubExp().genCode( this );
        switch ( node.getOp() ) {
        case NEG_OP:
            code.generateOp(Operation.NEGATE);
            break;
        default:
            fatal("Internal error: unknown unary operator " + node.getOp(),
                    node.getPosition() );
            code = null;
        }
        return code;
    }
 
    /** Generate code to dereference an RValue. */
    public Code visitDereferenceNode( ExpNode.DereferenceNode node ) {
        ExpNode lval = node.getLeftValue();
        Code code = lval.genCode( this );
        if( node.getType().getSpace() == 1 ) {
            code.generateOp( Operation.LOAD_FRAME );
        } else {
            code.genLoadConstant( node.getType().getSpace() );
            code.generateOp( Operation.LOAD_MULTI );
        }
        return code;
    }

    /** Generate code for an identifier. */
    public Code visitIdentifierNode(ExpNode.IdentifierNode node) {
        /** Visit the corresponding constant or variable node. */
        fatal("Internal error: code generator called on IdentifierNode",
                node.getPosition() );
        return null;
    }
    /** Generate code for a variable (Exp) reference. */
    public Code visitVariableNode( ExpNode.VariableNode node ) {
        SymEntry.VarEntry var = node.getVariable();
        Code code = new Code();
        code.genMemRef( staticLevel - var.getLevel(), var.getOffset() );
        return code;
    }
    /** Generate code to perform a bounds check on a subrange. */
    public Code visitNarrowSubrangeNode(ExpNode.NarrowSubrangeNode node) {
        Code code = node.getExp().genCode( this );
        code.genBoundsCheck(node.getSubrangeType().getLower(), 
                node.getSubrangeType().getUpper());
        return code;
    }

    /** Generate code to widen a subrange to an integer. */
    public Code visitWidenSubrangeNode(ExpNode.WidenSubrangeNode node) {
        // Widening doesn't require anything extra
        return node.getExp().genCode( this );
    }
    
	public Code visitActualParamListNode(ExpNode.ActualParamListNode node) {
		return new Code();
	}

	public Code visitActualParamNode(ExpNode.ActualParamNode node) {
		/* Get code for the value param -> offset */
		return node.getCondition().genCode(this);
	}
	
	public Code visitRefParamNode(ExpNode.RefParamNode node) {
		Code code = new Code();
		code.genLoadConstant(node.getVariable().getOffset());
		code.generateOp(Operation.LOAD_FRAME);
		code.generateOp(Operation.TO_LOCAL);
		return code;
	}

    private void fatal( String message, Position pos ) {
        errors.errorMessage( message, Severity.FATAL, pos);
    }


}
