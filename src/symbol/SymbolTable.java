package symbol;

import type.FuncReturnType;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, Symbol> table;
    private int level;  // 层次
    private SymbolTable preTable;   // 上一级符号表 链表的pre 只能存在一个
    private ArrayList<SymbolTable> nextTable;   // 下一级符号表 链表的next 可以存在多个

    private FuncReturnType type;    // 如果是函数的符号表 保存它的ReturnType

    public SymbolTable() {
        this.table = new HashMap<>();
        this.nextTable = new ArrayList<>();
    }
    public void setType(FuncReturnType type) {
        this.type = type;
    }

    public FuncReturnType getType() {
        return type;
    }

    public void addNextTable(SymbolTable table){
        this.nextTable.add(table);
    }


    public Symbol selectSymbolByName(String name){
        return table.get(name);
    }

    public void insertSymbol(Symbol symbol){
        this.table.put(symbol.getName(), symbol);
    }

    public boolean hasPreTable(){
        return preTable != null;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public HashMap<String, Symbol> getTable() {
        return table;
    }

    public void setTable(HashMap<String, Symbol> table) {
        this.table = table;
    }

    public int getLevel() {
        return level;
    }

    public SymbolTable getPreTable() {
        return preTable;
    }

    public void setPreTable(SymbolTable preTable) {
        this.preTable = preTable;
    }

    public ArrayList<SymbolTable> getNextTable() {
        return nextTable;
    }

    public void setNextTable(ArrayList<SymbolTable> nextTable) {
        this.nextTable = nextTable;
    }
}
