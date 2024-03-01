package AST;

import llvm.value.Value;
import symbol.SymbolManager;
import type.SyntaxType;

public class CompUnit extends Node{
    public CompUnit(Node node) {
        super(node);
    }

    @Override
    public boolean handleError(){
        SymbolManager.MANAGER.createRootSymbolTable();
        return super.handleError();
    }

    @Override
    public Value genIR(){
        SymbolManager.MANAGER.createRootSymbolTable();
        super.genIR();
        return null;
    }
}


