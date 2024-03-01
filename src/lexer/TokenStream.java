package lexer;

import type.TokenType;

import java.util.ArrayList;

public class TokenStream {
    private ArrayList<Token> tokens;

    private int pos;

    private Token curToken;

    private int savePoint;

    public TokenStream(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
        this.curToken = tokens.get(pos);
    }

    public Boolean isEOF() {
        return curToken == null;
    }

    public void next() {
        if (pos < tokens.size() - 1) {
            curToken = tokens.get(++pos);
        } else {
            curToken = null;
        }
    }

    public Token getCurToken() {
        return curToken;
    }

    // num == 1 时 peek(1) 等于往前看一个
    public Token peek(int num) {
        if (pos + num > tokens.size() - 1) {
            return null;
        } else {
            return tokens.get(pos + num);
        }
    }

    // 找到上一个Token的行号
    public int getPreTokenLine(){
        if(pos == 0){
            return 1;
        }
        return tokens.get(pos - 1).getLine();
    }

    public void save(){
        savePoint = pos;
    }

    public void load(){
        pos = savePoint;
        curToken = tokens.get(pos);
    }
}
