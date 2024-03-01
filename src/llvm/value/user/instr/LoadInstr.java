package llvm.value.user.instr;

import llvm.type.LLVMBasicType;
import llvm.type.LLVMPointerType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.MemInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

import java.util.ArrayList;
import java.util.Objects;

public class LoadInstr extends Instr {
    public LoadInstr(String name, Value ptrValue) {
        super(((LLVMPointerType) ptrValue.getType()).getPtrValType(), name, LLVMInstrType.LOAD);
        addOperands(ptrValue);
    }

    // %7 = load i32, i32* %6
    @Override
    public String toString() {
        Value ptrValue = getOperand1();
        return name + " = load " + type + ", " + ptrValue.getType() + " " + ptrValue.getName();
    }
    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.getName(), ((Value) obj).getName());
    }
    //
    @Override
    public void toMips() {
        super.toMips();
        Value ptrValue = this.getOperand1();
        // 查询寄存器表是否存在相关寄存器
        Reg rs = MipsModuleBuilder.getInstance().loadValue2Reg(ptrValue, Reg.K0);
        rs = (rs == null) ? Reg.K0 : rs;
        Reg dst = MipsModuleBuilder.getInstance().getRegOf(this);
        if(dst != null){
            new MemInstr(MipsInstrType.LW, dst, 0, rs);
        }else {
            new MemInstr(MipsInstrType.LW, Reg.K1, 0, rs);
            MipsModuleBuilder.getInstance().pushValue2Stack(Reg.K1, this);
        }
    }

    @Override
    public Value getDef() {
        return this;
    }

    @Override
    public ArrayList<Value> getUse() {
        return this.operandList;
    }
}
