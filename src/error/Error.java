package error;


import type.ErrorType;

public class Error implements Comparable<Error>{
    private int line;   // 行号
    private ErrorType type;     // 错误类型

    private String msg;     // 描述

    public Error(int line, ErrorType type, String msg) {
        this.line = line;
        this.type = type;
        this.msg = msg;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public ErrorType getType() {
        return type;
    }

    public void setType(ErrorType type) {
        this.type = type;
    }

    @Override
    public int compareTo(Error o) {
        return this.line - o.line;
    }

    @Override
    public String toString(){
        return line + " " +type.toString() + '\n';
    }
}
