package llvm.value.user.instr.optimizeInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.RRCalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

public class SrlInstr extends Instr {
    int rightValue;

    public SrlInstr(String name, Value value, int rightValue) {
        super(LLVMBasicType.INT32, name);
        this.instrType = LLVMInstrType.SRL;
        this.addOperands(value);
        this.rightValue = rightValue;
    }

    @Override
    public String toString() {
        return this.name + " = srl "+ this.getOperand1().getName() + " " + rightValue;
    }

    @Override
    public void toMips() {
        super.toMips();
        Value toSrl = this.getOperand1();
        // 查询寄存器表是否存在相关寄存器
        Reg rt = MipsModuleBuilder.getInstance().loadValue2Reg(toSrl, Reg.K1);
        rt = (rt == null) ? Reg.K1 : rt;
        Reg rd = Reg.K0;
        new RRCalInstr(MipsInstrType.SRL, rd, rt, rightValue);
        MipsModuleBuilder.getInstance().pushValue2Stack(rd, this);
    }
}
