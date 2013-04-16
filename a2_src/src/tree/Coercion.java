package tree;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import source.ErrorHandler;
import source.Position;
import source.Severity;
import syms.Type;
import syms.Type.ReferenceType;
import syms.Type.SubrangeType;

/** class Coercion provides a set of procedures for coercing an expression
 * (ExpNode) to a type. There are a couple of general versions of coercion
 * and then versions specific to the form of the type.
 * @version $Id: Coercion.java 9 2013-04-10 05:36:09Z ianh $
 */
public class Coercion {

    /** The coercion procedures will throw an IncompatibleTypes exception
     * if the expression being coerced can't be coerced to the given type.
     */
    public static class IncompatibleTypes extends Exception {
        Position pos;
        
        /** Constructor.
         * @param msg error message to be reported
         * @param pos position of the expression for error reporting
         */
        public IncompatibleTypes( String msg, Position pos ) {
            super( msg );
            this.pos = pos;
        }
        public Position getPosition() {
            return pos;
        }
    }
    
    /** Coerce an expression to a type and report error if incompatible */
    public static ExpNode coerce( ExpNode exp, Type toType ) {
        try {
            return toType.coerce( exp );
        } catch( IncompatibleTypes e ) {
            /** At this point the coercion has failed. 
             * If the type of exp is ERROR_TYPE that means a type error has
             * already been detected for the expression and it is annoying 
             * to give another error message for the same expression. */
            if( !exp.getType().equals( Type.ERROR_TYPE ) ) {
                ErrorHandler.getErrorHandler().errorMessage( 
                    "cannot coerce type " + exp.getType() + " to " + toType,
                    Severity.ERROR, e.getPosition() );
            }
            return exp; 
        }
    }
    /** This method implements Rule Dereference in the static semantics if
     * applicable, otherwise it leaves the expression unchanged. 
     * Optionally dereference a Reference type expression to get its base type
     * If exp is type ReferenceType(T) for some base type T,
     * a new DereferenceNode of type T is created with exp as a subtree
     * and returned, otherwise exp is returned unchanged.
     */
    public static ExpNode optDereference( ExpNode exp ) {
        Type fromType = exp.getType();
        if( fromType instanceof ReferenceType ) {
            return new ExpNode.DereferenceNode( 
                    ((ReferenceType)fromType).getBaseType(), exp );
        } else {
            return exp;
        }
    }
    /** Coerce expression to type toType
     * The objective is to create an expression of type toType
     * from exp.
     * @throws IncompatibleTypes exception if it is not possible to coerce 
     *         exp to type toType
     */
    public static ExpNode coerceToType( ExpNode exp, Type toType )
            throws IncompatibleTypes {
        /** First we optionally dereference exp, which if exp is of type
         * ReferenceType(T) will turn exp into an expression of type T
         * or leave exp alone otherwise. */ 
        ExpNode coerceExp = optDereference( exp );
        Type fromType = coerceExp.getType();
        if( toType.equals( fromType ) ) {
            return coerceExp;
        }
        throw new IncompatibleTypes( 
                "cannot treat " + exp.getType() + " as " + toType,
                exp.getPosition() );
    }
    /** Coerce expression to Scalar type toType.
     * The objective is to create an expression of the scalar type toType
     * from exp.
     * @throws IncompatibleTypes exception if it is not possible to coerce 
     *         exp to type toType
     */
    public static ExpNode coerceToScalarType(ExpNode exp, 
            Type.ScalarType toType ) throws IncompatibleTypes {
        /** First we optionally dereference exp, which if exp is of type
         * ReferenceType(T) will turn exp into an expression of type T
         * or leave exp alone otherwise. */ 
        ExpNode coerceExp = optDereference( exp );
        /* Extract the type of the (dereferenced) expression, coerceExp */
        Type fromType = coerceExp.getType();
        if( toType.equals( fromType ) ) {
            /** If the types match we are done */
            return coerceExp;
        } else if( fromType instanceof SubrangeType ) {
            /** This code implements Rule Widen subrange. 
             * If the types don't match, the only other possible type
             * for the expression which can be coerced to the scalar type 
             * toType is a subrange type, provided its base type matches
             * toType. If this is the case we insert a WidenSubrangeNode
             * of type toType and with the expression as a subtree.
             */
            Type baseType = ((SubrangeType)fromType).getBaseType();
            if( toType.equals( baseType ) ) {
                return new ExpNode.WidenSubrangeNode( exp.getPosition(), 
                        toType, coerceExp );
            }
        } 
        /** Otherwise we report the failure to coerce the expression via
         * an IncompatibleTypes exception.
         */
        throw new IncompatibleTypes( "can't coerce " + exp.getType() + 
                " to " + toType, exp.getPosition() );
    }
    /** Coerce expression to subrange type toType
     * The objective is to create an expression of the subrange type toType
     * from exp.
     * @throws IncompatibleTypes exception if it is not possible to coerce 
     *         exp to type toType
     */
    public static ExpNode coerceToSubrangeType( ExpNode exp, 
            Type.SubrangeType toType) throws IncompatibleTypes {
        /** First we optionally dereference exp, which if exp is of type
         * ReferenceType(T) will turn exp into an expression of type T
         * or leave exp alone otherwise. */ 
        ExpNode coerceExp = optDereference( exp );
        Type fromType = coerceExp.getType();
        if( toType.equals( fromType ) ) {
            /** If the types match we are done */
            return coerceExp;
        } else {
            /** This implements Rule Narrow subrange in the static semantics. 
             * If the types don't match, we can try coercing the expression
             * to the base type of the subrange toType, and then narrow that
             * to toType. If the coercion to the base type fails it will
             * generate an exception, which is allowed to pass up to the caller.
             */
            coerceExp = toType.getBaseType().coerce( coerceExp );
            /** If we get here, coerceExp is of the same type as the base 
             * type of the subrange type toType. We just need to narrow it
             * down to the subrange toType. 
             */
            return new ExpNode.NarrowSubrangeNode( exp.getPosition(), 
                        toType, coerceExp );
        }
    }
    /** Coerce expression to product type toProductType.
     * @param exp should be an ArgumentsNode with a list of 
     *     expressions of the same length as the product type 
     * @throws IncompatibleTypes exception if it is not possible to coerce 
     *         exp to type toProductType
     */
    public static ExpNode.ArgumentsNode coerceToProductType( ExpNode exp, 
            Type.ProductType toProductType ) 
            throws IncompatibleTypes {
        /** If exp is not an ArgumentsNode consisting of a list of expressions
         * of the same length as the product type toProductType, then we can't
         * coerce exp to toProductType and we raise an exception.
         */
        if( exp instanceof ExpNode.ArgumentsNode) {
            ExpNode.ArgumentsNode args = (ExpNode.ArgumentsNode)exp;
            if( toProductType.getTypes().size() == args.getArgs().size() ) {
                /** If exp is an ArgumentNode of the same size as toProductType,
                 * we coerce each expression in the list of arguments, to the
                 * corresponding type in the product, accumulating a new
                 * (coerced) list of expressions as we go.
                 * If any of the argument expressions can't be coerced,
                 * an exception will be raised, which we allow the
                 * caller to handle because the failure to coerce any
                 * expression in the list of arguments, corresponds to a 
                 * failure to coerce the whole arguments node.
                 */
                ListIterator<ExpNode> iterateArgs = 
                    args.getArgs().listIterator();
                List<ExpNode> newArgs = new LinkedList<ExpNode>();
                for( Type t : toProductType.getTypes() ) {
                    ExpNode subExp = iterateArgs.next();
                    /** Type incompatibilities detected in the
                     * coercion will generate an exception,
                     * which we allow to pass back up to the next level
                     */
                    ExpNode coerced = t.coerce( subExp );
                    newArgs.add( coerced );
                }
                /** If we get here, all expressions in the list have been
                 * successfully coerced to the corresponding type in the 
                 * product, and the coerced list of expressions newArgs will
                 * be of type toProductType. We return an ArgumentsNode of 
                 * type toProductType, with newArgs as its list of expressions.
                 */
                ExpNode.ArgumentsNode result = new ExpNode.ArgumentsNode( 
                        args.getPosition(), toProductType, newArgs );
                return result;
            } else {
                throw new IncompatibleTypes( 
                    "length mismatch in coercion to ProductType", 
                    exp.getPosition() );
            }
        } else {
            throw new IncompatibleTypes( 
                "Arguments node expected for coercion to ProductType",
                exp.getPosition() );
        }
    }
    /** An ExpNode can be coerced to a UnionType if it can be coerced
     * to one of the types of the union.
     * @throws IncompatibleTypes exception if it is not possible to coerce 
     *         exp to any type within the union
     */
    public static ExpNode coerceToUnionType( ExpNode exp, 
            Type.UnionType toUnionType ) throws IncompatibleTypes {
        /** We iterate through all the types in the union, trying 
         * to coerce the exp to each, until one succeeds and we return
         * that coerced expression. If a coercion to a type in the union 
         * fails it will throw an exception, which is caught. Once caught 
         * we ignore the exception, and allow the for loop to try the next
         * type in the union.
         */
        for( Type toType : toUnionType.getTypes() ) {
            try {
                return toType.coerce( exp );
            } catch( IncompatibleTypes ex ) {
                // allow "for" loop to try the next alternative 
            }
        }
        /** If we get here, we were unable to to coerce exp to any one of 
         * the types in the union, and hence we can't coerce exp the the 
         * union type.
         */
        throw new IncompatibleTypes( "none of types match",
                exp.getPosition() );
    }

    /** Coerce expression to type to a PointerType
     * The objective is to create an expression of type toType
     * from exp.
     * @throws IncompatibleTypes exception if it is not possible to coerce 
     *         exp to a Pointer Type
     */
    public static ExpNode coerceToPointerType( ExpNode exp, 
            Type.PointerType toType )
            throws IncompatibleTypes {
        /** First we optionally dereference exp, which if exp is of type
         * ReferenceType(T) will turn exp into an expression of type T
         * or leave exp alone otherwise. */ 
        ExpNode coerceExp = optDereference( exp );
        Type fromType = coerceExp.getType();
        if( toType.equals( fromType ) ) {
            return coerceExp;
        } else if( fromType.equals( Type.NIL_TYPE ) ) {
            return coerceExp;
        }
        throw new IncompatibleTypes( 
                "cannot treat " + exp.getType() + " as " + toType,
                exp.getPosition() );
    }
}
