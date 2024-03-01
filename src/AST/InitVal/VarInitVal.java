package AST.InitVal;

import AST.Exp.CalExp.Exp;
import AST.Node;
import llvm.value.Value;
import type.SymbolType;
import type.SyntaxType;

import java.util.ArrayList;

public class VarInitVal extends InitVal {
    private ArrayList<Value> values;

    public VarInitVal(Node node) {
        super(node);
    }

    // 给没有初始化值的VarDef用
    public VarInitVal() {
        super(SyntaxType.INIT_VAL);
        this.type = SymbolType.Unknown;
    }


    // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public ArrayList<Value> initLLVMValue() {
        ArrayList<Value> ans = new ArrayList<>();
        if (this.getChildren().get(0) instanceof Exp) {
            ans.add(this.getChildren().get(0).genIR());
        } else {
            for (Node child : this.getChildren()) {
                if (child instanceof VarInitVal) {
                    ArrayList<Value> recAns = ((VarInitVal) child).initLLVMValue();
                    ans.addAll(recAns);
                }
            }
        }
        this.values = ans;
        return ans;
    }
}
