package llvm.value.user.instr.aluInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.HILOInstr;
import mips.instr.RRCalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

// <result> = srem <type> <op1>, <op2>
public class SremInstr extends AluInstr {
    public SremInstr(String name, Value operand1, Value operand2) {
        super(LLVMBasicType.INT32, name, LLVMInstrType.SREM);
        this.addOperands(operand1);
        this.addOperands(operand2);
    }
    @Override
    public void toMips() {
        super.toMips();
        new RRCalInstr(MipsInstrType.DIV, rs, rt);
        new HILOInstr(MipsInstrType.MFHI, rd);
        if(rd == Reg.K0){
            MipsModuleBuilder.getInstance().pushValue2Stack(rd, this);
        }
    }
}
