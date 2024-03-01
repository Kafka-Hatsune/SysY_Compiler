package AST;

import AST.Exp.CalExp.AddExp;
import AST.Exp.CalExp.MulExp;
import AST.Exp.CondExp.EqExp;
import AST.Exp.CondExp.LAndExp;
import AST.Exp.CondExp.LOrExp;
import AST.Exp.CondExp.RelExp;
import lexer.Token;
import llvm.value.Value;
import type.SyntaxType;

import java.util.ArrayList;

public class Node {
    private int startLine;
    private int endLine;
    private SyntaxType syntaxType;  // TOKEN / else;
    // 非终结符属性
    private ArrayList<Node> children;
    // 终结符属性
    private Token terminal;


    // 非终结符的构造函数
    public Node(SyntaxType syntaxType) {
        this.syntaxType = syntaxType;
        this.children = new ArrayList<>();
    }

    // 终结符的构造函数
    public Node(Token token) {
        this.syntaxType = SyntaxType.TOKEN;
        this.terminal = token;
        this.startLine = token.getLine();
        this.endLine = token.getLine();
    }

    // 给子类的构造函数
    public Node(Node node) {
        this.syntaxType = node.syntaxType;
        this.children = node.children;
        this.startLine = node.startLine;
        this.endLine = node.endLine;
        this.terminal = node.terminal;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getStartLine() {
        return startLine;
    }

    public Token getTerminal() {
        return terminal;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public SyntaxType getSyntaxType() {
        return syntaxType;
    }

    public void addChild(Node node) {
        this.children.add(node);
    }

    // 将this的children的所有结点合并为syntaxType=type, children=this
    public void adjustExp(SyntaxType type) {
        Node newNode = new Node(type);
        for (int i = 0; i < this.children.size(); i++) {
            newNode.addChild(this.children.get(i));
        }
        this.children.clear();
        switch (type){
            case ADD_EXP -> {
                this.children.add(new AddExp(newNode));
            }
            case LOR_EXP -> {
                this.children.add(new LOrExp(newNode));
            }
            case LAND_EXP -> {
                this.children.add(new LAndExp(newNode));
            }
            case EQ_EXP -> {
                this.children.add(new EqExp(newNode));
            }
            case REL_EXP -> {
                this.children.add(new RelExp(newNode));
            }
            case MUL_EXP -> {
                this.children.add(new MulExp(newNode));
            }
            default -> {
                this.children.add(new Node(newNode));
            }
        }
    }

    @Override
    public String toString() {
        if (terminal != null) {
            return terminal.toString();
        } else {
            return "<" + syntaxType.toString() + ">" + "\n";
        }
        // debug
//        if (terminal != null) {
//            return terminal.toString();
//        } else {
//            return children.toString();
//        }
    }


    public boolean handleError(){
        // 只对非终结符进行错误处理动作
        if (this.syntaxType == SyntaxType.TOKEN) {
            return true;
        }
        boolean allCheck = true;
        for (Node node : this.children) {
            if(!node.handleError()){
                allCheck = false;
            };
        }
        return allCheck;
    }

    // 有时ir需要从上到下生成 从下往上传递
    public Value genIR(){
        if (this.syntaxType == SyntaxType.TOKEN) {
            return null;
        }
        if (this.children.size() == 1){
            return this.children.get(0).genIR();
        }else {
            for (Node child : this.children) {
                child.genIR();
            }
            return null;
        }
    }


    // Exp AddExp MulExp的维度信息 {-1:VOID,0:INT,1:INT[],2:INT[][]}
    public Integer getDim() {
        return 0;
    }
}
