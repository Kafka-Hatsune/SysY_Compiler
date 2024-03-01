package llvm.value.user.instr.optimizeInstr;

import llvm.type.LLVMBasicType;
import llvm.type.LLVMType;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.aluInstr.AluInstr;
import mips.MipsModuleBuilder;
import mips.instr.HILOInstr;
import mips.instr.RRCalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

// toMips: mul结果在hi的mul
public class MulHInstr extends AluInstr {

    public MulHInstr(String name, Value operand1, Value operand2) {
        super(LLVMBasicType.INT32, name);
        this.instrType = LLVMInstrType.MULH;
        this.addOperands(operand1);
        this.addOperands(operand2);
    }

    @Override
    public void toMips() {
        super.toMips();
        new RRCalInstr(MipsInstrType.MULT, rs, rt);
        new HILOInstr(MipsInstrType.MFHI, rd);
        MipsModuleBuilder.getInstance().pushValue2Stack(rd, this);
    }
}
