package de.sirati97.minherit.test;


import de.sirati97.minherit.IMapper;
import de.sirati97.minherit.Processor;

import java.util.function.Supplier;

public abstract class ImplExample extends Base implements Interface {
    public final static Supplier<IMapper> mapperCtor1;// = MapperTestImpl::new;
    public final static Supplier<IMapper> mapperCtor2;// = MapperTestImpl::new;

    public ImplExample(int num, boolean human) {
        this(mapperCtor1.get().inInt(0, num).inBoolean(1, human));
    }


    private ImplExample(IMapper in) {
        super(in.outObj(0));
    }

    static {
        {
            Supplier<IMapper>[] suppliers = Processor.getMapperSupplier(ImplExample.class);
            mapperCtor1 = suppliers[0];
            mapperCtor2 = suppliers[1];
        }
        Supplier<IMapper>[] suppliers = new Supplier[3];
        suppliers[1] = suppliers[0];
    }

}
