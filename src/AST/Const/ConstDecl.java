package AST.Const;

import AST.Def.ConstDef;
import AST.Node;
import AST.Var.BType;
import llvm.value.Value;
import type.BasicType;

import java.util.ArrayList;

// ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
public class ConstDecl extends Node {
    private BasicType type;
    private ArrayList<ConstDef> constDefs;

    public ConstDecl(Node node) {
        super(node);
        this.type = ((BType) this.getChildren().get(1)).getBasicType();
        constDefs = new ArrayList<>();
        for (Node child : this.getChildren()) {
            if (child instanceof ConstDef) {
                constDefs.add((ConstDef) child);
            }
        }
    }

    @Override
    public boolean handleError() {
        return super.handleError();
    }

    @Override
    public Value genIR() {
        return super.genIR();
    }
}
