package syms;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import machine.StackMachine;

import source.ErrorHandler;
import source.Position;
import source.Severity;
import syms.SymEntry.ConstantEntry;
import tree.Coercion;
import tree.ConstExp;
import tree.ExpNode;
import tree.Coercion.IncompatibleTypes;

/** This class provides the type structures defining the types
 * available in the language.
 * @version $Id: Type.java 10 2013-04-15 23:26:08Z uqihayes $
 * It provides subclasses for each of the different kinds of "type",
 * e.g., scalar types, subranges, products of types, function types,
 * unions of types, reference types, and the procedure type.
 * IdRefType is provided to allow type names to be used as types.
 * As well as the constructors for the types it provides a number of
 * access methods.
 * Each type provides a method for coercing an expression to the type.
 * Type also provides structures for the predefined types and 
 * the special type ERROR_TYPE, which is used for handling type errors.
 */
public abstract class Type 
{
    /** All types require space to be allocated (may be 0) */
    protected int space;
    /** Track whether type has been resolved */
    protected boolean resolved;
    /** Only subclasses provide public constructors. */
    private Type( int n ) {
        space = n;
        resolved = false;
    }
    /** return the space required for an element of the type */
    public int getSpace() {
        return space;
    }
    /** Resolve identifier references anywhere within type 
     * default does nothing and needs to be overridden when appropriate */
    public Type resolveType( Position pos ) {
        resolved = true;
        return this;
    }
    /** (Attempt to) coerce an expression node passed as a parameter to
     * be of this type. This default version is just checking
     * they are the same type. Subclasses of Type override this method.
     * @param exp to be coerce
     * @return resulting coerced expression node. 
     * @throws IncompatibleTypes exception if it can't coerce.
     */
    public ExpNode coerce(ExpNode exp ) throws IncompatibleTypes {
        Type fromType = exp.getType();
        if( this.equals( fromType ) ) {
            return exp;
        }
        throw new IncompatibleTypes( 
                "cannot treat " + fromType + " as " + this,
                exp.getPosition() );
    }
    /** Type equality. Overridden for most subclasses. */
    public boolean equals( Type other ) {
        return this == other;
    }
    @Override
    public String toString() {
        return "size " + space;
    }
    /** To avoid generating spurious error messages ERROR_TYPE
     * is compatible with everything.
     */
    public static final Type ERROR_TYPE = new Type(0) {
        
        @Override
        public ExpNode coerce( ExpNode exp ) {
            return exp;
        }
        @Override
        public String toString() { 
            return "ERROR_TYPE";
        }
    };  
    /** Predefined integer type. */
    public static final ScalarType INTEGER_TYPE = 
        new ScalarType( StackMachine.SIZE_OF_INT, 
                Integer.MIN_VALUE, Integer.MAX_VALUE ) {
            @Override
            public String toString() { 
                return "INT"; 
            }
        };
    /** Predefined boolean type. */
    public static final ScalarType BOOLEAN_TYPE = 
        new ScalarType( StackMachine.SIZE_OF_BOOLEAN, 
                StackMachine.FALSE_VALUE, StackMachine.TRUE_VALUE ) {
            @Override
            public String toString() { 
                return "BOOLEAN"; 
            }
        };
    public static final ProductType PAIR_INTEGER_TYPE =
        new ProductType( INTEGER_TYPE, INTEGER_TYPE );
                
    public static final ProductType PAIR_BOOLEAN_TYPE =
        new ProductType( BOOLEAN_TYPE, BOOLEAN_TYPE );
                
    public static final FunctionType ARITHMETIC_BINARY =
        new FunctionType( PAIR_INTEGER_TYPE, INTEGER_TYPE );
    
    public static final FunctionType INT_RELATIONAL_TYPE =
        new FunctionType( PAIR_INTEGER_TYPE, BOOLEAN_TYPE );
            
    public static final FunctionType BOOL_RELATIONAL_TYPE =
        new FunctionType( PAIR_BOOLEAN_TYPE, BOOLEAN_TYPE );
    
    public static final FunctionType ARITH_UNARY =
        new FunctionType( INTEGER_TYPE, INTEGER_TYPE );
            
    public static final FunctionType LOGICAL_UNARY =
        new FunctionType( BOOLEAN_TYPE, BOOLEAN_TYPE );
            
    /** Predefined type of a nil pointer */
    public static final PointerType NIL_TYPE =
        new PointerType( ERROR_TYPE ) {
            @Override
            public String toString() {
                return "NIL_TYPE";
            }
    };
    public static final ProductType NIL_TYPE_PAIR =
        new ProductType( NIL_TYPE, NIL_TYPE);
    public static final FunctionType NIL_RELATIONAL_TYPE =
        new FunctionType( NIL_TYPE_PAIR, BOOLEAN_TYPE );

    /** Scalar types are simple unstructured types that just have a range of
     * possible values. int and boolean are scalar types */
    public static class ScalarType extends Type {
    /** lower and upper bounds of scalar type */
        protected int lower, upper;
 
        public ScalarType( int size, int lower, int upper ) {
            super( size );
            this.lower = lower;
            this.upper = upper;
        }
        /** Constructor when bounds evaluated later */
        public ScalarType( int size ) {
            super( size );
        }
        /** The least element of the type */
        public int getLower() {
            return lower;
        }
        protected void setLower( int lower ) {
            this.lower = lower;
        }
        /** The greatest element of the type */
        public int getUpper() {
            return upper;
        }
        protected void setUpper( int upper ) {
            this.upper = upper;
        }
        /** Coerce expression to this type 
         * @throws IncompatibleTypes */
        @Override
        public ExpNode coerce( ExpNode exp ) throws IncompatibleTypes {
            return Coercion.coerceToScalarType( exp, this );
        }
    }
    /** If SubrangeType then cast to SubrangeType and return
     * else return null */
    public SubrangeType getSubrangeType() {
        if( this instanceof SubrangeType ) {
            return (SubrangeType)this;
        }
        return null;
    }
    /** If this is a subrange type widen it to its base type. */
    public Type optWidenSubrange() {
        if( this instanceof SubrangeType ) {
            return ((SubrangeType)this).getBaseType();
        }
        return this;
    }
    /** Types defined as a subrange of a scalar type. */
    public static class SubrangeType extends ScalarType {
        /** The base type of the subrange type */
        private Type baseType;
        /** Constant expression trees for lower and upper bounds 
         * before evaluation */
        private ConstExp lowerExp, upperExp;

        public SubrangeType( ConstExp lowerExp, ConstExp upperExp ) {
            /** On a byte addressed machine, the size could be scaled to
             * just fit the subrange, e.g., a subrange of 0..255
             * might only require 1 byte.
             */
            super( StackMachine.SIZE_OF_INT );
            /* Symbol table scope in which expressions are evaluated */
            this.lowerExp = lowerExp;
            this.upperExp = upperExp;
        }
        public Type getBaseType() {
            return baseType;
        }
        @Override
        public ExpNode coerce( ExpNode e ) throws IncompatibleTypes {
            return Coercion.coerceToSubrangeType( e, this );
        }
        @Override
        public Type resolveType( Position pos ) {
            if( !resolved ) {
                lower = lowerExp.getValue();
                upper = upperExp.getValue();
                if( upper < lower ) {
                    error( "Upper bound of subrange less than lower bound", pos );
                }
                baseType = upperExp.getType();
                if( !upperExp.getType().equals(lowerExp.getType())) {
                    error( "Types of bounds of subrange should match", pos );
                    baseType = Type.ERROR_TYPE;
                }
                resolved = true;
            }
            return this;
        }
        @Override
        public boolean equals( Type other ) {
            if( other instanceof SubrangeType ) {
                SubrangeType otherSubrange = (SubrangeType)other;
                return baseType.equals( otherSubrange.getBaseType() ) &&
                        lower == otherSubrange.getLower() &&
                        upper == otherSubrange.getUpper();
            } else {
                return false;
            }
        }
        @Override
        public String toString() {
            return "subrange(" + baseType.toString() + "," +
                lower + "," + upper + ")";
        }   
    }

    /** Product types represent the product of a sequence of types */
    public static class ProductType extends Type {
        private List<Type> types;
        
        public ProductType() {
            super( 0 );
            types = new LinkedList<Type>();
        }
        public ProductType( Type... typeList ) {
            this();
            for(Type t : typeList ) {
                types.add( t );
            }
            space = calcSpace( types );
        }
        public ProductType( List<Type> types ) {
            super(0);
            this.types = types;
            space = calcSpace( types );
        }
        /** The space required for a product of types is the sum of
         * the spaces required for each type in the product.
         */
        private int calcSpace( List<Type> types ) {
            int space = 0;
            for( Type t : types ) {
                space += t.getSpace();
            }
            return space;
        }
        public List<Type> getTypes() {
            return types;
        }
        /** For a product of two types return first (left) */
        public Type getLeftType() {
            assert types.size() == 2;
            return types.get(0);
        }
        /** For a product of two types return second (right) */
        public Type getRightType() {
            assert types.size() == 2;
            return types.get(1);
        }
        /** Resolve identifier references anywhere within type */
        @Override
        public ProductType resolveType( Position pos ) {
            if( ! resolved ) {
                List<Type> resolvedTypes = new LinkedList<Type>();
                for( Type t : types ) {
                    resolvedTypes.add( t.resolveType( pos ) );
                }
                types = resolvedTypes;
            }
            return this;
        }
        @Override
        public boolean equals( Type other ) {
            if( other instanceof ProductType ) {
                List<Type> otherTypes = ((ProductType) other).getTypes();
                if( types.size() == otherTypes.size() ) {
                    Iterator<Type> iterateOther = otherTypes.iterator();
                    for( Type t : types ) {
                        Type otherType = iterateOther.next();
                        if( ! t.equals( otherType ) ) {
                            return false;
                        }
                    }
                    /* If we reach here then every type in the product has
                     * matched the corresponding type in the other product
                     */
                    return true;
                }
            }
            return false;
        }
        /** Coerce expression to this type
         * @param exp should be an ArgumentsNode with a list of 
         *     expressions of the same length as the product type 
         */
        @Override
        public ExpNode.ArgumentsNode coerce( ExpNode exp ) 
                throws IncompatibleTypes {
            return Coercion.coerceToProductType(exp, this);
        }
        @Override
        public String toString() {
            String result = "(";
            String sep = "";
            for( Type t: types ) {
                result += sep + t;
                sep = "*";
            }
            return result + ")";
        }
    }
    /** Function types represent a function from and argument type
     * to a result type.
     */
    public static class FunctionType extends Type {
        private Type argType;
        private Type resultType;
        
        public FunctionType( Type arg, Type result ) {
            super( 0 );
            this.argType = arg;
            this.resultType = result;
        }
        public Type getArgType() {
            return argType;
        }
        public Type getResultType() {
            return resultType;
        }
        /** Resolve identifier references anywhere within type */
        @Override
        public FunctionType resolveType( Position pos ) {
            if( ! resolved ) {
                argType = argType.resolveType( pos );
                resultType = resultType.resolveType( pos );
                resolved = true;
            }
            return this;
        }
        @Override
        public boolean equals( Type other ) {
            if( other instanceof FunctionType ) {
                FunctionType otherFunction = (FunctionType)other;
                return getArgType().equals(otherFunction.getArgType()) &&
                    getResultType().equals(otherFunction.getResultType());
            }
            return false;
        }
        @Override
        public String toString() {
            return "(" + argType + "->" + resultType + ")";
        }
    }
    /** Union types represent the union of a set of types */
    public static class UnionType extends Type {
        private List<Type> types;
        
        public UnionType() {
            super( 0 );
            types = new LinkedList<Type>();
        }
        /** Assumes the types are distinct */
        public UnionType( List<Type> typeList ) {
            super(0);
            for( Type t : typeList ) {
                addType( t );
            }
            space = calcSpace( types );
        }
        /** Assumes the types are distinct */
        public UnionType( Type... typeArray ) {
            this();
            for( Type t : typeArray ) {
                addType( t );
            }
            space = calcSpace( types );
        }
        /** Add a type to the list of types, but if it is a UnionType
         * flatten it and add each type in the union. 
         */
        public void addType( Type t ) {
            if( t instanceof UnionType ) {
                types.addAll( ((UnionType)t).getTypes() );
            } else {
                types.add( t );
            }
        }
        /** Space required by a union of types is the maximum of
         * the spaces required by each type in the union.
         */
        private int calcSpace( List<Type> types) {
            int space = 0;
            for( Type t : types ) {
                if( space < t.getSpace() ) {
                    space = t.getSpace();
                }
            }
            return space;
        }
        public List<Type> getTypes() {
            return types;
        }
        /** Resolve identifier references anywhere within type */
        @Override
        public UnionType resolveType( Position pos ) {
            if( !resolved ) {
                List<Type> resolvedTypes = new LinkedList<Type>();
                for( Type t : types ) {
                    resolvedTypes.add( t.resolveType( pos ) );
                }
                types = resolvedTypes;
            }
            return this;
        }
        @Override
        public boolean equals( Type other ) {
            if( other instanceof UnionType ) {
                List<Type> otherTypes = ((UnionType) other).getTypes();
                if( types.size() == otherTypes.size() ) {
                    for( Type t : types ) {
                        if( ! otherTypes.contains( t ) ) {
                            return false;
                        }
                    }
                    /** If we reach here then all types in this union
                     * are also contained in the other union, and hence
                     * the two unions are equivalent.
                     */
                    return true;
                }
            }
            return false;
        }
        /** An ExpNode can be coerced to a UnionType if it can be coerced
         * to one of the types of the union.
         * @throws IncompatibleTypes 
         */
        @Override
        public ExpNode coerce( ExpNode exp ) throws IncompatibleTypes {
            return Coercion.coerceToUnionType( exp, this );
        }
        @Override
        public String toString() {
            String s = "(";
            String sep = "";
            for( Type t : types ) {
                s += sep + t;
                sep = " | ";
            }
            return s + ")";
        }
    }
    /** Type for a procedure. */
    public static class ProcedureType extends Type {
        
        public ProcedureType() {
            super(2); // allow for procedures as parameters
        }
        @Override
        public Type resolveType( Position pos ) {
            return this;
        }
        @Override
        public String toString() {
            String s = "PROCEDURE"; 
            return s;
        }
    }

    /** If this type is an IdRefType then cast to IdRefType and return
     * else return null */
    public IdRefType getIdRefType() {
        if( this instanceof IdRefType ) {
            return (IdRefType)this;
        }
        return null;
    }
    /** Type for a type identifier. Used until the type identifier can
     * be resolved.
     */
    public static class IdRefType extends Type {
        /** Name of the referenced type */
        private String name;
        /** Symbol table scope at the point of definition of the type
         * reference. Used when resolving the reference. */
        private Scope scope;
        /** Position of use of type identifier */
        Position pos;
        /** Resolved real type, or ERROR_TYPE if can't be resolved. */
        private Type realType;
        /** Status of resolution of reference. */
        private enum Status{ Unresolved, Resolving, Resolved }
        private Status status;
        
        public IdRefType( String name, Scope scope, Position pos ) {
            super( 0 );
            this.name = name;
            this.scope = scope;
            this.pos = pos;
            this.status = Status.Unresolved;
        }
        public String getName() {
            return name;
        }
        public void checkDefined() {
            SymEntry typeEntry = scope.lookup( name );
            if( typeEntry == null || !(typeEntry instanceof SymEntry.TypeEntry) ) {
                error( "Type " + name + " undefined", pos );
            }
        }
        @Override
        public Type resolveType( Position pos ) {
            // System.out.println( "Resolving " + name );
            switch( status ) {
            case Unresolved:
                status = Status.Resolving;
                realType = Type.ERROR_TYPE;
                SymEntry entry = scope.lookup( name );
                if( entry != null && entry instanceof SymEntry.TypeEntry ) {
                    /* resolve identifiers in the referenced type */
                    entry.resolve();
                    // if status of this entry has resolved then there
                    // was a circular reference and we leave the realType
                    // as ERROR_TYPE to avoid other parts of the compiler
                    // getting into infinite loops chasing types.
                    if( status == Status.Resolving ) {
                        realType = entry.getType();
                    }
                    assert realType != null;
                } else {
                    error( "Undefined type: " + name, pos );
                }
                status = Status.Resolved;
                break;
            case Resolving:
                error( name + " is circularly defined", pos );
                /* Will resolve to ERROR_TYPE */
                status = Status.Resolved;
                break;
            case Resolved:
                /* Already resolved */
                break;
            }
            return realType;
        }
        @Override
        public String toString() {
            return name;
        }
    }
    /** AddressType is the common part of ReferenceType (and PointerType) */ 
    public static class AddressType extends Type {
        protected Type baseType;
        
        public AddressType( Type baseType ) {
            super( StackMachine.SIZE_OF_ADDRESS );
            this.baseType = baseType;
        }
        public Type getBaseType() {
            return baseType;
        }
        @Override
        public Type resolveType( Position pos ) {
            if( !resolved ) {
                baseType = baseType.resolveType( pos );
                if( baseType == Type.ERROR_TYPE ) {
                    return Type.ERROR_TYPE;
                }
                resolved = true;
            }
            return this;
        }
        @Override
        public String toString() {
            return "address(" + baseType + ")";
        }
    }
    public static Type optDereference( Type t ) {
        if( t instanceof ReferenceType ) {
            return ((ReferenceType)t).getBaseType();
        }
        return t;
    }
    public static class ReferenceType extends AddressType {
        
        public ReferenceType( Type baseType ) {
            super( baseType );
        }
        @Override
        public boolean equals( Type other ) {
            return other instanceof ReferenceType &&
                ((ReferenceType)other).getBaseType().equals(
                        this.getBaseType() );
        }
        @Override
        public String toString() {
            return "ref(" + baseType + ")";
        }
    }
    
    public PointerType getPointerType() {
        Type thisType = this.resolveType( Position.NO_POSITION );
        if( thisType instanceof ReferenceType ) {
            thisType = ((ReferenceType)thisType).getBaseType();
        }
        if( thisType instanceof PointerType ) {
            return (PointerType)thisType;
        }
        return null;
    }
    public static class PointerType extends AddressType {
        
        public PointerType( Type baseType ) {
            super( baseType ); // A pointer is an address
        }
        /** When resolving a pointer type we don't resolve
         * its baseType as that may generate an incorrect
         * error message indicating the type is circularly defined.
         * Circularity via pointer is OK.
         * As the base type must be an identifier, its type
         * will be resolved separately.
         */
        @Override
        public Type resolveType( Position pos ) {
            if( !resolved ) {
                /* Base type must be a type identifier */
                if( baseType instanceof IdRefType ) {
                    ((IdRefType)baseType).checkDefined();
                } else {
                    fatal( "Base type of pointer type is not a type identifier", 
                            Position.NO_POSITION );
                }
                resolved = true;
            }
            return this;
        }
        @Override
        public Type getBaseType() {
            return baseType.resolveType( Position.NO_POSITION );
        }
        @Override
        public ExpNode coerce( ExpNode exp ) throws IncompatibleTypes {
            return Coercion.coerceToPointerType( exp, this );
        }
        @Override
        public boolean equals( Type other ) {
            return other instanceof PointerType &&
                ((PointerType)other).getBaseType().equals(
                        this.getBaseType() );
        }
        @Override
        public String toString() {
            return "PointerType(" + baseType + ")";
        }
    }
    public RecordType getRecordType() {
        Type thisType = this;
        if( thisType instanceof Type.ReferenceType ) {
            thisType = ((Type.ReferenceType)thisType).getBaseType();
        }
        if( thisType instanceof RecordType ) {
            return (RecordType)thisType;
        }
        return null;
    }
    /** Type representing a record. */
    public static class RecordType extends Type {
        private Position pos;
        private List<Field> fieldList; /* In order declared */
        private SortedMap<String,Field> fields;
        private boolean allocated;
        
        public RecordType() {
            super( 0 );           // Type size will need to be resolved later
            this.pos = Position.NO_POSITION;
            this.fieldList = new ArrayList<Field>();
            this.fields = new TreeMap<String,Field>();
            allocated = false;
        }
        /** When retrieving the space required for a record.
         * @return the number of words required to store a single element
         *         of this type.
         */
        @Override
        public int getSpace() {
            assert allocated;
            /* If the offsets of the fields haven't already been allocated,
             * do so. 
             */
            return space;
        }
        public Position getPosition() {
            return pos;
        }
        public void setPosition( Position pos ) {
            this.pos = pos;
        }
        public List<Field> getFieldList() {
            return fieldList;
        }
        @Override
        public ExpNode coerce( ExpNode exp ) throws IncompatibleTypes {
            return Coercion.coerceToType( exp , this );
        }
        /** @param id - name of the field
         * @return true iff id is a field of the record.
         */
        public boolean containsField( String id ) {
            return fields.containsKey( id );
        }
        /** Add a field to a record. */
        public void add( Field f ) {
            fieldList.add( f );
            fields.put( f.getId(), f );
        }
        /** Lookup a field
         * @param id name of field
         * @return the field's type, or ERROR_TYPE if not a field
         */
        public Type getFieldType( String id ) {
            Field field = fields.get( id );
            if( field == null ) {
                return ERROR_TYPE;
            }
            return field.getType();
        }
        /** Get the location of a field in a record.
         * @param id - the name of the field
         * @return the offset of the field from the start of the record
         */
        public int getOffset( String id ) {
            assert fields.containsKey( id );
            assert allocated;
            Field field = fields.get( id );
            return field.getOffset();
        }
        /** Allocate the offsets of the fields and 
         * calculate the size of the record 
         */
        @Override
        public Type resolveType( Position pos ) {
            if( !allocated ) {
                int offset = 0;
                /* Allocate fields in the order declared */
                for( Field field : fieldList ) {
                    field.setOffset( offset );
                    Type fieldType = field.resolveType();
                    offset += fieldType.getSpace();
                }
                space = offset;
                allocated = true;
            }
            return this;
        }
        @Override
        public String toString() {
            String result = "RECORD ";
            String sep = "";
            for( Field field : fields.values() ) {
                result += sep + field.toString();
                sep = "; ";
            }
            return result + " END";
        }
    }
    /** A single field in a record */
    public static class Field {
        private Position pos;
        private String id;
        private Type type;
        private Type declaredType;
        private int offset;
        
        public Field( Position pos, String id, Type t ) {
            this.pos = pos;
            this.id = id;
            this.type = t;
            this.declaredType = t;
        }
        public Position getPosn() {
            return pos;
        }
        public String getId() {
            return id;
        }
        public Type resolveType() {
            type = type.resolveType( pos );
            return type;
        }
        public Type getType() {
            return type;
        }
        public int getOffset() {
            return offset;
        }
        public void setOffset( int offset ) {
            this.offset = offset;
        }
        @Override
        public String toString() {
            return id + ":" + declaredType;
        }
    }
    public static void error( String message, Position pos ) {
        ErrorHandler.getErrorHandler().errorMessage( message, 
                Severity.ERROR, pos);
    }
    public static void fatal( String message, Position pos ) {
        ErrorHandler.getErrorHandler().errorMessage( message, 
                Severity.FATAL, pos);
    }
}
