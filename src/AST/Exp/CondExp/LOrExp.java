package AST.Exp.CondExp;

import AST.Node;
import llvm.ModuleBuilder;
import llvm.value.BasicBlock;
import llvm.value.user.Function;
import utils.NameGen;

// LOrExp → LAndExp | LOrExp '||' LAndExp
public class LOrExp extends Node {
    public LOrExp(Node node) {
        super(node);
    }
    @Override
    public boolean handleError(){
        return super.handleError();
    }

    public void genLOrIR(BasicBlock trueBB, BasicBlock falseBB){
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        if(this.getChildren().size() == 1){
            ((LAndExp)this.getChildren().get(0)).genLAndIR(trueBB, falseBB);
        }else {
            BasicBlock oriBB = ModuleBuilder.getInstance().getCurBB();
            BasicBlock newBB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction), ModuleBuilder.getInstance().getCurFunction());
            ModuleBuilder.getInstance().setCurBB(oriBB);
            ((LOrExp)this.getChildren().get(0)).genLOrIR(trueBB, newBB);
            ModuleBuilder.getInstance().setCurBB(newBB);
            ((LAndExp)this.getChildren().get(2)).genLAndIR(trueBB, falseBB);
        }
    }
}
