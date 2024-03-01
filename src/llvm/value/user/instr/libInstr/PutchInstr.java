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

public class PutchInstr extends Instr {
    public PutchInstr(Value value) {
        super(LLVMBasicType.VOID, NameGen.getInstance().Placeholder, LLVMInstrType.LIB);
        this.addOperands(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("call void @putch(");
        sb.append(this.getOperand1().getType());
        sb.append(" ");
        sb.append(this.getOperand1().getName());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void toMips() {
        new LiInstr(Reg.V0,  11);
        new LiInstr(Reg.A0, ((Constant)this.getOperand1()).getValue());
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
