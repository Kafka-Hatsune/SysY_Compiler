package symbol;

import type.FuncReturnType;
import type.SymbolType;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private FuncReturnType returnType;    // 函数返回类型
    // 形参的维度信息 int func(int a,int b[2], int c[][2]) -- 0,1,2
    private ArrayList<Integer> fParaDims;



    public FuncSymbol(SymbolTable table, String name, SymbolType type, FuncReturnType returnType) {
        super(table, name, type);
        this.returnType = returnType;
        this.fParaDims = new ArrayList<>();
    }

    public FuncReturnType getReturnType() {
        return returnType;
    }

    public int getParaNum() {
        return fParaDims.size();
    }

    // Func被作为函数实参时
    @Override
    public int getDim(){
        if(returnType == FuncReturnType.VOID){
            return -1;
        }else {
            return 0;
        }
    }

    public void registerFParams(ArrayList<Integer> fParaDims) {
        this.fParaDims = fParaDims;
    }

    public ArrayList<Integer> getFParaDims() {
        return fParaDims;
    }
}
