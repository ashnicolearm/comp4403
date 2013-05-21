package tree;

/**
 * interface ExpTransform - Handles visitor pattern for transforming expressions.
 * @version $Revision: 15 $  $Date: 2013-05-08 13:58:21 +1000 (Wed, 08 May 2013) $
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
    ResultType visitActualParamNode(ExpNode.ActualParamNode node);
    ResultType visitActualParamListNode(ExpNode.ActualParamListNode node);
    ResultType visitRefParamNode(ExpNode.RefParamNode node);
}
