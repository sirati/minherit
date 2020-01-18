package de.sirati97.minherit.test;


import de.sirati97.minherit.IMapper;

import java.util.function.Supplier;

public abstract class Impl extends Base implements Interface {
    public static Supplier<IMapper> mapperCtor1;// = MapperTestImpl::new;

    public Impl(int num, boolean human) {
        super(null); //this line is going to get omitted
        System.out.println("Impl got " + num + "&" + human);
    }

    @Override
    public void foo() {
        System.out.println("foo of Impl");

        super.foo();
    }
}
