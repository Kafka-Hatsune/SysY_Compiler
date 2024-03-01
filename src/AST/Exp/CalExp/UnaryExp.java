package AST.Exp.CalExp;

import AST.Exp.UnaryOp;
import AST.Func.FuncRParams;
import AST.Node;
import IO.Output;
import error.Error;
import lexer.Token;
import llvm.ModuleBuilder;
import llvm.type.LLVMBasicType;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.CallInstr;
import llvm.value.user.instr.aluInstr.IcmpInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.aluInstr.SubInstr;
import llvm.value.user.instr.ZextInstr;
import symbol.FuncSymbol;
import symbol.SymbolManager;
import type.ErrorType;
import type.InstrIcmpCondType;
import type.SyntaxType;
import type.TokenType;
import utils.Calculable;
import utils.NameGen;

import java.util.ArrayList;
import java.util.Objects;

//  UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // c d e j
public class UnaryExp extends Node implements Calculable {
    FuncRParams funcRParams = null;

    public UnaryExp(Node node) {
        super(node);
        for (Node child : this.getChildren()) {
            if (child instanceof FuncRParams) {
                this.funcRParams = (FuncRParams) child;
            }
        }
    }

    @Override
    public boolean handleError() {
        boolean check;
        if (this.getChildren().get(0).getSyntaxType() == SyntaxType.TOKEN) {
            return handleFuncCallError();
        }
        // 处理相同
        else {
            return super.handleError();
        }
    }

    public boolean handleFuncCallError() {
        boolean check = true;
        Token ident = this.getChildren().get(0).getTerminal();
        FuncSymbol funcSymbol = SymbolManager.MANAGER.selectFuncSymbolByName(ident.getValue());
        // c 未定义的名字 <UnaryExp>→<Ident> …
        if (funcSymbol == null) {
            Output.output.addErrorMsg(new Error(ident.getLine(), ErrorType.c, "UnaryExp使用了未定义符号"));
            super.handleError();    // 有可能其他错误在下面的行
            return false;
        }
        check &= super.handleError();   // 检查FuncRParams
        if (!check) {
            return false;
        }
        // 必定为FuncSymbol?
        ArrayList<Integer> fSymbols = funcSymbol.getFParaDims();
        ArrayList<Integer> rSymbols = funcRParams == null ? new ArrayList<>() : funcRParams.getRParamsDims();
        int fParaNum = fSymbols.size();
        int rParaNum = rSymbols.size();
        // d 函数参数个数不匹配
        if (fParaNum != rParaNum) {
            Output.output.addErrorMsg(new Error(ident.getLine(), ErrorType.d, "UnaryExp:函数参数个数不匹配"));
            super.handleError();
            return false;
        }
        // e 函数参数类型不匹配
        for (int i = 0; i < fParaNum; i++) {
            // 形参为 int[]
            if (fSymbols.get(i) == -1) {

            } else if (!Objects.equals(fSymbols.get(i), rSymbols.get(i))) {
                Output.output.addErrorMsg(new Error(ident.getLine(), ErrorType.e, "UnaryExp:函数参数类型不匹配"));
                super.handleError();
                return false;
            }
        }
        return check; //
    }

    @Override
    public Integer calculate() {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        Node node = this.getChildren().get(0);
        if (node instanceof PrimaryExp) {
            return ((PrimaryExp)node).calculate();
        }
        else if (node instanceof UnaryOp) {
            TokenType type = ((UnaryOp) node).getType();
            UnaryExp unaryExp = (UnaryExp) this.getChildren().get(1);
            switch (type) {
                case PLUS -> {
                    return unaryExp.calculate();
                }
                case MINU -> {
                    return -unaryExp.calculate();
                }
                case NOT -> {
                    Integer ans = unaryExp.calculate();
                    if (ans == null) {
                        return null;
                    } else {
                        return ans.equals(0) ? 1 : 0;
                    }
                }
                default -> {
                    System.err.println("UnaryExp:在常量计算中解析出未知符号");
                }
            }
        }
        // 计算中出现了函数调用 Ident '(' [FuncRParams] ')' 认为肯定算不出来
        else {
            return null;
        }
        return null;
    }

    public Integer getDim() {
        Node node = this.getChildren().get(0);
        if (node instanceof PrimaryExp) {
            return node.getDim();
        } else if (node instanceof UnaryOp) {
            return this.getChildren().get(1).getDim();
        } else {
            Token ident = this.getChildren().get(0).getTerminal();
            FuncSymbol funcSymbol = (FuncSymbol) SymbolManager.MANAGER.selectSymbolByName(ident.getValue());
            switch (funcSymbol.getReturnType()) {
                case INT -> {
                    return 0;
                }
                case VOID -> {
                    return -1;
                }
            }
        }
        return 0;
    }

    @Override
    public Value genIR() {
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        Node node = this.getChildren().get(0);
        if (node instanceof PrimaryExp) {
            return node.genIR();
        } else if (node instanceof UnaryOp) {
            TokenType type = ((UnaryOp) node).getType();
            UnaryExp unaryExp = (UnaryExp) this.getChildren().get(1);
            switch (type) {
                case PLUS -> {
                    return unaryExp.genIR();
                }
                case MINU -> {
                    Value value = unaryExp.genIR();
                    return new SubInstr(NameGen.getInstance().genLocalVarName(curFunction), Constant.ConstantZero, value);
                }
                case NOT -> {
                    String instr1Name = NameGen.getInstance().genLocalVarName(curFunction);
                    String instr2Name = NameGen.getInstance().genLocalVarName(curFunction);
                    Value value = unaryExp.genIR();
                    Instr instr1 = new IcmpInstr(instr1Name, InstrIcmpCondType.EQ, value, Constant.ConstantZero);
                    Instr instr2 = new ZextInstr(LLVMBasicType.INT1, instr1, LLVMBasicType.INT32, instr2Name);
                    return instr2;
                }
                default -> {
                    System.err.println("UnaryExp-genIR:在常量计算中解析出未知符号");
                }
            }
        } else {
            Token ident = this.getChildren().get(0).getTerminal();
            FuncSymbol funcSymbol = (FuncSymbol) SymbolManager.MANAGER.selectSymbolByName(ident.getValue());
            Function function = (Function) funcSymbol.getLlvmValue();
            ArrayList<Value> funcRValues = new ArrayList<>();
            if(this.funcRParams != null){
                ArrayList<Exp> funcRExps = this.funcRParams.getExps();
                for (Exp exp :funcRExps){
                    funcRValues.add(exp.genIR());
                }
            }
            return new CallInstr(function.getType(), NameGen.getInstance().genLocalVarName(curFunction), function, funcRValues);
        }
        return null;
    }
}
