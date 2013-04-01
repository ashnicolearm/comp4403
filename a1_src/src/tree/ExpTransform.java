package tree;

/**
 * interface ExpTransform - Handles visitor pattern for transforming expressions.
 * @version $Id: ExpTransform.java 8 2013-02-22 06:25:04Z ianh $
 */
public interface ExpTransform<ResultType> {
    ResultType visitErrorExpNode(ExpNode.ErrorNode node);
    ResultType visitConstNode(ExpNode.ConstNode node);
    ResultType visitIdentifierNode(ExpNode.IdentifierNode node);
    ResultType visitVariableNode(ExpNode.VariableNode node);
    ResultType visitReadNode(ExpNode.ReadNode node);
    ResultType visitBinaryOpNode(ExpNode.BinaryOpNode node);
    ResultType visitUnaryOpNode(ExpNode.UnaryOpNode node);
    ResultType visitArgumentsNode(ExpNode.ArgumentsNode node);
    ResultType visitDereferenceNode(ExpNode.DereferenceNode node);
    ResultType visitNarrowSubrangeNode(ExpNode.NarrowSubrangeNode node);
    ResultType visitWidenSubrangeNode(ExpNode.WidenSubrangeNode node);
}
