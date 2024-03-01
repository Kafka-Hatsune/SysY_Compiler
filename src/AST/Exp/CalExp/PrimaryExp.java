package AST.Exp.CalExp;

import AST.Node;
import llvm.value.Value;
import utils.Calculable;

// PrimaryExp → '(' Exp ')' | LVal | Number
public class PrimaryExp extends Node implements Calculable {
    public PrimaryExp(Node node) {
        super(node);
    }

    @Override
    public boolean handleError(){
        return super.handleError();
    }


    @Override
    public Integer calculate() {
        Node node = this.getChildren().get(0);
        if (node instanceof Number) {
            return ((Number)node).calculate();
        } else if (node instanceof LVal) {
            return ((LVal)node).calculate();
        } else {
            return ((Exp)this.getChildren().get(1)).calculate();
        }
    }

    public Integer getDim() {
        Node node = this.getChildren().get(0);
        if (node instanceof Number) {
            return 0;
        } else if (node instanceof LVal) {
            return node.getDim();
        } else {
            return this.getChildren().get(1).getDim();
        }
    }

    @Override
    public Value genIR(){
        Node node = this.getChildren().get(0);
        if (node instanceof Number) {
            return node.genIR();
        } else if (node instanceof LVal) {
            return ((LVal)node).loadLValIR();
        } else {
            return this.getChildren().get(1).genIR();
        }
    }
}
