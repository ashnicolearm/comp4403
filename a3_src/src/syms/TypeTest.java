package syms;

import source.Position;
import tree.BinaryOperator;
import tree.ConstExp;
import tree.ExpNode;
import tree.Coercion.IncompatibleTypes;
import junit.framework.TestCase;

/**
 * class TestType - JUnit test for class Type.
 * @version $Revision: 14 $  $Date: 2013-05-08 10:40:38 +1000 (Wed, 08 May 2013) $
 */
public class TypeTest extends TestCase {

    public TypeTest(String arg0) {
        super(arg0);
    }

    Type et;
    Type.ScalarType it;
    Type.ScalarType bt;
    Type.ProcedureType pt;
    Type.SubrangeType ist;
    Type.SubrangeType bst;
    Type.SubrangeType isst;
    BinaryOperator addop;
    BinaryOperator eqop;
    Type.ReferenceType rit;
    Type.ProductType iit;
    Type.ProductType bbt;
    Type.FunctionType iiit;
    Type.FunctionType iibt;
    Type.FunctionType bbbt;
    ExpNode.ConstNode ix;
    ExpNode.VariableNode ivx;
    ExpNode.NarrowSubrangeNode isx;
    ExpNode.DereferenceNode rix;
    SymEntry.VarEntry iv;   
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Position nopos = Position.NO_POSITION;
        SymbolTable symtab = new SymbolTable();
        et = Type.ERROR_TYPE;
        it = Type.INTEGER_TYPE;
        bt = Type.BOOLEAN_TYPE;
        pt = new Type.ProcedureType();
        ist = new Type.SubrangeType(  
                new ConstExp.NumberNode( Position.NO_POSITION, 
                        symtab.getCurrentScope(), it, 3),
                new ConstExp.NumberNode( Position.NO_POSITION,
                        symtab.getCurrentScope(), it, 7) );
        ist.resolveType(nopos);
        bst = new Type.SubrangeType(  
                new ConstExp.NumberNode( Position.NO_POSITION, 
                        symtab.getCurrentScope(), bt, 0),
                new ConstExp.NumberNode( Position.NO_POSITION,
                        symtab.getCurrentScope(), bt, 1) );
        bst.resolveType(nopos);
        isst = new Type.SubrangeType(  
                new ConstExp.NumberNode( Position.NO_POSITION, 
                        symtab.getCurrentScope(), ist, 5),
                new ConstExp.NumberNode( Position.NO_POSITION,
                        symtab.getCurrentScope(), ist, 7) );
        isst.resolveType(nopos);
        addop = BinaryOperator.ADD_OP;
        eqop = BinaryOperator.EQUALS_OP;
        
        rit = new Type.ReferenceType( it );
        iit = new Type.ProductType( it, it );
        bbt = new Type.ProductType( bt, bt );
        iiit = new Type.FunctionType( iit, it );
        iibt = new Type.FunctionType( iit, bt );
        bbbt = new Type.FunctionType( bbt, bt );
        
        ix = new ExpNode.ConstNode(null, it, 42 );
        ix.setType( it );
        iv = new SymEntry.VarEntry( "iv", null, symtab.getCurrentScope(), rit );
        ivx = new ExpNode.VariableNode(null, iv );
//      ivx.setType( it );
        rix = new ExpNode.DereferenceNode( it, ivx );
        isx = new ExpNode.NarrowSubrangeNode(null, ist, ix );
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'pl0.symbol_table.Type.getSpace()'
     */
    public void testGetSpace() {
        assertEquals( 0, et.getSpace() );
        assertEquals( 1, it.getSpace() );
        assertEquals( 1, bt.getSpace() );
        assertEquals( 2, pt.getSpace() );
        assertEquals( 1, ist.getSpace() );
        assertEquals( 3, ist.getLower() );
        assertEquals( 7, ist.getUpper() );
        assertEquals( 1, bst.getSpace() );
        assertEquals( 0, bst.getLower() );
        assertEquals( 1, bst.getUpper() );
        assertEquals( 1, rit.getSpace() );
        assertEquals( 2, iit.getSpace() );
        assertEquals( 2, bbt.getSpace() );
    }

    /*
     * Test method for 'pl0.symbol_table.Type.coerce()'
     */
    public void testCoerce() throws IncompatibleTypes {
        ExpNode result = it.coerce( ix );
        assertTrue( "int compatible with int",
                result == ix );
        result = it.coerce( ivx );
        assertTrue( "int variable coerces to dereference",
                result instanceof ExpNode.DereferenceNode &&
                ((ExpNode.DereferenceNode)result).getLeftValue() == ivx);
        result = ist.coerce( ix );
        assertTrue( "int coerces to subrange of int",
                result instanceof ExpNode.NarrowSubrangeNode &&
                ((ExpNode.NarrowSubrangeNode)result).getExp() == ix );
        result = it.coerce( isx );
        assertTrue( "int subrange coerces to int" + result,
                result instanceof ExpNode.WidenSubrangeNode && 
                ((ExpNode.WidenSubrangeNode)result).getExp() == isx );
    }   
    /*
     * Test method for 'pl0.symbol_table.Type.toString()'
     */
    public void testToString() {
        assertEquals( "INT", it.toString() );
        assertEquals( "BOOLEAN", bt.toString() );
        assertEquals( "subrange(INT,3,7)", ist.toString() );
        assertEquals( "subrange(BOOLEAN,0,1)", bst.toString() );
        assertEquals( "ref(INT)", rit.toString() );
        assertEquals( "(INT*INT)", iit.toString() );
        assertEquals( "(BOOLEAN*BOOLEAN)", bbt.toString() );
        assertEquals( "((INT*INT)->INT)", iiit.toString() );
        assertEquals( "((INT*INT)->BOOLEAN)", iibt.toString() );
        assertEquals( "((BOOLEAN*BOOLEAN)->BOOLEAN)", bbbt.toString() );
    }
}
