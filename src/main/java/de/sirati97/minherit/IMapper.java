package de.sirati97.minherit;

public interface IMapper {
    static <T> T notImpl() {
        throw new IllegalStateException("the accessor has not been overwritten meaning that this access in erroneous");
    }
    default <T> IMapper inObj(int argIndex, T argObj) {return notImpl();}
    default  IMapper inByte(int argIndex, byte argByte) {return notImpl();}
    default  IMapper inShort(int argIndex, short argShort) {return notImpl();}
    default  IMapper inInt(int argIndex, int argInt) {return notImpl();}
    default  IMapper inLong(int argIndex, long argLong) {return notImpl();}
    default  IMapper inFloat(int argIndex, float argFloat) {return notImpl();}
    default  IMapper inDouble(int argIndex, double argDouble) {return notImpl();}
    default  IMapper inBoolean(int argIndex, boolean argBoolean) {return notImpl();}
    default  IMapper inChar(int argIndex, char argChar) {return notImpl();}


    default <T> T outObj(int argIndex) {return notImpl();}
    default  byte outByte(int argIndex) {return notImpl();}
    default  short outShort(int argIndex) {return notImpl();}
    default  int outInt(int argIndex) {return notImpl();}
    default  long outLong(int argIndex) {return notImpl();}
    default  float outFloat(int argIndex) {return notImpl();}
    default  double outDouble(int argIndex) {return notImpl();}
    default  boolean outBoolean(int argIndex) {return notImpl();}
    default  char outChar(int argIndex) {return notImpl();}
}
