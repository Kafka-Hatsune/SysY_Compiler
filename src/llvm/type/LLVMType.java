package llvm.type;

// 最顶级超类
public class LLVMType {
    public boolean isInt32(){
        return (this instanceof LLVMBasicType) && ((LLVMBasicType) this).value.equals("i32");
    }

    public boolean isArray() {
        return (this instanceof LLVMArrayType);
    }
    // 是一维数组
    public boolean isArray1() {
        return (this instanceof LLVMArrayType) && (((LLVMArrayType) this).getRightType() instanceof LLVMBasicType);
    }
    // 是二维数组
    public boolean isArray2(){
        return (this instanceof LLVMArrayType) && (((LLVMArrayType) this).getRightType() instanceof LLVMArrayType);
    }
    // 获取Type包含的元素个数
    // 指针为地址 实际上也是一个元素大小
    public int getEleNum(){
        if (this.isArray()) {
            int leftNum = ((LLVMArrayType)this).getLeftNum();
            LLVMType rightType = ((LLVMArrayType)this).getRightType();
            return leftNum * rightType.getEleNum();
        }
        else return 1;
    }
}
