package symbol;

import llvm.value.Value;
import type.SymbolType;

import java.util.ArrayList;

public class Symbol {
    protected SymbolTable table;    // 所在的符号表
    protected String name;    // 该符号对应的名称
    protected SymbolType type;    // 符号的类型 0-2维数组/函数
    protected Value llvmValue;  // 需要在符号表中记录LLVM的Value,以便后续查找

    public Symbol(SymbolTable table, String name, SymbolType type) {
        this.table = table;
        this.name = name;
        this.type = type;
    }

    public Value getLlvmValue() {
        return llvmValue;
    }

    public void setLlvmValue(Value llvmValue) {
        this.llvmValue = llvmValue;
    }

    public String getName() {
        return name;
    }

    public SymbolType getType() {
        return type;
    }

    public int getDim() {
        return 0;
    }
    public ArrayList<Integer> getDimList() {
        return null;
    }
    public int getInitValue(ArrayList<Integer> dims) {
        return 0;
    }

    public int getInitValue() {
        return 0;
    }
}
