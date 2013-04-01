package tree;

/** 
 * interface ExpVisitor - Provides visitor pattern over expressions.
 * @version $Id: ExpVisitor.java 8 2013-02-22 06:25:04Z ianh $
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

}
