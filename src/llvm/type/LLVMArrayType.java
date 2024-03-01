package llvm.type;

public class LLVMArrayType extends LLVMType {
    private int leftNum;
    private LLVMType rightType;

    public LLVMArrayType(int leftNum, LLVMType rightType) {
        this.leftNum = leftNum;
        this.rightType = rightType;
    }
    // [6 x i8]
    @Override
    public String toString() {
        return "[" + leftNum + " x " + rightType + "]";
    }

    public int getLeftNum() {
        return leftNum;
    }

    public LLVMType getRightType() {
        return rightType;
    }

    // [5 x i32] == 1
    // [2 x [3 x i32]] == 2
    public int getDim(){
        return rightType.isInt32() ? 1 : 2;
    }
}
