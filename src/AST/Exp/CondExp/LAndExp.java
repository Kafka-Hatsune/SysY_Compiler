package AST.Exp.CondExp;

import AST.Node;
import llvm.ModuleBuilder;
import llvm.value.BasicBlock;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.BrInstr;
import llvm.value.user.instr.aluInstr.IcmpInstr;
import llvm.value.user.instr.Instr;
import type.InstrIcmpCondType;
import utils.NameGen;

// LAndExp → EqExp | LAndExp '&&' EqExp
public class LAndExp extends Node {
    public LAndExp(Node node) {
        super(node);
    }
    @Override
    public boolean handleError(){
        return super.handleError();
    }

    public void genLAndIR(BasicBlock trueBB, BasicBlock falseBB){
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        if(this.getChildren().size() == 1){
            EqExp eqExp = (EqExp) this.getChildren().get(0);
            Value eqExpIR = eqExp.genIR();
            if(eqExpIR.getType().isInt32()){
                Instr icmpInstr = new IcmpInstr(NameGen.getInstance().genLocalVarName(curFunction), InstrIcmpCondType.NE, Constant.ConstantZero, eqExpIR);
                Instr brInstr = new BrInstr(icmpInstr, trueBB, falseBB);
            }else {
                Instr brInstr = new BrInstr(eqExpIR, trueBB, falseBB);
            }
        }else {
            BasicBlock oriBB = ModuleBuilder.getInstance().getCurBB();
            BasicBlock newBB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction), ModuleBuilder.getInstance().getCurFunction());
            ModuleBuilder.getInstance().setCurBB(oriBB);
            ((LAndExp)this.getChildren().get(0)).genLAndIR(newBB, falseBB);
            ModuleBuilder.getInstance().setCurBB(newBB);
            Value eqExpIR = ((EqExp)this.getChildren().get(2)).genIR();
            if(eqExpIR.getType().isInt32()){
                Instr icmpInstr = new IcmpInstr(NameGen.getInstance().genLocalVarName(curFunction), InstrIcmpCondType.NE, Constant.ConstantZero, eqExpIR);
                Instr brInstr = new BrInstr(icmpInstr, trueBB, falseBB);
            }else {
                Instr brInstr = new BrInstr(eqExpIR, trueBB, falseBB);
            }
        }
    }
}
