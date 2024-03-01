package AST;

import AST.Exp.CalExp.Exp;
import AST.Exp.CalExp.LVal;
import AST.Exp.CondExp.Cond;
import IO.Output;
import error.Error;
import llvm.ModuleBuilder;
import llvm.type.LLVMBasicType;
import llvm.value.BasicBlock;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.BrInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.RetInstr;
import llvm.value.user.instr.libInstr.GetintInstr;
import llvm.value.user.instr.libInstr.PutchInstr;
import llvm.value.user.instr.libInstr.PutintInstr;
import symbol.ConstSymbol;
import symbol.Symbol;
import symbol.SymbolManager;
import type.ErrorType;
import type.FuncReturnType;
import type.StmtType;
import type.SyntaxType;
import type.TokenType;
import utils.NameGen;

import java.util.ArrayList;

public class Stmt extends Node {
    private StmtType type;

    public Stmt(Node node, StmtType type) {
        super(node);
        this.type = type;
    }

    public StmtType getType() {
        return type;
    }

    @Override
    public boolean handleError() {
        switch (type) {
            case LVAL_STMT -> {
                return handleLVal();
            }
            case EXP_STMT -> {
                return handleExp();
            }
            case BLOCK_STMT -> {
                return handleBlock();
            }
            case IF_STMT -> {
                return handleIf();
            }
            case FOR_STMT -> {
                return handleFor();
            }
            case BREAK_STMT -> {
                return handleBreak();
            }
            case CONTINUE_STMT -> {
                return handleContinue();
            }
            case RETURN_STMT -> {
                return handleReturn();
            }
            case GETINT_STMT -> {
                return handleGetInt();
            }
            case PRINTF_STMT -> {
                return handlePrintf();
            }
            default -> {
                System.err.println("Stmt:未知的stmt类型");
                return false;
            }
        }
    }

    // LVal '=' Exp ';'  // h
    public boolean handleLVal() {
        boolean check = true;
        LVal lVal = (LVal) this.getChildren().get(0);
        Symbol symbol;
        if ((symbol = SymbolManager.MANAGER.selectSymbolByName(lVal.getIdent().getValue())) != null) {
            if (symbol instanceof ConstSymbol) {
                Output.output.addErrorMsg(new Error(lVal.getStartLine(), ErrorType.h, "Stmt:LVal是常量却被赋值"));
                check = false;
            }
        }
        check &= super.handleError();
        return check;
    }

    // [Exp] ';'
    public boolean handleExp() {
        return super.handleError();
    }

    // Block
    public boolean handleBlock() {
        SymbolManager.MANAGER.enterBlock();
        boolean ans = super.handleError();
        SymbolManager.MANAGER.quitBlock();
        return ans;
    }

    // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public boolean handleIf() {
        return super.handleError();
    }

    // 'for' '('[ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    public boolean handleFor() {
        SymbolManager.MANAGER.enterFor();
        boolean ans = super.handleError();
        SymbolManager.MANAGER.quitFor();
        return ans;
    }

    // 'break' ';'
    public boolean handleBreak() {
        if (!SymbolManager.MANAGER.checkLoopDepth()) {
            Output.output.addErrorMsg(new Error(this.getStartLine(), ErrorType.m, "在非循环块中使用break"));
            return false;
        }
        return true;
    }

    // 'continue' ';'
    public boolean handleContinue() {
        if (!SymbolManager.MANAGER.checkLoopDepth()) {
            Output.output.addErrorMsg(new Error(this.getStartLine(), ErrorType.m, "在非循环块中使用continue"));
            return false;
        }
        return true;
    }

    // 'return' [Exp] ';'
    public boolean handleReturn() {
        boolean check = true;
        if (SymbolManager.MANAGER.getCurTableFuncReturnType() == FuncReturnType.VOID && this.getChildren().size() > 2) {
            Output.output.addErrorMsg(new Error(this.getStartLine(), ErrorType.f, "Stmt:VOID类型函数存在不匹配的return语句"));
            check = false;
        }
        check &= super.handleError();
        return check;
    }

    // LVal '=' 'getint''('')'';' // h i j
    public boolean handleGetInt() {
        boolean check = true;
        LVal lVal = (LVal) this.getChildren().get(0);
        Symbol symbol;
        // <LVal>为常量时，不能对其修改。
        if ((symbol = SymbolManager.MANAGER.selectSymbolByName(lVal.getIdent().getValue())) != null) {
            if (symbol instanceof ConstSymbol) {
                Output.output.addErrorMsg(new Error(lVal.getStartLine(), ErrorType.h, "Stmt:LVal是常量却被赋值"));
                check = false;
            }
        }
        check &= super.handleError();
        return check;
    }

    // 'printf''('FormatString{,Exp}')'';' // i j l
    public boolean handlePrintf() {
        boolean check = true;
        check &= super.handleError();
        String formatString = this.getChildren().get(2).getTerminal().getValue();
        int formatCount = getParaCount(formatString);
        int expCount = 0;
        for (Node node : this.getChildren()) {
            if (node instanceof Exp) {
                expCount++;
            }
        }
        if (expCount != formatCount) {
            Output.output.addErrorMsg(new Error(this.getStartLine(), ErrorType.l, "printf参数数量不匹配"));
            check = false;
        }
        return check;
    }

    public int getParaCount(String input) {
        int count = 0;
        int index = 0;
        String pattern = "%d";
        while (index != -1) {
            index = input.indexOf(pattern, index);
            if (index != -1) {
                count++;
                index += pattern.length();
            }
        }
        return count;
    }

    @Override
    public Value genIR() {
        switch (type) {
            case LVAL_STMT -> {
                return genLVal();
            }
            case EXP_STMT -> {
                return genExp();
            }
            case BLOCK_STMT -> {
                return genBlock();
            }
            case IF_STMT -> {
                return genIf();
            }
            case FOR_STMT -> {
                return genFor();
            }
            case BREAK_STMT -> {
                return genBreak();
            }
            case CONTINUE_STMT -> {
                return genContinue();
            }
            case RETURN_STMT -> {
                return genReturn();
            }
            case GETINT_STMT -> {
                return genGetInt();
            }
            case PRINTF_STMT -> {
                return genPrintf();
            }
            default -> {
                System.err.println("Stmt:未知的stmt类型");
                return null;
            }
        }
    }

    private Value genPrintf() {
        String oriString = this.getChildren().get(2).getTerminal().getValue();
        String fString = oriString.substring(1, oriString.length() - 1);
        ArrayList<Exp> exps = new ArrayList<>();
        for (Node child : this.getChildren()) {
            if (child instanceof Exp) {
                exps.add((Exp) child);
            }
        }
        int cnt = 0;
        for (int i = 0; i < fString.length(); i++) {
            if (fString.charAt(i) == '%') {
                // skip '%'
                i++;    // skip 'd' in forStmt i++
                new PutintInstr(exps.get(cnt++).genIR());
            } else if (fString.charAt(i) == '\\') {
                i++;    // skip 'n'
                new PutchInstr(new Constant(LLVMBasicType.INT32, 10));
            } else {
                new PutchInstr(new Constant(LLVMBasicType.INT32, fString.charAt(i)));
            }
        }
        return null;
    }

    private Value genGetInt() {
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        Instr getintInstr = new GetintInstr(NameGen.getInstance().genLocalVarName(curFunction));
        LVal lVal = (LVal) this.getChildren().get(0);
        lVal.storeLValIR(getintInstr);
        return null;
    }

    private Value genReturn() {
        // return;
        Instr instr;
        if (this.getChildren().size() == 2) {
            instr = new RetInstr();
        } else {
            Value retValue = this.getChildren().get(1).genIR();
            instr = new RetInstr(retValue);
        }
        return instr;
    }

    private Value genContinue() {
        new BrInstr(ModuleBuilder.getInstance().getForStmt2BB());
        return null;
    }

    private Value genBreak() {
        new BrInstr(ModuleBuilder.getInstance().getAfterBB());
        return null;
    }

    // 'for' '(' [ForStmt1] ';' [Cond] ';' [ForStmt2] ')' Stmt
    private Value genFor() {
        SymbolManager.MANAGER.enterFor();
        // 生成ForStmt1 IR
        ForStmt forStmt1 = (this.getChildren().get(2) instanceof ForStmt) ?
                (ForStmt) this.getChildren().get(2) : null;
        if (forStmt1 != null) {
            forStmt1.genIR();
        }
        // 解析cond和forStmt2
        Cond cond = null;
        ForStmt forStmt2 = null;
        for (int i = 0; i < this.getChildren().size(); i++) {
            Node node = this.getChildren().get(i);
            if (node instanceof Cond) {
                cond = (Cond) node;
            }
            if (node.getSyntaxType() == SyntaxType.TOKEN && node.getTerminal().getType() == TokenType.RPARENT) {
                Node node1 = this.getChildren().get(i - 1);
                if (node1 instanceof ForStmt) {
                    forStmt2 = (ForStmt) node1;
                }
            }
        }
        Stmt stmt = (Stmt) this.getChildren().get(this.getChildren().size() - 1);
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        BasicBlock curBB = ModuleBuilder.getInstance().getCurBB();
        BasicBlock condBB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction));
        BasicBlock stmtBB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction));
        BasicBlock forStmt2BB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction));
        BasicBlock afterBB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction));
        // prepare for break and continue
        ModuleBuilder.getInstance().setForStmt2BB(forStmt2BB);
        ModuleBuilder.getInstance().setAfterBB(afterBB);
        // ForStmt1 跳入 CondBB
        ModuleBuilder.getInstance().setCurBB(curBB);
        new BrInstr(condBB);
        // 解析Cond IR
        ModuleBuilder.getInstance().setCurBB(condBB);
        // 有条件跳转 成功BB=stmtBB 失败BB=afterBB
        if (cond != null) {
            cond.genCondIR(stmtBB, afterBB);
        }
        // 无条件跳转 到stmtBB
        else {
            new BrInstr(stmtBB);
        }
        // 解析stmt IR 完毕后跳转到forStmt2BB
        ModuleBuilder.getInstance().setCurBB(stmtBB);
        stmt.genIR();
        new BrInstr(forStmt2BB);
        // 解析stmt2 完毕后跳转到condBB
        ModuleBuilder.getInstance().setCurBB(forStmt2BB);
        if (forStmt2 != null) {
            forStmt2.genIR();
        }
        new BrInstr(condBB);
        // 转到afterBB
        ModuleBuilder.getInstance().setCurBB(afterBB);
        // addBBs2Func
        condBB.add2Func(curFunction);
        stmtBB.add2Func(curFunction);
        forStmt2BB.add2Func(curFunction);
        afterBB.add2Func(curFunction);

        SymbolManager.MANAGER.quitFor();
        return null;
    }

    // 'if' '(' Cond ')' Stmt1 [ 'else' Stmt2 ]
    private Value genIf() {
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        BasicBlock curBB = ModuleBuilder.getInstance().getCurBB();
        BasicBlock stmt1BB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction));
        BasicBlock afterBB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction));
        // 无else
        if (this.getChildren().size() == 5) {
            // 在当前BB下开始Cond解析
            ModuleBuilder.getInstance().setCurBB(curBB);
            Cond cond = (Cond) this.getChildren().get(2);
            cond.genCondIR(stmt1BB, afterBB);
            // 设置curBB = stmt1BB 开始Stmt1 IR生成
            ModuleBuilder.getInstance().setCurBB(stmt1BB);
            this.getChildren().get(4).genIR();
            new BrInstr(afterBB);
            // 设置curBB = afterBB 继续之后的IR生成
            ModuleBuilder.getInstance().setCurBB(afterBB);
            // 将生成的BB在此时加入Func 避免递归的顺序混乱(虽然在Cond短路求值的过程中还是会生成一堆)
            stmt1BB.add2Func(curFunction);
            afterBB.add2Func(curFunction);
        }
        // 有else
        else {
            BasicBlock stmt2BB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction));

            ModuleBuilder.getInstance().setCurBB(curBB);
            Cond cond = (Cond) this.getChildren().get(2);
            cond.genCondIR(stmt1BB, stmt2BB);
            // 设置curBB = stmt1BB 开始Stmt1 IR生成
            ModuleBuilder.getInstance().setCurBB(stmt1BB);
            this.getChildren().get(4).genIR();
            new BrInstr(afterBB);
            // 设置curBB = stmt2BB 开始Stmt2 IR生成
            ModuleBuilder.getInstance().setCurBB(stmt2BB);
            this.getChildren().get(6).genIR();
            Instr stmt2BrInstr = new BrInstr(afterBB);
            // 设置curBB = afterBB 继续之后的IR生成
            ModuleBuilder.getInstance().setCurBB(afterBB);
            // addBBs2Func
            stmt1BB.add2Func(curFunction);
            stmt2BB.add2Func(curFunction);
            afterBB.add2Func(curFunction);
        }
        return null;
    }

    private Value genBlock() {
        SymbolManager.MANAGER.enterBlock();
        Value ans = super.genIR();
        SymbolManager.MANAGER.quitBlock();
        return ans;
    }

    // 由于可能存在Exp->Ident(),即函数调用,将全局变量值改变.所以有必要写
    private Value genExp() {
        return super.genIR();
    }

    private Value genLVal() {
        LVal lVal = (LVal) this.getChildren().get(0);
        Exp exp = (Exp) this.getChildren().get(2);
        return lVal.storeLValIR(exp.genIR());
    }

}
