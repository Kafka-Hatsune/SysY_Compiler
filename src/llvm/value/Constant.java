package llvm.value;

import llvm.type.LLVMBasicType;
import llvm.type.LLVMType;
import type.BasicType;

// i32 8
// i1 1 == true, i1 0 == false
// 作为AddExp等CalExp的operands
public class Constant extends Value{
    private int value;

    public static Constant ConstantZero = new Constant(LLVMBasicType.INT32, 0);
    public static Constant ConstantOne = new Constant(LLVMBasicType.INT32, 1);

    public Constant(LLVMBasicType llvmBasicType,int value) {
        super(llvmBasicType, String.valueOf(value));
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString(){
        return type.toString() + " " + value;
    }
}
