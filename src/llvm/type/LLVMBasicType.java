package llvm.type;

public class LLVMBasicType extends LLVMType {
    public String value;

    public static LLVMBasicType VOID = new LLVMBasicType(0);
    public static LLVMBasicType INT1 = new LLVMBasicType(1);
    public static LLVMBasicType INT8 = new LLVMBasicType(8);
    public static LLVMBasicType INT32 = new LLVMBasicType(32);



    public LLVMBasicType(int width) {
        switch (width){
            case 0 -> value = "void";
            case 1 -> value = "i1";
            case 8 -> value = "i8";
            case 32 -> value = "i32";
        }
    }
    @Override
    public String toString() {
        return value;
    }
}
