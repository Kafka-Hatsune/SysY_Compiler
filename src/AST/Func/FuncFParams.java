package AST.Func;

import AST.Node;
import llvm.value.Value;
import type.SymbolType;

import java.util.ArrayList;

// FuncFParams → FuncFParam { ',' FuncFParam }
public class FuncFParams extends Node {

    private ArrayList<FuncFParam> funcFParams;
    public FuncFParams(Node node) {
        super(node);
        this.funcFParams = new ArrayList<>();
        for(Node child:this.getChildren()){
            if(child instanceof FuncFParam){
                this.funcFParams.add((FuncFParam) child);
            }
        }
    }
    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    public ArrayList<SymbolType> getFuncFParamsSymbolTypes(){
        ArrayList<SymbolType> symbolTypes = new ArrayList<>();
        for(FuncFParam funcFParam : funcFParams){
            symbolTypes.add(funcFParam.getSymbolType());
        }
        return symbolTypes;
    }

    @Override
    public boolean handleError(){
        return super.handleError();
    }

    public ArrayList<Integer> getFParamDims(){
        ArrayList<Integer> arrayList = new ArrayList<>();
        for(FuncFParam funcFParam : this.funcFParams){
            arrayList.add(funcFParam.getDim());
        }
        return arrayList;
    }

    @Override
    public Value genIR(){
        return super.genIR();
    }
}
