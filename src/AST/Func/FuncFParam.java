package AST.Func;

import AST.Exp.CalExp.ConstExp;
import AST.Node;
import IO.Output;
import error.Error;
import lexer.Token;
import llvm.ModuleBuilder;
import llvm.type.LLVMArrayType;
import llvm.type.LLVMBasicType;
import llvm.type.LLVMPointerType;
import llvm.value.FuncParam;
import llvm.value.Value;
import llvm.value.user.Function;
import symbol.SymbolManager;
import symbol.VarSymbol;
import type.ErrorType;
import type.SymbolType;
import type.SyntaxType;
import type.TokenType;
import utils.NameGen;

import java.util.ArrayList;

// FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
public class FuncFParam extends Node {
    private SymbolType symbolType;
    private Token ident;
    private ArrayList<Integer> dimList;     // 空缺的ConstExp用-1占位
    private int dim;    // FuncFParam形式 int a[], int a[][3]
    private VarSymbol symbol;

    public FuncFParam(Node node) {
        super(node);
        ident = this.getChildren().get(1).getTerminal();
        this.dimList = new ArrayList<>();
        for (int i = 0; i < this.getChildren().size(); i++) {
            Node child = this.getChildren().get(i);
            if (child.getSyntaxType() == SyntaxType.TOKEN && child.getTerminal().getType() == TokenType.LBRACK) {
                // i + 1 < this.getChildren().size() : 如果 ']' 缺失, 则 '[' 为最后一个child
                if (i + 1 < this.getChildren().size() && this.getChildren().get(++i) instanceof ConstExp) {
                    this.dimList.add(((ConstExp) this.getChildren().get(i)).calculate());
                } else {
                    this.dimList.add(-1);
                }
            }
        }
        dim = this.dimList.size();
        switch (dim) {
            case 0 -> this.symbolType = SymbolType.ARRAY_DIM_0;
            case 1 -> this.symbolType = SymbolType.ARRAY_DIM_1;
            case 2 -> this.symbolType = SymbolType.ARRAY_DIM_2;
            default -> System.err.println("FuncFParam: 未知的数组维度");
        }
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public boolean checkRedefine() {
        String name = this.ident.getValue();
        if (SymbolManager.MANAGER.checkRedefine(name)) {
            return true;
        } else {
            Output.output.addErrorMsg(new Error(ident.getLine(), ErrorType.b, "FuncFPara重定义"));
            return false;
        }
    }

    @Override
    public boolean handleError() {
        if (super.handleError()) {
            if (!checkRedefine()) {
                return false;
            }
            this.symbol = SymbolManager.MANAGER.registerVarSymbol(ident.getValue(), dim, dimList);
        }
        return true;
    }

    public Integer getDim() {
        return dim;
    }

    public VarSymbol getSymbol() {
        return symbol;
    }

    @Override
    public Value genIR() {
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        // 函数参数为值传递
        // 当参数为0维时, 需要将参数保存到内部临时寄存器防止修改参数
        // 当参数为1维/2维时, 直接使用传入的参数(此时参数是地址, 题目不会改变地址)
        // 函数的paramList为对应的参数寄存器的值 如%var0, %var1,但是函数符号表中的参数的llvmValue是经过保存后的值
        this.symbol = SymbolManager.MANAGER.registerVarSymbol(ident.getValue(), dim, dimList);
        Value param;
        String paramName = NameGen.getInstance().genLocalVarName(curFunction);
        if (dim == 0) {
            param = new Value(LLVMBasicType.INT32, paramName);
        } else if (dim == 1) {
            param = new Value(new LLVMPointerType(LLVMBasicType.INT32), paramName);
        } else {
            int dimValue = this.dimList.get(1);
            LLVMPointerType paraType = new LLVMPointerType(new LLVMArrayType(dimValue, LLVMBasicType.INT32));
            param = new Value(paraType, paramName);
        }
        // Func的paramList保存的是原来参数的寄存器
        ModuleBuilder.getInstance().addParam2CurFunc(param);
        // SymbolTable保存的是经过保存的寄存器值
        symbol.setLlvmValue(new FuncParam(param, dim).getAddr());
        return null;
    }
}
