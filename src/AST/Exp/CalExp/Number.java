package AST.Exp.CalExp;

import AST.Node;
import llvm.type.LLVMBasicType;
import llvm.value.Constant;
import llvm.value.Value;
import utils.Calculable;

public class Number extends Node implements Calculable {
    int value;

    public Number(Node node) {
        super(node);
        value = Integer.parseInt(this.getChildren().get(0).getTerminal().getValue());
    }

    @Override
    public Integer calculate(){
        return value;
    }

    @Override
    public boolean handleError(){
        return true;
    }

    @Override
    public Value genIR(){
        if(value == 0){
            return Constant.ConstantZero;
        } else if (value == 1) {
            return Constant.ConstantOne;
        }else {
            return new Constant(LLVMBasicType.INT32, this.value);
        }
    }


}
