package lexer;

import IO.Output;
import error.Error;
import type.ErrorType;
import type.TokenType;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;

public class Lexer {
    private final char EOF = (char) -1;     // reader读到末尾返回-1
    private final PushbackInputStream reader;
    private final ArrayList<Token> tokenList = new ArrayList<>();
    private int line; // 用于记录当前读取到的行
    private char curChar; // 当前读到的字符

    public Lexer(PushbackInputStream reader) throws IOException {
        this.reader = reader;
        read();
        this.line = 1;
    }

    public void read() throws IOException {
        int ans = reader.read();
        char res = (char) ans;
        curChar = res;
    }

    // read后curChar就是下一个字符。要再看当前字符的下两个字符，用peek
    public char peek() throws IOException {
        char nextChar = (char) reader.read();
        reader.unread(nextChar);
        return nextChar;
    }

    public void skipBlank() throws IOException {
        while (Character.isWhitespace(curChar)) {
            if (curChar == '\n') {
                line++;
            }
            this.read();
        }
    }

    public TokenType getIdentType(String ident) {
        return switch (ident) {
            case "main" -> TokenType.MAINTK;
            case "const" -> TokenType.CONSTTK;
            case "int" -> TokenType.INTTK;
            case "break" -> TokenType.BREAKTK;
            case "continue" -> TokenType.CONTINUETK;
            case "if" -> TokenType.IFTK;
            case "else" -> TokenType.ELSETK;
            case "for" -> TokenType.FORTK;
            case "getint" -> TokenType.GETINTTK;
            case "printf" -> TokenType.PRINTFTK;
            case "return" -> TokenType.RETURNTK;
            case "void" -> TokenType.VOIDTK;
            default -> TokenType.IDENFR;
        };
    }

    public Token next() throws IOException {
        skipBlank();

        if (curChar == this.EOF) {
            return null;
        }

        // IDENFR
        if (curChar == '_' || Character.isLetter(curChar)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(curChar);
            read();
            while (curChar == '_' || Character.isLetter(curChar) || Character.isDigit(curChar)) {
                stringBuilder.append(curChar);
                read();
            }
            String value = stringBuilder.toString();
            return new Token(getIdentType(value), value, this.line);
        }
        // IntConst
        if (Character.isDigit(curChar)) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(curChar);
            read();
            while (Character.isDigit(curChar)) {
                stringBuilder.append(curChar);
                read();
            }
            String value = stringBuilder.toString();
            return new Token(TokenType.INTCON, value, this.line);
        }
        // STRCON
        if (curChar == '\"') {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(curChar);
            read(); // 读掉“
            while (curChar != '\"') {
                stringBuilder.append(curChar);
                read();
            }
            stringBuilder.append(curChar);
            read(); // 读掉”
            // value形式 : "xxx"
            String value = stringBuilder.toString();
            String tmp = value.substring(1, value.length() - 1).replace("%d", "").replace("\\n", "");
            for (int i = 0; i < tmp.length(); i++) {
                char c = tmp.charAt(i);
                // '%' 37 '\' 92
                if (c == ' ' || c == '!' || ((int) c >= 40 && (int) c <= 126 && c != '\\')) {
                    continue;
                } else {
                    Output.output.addErrorMsg(new Error(this.line, ErrorType.a, "格式字符串中出现非法字符"));
                    break;
                }
            }
            return new Token(TokenType.STRCON, value, this.line);
        }

        switch (curChar) {
            case '!':
                read();
                if (curChar == '=') {
                    read();
                    return new Token(TokenType.NEQ, "!=", this.line);
                } else {
                    return new Token(TokenType.NOT, "!", this.line);
                }
            case '&':
                read();
                if (curChar == '&') {
                    read();
                    return new Token(TokenType.AND, "&&", this.line);
                }
                break;
            case '|':
                read();
                if (curChar == '|') {
                    read();
                    return new Token(TokenType.OR, "||", this.line);
                }
                break;
            case '+':
                read();
                return new Token(TokenType.PLUS, "+", this.line);
            case '-':
                read();
                return new Token(TokenType.MINU, "-", this.line);
            case '*':
                read();
                return new Token(TokenType.MULT, "*", this.line);
            case '/':
                read();
                if (curChar == '/') {
                    read();
                    while (curChar != '\n' && curChar != EOF) {
                        read();
                    }
                    line++;
                    read(); // 跳过\n
                    return next();
                } else if (curChar == '*') {
                    // 简单的有限状态自动机
                    read();
                    while (true) {
                        while (curChar != '*') {
                            if (curChar == '\n') {
                                line++;
                            }
                            read();
                        }
                        while (curChar == '*') {
                            read();
                        }
                        if (curChar == '/') {
                            read();
                            break;
                        }
                    }
                    return next();
                } else {
                    return new Token(TokenType.DIV, "/", this.line);
                }
            case '%':
                read();
                return new Token(TokenType.MOD, "%", this.line);
            case '<':
                read();
                if (curChar == '=') {
                    read();
                    return new Token(TokenType.LEQ, "<=", this.line);
                } else {
                    return new Token(TokenType.LSS, "<", this.line);
                }
            case '>':
                read();
                if (curChar == '=') {
                    read();
                    return new Token(TokenType.GEQ, ">=", this.line);
                } else {
                    return new Token(TokenType.GRE, ">", this.line);
                }
            case '=':
                read();
                if (curChar == '=') {
                    read();
                    return new Token(TokenType.EQL, "==", this.line);
                } else {
                    return new Token(TokenType.ASSIGN, "=", this.line);
                }
            case ';':
                read();
                return new Token(TokenType.SEMICN, ";", this.line);
            case ',':
                read();
                return new Token(TokenType.COMMA, ",", this.line);
            case '(':
                read();
                return new Token(TokenType.LPARENT, "(", this.line);
            case ')':
                read();
                return new Token(TokenType.RPARENT, ")", this.line);
            case '[':
                read();
                return new Token(TokenType.LBRACK, "[", this.line);
            case ']':
                read();
                return new Token(TokenType.RBRACK, "]", this.line);
            case '{':
                read();
                return new Token(TokenType.LBRACE, "{", this.line);
            case '}':
                read();
                return new Token(TokenType.RBRACE, "}", this.line);
            default:

        }
        return new Token(TokenType.ERROR, "error", this.line);
    }

    public ArrayList<Token> getTokenList() throws IOException {
        Token token;
        while ((token = next()) != null) {
            this.tokenList.add(token);
        }
        return this.tokenList;
    }
}
