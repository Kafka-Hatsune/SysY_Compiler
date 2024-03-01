package AST;


import AST.Exp.CalExp.Exp;
import AST.Exp.CalExp.LVal;
import IO.Output;
import error.Error;
import llvm.value.Value;
import symbol.ConstSymbol;
import symbol.Symbol;
import symbol.SymbolManager;
import type.ErrorType;

// ForStmt → LVal '=' Exp   //h
public class ForStmt extends Node{
    public ForStmt(Node node) {
        super(node);
    }

    @Override
    public boolean handleError(){
        LVal lVal = (LVal) this.getChildren().get(0);
        Symbol symbol;
        if ((symbol = SymbolManager.MANAGER.selectSymbolByName(lVal.getIdent().getValue())) != null) {
            if (symbol instanceof ConstSymbol) {
                Output.output.addErrorMsg(new Error(lVal.getStartLine(), ErrorType.h, "ForStmt:LVal是常量却被赋值"));
                return false;
            }
        }
        return super.handleError();
    }

    @Override
    public Value genIR() {
        LVal lVal = (LVal) this.getChildren().get(0);
        Exp exp = (Exp) this.getChildren().get(2);
        return lVal.storeLValIR(exp.genIR());
    }
}
