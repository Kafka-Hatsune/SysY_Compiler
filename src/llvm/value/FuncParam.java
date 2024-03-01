package llvm.value;

import llvm.ModuleBuilder;
import llvm.type.LLVMBasicType;
import llvm.type.LLVMPointerType;
import llvm.value.user.Function;
import llvm.value.user.instr.AllocaInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.LoadInstr;
import llvm.value.user.instr.StoreInstr;
import utils.NameGen;

import java.util.Objects;

public class FuncParam extends Value {
    private Value addr;     // 将经过值传递后的可使用的函数参数地址保存
    // 在构造FuncParam的时候在函数体中构造值传递语句
    public FuncParam(Value value, int dim) {
        // 先占位
        super(LLVMBasicType.VOID, "unnamed");
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        // int参数经过保存得到一个int*指针
        if (dim == 0) {
            this.type = new LLVMPointerType(LLVMBasicType.INT32);
            String allocaName = NameGen.getInstance().genLocalVarName(curFunction);
            this.name = allocaName;
            Instr allocaInstr = new AllocaInstr(value.getType(), allocaName);
            new StoreInstr(value, allocaInstr);
            addr = allocaInstr;
        }
        // i32* noundef %1
        // %5 = alloca i32*
        // store i32* %1, i32** %5
        //
        else {
            Instr allocaInstr = new AllocaInstr(value.getType(), NameGen.getInstance().genLocalVarName(curFunction));
            new StoreInstr(value, allocaInstr);
            Instr loadInstr = new LoadInstr(NameGen.getInstance().genLocalVarName(curFunction), allocaInstr);
            this.type = loadInstr.type;
            this.name = loadInstr.name;
            addr = loadInstr;
        }
    }

    public Value getAddr() {
        return addr;
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.getName(), ((Value) obj).getName());
    }
}
