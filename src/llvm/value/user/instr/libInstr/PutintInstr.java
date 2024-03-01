package llvm.value.user.instr.libInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.LiInstr;
import mips.instr.MoveInstr;
import mips.instr.SysCallInstr;
import type.LLVMInstrType;
import utils.NameGen;

import java.util.ArrayList;

public class PutintInstr extends Instr {
    public PutintInstr(Value value) {
        super(LLVMBasicType.VOID, NameGen.getInstance().Placeholder, LLVMInstrType.LIB);
        this.addOperands(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("call void @putint(");
        sb.append(this.getOperand1().getType());
        sb.append(" ");
        sb.append(this.getOperand1().getName());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void toMips() {
        new LiInstr(Reg.V0, 1);
        Reg reg = MipsModuleBuilder.getInstance().loadValue2Reg(this.getOperand1(), Reg.A0);
        if (reg != null && reg != Reg.A0) {
            new MoveInstr(Reg.A0, reg);
        }
        new SysCallInstr();
    }

    @Override
    public Value getDef(){
        return null;
    }

    @Override
    public ArrayList<Value> getUse(){
        return this.operandList;
    }
}
