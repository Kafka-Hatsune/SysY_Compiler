package AST.Var;

import AST.Node;
import type.BasicType;
import type.TokenType;

public class BType extends Node {
    private BasicType basicType;
    public BType(Node node) {
        super(node);
        if (node.getChildren().get(0).getTerminal().getType() == TokenType.INTTK) {
            this.basicType = BasicType.INT;
        }
    }

    public BasicType getBasicType() {
        return basicType;
    }

    @Override
    public boolean handleError(){
        return true;
    }
}
