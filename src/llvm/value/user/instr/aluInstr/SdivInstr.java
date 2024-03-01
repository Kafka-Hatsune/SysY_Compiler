package llvm.value.user.instr.aluInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.HILOInstr;
import mips.instr.RRCalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

//<result> = sdiv <ty> <op1>, <op2>
public class SdivInstr extends AluInstr {
    public SdivInstr(String name, Value operand1, Value operand2) {
        super(LLVMBasicType.INT32, name, LLVMInstrType.SDIV);
        this.addOperands(operand1);
        this.addOperands(operand2);
    }
    @Override
    public void toMips() {
        super.toMips();
        new RRCalInstr(MipsInstrType.DIV, rs, rt);
        new HILOInstr(MipsInstrType.MFLO, rd);
        if(rd == Reg.K0){
            MipsModuleBuilder.getInstance().pushValue2Stack(rd, this);
        }
    }
}
