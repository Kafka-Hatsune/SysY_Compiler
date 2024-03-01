package AST.Exp.CalExp;

import AST.Node;
import llvm.value.Value;
import type.SyntaxType;
import utils.Calculable;

import java.util.ArrayList;

// Exp → AddExp
public class Exp extends Node implements Calculable {

    public Exp(Node node) {
        super(node);
    }

    public Integer calculate(){
        return ((AddExp)this.getChildren().get(0)).calculate();
    }

    public Integer getDim() {
        return this.getChildren().get(0).getDim();
    }

    @Override
    public boolean handleError(){
        return super.handleError();
    }

    @Override
    public Value genIR(){
        return super.genIR();
    }
}
