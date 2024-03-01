package symbol;

import AST.InitVal.ConstInitVal;
import type.SymbolType;

import java.util.ArrayList;

public class ConstSymbol extends Symbol {
    private int dim;
    private ArrayList<Integer> dimList;
    private ConstInitVal constInitVal;  // 常量值

    public ConstSymbol(SymbolTable table, String name, SymbolType type, int dim, ArrayList<Integer> dimList, ConstInitVal constInitVal) {
        super(table, name, type);
        this.dim = dim;
        this.dimList = dimList;
        this.constInitVal = constInitVal;
    }

    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }


    public ArrayList<Integer> getDimList() {
        return dimList;
    }

    @Override
    public int getDim() {
        return dim;
    }

    @Override
    public String toString() {
        return super.name + "=" + constInitVal.toString();
    }

    @Override
    public int getInitValue(ArrayList<Integer> dims) {
        return constInitVal.getValueByIndex(dims);
    }

    @Override
    public int getInitValue() {
        return constInitVal.getValueByIndex();
    }
}
