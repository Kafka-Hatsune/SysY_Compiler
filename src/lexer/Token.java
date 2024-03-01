package lexer;

import type.TokenType;

public class Token {
    private TokenType type;
    private String value;
    private int line;       // 该token在第几行

    public Token(TokenType type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return type.toString() + " " + value + "\n";
    }
}
