package de.sirati97.minherit;

import com.sun.xml.internal.ws.wsdl.writer.document.ParamType;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.ClassWriter.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;

public class Processor {

    @SuppressWarnings("unchecked")
    public static  <TResult extends TBase, TInterface, TBase, TParent extends TBase, TRefImpl extends TInterface>
    Class<TResult> unify(Class<TInterface> interfaceClass, Class<TParent> parentClass, Class<TRefImpl> implClass, Supplier<IMapper> mapperSupplier) {
        Type implType = Type.getType(implClass);
        Type parentType = Type.getType(parentClass);
        Type interfaceType = Type.getType(interfaceClass);

        try {
            ClassNode parentCN = getNode(parentClass);
            ClassNode result = getNode(implClass);

            //ClassNode result = implCN;
            result.version = Math.max(parentCN.version, result.version);
            result.access = ACC_PUBLIC;

            String name = conv(implClass.getPackage().getName()) + "/autogenerated/" + parentClass.getSimpleName() + "Ext" + interfaceClass.getSimpleName();
            String oldSuperName = result.superName;
            String oldName = result.name;
            result.superName = parentType.getInternalName();
            result.name = name;

            MethodNode ctor = null;
            MethodInsnNode ctorSuperCall = null;
            for (MethodNode method:result.methods) {
                if ("<init>".equals(method.name)) {
                    ctor = method;
                }
                Iterable<AbstractInsnNode> iterable = method.instructions::iterator;
                for (AbstractInsnNode inst:iterable) {
                    if (inst instanceof MethodInsnNode) {
                        if (oldSuperName.equals(((MethodInsnNode) inst).owner)) {
                            ((MethodInsnNode) inst).owner = result.superName;
                            if (method == ctor) {
                                ctorSuperCall = (MethodInsnNode) inst;
                            }
                        }
                        if (oldName.equals(((MethodInsnNode) inst).owner)) {
                            ((MethodInsnNode) inst).owner = result.name;
                        }
                    }
                }
            }
            Consumer<Class<?>> fieldInit = (c)->{};
            fieldInit = fieldInit.andThen(ctorMapper(Type.getArgumentTypes(ctor.desc), Type.getArgumentTypes(ctorSuperCall.desc), 0, mapperSupplier, result, ctor));



            ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
            result.accept(cw);
            byte[] b = cw.toByteArray();

            /*TraceClassVisitor pcv = new TraceClassVisitor(new PrintWriter(System.out));
            ClassReader cr = new ClassReader(b, 0, b.length);
            cr.accept(pcv, EXPAND_FRAMES);*/

            Class<TResult> resultClass = (Class<TResult>)  loadClass(b);
            fieldInit.accept(resultClass);
            return resultClass;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final Type IMapperType = getType(IMapper.class);
    private static final Type SupplierType = getType(Supplier.class);
    private static final Type ObjectType = getType(Object.class);
    private static final String[] EMPTY_ARRAY = new String[0];
    protected static Consumer<Class<?>> ctorMapper(Type[] from, Type[] to, int mapperId, Supplier<IMapper> mapperSupplier, ClassNode classNode, MethodNode originalCtor) {
        Type mapperSupplierType = getType(mapperSupplier.getClass());
        String fieldname = "_minherit_autogen_ctor_mapper_factory_" + mapperId;
        FieldNode mapperFactory = new FieldNode(ACC_PUBLIC | ACC_STATIC, fieldname, SupplierType.getDescriptor(), null, null);
        classNode.fields.add(mapperFactory);
        //wrint in
        MethodNode adapterIn = new MethodNode(originalCtor.access, "<init>", originalCtor.desc, originalCtor.signature, EMPTY_ARRAY);
        setLineNumber(adapterIn, Integer.MAX_VALUE);

        adapterIn.visitVarInsn(ALOAD, 0);
        adapterIn.visitFieldInsn(GETSTATIC, classNode.name, fieldname, SupplierType.getDescriptor());
        adapterIn.visitMethodInsn(INVOKEINTERFACE, SupplierType.getInternalName(), "get", Type.getMethodDescriptor(ObjectType), true);
        adapterIn.visitTypeInsn(CHECKCAST, IMapperType.getInternalName());

        for (int i = 0; i < from.length; i++) {
            Type fromType = from[i];
            Type paramType = fromType.getSort() == OBJECT?ObjectType:fromType;

            loadIntConst(adapterIn, i);
            adapterIn.visitVarInsn(ILOAD, i+1);
            adapterIn.visitMethodInsn(INVOKEINTERFACE, IMapperType.getInternalName(), mapperIn(paramType), Type.getMethodDescriptor(IMapperType, INT_TYPE, paramType), true);
        }
        adapterIn.visitMethodInsn(INVOKESPECIAL, classNode.name, "<init>", "(Lde/sirati97/minherit/IMapper;)V", false);

        Iterable<AbstractInsnNode> iterable = originalCtor.instructions::iterator;
        boolean found = false;
        for (AbstractInsnNode inst:iterable) {
            if (found) {
                inst.accept(adapterIn);
            }
            if (inst instanceof MethodInsnNode) {
                MethodInsnNode methodInsnNode = (MethodInsnNode) inst;
                if (methodInsnNode.getOpcode() == INVOKESPECIAL && "<init>".equals(methodInsnNode.name)) {
                    found = true;
                }
            }
        }
        adapterIn.visitMaxs(0,0); //apparently this makes it calc it
        adapterIn.visitEnd();
        classNode.methods.remove(originalCtor);
        classNode.methods.add(adapterIn);

        //write out
        MethodNode adapterOut = new MethodNode(ACC_PRIVATE, "<init>", getMethodDescriptor(VOID_TYPE, IMapperType), null, EMPTY_ARRAY);
        setLineNumber(adapterOut, Integer.MAX_VALUE);
        adapterOut.visitVarInsn(ALOAD, 0);
        for (int i = 0; i < to.length; i++) {
            Type toType = to[i];
            adapterOut.visitVarInsn(ALOAD, 1);
            loadIntConst(adapterOut, i);

            Type returnType = toType.getSort() == OBJECT?ObjectType:toType;
            adapterOut.instructions.add(new MethodInsnNode(INVOKEINTERFACE, IMapperType.getInternalName(), mapperOut(returnType), Type.getMethodDescriptor(returnType, INT_TYPE), true));
            if (toType.getSort() == OBJECT) {
                adapterOut.visitTypeInsn(CHECKCAST, toType.getDescriptor());
            }
        }

        adapterOut.visitMethodInsn(INVOKESPECIAL, classNode.superName, "<init>", Type.getMethodDescriptor(VOID_TYPE, to), false);
        adapterOut.visitInsn(RETURN);
        adapterOut.visitMaxs(0,0); //apparently this makes it calc it
        adapterOut.visitEnd();

        classNode.methods.add(adapterOut);

        return (clazz) -> {
            try {
                clazz.getDeclaredField(fieldname).set(null, mapperSupplier);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    public static Label setLineNumber(MethodNode mn, int line) {
        Label l0 = new Label();
        mn.visitLabel(l0);
        mn.visitLineNumber(line, l0);
        return l0;
    }

    private static void loadIntConst(MethodNode node, int value) {
        if (value >= 0 && value <= 5) {
            int op;
            switch (value) {
                case 0:op=ICONST_0;break;
                case 1:op=ICONST_1;break;
                case 2:op=ICONST_2;break;
                case 3:op=ICONST_3;break;
                case 4:op=ICONST_4;break;
                case 5:op=ICONST_5;break;
                default:
                    throw new IllegalStateException("Unexpected value: " + value);
            }
            node.visitInsn(op);
        } else  {
            node.visitVarInsn(BIPUSH, value);
        }

    }

    private static String mapperOut(Type type) {
        switch (type.getSort()) {
            case BOOLEAN: return "outBoolean";
            
            case BYTE: return "outByte";
            case SHORT: return "outShort";
            case INT: return "outInt";
            case Type.LONG: return "outLong";
            
            case Type.FLOAT: return "outFloat";
            case Type.DOUBLE: return "outDouble";
            
            case CHAR: return "outChar";
            default: return "outObj";
        }
    }


    private static String mapperIn(Type type) {
        switch (type.getSort()) {
            case BOOLEAN: return "inBoolean";

            case BYTE: return "inByte";
            case SHORT: return "inShort";
            case INT: return "inInt";
            case Type.LONG: return "inLong";

            case Type.FLOAT: return "inFloat";
            case Type.DOUBLE: return "inDouble";

            case CHAR: return "inChar";
            default: return "inObj";
        }
    }
    
    protected static String conv(String in) {
        return in.replace('.', '/');
    }


    public static ClassNode getNode(Class<?> clazz) {
        try {
            ClassNode cn = new ClassNode();
            ClassReader cr = new ClassReader(clazz.getResourceAsStream(clazz.getSimpleName() + ".class"));
            cr.accept(cn, EXPAND_FRAMES);
            return cn;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public static Class<?> loadClass(byte[] bytecode) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ClassLoader scl = ClassLoader.getSystemClassLoader();
        Object[] args = new Object[] {
                null, bytecode, 0, bytecode.length
        };
        Method m = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        m.setAccessible(true);
        return (Class<?>) m.invoke(scl, args);
    }
}
