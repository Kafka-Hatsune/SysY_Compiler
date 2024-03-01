package llvm.value.user.instr;

import llvm.type.LLVMBasicType;
import llvm.value.BasicBlock;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.BranchInstr;
import mips.instr.JumpInstr;
import type.LLVMInstrType;
import type.MipsInstrType;
import utils.NameGen;

import java.util.ArrayList;


// br label %1 ???
public class BrInstr extends Instr{
    // br i1 %2, label %12, label %3
    public BrInstr(Value cond, BasicBlock trueBB, BasicBlock falseBB) {
        super(LLVMBasicType.VOID, NameGen.getInstance().Placeholder, LLVMInstrType.BR);
        this.addOperands(cond);
        this.addOperands(trueBB);
        this.addOperands(falseBB);
    }
    // br label <dest>
    public BrInstr(BasicBlock desBB) {
        super(LLVMBasicType.VOID, NameGen.getInstance().Placeholder, LLVMInstrType.BR);
        this.addOperands(desBB);
    }

    public BrInstr() {
        super(LLVMBasicType.VOID, "unNamed Br");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(this.operandList.size() == 3){
            sb.append("br ");
            sb.append(this.getOperand1().getType());
            sb.append(" ");
            sb.append(this.getOperand1().getName());
            sb.append(", label ");
            sb.append(this.getOperand2().getName());
            sb.append(", label ");
            sb.append(this.getOperand3().getName());
        }else {
            sb.append("br label ");
            sb.append(this.getOperand1().getName());
        }
        return sb.toString();
    }

    @Override
    public void toMips() {
        super.toMips();
        if (this.operandList.size() == 1){
            Value desBB =  this.getOperand1();
            new JumpInstr(MipsInstrType.J, desBB.getName().substring(1));
        }else {
            Value cond = this.getOperand1();
            Value trueBB = this.getOperand2();
            Value falseBB = this.getOperand3();
            Reg condReg = MipsModuleBuilder.getInstance().loadValue2Reg(cond, Reg.K0);
            condReg = (condReg == null)? Reg.K0 : condReg;
            // bne cond, $0, trueBB
            new BranchInstr(MipsInstrType.BNE, condReg, Reg.ZERO, trueBB.getName().substring(1));
            // j falseBB
            new JumpInstr(MipsInstrType.J, falseBB.getName().substring(1));
        }
    }

    @Override
    public Value getDef(){
        return null;
    }

    @Override
    public ArrayList<Value> getUse(){
        ArrayList<Value> ans = new ArrayList<>();
        if(this.operandList.size() == 3){
            ans.add(this.getOperand1());
        }
        return ans;
    }
}
