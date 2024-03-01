package llvm.value.user.instr.optimizeInstr;

import llvm.type.LLVMBasicType;
import llvm.type.LLVMType;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.LiInstr;
import mips.instr.MoveInstr;
import type.LLVMInstrType;

import java.util.ArrayList;

public class LLVMMoveInstr extends Instr {
    public LLVMMoveInstr(Value dst, Value src) {
        super(LLVMBasicType.VOID, "unNamed");
        this.instrType = LLVMInstrType.MOVE;
        this.addOperands(dst);
        this.addOperands(src);
    }

    @Override
    public String toString() {
        Value dst = this.getOperand1();
        Value src = this.getOperand2();
//        return dst.getName() + " = add " + dst.getType() + " " + src.getName() + " ,    0";
        return dst.getName() + " = move " + dst.getType() + " " + src.getName();
    }

    // %main_var7 = move i32 11
    @Override
    public void toMips() {
        // 写注释
        super.toMips();
        Value dst = this.getOperand1();
        Value src = this.getOperand2();
        if(src instanceof Constant){
            int constValue = ((Constant) src).getValue();
            Reg dstReg = MipsModuleBuilder.getInstance().loadValue2Reg(dst, Reg.K0);
            dstReg = (dstReg == null) ? Reg.K0 : dstReg;
            new LiInstr(dstReg, constValue);
            if(dstReg == Reg.K0){
                MipsModuleBuilder.getInstance().pushValue2Stack(dstReg, dst);
            }
        }else {
            Reg srcReg = MipsModuleBuilder.getInstance().loadValue2Reg(src, Reg.K1);
            srcReg = (srcReg == null) ? Reg.K1 : srcReg;
            Reg dstReg = MipsModuleBuilder.getInstance().loadValue2Reg(dst, Reg.K0);
            dstReg = (dstReg == null) ? Reg.K0 : dstReg;
            if(srcReg == dstReg){
                return;
            }
            new MoveInstr(dstReg, srcReg);
            if(dstReg == Reg.K0){
                MipsModuleBuilder.getInstance().pushValue2Stack(dstReg, dst);
            }
        }


    }

    @Override
    public Value getDef(){
        return this.getOperand1();
    }

    @Override
    public ArrayList<Value> getUse(){
        ArrayList<Value> ans = new ArrayList<>();
        ans.add(this.getOperand2());
        return ans;
    }
}
