package llvm.value.user.instr;

import llvm.type.LLVMPointerType;
import llvm.type.LLVMType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.MemInstr;
import mips.instr.RICalInstr;
import mips.instr.RRCalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

import java.util.ArrayList;
import java.util.Objects;

// def %1 = alloca i32
// use store i32 %2, i32* %1
public class AllocaInstr extends Instr {
    public AllocaInstr(LLVMType ptrValType, String name) {
        super(new LLVMPointerType(ptrValType), name, LLVMInstrType.ALLOCA);
    }

    public LLVMType getPtrType(){
        return ((LLVMPointerType)this.type).getPtrValType();
    }
    @Override
    public String toString() {
        return name + " = alloca " + ((LLVMPointerType) this.type).getPtrValType();
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.getName(), ((Value) obj).getName());
    }

    @Override
    public void toMips() {
        super.toMips();
        LLVMType ptrType = ((LLVMPointerType) this.type).getPtrValType();
        // 获取应该在栈上alloc的空间大小 注意指针算一个int
        int offset = ptrType.getEleNum() * 4;
        // 在栈上分配空间
        MipsModuleBuilder.getInstance().allocStack(offset);
        // 当前的栈指针
        int stackPointer = MipsModuleBuilder.getInstance().getStackPointer();
        Reg thisReg = MipsModuleBuilder.getInstance().getRegOf(this);
        // alloc的指针有寄存器:将数组首地址赋给它
        if(thisReg != null){
            // 注意stackPointer代表当前可分配的空间 真正的数组首地址在它上面一个位置
            new RICalInstr(MipsInstrType.ADDI, thisReg, Reg.SP, stackPointer + 4);
        }
        // alloc的指针没有寄存器 将地址也存在栈上
        else {
            new RICalInstr(MipsInstrType.ADDI, Reg.K0, Reg.SP, stackPointer + 4);
            new MemInstr(MipsInstrType.SW, Reg.K0, stackPointer, Reg.SP);
            MipsModuleBuilder.getInstance().getValue2Offset().put(this, stackPointer);
            MipsModuleBuilder.getInstance().allocStack(4);
        }
    }
}
