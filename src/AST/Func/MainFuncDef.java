package AST.Func;

import AST.Block.Block;
import AST.Node;
import AST.Stmt;
import IO.Output;
import error.Error;
import llvm.ModuleBuilder;
import llvm.type.LLVMBasicType;
import llvm.value.BasicBlock;
import llvm.value.Value;
import llvm.value.user.Function;
import symbol.SymbolManager;
import type.ErrorType;
import type.FuncReturnType;
import type.StmtType;
import utils.NameGen;

// MainFuncDef → 'int' 'main' '(' ')' Block
public class MainFuncDef extends Node {
    Block block;

    public MainFuncDef(Node node) {
        super(node);
        this.block = (Block) this.getChildren().get(4);
    }

    public boolean checkReturn() {
        // return 必是最后一个BlockItem(简化条件)
        // Block → '{' { BlockItem } '}'
        if (block.hasBlockItem()) {
            Node retStmt = block.getChildren().get(block.getChildren().size() - 2);     // size() >= 2
            // BlockItem → Decl | Stmt
            Node ret = retStmt.getChildren().get(0);
            if (ret instanceof Stmt && ((Stmt) ret).getType() == StmtType.RETURN_STMT && ret.getChildren().size() >= 2) {
                return true;
            } else {
                Output.output.addErrorMsg(new Error(this.getEndLine(), ErrorType.g, "MainFuncDef:无Return或Return出错"));
                return false;
            }
        } else {
            Output.output.addErrorMsg(new Error(this.getEndLine(), ErrorType.g, "MainFuncDef:返回类型为int却无Return"));
            return false;
        }
    }

    @Override
    public boolean handleError() {
        checkReturn();
        boolean ans;
        SymbolManager.MANAGER.enterBlock();
        ans = super.handleError();
        SymbolManager.MANAGER.quitBlock();
        return ans;
    }

    /*
    define dso_local i32 @main(){

    }
    */
    @Override
    public Value genIR(){
        SymbolManager.MANAGER.enterFunc(FuncReturnType.INT);
        String funcName = NameGen.getInstance().genFuncName("main");
        Function mainFunc = new Function(LLVMBasicType.INT32, funcName);
        ModuleBuilder.getInstance().initFunction(mainFunc);
        // 函数参数和函数体生成IR
        super.genIR();
        SymbolManager.MANAGER.quitFunc();
        return null;
    }
}
