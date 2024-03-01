package symbol;

import AST.InitVal.ConstInitVal;
import type.FuncReturnType;
import type.SymbolType;

import java.util.ArrayList;

public class SymbolManager {
    public static final SymbolManager MANAGER = new SymbolManager();    // 单例模式
    public SymbolTable rootTable;  // 根符号表
    public SymbolTable curTable;  // 当前的符号表
    public int loopDepth;    // 当前的循环深度
    public int blockDepth;   // 当前的符号表深度

    public SymbolManager() {
        loopDepth = 0;
        blockDepth = 0;
    }

    // 最顶层(CompUnit)的enterBlock
    public void createRootSymbolTable() {
        MANAGER.rootTable = new SymbolTable();
        curTable = MANAGER.rootTable;
    }

    public void enterBlock() {
        blockDepth++;
        SymbolTable table = new SymbolTable();
        curTable.addNextTable(table);
        table.setPreTable(curTable);
        curTable = table;
    }

    public void quitBlock() {
        blockDepth--;
        curTable = curTable.getPreTable();
    }

    public void enterFunc(FuncReturnType type) {
        enterBlock();
        curTable.setType(type);
    }

    public void quitFunc() {
        quitBlock();
    }

    public void enterFor() {
        loopDepth++;
    }

    public void quitFor() {
        loopDepth--;
    }

    public FuncReturnType getCurTableFuncReturnType() {
        return curTable.getType();
    }

    public boolean checkLoopDepth() {
        return this.loopDepth != 0;
    }

    // true代表检查通过 没有redefine
    public boolean checkRedefine(String name) {
        return curTable.selectSymbolByName(name) == null;
    }

    public Symbol selectSymbolByName(String name) {
        if (curTable.selectSymbolByName(name) != null) {
            return curTable.selectSymbolByName(name);
        } else {
            SymbolTable table = curTable;
            while (table.hasPreTable()) {
                table = table.getPreTable();
                if (table.selectSymbolByName(name) != null) {
                    return table.selectSymbolByName(name);
                }
            }
            return null;
        }
    }

    public FuncSymbol selectFuncSymbolByName(String name) {
        Symbol symbol;
        if ((symbol = curTable.selectSymbolByName(name)) != null && symbol instanceof FuncSymbol) {
            return (FuncSymbol) symbol;
        } else {
            SymbolTable table = curTable;
            while (table.hasPreTable()) {
                table = table.getPreTable();
                if ((symbol = table.selectSymbolByName(name)) != null && symbol instanceof FuncSymbol) {
                    return (FuncSymbol) symbol;
                }
            }
            return null;
        }
    }

    public Symbol selectVCSymbolByName(String name) {
        Symbol symbol;
        if ((symbol = curTable.selectSymbolByName(name)) != null && (symbol instanceof VarSymbol || symbol instanceof ConstSymbol)) {
            return symbol;
        } else {
            SymbolTable table = curTable;
            while (table.hasPreTable()) {
                table = table.getPreTable();
                if ((symbol = table.selectSymbolByName(name)) != null && (symbol instanceof VarSymbol || symbol instanceof ConstSymbol)) {
                    return symbol;
                }
            }
            return null;
        }
    }

    public VarSymbol registerVarSymbol(String name, int dim, ArrayList<Integer> dimList) {
        VarSymbol varSymbol = null;
        switch (dim) {
            case 0 -> varSymbol = new VarSymbol(curTable, name, SymbolType.ARRAY_DIM_0, dim, dimList);
            case 1 -> varSymbol = new VarSymbol(curTable, name, SymbolType.ARRAY_DIM_1, dim, dimList);
            case 2 -> varSymbol = new VarSymbol(curTable, name, SymbolType.ARRAY_DIM_2, dim, dimList);
            default -> System.err.println("registerVarSymbol:未知的ConstExp长度");
        }
        curTable.insertSymbol(varSymbol);
        return varSymbol;
    }

    public FuncSymbol registerFuncSymbol(String name, FuncReturnType returnType) {
        FuncSymbol funcSymbol = new FuncSymbol(curTable, name, SymbolType.FUNC, returnType);
        curTable.insertSymbol(funcSymbol);
        return funcSymbol;
    }

    public ConstSymbol registerConstSymbol(String name, int dim, ArrayList<Integer> dimList, ConstInitVal constInitVal) {
        ConstSymbol constSymbol = null;
        switch (dim) {
            case 0 -> constSymbol = new ConstSymbol(curTable, name, SymbolType.ARRAY_DIM_0, dim, dimList, constInitVal);
            case 1 -> constSymbol = new ConstSymbol(curTable, name, SymbolType.ARRAY_DIM_1, dim, dimList, constInitVal);
            case 2 -> constSymbol = new ConstSymbol(curTable, name, SymbolType.ARRAY_DIM_2, dim, dimList, constInitVal);
            default -> System.err.println("registerConstSymbol:未知的ConstExp长度");
        }
        curTable.insertSymbol(constSymbol);
        return constSymbol;
    }

    // 检查当前是否为全局范围(最顶层)的变量
    public boolean isGlobal() {
        return curTable == rootTable;
    }
}
