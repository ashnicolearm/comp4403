package tree;

/** 
 * interface StatementVisitor - Provides the interface for the visitor pattern 
 * to be applied to an abstract syntax tree node for a statement. 
 * @version $Id: StatementVisitor.java 8 2013-02-22 06:25:04Z ianh $
 * A class implementing this interface (such as the code generator) must 
 * provide implementations for visit methods for each of the statement 
 * node type. 
 * For example, the visit methods provided by the code generator statement
 * visitor implement the code generation for each type of statement node. 
 */
public interface  StatementTransform<ResultType> {
    
    ResultType visitStatementErrorNode( StatementNode.ErrorNode node );

    ResultType visitStatementListNode( StatementNode.ListNode node );

    ResultType visitAssignmentNode( StatementNode.AssignmentNode node);
    
    ResultType visitSingleAssignmentNode( StatementNode.SingleAssignmentNode node);

    ResultType visitWriteNode( StatementNode.WriteNode node);

    ResultType visitCallNode( StatementNode.CallNode node);
    ResultType visitIfNode( StatementNode.IfNode node);

    ResultType visitWhileNode( StatementNode.WhileNode node);

	ResultType visitSkipNode( StatementNode.SkipNode node);

	ResultType visitDoBranchNode( StatementNode.DoBranchNode doBranchNode);

	ResultType visitDoStatementNode( StatementNode.DoStatementNode doStatementNode);
}
