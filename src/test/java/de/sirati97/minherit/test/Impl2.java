package de.sirati97.minherit.test;


import de.sirati97.minherit.IMapper;

import java.util.function.Supplier;

public abstract class Impl2 extends Base implements Interface {
    public static Supplier<IMapper> mapperCtor1;// = MapperTestImpl::new;

    public Impl2(int num, boolean human) {
        super(null); //this line is going to get omitted
        System.out.println("Impl got " + num + "&" + human);
    }

    @Override
    public void foo() {
        System.out.println("foo of Impl");

        super.foo();
    }


    static {
        int i = 1;
        i = i + i;

    }

}
