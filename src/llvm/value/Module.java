package llvm.value;

import llvm.lib.IO;
import llvm.type.LLVMModule;
import llvm.value.user.Function;
import mips.MipsModule;
import mips.Reg;
import mips.instr.JumpInstr;
import mips.instr.LiInstr;
import mips.instr.SysCallInstr;
import optimize.Optimizer;
import type.MipsInstrType;

import java.io.IOException;
import java.util.ArrayList;

public class Module extends Value{
    private ArrayList<String> importList;      // 库函数列表
    private ArrayList<Function> functionList;   // 函数列表 main函数是该列表的最后一个
    private ArrayList<FString> fStringList;
    private ArrayList<GlobalVar> globalVarList; // 全局变量列表


    public Module() {
        super(new LLVMModule(), "module");
        this.importList = new ArrayList<>();
        this.functionList =new ArrayList<>();
        this.fStringList = new ArrayList<>();
        this.globalVarList = new ArrayList<>();
        for(IO io : IO.values()){
            this.importList.add(io.toString());
        }
    }

    public ArrayList<String> getImportList() {
        return importList;
    }

    public ArrayList<Function> getFunctionList() {
        return functionList;
    }

    public ArrayList<FString> getfStringList() {
        return fStringList;
    }

    public ArrayList<GlobalVar> getGlobalVarList() {
        return globalVarList;
    }

    public void addFunction(Function function){
        functionList.add(function);
    }
    public void addGlobalVar(GlobalVar globalVar){
        globalVarList.add(globalVar);
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(String i:importList){
            sb.append(i);
            sb.append("\n");
        }
        sb.append("\n");
        for(GlobalVar i:globalVarList){
            sb.append(i);
            sb.append("\n");
        }
        sb.append("\n");
        for (Function function :functionList){
            sb.append(function.toString());
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    public void toMips(){
        for (GlobalVar globalVar : globalVarList) {
            globalVar.toMips();
        }
        // TODO stringLiteral
        new JumpInstr(MipsInstrType.JAL, "main");
        new LiInstr(Reg.V0, 10);
        new SysCallInstr();

        for (Function function : functionList) {
            function.toMips();
        }
    }

    public void Optimize() {
        Optimizer optimizer =  new Optimizer(this);
        optimizer.optimize();
    }
}
