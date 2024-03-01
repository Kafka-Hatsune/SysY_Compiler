package llvm.value.user.instr.aluInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.RRCalInstr;
import type.InstrIcmpCondType;
import type.LLVMInstrType;
import type.MipsInstrType;

//<result> = icmp <cond> <ty> <op1>, <op2>
public class IcmpInstr extends AluInstr {
    private InstrIcmpCondType condType;

    // 该指令返回值类型必为bool
    public IcmpInstr(String name, InstrIcmpCondType condType, Value operand1, Value operand2) {
        super(LLVMBasicType.INT1, name, LLVMInstrType.ICMP);
        this.condType = condType;
        this.addOperands(operand1);
        this.addOperands(operand2);
    }

    @Override
    public String toString() {
        Value operand1 = this.getOperand1();
        Value operand2 = this.getOperand2();
        return name + " = icmp " + condType.toString() + " " + operand1.getType() + " " + operand1.getName() + ", " + operand2.getName();
    }

    @Override
    public void toMips() {
        super.toMips();
        switch (condType) {
            case EQ -> new RRCalInstr(MipsInstrType.SEQ, rd, rs, rt);
            case NE -> new RRCalInstr(MipsInstrType.SNE, rd, rs, rt);
            case SGT -> new RRCalInstr(MipsInstrType.SGT, rd, rs, rt);
            case SGE -> new RRCalInstr(MipsInstrType.SGE, rd, rs, rt);
            case SLT -> new RRCalInstr(MipsInstrType.SLT, rd, rs, rt);
            case SLE -> new RRCalInstr(MipsInstrType.SLE, rd, rs, rt);
            default -> System.err.println("IcmpInstr-toMips:未知的condType");
        }
        if(rd == Reg.K0){
            MipsModuleBuilder.getInstance().pushValue2Stack(rd, this);
        }
    }


}
