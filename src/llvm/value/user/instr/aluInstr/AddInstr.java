package llvm.value.user.instr.aluInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.RRCalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

//<result> = add <ty> <op1>, <op2>
public class AddInstr extends AluInstr {
    public AddInstr(String name, Value operand1, Value operand2) {
        super(LLVMBasicType.INT32, name, LLVMInstrType.ADD);
        this.addOperands(operand1);
        this.addOperands(operand2);
    }


    @Override
    public void toMips() {
        super.toMips();
        new RRCalInstr(MipsInstrType.ADDU, rd, rs, rt);
        if(rd == Reg.K0){
            MipsModuleBuilder.getInstance().pushValue2Stack(rd, this);
        }
    }
}
