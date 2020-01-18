package de.sirati97.minherit.test;


import de.sirati97.minherit.IMapper;

import java.util.function.Supplier;

public abstract class ImplExample extends Base implements Interface {
    public static Supplier<IMapper> mapperCtor1;// = MapperTestImpl::new;

    public ImplExample(int num, boolean human) {
        this(mapperCtor1.get().inInt(0, num).inBoolean(1, human));
    }


    private ImplExample(IMapper in) {
        super(in.outObj(0));
    }

}
