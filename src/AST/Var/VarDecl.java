package AST.Var;

import AST.Def.VarDef;
import AST.Node;
import llvm.value.Value;
import type.BasicType;

import java.util.ArrayList;

// VarDecl → BType VarDef { ',' VarDef } ';'
public class VarDecl extends Node {
    private BasicType type;
    private ArrayList<VarDef> varDefs;

    public VarDecl(Node node) {
        super(node);
        this.type = ((BType) this.getChildren().get(0)).getBasicType();
        varDefs = new ArrayList<>();
        for(Node child: this.getChildren()){
            if(child instanceof VarDef){
                varDefs.add((VarDef) child);
            }
        }
    }

    @Override
    public boolean handleError(){
        return super.handleError();
    }

    @Override
    public Value genIR(){
        return super.genIR();
    }
}
