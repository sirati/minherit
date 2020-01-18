package de.sirati97.minherit.test;

import de.sirati97.minherit.IMapper;

@SuppressWarnings("unchecked")
public class MapperTestImpl implements IMapper {
    private int num;
    private boolean human;
    private String base = "";

    @Override
    public <T> IMapper inObj(int argIndex, T argObj) {
        switch (argIndex) {
            case 2: base = (String) argObj; return this;
        }
        return IMapper.notImpl();
    }

    @Override
    public IMapper inBoolean(int argIndex, boolean argBoolean) {
        switch (argIndex) {
            case 1: human = argBoolean; return this;
        }
        return IMapper.notImpl();
    }

    @Override
    public IMapper inInt(int argIndex, int argInt) {
        switch (argIndex) {
            case 0: num = argInt; return this;
        }
        return IMapper.notImpl();
    }

    @Override
    public <T> T outObj(int argIndex) {
        switch (argIndex) {
            case 0: return (T) (base + " num-5=" + num + ", human=" + human);
        }
        return IMapper.notImpl();
    }

    @Override
    public int outInt(int argIndex) {
        switch (argIndex) {
            case 1: return num + 5;
        }
        return IMapper.notImpl();
    }
}
