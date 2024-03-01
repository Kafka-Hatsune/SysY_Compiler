package AST.Exp.CondExp;

import AST.Node;
import llvm.value.BasicBlock;

// Cond → LOrExp
public class Cond extends Node {
    public Cond(Node node) {
        super(node);
    }
    @Override
    public boolean handleError(){
        return super.handleError();
    }

    public void genCondIR(BasicBlock trueBB, BasicBlock falseBB){
        ((LOrExp)this.getChildren().get(0)).genLOrIR(trueBB, falseBB);
    }
}
