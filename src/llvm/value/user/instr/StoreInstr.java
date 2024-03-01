package llvm.value.user.instr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.MemInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

import java.util.ArrayList;

// store <ty> <value>, <ty>* <pointer>
public class StoreInstr extends Instr {
    public StoreInstr(Value value, Value pValue) {
        super(LLVMBasicType.VOID, "unNamed", LLVMInstrType.STORE);
        this.addOperands(value);
        this.addOperands(pValue);
    }

    @Override
    public String toString() {
        return "store " +
                this.getOperand1().getType() + " " + this.getOperand1().getName() + ", " +
                this.getOperand2().getType() + " " + this.getOperand2().getName();
    }

    @Override
    public void toMips() {
        super.toMips();
        Value value = this.getOperand1();
        Value ptrValue = this.getOperand2();
        Reg valueReg = MipsModuleBuilder.getInstance().loadValue2Reg(value, Reg.K0);
        valueReg = (valueReg == null)? Reg.K0 : valueReg;
        Reg addrReg = MipsModuleBuilder.getInstance().loadValue2Reg(ptrValue, Reg.K1);
        addrReg = (addrReg == null)? Reg.K1 : addrReg;
        new MemInstr(MipsInstrType.SW, valueReg, 0, addrReg);
//        // 在栈上有记录的局部变量
//        if(MipsModuleBuilder.getInstance().valueInStack(ptrValue)){
//            int offset = MipsModuleBuilder.getInstance().getStackOffset(ptrValue);
//            new MemInstr(MipsInstrType.SW, valueReg, offset, Reg.SP);
//        }
//        // 全局变量或其他
//        else {
//
//        }
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
