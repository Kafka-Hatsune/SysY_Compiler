package AST.Def;

import AST.Exp.CalExp.ConstExp;
import AST.Node;
import IO.Output;
import error.Error;
import lexer.Token;
import llvm.type.LLVMArrayType;
import llvm.type.LLVMBasicType;
import llvm.type.LLVMType;
import symbol.SymbolManager;
import type.ErrorType;

import java.util.ArrayList;

// VarDef → Ident { '[' ConstExp ']' }
// VarDef → Ident { '[' ConstExp ']' } '=' InitVal
// ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
public class Def extends Node {
    protected int dim;
    protected ArrayList<ConstExp> constExps;
    protected ArrayList<Integer> dimList;
    protected Token ident;

    public Def(Node node) {
        super(node);
        this.ident = this.getChildren().get(0).getTerminal();
        this.dimList = new ArrayList<>();
        this.constExps = new ArrayList<>();
        for (Node child : this.getChildren()) {
            if (child instanceof ConstExp) {
                constExps.add((ConstExp) child);
            }
        }
        this.dim = constExps.size();
    }

    public boolean checkRedefine() {
        String name = this.ident.getValue();
        if (SymbolManager.MANAGER.checkRedefine(name)) {
            return true;
        } else {
            Output.output.addErrorMsg(new Error(ident.getLine(), ErrorType.b, "VarDef重定义"));
            return false;
        }
    }

    // 符号表创建后计算
    public LLVMType getLLVMType() {
        if (dim == 0) {
            return LLVMBasicType.INT32;
        } else if (dim == 1) {
            return new LLVMArrayType(dimList.get(0), LLVMBasicType.INT32);
        } else if (dim == 2) {
            LLVMArrayType dim1Type = new LLVMArrayType(dimList.get(1), LLVMBasicType.INT32);
            return new LLVMArrayType(dimList.get(0), dim1Type);
        } else {
            System.err.println("getLLVMType:dimList出错");
            return null;
        }
    }
}
