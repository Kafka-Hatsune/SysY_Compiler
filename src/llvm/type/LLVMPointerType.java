package llvm.type;

public class LLVMPointerType extends LLVMType {
    private LLVMType ptrValType;

    public LLVMPointerType(LLVMType ptrValType) {
        this.ptrValType = ptrValType;
    }

    public LLVMType getPtrValType() {
        return ptrValType;
    }

    // [6 x i8]*
    // i32* * %3
    @Override
    public String toString() {
        if(ptrValType instanceof LLVMPointerType){
            return ptrValType.toString() + " *";
        }else {
            return ptrValType.toString() + "*";
        }
    }
}
