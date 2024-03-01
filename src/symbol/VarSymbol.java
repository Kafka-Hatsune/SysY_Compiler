package symbol;

import AST.InitVal.VarInitVal;
import type.SymbolType;

import java.util.ArrayList;

public class VarSymbol extends Symbol {
    private int dim;
    private ArrayList<Integer> dimList;
    private VarInitVal initVal;

    public VarSymbol(SymbolTable table, String name, SymbolType type, int dim, ArrayList<Integer> dimList) {
        super(table, name, type);
        this.dim = dim;
        this.dimList = dimList;
    }

    public ArrayList<Integer> getDimList() {
        return dimList;
    }

    public void setInitVal(VarInitVal initVal) {
        this.initVal = initVal;
    }
    @Override
    public int getInitValue(ArrayList<Integer> dims) {
        return initVal.getValueByIndex(dims);
    }
    @Override
    public int getInitValue() {
        return initVal.getValueByIndex();
    }
    @Override
    public int getDim() {
        return dim;
    }

}
