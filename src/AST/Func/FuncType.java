package AST.Func;

import AST.Node;
import type.FuncReturnType;

public class FuncType extends Node {
    private FuncReturnType funcType;
    public FuncType(Node node) {
        super(node);
        switch (this.getChildren().get(0).getTerminal().getType()){
            case VOIDTK -> funcType = FuncReturnType.VOID;
            case INTTK -> funcType = FuncReturnType.INT;
            default -> System.err.println("FuncType: 未知的FuncType");
        }
    }
    public FuncReturnType getFuncType() {
        return funcType;
    }

    @Override
    public boolean handleError(){
        return true;
    }
}
