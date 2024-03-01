package AST.Exp;

import AST.Node;
import type.TokenType;

// UnaryOp → '+' | '−' | '!' '!'仅出现在条件表达式中
public class UnaryOp extends Node {
    TokenType type;

    public UnaryOp(Node node) {
        super(node);
        type = this.getChildren().get(0).getTerminal().getType();
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public boolean handleError(){
        return true;
    }
}
