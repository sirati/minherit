package de.sirati97.minherit.test;

import de.sirati97.minherit.Processor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

public class Test {
    public static void main(String... args) throws Exception {

        ClassNode example = Processor.getNode(ImplExample.class);
        TraceClassVisitor pcv = new TraceClassVisitor(null, new ASMifier(),new PrintWriter(System.out));
        example.accept(pcv);


        Class<Interface> unifiedClass = Processor.unify(Interface.class, Parent.class, Impl.class, MapperTestImpl::new);
        Interface i = unifiedClass.getConstructor(int.class, boolean.class).newInstance(143, false);


        i.foo();
    }
}
