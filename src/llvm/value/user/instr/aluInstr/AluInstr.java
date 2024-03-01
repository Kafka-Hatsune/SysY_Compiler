package llvm.value.user.instr.aluInstr;

import llvm.type.LLVMType;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import mips.MipsModuleBuilder;
import mips.Reg;
import type.LLVMInstrType;

import java.util.ArrayList;

public class AluInstr extends Instr {
    protected Reg rd;
    protected Reg rs;
    protected Reg rt;

    public AluInstr(LLVMType type, String name, LLVMInstrType instrType) {
        super(type, name, instrType);
    }

    public AluInstr(LLVMType type, String name) {
        super(type, name);
    }

    @Override
    public String toString() {
        Value operand1 = this.getOperand1();
        Value operand2 = this.getOperand2();
        return name + " = " + instrType.toString() + " " + type + " " + operand1.getName() + ", " + operand2.getName();
    }

    @Override
    public Value getDef(){
        return this;
    }

    @Override
    public ArrayList<Value> getUse(){
        return this.operandList;
    }

    @Override
    public void toMips() {
        super.toMips();
        // TODO addi subi等优化
        Value operand1 = this.getOperand1();
        Value operand2 = this.getOperand2();
        // 查询寄存器表是否存在相关寄存器
        rs = MipsModuleBuilder.getInstance().loadValue2Reg(operand1, Reg.K0);
        rs = (rs == null) ? Reg.K0 : rs;
        rt = MipsModuleBuilder.getInstance().loadValue2Reg(operand2, Reg.K1);
        rt = (rt == null) ? Reg.K1 : rt;
        rd = MipsModuleBuilder.getInstance().getRegOf(this);
        rd = (rd == null) ? Reg.K0 : rd;
    }
}
