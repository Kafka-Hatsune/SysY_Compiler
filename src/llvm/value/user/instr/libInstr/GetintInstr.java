package llvm.value.user.instr.libInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.LiInstr;
import mips.instr.MoveInstr;
import mips.instr.SysCallInstr;
import type.LLVMInstrType;

import java.util.ArrayList;

public class GetintInstr extends Instr {
    public GetintInstr(String name) {
        super(LLVMBasicType.INT32, name, LLVMInstrType.LIB);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" = call i32 @getint()");
        return sb.toString();
    }

    @Override
    public void toMips() {
        super.toMips();
        Reg reg = MipsModuleBuilder.getInstance().getRegOf(this);
        if(reg != null){
            new LiInstr(Reg.V0, 5);
            new SysCallInstr();
            new MoveInstr(reg, Reg.V0);
        }
        // 存到栈上
        else {
            new LiInstr(Reg.V0, 5);
            new SysCallInstr();
            MipsModuleBuilder.getInstance().pushValue2Stack(Reg.V0, this);
        }
    }

    @Override
    public Value getDef() {
        return this;
    }

    @Override
    public ArrayList<Value> getUse() {
        return new ArrayList<>();
    }
}
