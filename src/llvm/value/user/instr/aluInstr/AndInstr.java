package llvm.value.user.instr.aluInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.RRCalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

// <result> = and <ty> <op1>, <op2>
public class AndInstr extends AluInstr {
    public AndInstr(String name, Value operand1, Value operand2) {
        super(LLVMBasicType.INT32, name, LLVMInstrType.AND);
        this.addOperands(operand1);
        this.addOperands(operand2);
    }

    @Override
    public void toMips() {
        super.toMips();
        new RRCalInstr(MipsInstrType.AND, rd, rs, rt);
        if(rd == Reg.K0){
            MipsModuleBuilder.getInstance().pushValue2Stack(rd, this);
        }
    }
}
