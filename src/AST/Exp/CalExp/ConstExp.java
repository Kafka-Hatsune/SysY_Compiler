package AST.Exp.CalExp;

import AST.Node;
import llvm.value.Value;
import type.SyntaxType;
import utils.Calculable;

// ConstExp → AddExp
public class ConstExp extends Node implements Calculable {

    public ConstExp(Node node) {
        super(node);
    }

    @Override
    public Integer calculate(){
        return ((AddExp)this.getChildren().get(0)).calculate();
    }
    @Override
    public boolean handleError(){
        return super.handleError();
    }
    @Override
    public Value genIR() {
        return super.genIR();
    }
}
