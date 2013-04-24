package tree;

/** 
 * interface ExpVisitor - Provides visitor pattern over expressions.
 * @version $Id: ExpVisitor.java 10 2013-04-15 23:26:08Z uqihayes $
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
    void visitRecordConstructorNode(ExpNode.RecordConstructorNode node);
    void visitRecordFieldsNode(ExpNode.RecordFieldsNode node);
    void visitRecordEntryNode(ExpNode.RecordEntryNode node);
    void visitPointerConstructorNode(ExpNode.PointerConstructorNode node);
    void visitPointerNode(ExpNode.PointerNode node);
}
