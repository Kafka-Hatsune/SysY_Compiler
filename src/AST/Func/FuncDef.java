package AST.Func;

import AST.Block.Block;
import AST.Node;
import AST.Stmt;
import IO.Output;
import error.Error;
import lexer.Token;
import llvm.ModuleBuilder;
import llvm.type.LLVMBasicType;
import llvm.type.LLVMType;
import llvm.value.Value;
import llvm.value.user.Function;
import symbol.FuncSymbol;
import symbol.SymbolManager;
import type.ErrorType;
import type.FuncReturnType;
import type.StmtType;
import type.SyntaxType;
import utils.NameGen;

import java.util.ArrayList;

// FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
public class FuncDef extends Node {
    private FuncReturnType funcReturnType;
    private Token ident;
    private FuncFParams funcFParams;
    private FuncSymbol funcSymbol;

    public FuncDef(Node parent) {
        super(parent);
        this.funcReturnType = ((FuncType) (this.getChildren().get(0))).getFuncType();
        this.ident = this.getChildren().get(1).getTerminal();
        if (this.getChildren().get(3) instanceof FuncFParams) {
            this.funcFParams = (FuncFParams) this.getChildren().get(3);
        } else {
            this.funcFParams = null;
        }
    }

    /*
        有返回值的函数缺少return语句
     */
    public boolean checkReturn() {
        // return 必是最后一个BlockItem(简化条件)
        // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        if (funcReturnType == FuncReturnType.VOID) {
            return true;
        }
        Block block = (Block) this.getChildren().get(this.getChildren().size() - 1);
        // Block → '{' { BlockItem } '}'
        if (block.hasBlockItem()) {
            Node retStmt = block.getChildren().get(block.getChildren().size() - 2);     // size() >= 2
            // BlockItem → Decl | Stmt
            if (retStmt.getSyntaxType() == SyntaxType.TOKEN) {
                // 无return
                Output.output.addErrorMsg(new Error(this.getEndLine(), ErrorType.g, "FuncDef:返回类型为int却无Return"));
                return false;
            }
            Node ret = retStmt.getChildren().get(0);
            if (ret instanceof Stmt && ((Stmt) ret).getType() == StmtType.RETURN_STMT && ret.getChildren().size() >= 2) {
                return true;
            } else {
                Output.output.addErrorMsg(new Error(this.getEndLine(), ErrorType.g, "FuncDef:返回类型为int却无Return"));
                return false;
            }
        } else {
            Output.output.addErrorMsg(new Error(this.getEndLine(), ErrorType.g, "FuncDef:返回类型为int却无Return"));
            return false;
        }
    }

    @Override
    public boolean handleError() {
        // 检查到重定义不应该插入符号表 其他情况都应该插入到符号表
        boolean checkAns = true;
        if (SymbolManager.MANAGER.checkRedefine(ident.getValue())) {
            String name = ident.getValue();
            funcSymbol = SymbolManager.MANAGER.registerFuncSymbol(name, funcReturnType);
        } else {
            Output.output.addErrorMsg(new Error(ident.getLine(), ErrorType.b, "FuncDef重定义"));
            checkAns = false;
        }
        checkAns &= checkReturn();
        // 进入函数
        SymbolManager.MANAGER.enterFunc(funcReturnType);
        // 处理形参的错误处理
        // 为当前函数注册形参的维度
        // 即使形参报错, 比如重定义, 也加入到函数的形参中
        if (this.funcFParams != null) {
            for (Node child : this.funcFParams.getFuncFParams()) {
                checkAns &= child.handleError();
            }
            ArrayList<Integer> fParamsDims = this.funcFParams.getFParamDims();
            funcSymbol.registerFParams(fParamsDims);
        }
        // 处理Block
        Block block = (Block) this.getChildren().get(this.getChildren().size() - 1);
        checkAns &= block.handleError();
        // 退出函数
        SymbolManager.MANAGER.quitFunc();
        return checkAns;
    }

    // TODO
    @Override
    public Value genIR() {
        funcSymbol = SymbolManager.MANAGER.registerFuncSymbol(ident.getValue(), funcReturnType);
        SymbolManager.MANAGER.enterFunc(this.funcReturnType);
        // new Function
        LLVMType funcRetType = this.funcReturnType == FuncReturnType.INT ? LLVMBasicType.INT32 :
                this.funcReturnType == FuncReturnType.VOID ? LLVMBasicType.VOID : LLVMBasicType.INT32;
        String funName = NameGen.getInstance().genFuncName(this.ident.getValue());
        Function func = new Function(funcRetType, funName);
        // ModuleBuilder init
        ModuleBuilder.getInstance().initFunction(func);
        // 先设置符号表对应的llvm 否则递归函数无法找到function
        funcSymbol.setLlvmValue(func);
        // 函数参数和函数体生成IR
        super.genIR();
        // 如果是void函数 最后一行加一句ret void(测试中有)
        func.addRetIfAbsent();
        SymbolManager.MANAGER.quitFunc();
        return func;
    }
}
