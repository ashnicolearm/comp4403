package tree;

/** 
 * interface ExpVisitor - Provides visitor pattern over expressions.
 * @version $Revision: 15 $  $Date: 2013-05-08 13:58:21 +1000 (Wed, 08 May 2013) $
 * A class implementing this interface must provide implementations 
 * for visit methods for each of the tree node type. 
 */
public interface ExpVisitor {

    void visitErrorExpNode(ExpNode.ErrorNode node);
    void visitConstNode(ExpNode.ConstNode node);
    void visitIdentifierNode(ExpNode.IdentifierNode node);
    void visitVariableNode(ExpNode.VariableNode node);
    void visitReadNode(ExpNode.ReadNode node);
    void visitBinaryOpNode(ExpNode.BinaryOpNode node);
    void visitUnaryOpNode(ExpNode.UnaryOpNode node);
    void visitArgumentsNode(ExpNode.ArgumentsNode node);
    void visitDereferenceNode(ExpNode.DereferenceNode node);
    void visitNarrowSubrangeNode(ExpNode.NarrowSubrangeNode node);
    void visitWidenSubrangeNode(ExpNode.WidenSubrangeNode node);
    void visitActualParamListNode(ExpNode.ActualParamListNode node);
    void visitActualParamNode(ExpNode.ActualParamNode node);
    void visitRefParamNode(ExpNode.RefParamNode node);
}
