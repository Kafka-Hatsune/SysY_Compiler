package AST.Exp.CalExp;

import AST.Node;
import IO.Output;
import error.Error;
import lexer.Token;
import llvm.ModuleBuilder;
import llvm.type.LLVMArrayType;
import llvm.type.LLVMPointerType;
import llvm.type.LLVMType;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.GEPInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.LoadInstr;
import llvm.value.user.instr.StoreInstr;
import symbol.Symbol;
import symbol.SymbolManager;
import type.ErrorType;
import utils.Calculable;
import utils.NameGen;

import java.util.ArrayList;

// LVal → Ident {'[' Exp ']'}
public class LVal extends Node implements Calculable {
    private Token ident;
    private ArrayList<Exp> exps;

    public LVal(Node node) {
        super(node);
        ident = node.getChildren().get(0).getTerminal();
        exps = new ArrayList<>();
        for (Node child : this.getChildren()) {
            if (child instanceof Exp) {
                exps.add((Exp) child);
            }
        }
    }

    @Override
    public Integer calculate() {
        // TODO 注册变量初始值
        Symbol symbol = SymbolManager.MANAGER.selectSymbolByName(ident.getValue());
        ArrayList<Integer> dims = new ArrayList<>();
        for (Node child : this.getChildren()) {
            if (child instanceof Exp) {
                dims.add(((Exp) child).calculate());
            }
        }
        // 需要数组元素查询
        if (dims.size() > 0) {
            return symbol.getInitValue(dims);
        } else {
            return symbol.getInitValue();
        }
    }

    public Token getIdent() {
        return ident;
    }

    public boolean checkUndefine() {
        String name = ident.getValue();
        if (SymbolManager.MANAGER.selectVCSymbolByName(name) == null) {
            Output.output.addErrorMsg(new Error(ident.getLine(), ErrorType.c, "LVal使用了未定义符号"));
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean handleError() {
        boolean check;
        check = checkUndefine();
        check &= super.handleError();
        return check;
    }

    // 查表 def int a[][] 使用 a[] dim = 1
    public Integer getDim() {
        Symbol symbol = SymbolManager.MANAGER.selectVCSymbolByName(ident.getValue());
        int dim = symbol.getDim();
        return dim - exps.size();
    }

    public Value storeLValIR(Value value) {
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        int declDim = SymbolManager.MANAGER.selectVCSymbolByName(ident.getValue()).getDim();
        int useDim = exps.size();
        Symbol symbol = SymbolManager.MANAGER.selectVCSymbolByName(ident.getValue());
        Value alloca = symbol.getLlvmValue();
        LLVMType gepTy = ((LLVMPointerType)alloca.getType()).getPtrValType();
        Value lValValue;
        if (useDim == 0) {
            lValValue = alloca;
        } else if (useDim == 1) {
            ArrayList<Value> offsets = new ArrayList<>();
            if(gepTy.isInt32()){
                offsets.add(exps.get(0).genIR());
            }else {
                offsets.add(Constant.ConstantZero);
                offsets.add(exps.get(0).genIR());
            }
            lValValue = new GEPInstr(NameGen.getInstance().genLocalVarName(curFunction), gepTy ,alloca, offsets);
        } else if (useDim == 2) {
            ArrayList<Value> offsets = new ArrayList<>();
            if(((LLVMArrayType)gepTy).getDim() == 2){
                offsets.add(Constant.ConstantZero);
            }
            offsets.add(exps.get(0).genIR());
            offsets.add(exps.get(1).genIR());
            lValValue = new GEPInstr(NameGen.getInstance().genLocalVarName(curFunction),  gepTy,alloca, offsets);
        } else {
            System.err.println("LVal-storeLValValue:useDim conflicts declDim");
            lValValue = null;
        }
        Instr storeInstr = new StoreInstr(value, lValValue);
        return lValValue;
    }

    public Value loadLValIR() {
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        Symbol symbol = SymbolManager.MANAGER.selectVCSymbolByName(ident.getValue());
        int declDim = symbol.getDim();
        int lvalDim = exps.size();
        // 共9种情况 6种合法情况
        // declDim = 0; lvalDim = 0;    load
        // declDim = 1; lvalDim = 0;    gep
        // declDim = 1; lvalDim = 1;    gep+load
        // declDim = 2; lvalDim = 0;    gep
        // declDim = 2; lvalDim = 1;
        // declDim = 2; lvalDim = 2;    传地址
        Value lValLLVMValue = symbol.getLlvmValue();
        LLVMType gepTy = ((LLVMPointerType) lValLLVMValue.getType()).getPtrValType();
        if (declDim == 0 && lvalDim == 0) {
            Instr loadInstr = new LoadInstr(NameGen.getInstance().genLocalVarName(curFunction), lValLLVMValue);
            return loadInstr;
        }
        // int b[3]; func(b);
        else if (declDim == 1 && lvalDim == 0) {
            ArrayList<Value> offsets = new ArrayList<>();
            // gep i32, i32* %1, i32 ?
            if (gepTy.isInt32()) {
                offsets.add(Constant.ConstantZero);
            }
            // gep [3 x i32], [3 x i32]* @arr, i64 0, i64 0
            else {
                offsets.add(Constant.ConstantZero);
                offsets.add(Constant.ConstantZero);
            }
            Instr gepInstr = new GEPInstr(NameGen.getInstance().genLocalVarName(curFunction), gepTy, lValLLVMValue, offsets);
            return gepInstr;
        }
        // int b[3]; a=b[1]; gep+load
        else if (declDim == 1 && lvalDim == 1) {
            ArrayList<Value> offsets = new ArrayList<>();
            // gep i32, i32* %1, i32 ?
            if (gepTy.isInt32()) {
                offsets.add(exps.get(0).genIR());
            }
            // gep [3 x i32], [3 x i32]* @arr, i64 0, i64 1
            else {
                offsets.add(Constant.ConstantZero);
                offsets.add(exps.get(0).genIR());
            }
            Instr gepInstr = new GEPInstr(NameGen.getInstance().genLocalVarName(curFunction), gepTy, lValLLVMValue, offsets);
            Instr loadInstr = new LoadInstr(NameGen.getInstance().genLocalVarName(curFunction), gepInstr);
            return loadInstr;
        }
        // int a[2][2]; func(a);
        else if (declDim == 2 && lvalDim == 0) {
            ArrayList<Value> offsets = new ArrayList<>();
            // [2 x [2 x i32]]*
            if (((LLVMArrayType) gepTy).getDim() != 1){
                offsets.add(Constant.ConstantZero);
                offsets.add(Constant.ConstantZero);
                Instr gepInstr = new GEPInstr(NameGen.getInstance().genLocalVarName(curFunction), gepTy, lValLLVMValue, offsets);
                return gepInstr;
            }
            // [2 x i32]*
            else {
                return lValLLVMValue;
            }

        }
        // int a[2][2]; a[1];
        // int a[][2]; a[1] = {1,2,3}
        else if (declDim == 2 && lvalDim == 1) {
            ArrayList<Value> offsets = new ArrayList<>();
            // [2 x i32]*
            if (((LLVMArrayType) gepTy).getDim() == 1) {
                offsets.add(exps.get(0).genIR());
                offsets.add(Constant.ConstantZero);
            }
            // [2 x [3 x i32]]*
            else {
                offsets.add(Constant.ConstantZero);
                offsets.add(exps.get(0).genIR());
                offsets.add(Constant.ConstantZero);
            }
            Instr gepInstr = new GEPInstr(NameGen.getInstance().genLocalVarName(curFunction), gepTy, lValLLVMValue, offsets);
            return gepInstr;
        }
        // int a[2][2]; a[1][1];
        // int a[][2]; a[1][1];
        else if (declDim == 2 && lvalDim == 2) {
            ArrayList<Value> offsets = new ArrayList<>();
            // int a[][2] %13 = getelementptr [3 x i32], [3 x i32]* %12, i32 2, i32 2
            // int a[2][2] %16 = getelementptr [3 x [3 x i32]], [3 x [3 x i32]]* @b, i32 0, i32 2, i32 2
            if (((LLVMArrayType) gepTy).getDim() != 1) {
                offsets.add(Constant.ConstantZero);
            }
            offsets.add(exps.get(0).genIR());
            offsets.add(exps.get(1).genIR());
            String gepName = NameGen.getInstance().genLocalVarName(curFunction);
            String loadName = NameGen.getInstance().genLocalVarName(curFunction);
            Instr gepInstr = new GEPInstr(gepName, gepTy ,lValLLVMValue, offsets);
            Instr loadInstr = new LoadInstr(loadName, gepInstr);
            return loadInstr;
        } else {
            System.err.println("LVal-genIR:declDim和lvalDim错误");
            return null;
        }
    }

}
