package llvm.value.user.instr.aluInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.RRCalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

// <result> = or <ty> <op1>, <op2>
public class OrInstr extends AluInstr {
    public OrInstr(String name, Value operand1, Value operand2) {
        super(LLVMBasicType.INT32, name, LLVMInstrType.OR);
        this.addOperands(operand1);
        this.addOperands(operand2);
    }

    @Override
    public void toMips() {
        super.toMips();
        new RRCalInstr(MipsInstrType.OR, rd, rs, rt);
        if(rd == Reg.K0){
            MipsModuleBuilder.getInstance().pushValue2Stack(rd, this);
        }
    }
}
