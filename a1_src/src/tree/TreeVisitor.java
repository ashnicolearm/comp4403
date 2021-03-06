package tree;


/** 
 * interface TreeVisitor - Visitor pattern for declarations and procvedures.
 * @version $Id: TreeVisitor.java 8 2013-02-22 06:25:04Z ianh $
 * Provides the interface for the visitor pattern to be applied to an
 * abstract syntax tree. A class implementing this interface (such as the
 * static checker) must provide implementations for visit methods for
 * each of the tree node type. 
 * For example, the visit methods provided by the static checker tree
 * visitor implement the type checks for each type of tree node. 
 */
public interface TreeVisitor {

    void visitProgramNode(Tree.ProgramNode node);

    void visitBlockNode(Tree.BlockNode node);

    void visitProcedureNode(DeclNode.ProcedureNode node);
}
