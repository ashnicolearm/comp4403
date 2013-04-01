package tree;

/** 
 * interface StatementVisitor - Provides the interface for the visitor pattern 
 * to be applied to an abstract syntax tree node for a statement. 
 * @version $Id: StatementVisitor.java 8 2013-02-22 06:25:04Z ianh $
 * A class implementing this interface must provide implementations for visit 
 * methods for each of the statement node type. 
 */
public interface StatementVisitor {

    void visitStatementErrorNode( StatementNode.ErrorNode node );

    void visitStatementListNode( StatementNode.ListNode node );

    void visitAssignmentNode( StatementNode.AssignmentNode node);
    
    void visitSingleAssignmentNode( StatementNode.SingleAssignmentNode node);

    void visitWriteNode( StatementNode.WriteNode node);

    void visitCallNode( StatementNode.CallNode node);
    void visitIfNode( StatementNode.IfNode node);

    void visitWhileNode( StatementNode.WhileNode node);

	void visitSkipNode( StatementNode.SkipNode node);
}
