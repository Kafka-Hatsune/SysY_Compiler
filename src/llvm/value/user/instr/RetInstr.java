package llvm.value.user.instr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.JumpInstr;
import mips.instr.MoveInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

import java.util.ArrayList;

// ret <type> <value> | ret void
// ret i32 %4
// ret i32 0
public class RetInstr extends Instr {
    // Operand内容: (Value 返回值)

    public RetInstr(Value returnValue) {
        super(LLVMBasicType.VOID, "unNamed", LLVMInstrType.RET);
        this.addOperands(returnValue);
    }

    // ret void
    public RetInstr() {
        super(LLVMBasicType.VOID, "unNamed", LLVMInstrType.RET);
    }

    @Override
    public String toString() {
        if (this.operandList.size() == 0) {
            return "ret void";
        } else {
            return "ret " + this.getOperand1().getType() + " " + this.getOperand1().getName();
        }
    }

    @Override
    public void toMips() {
        super.toMips();
        // ret 有返回值 存入v0寄存器
        if (this.operandList.size() > 0) {
            Value retValue = this.getOperand1();
            if (MipsModuleBuilder.getInstance().getRegOf(retValue) != null) {
                Reg retReg = MipsModuleBuilder.getInstance().getRegOf(retValue);
                new MoveInstr(Reg.V0, retReg);
            } else {
                Reg retReg = MipsModuleBuilder.getInstance().loadValue2Reg(retValue, Reg.V0);
                // 可能是返回$Zero
                if(retReg != null){
                    new MoveInstr(Reg.V0, retReg);
                }
            }
        }

        new JumpInstr(MipsInstrType.JR, Reg.RA);
    }

    @Override
    public Value getDef() {
        return null;
    }

    @Override
    public ArrayList<Value> getUse() {
        return this.operandList;
    }
}
