package tree;

/**
 * interface ExpTransform - Handles visitor pattern for transforming expressions.
 * @version $Id: ExpTransform.java 10 2013-04-15 23:26:08Z uqihayes $
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
