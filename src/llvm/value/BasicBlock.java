package llvm.value;

import llvm.ModuleBuilder;
import llvm.type.LLVMBasicBlockType;
import llvm.type.LLVMType;
import llvm.value.user.Function;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.RetInstr;
import mips.instr.TextLabel;

import java.util.ArrayList;
import java.util.Comparator;

// 特殊的User
public class BasicBlock extends Value  {
    private ArrayList<Instr> instrList;

    private Function parentFunc;
    public BasicBlock(String name, Function parentFunc) {
        super(new LLVMBasicBlockType(), name);
        instrList = new ArrayList<>();
        parentFunc.addBB(this);
        this.parentFunc = parentFunc;
    }
    public BasicBlock(String name) {
        super(new LLVMBasicBlockType(), name);
        instrList = new ArrayList<>();
    }

    public ArrayList<Instr> getInstrList() {
        return instrList;
    }

    public void add2Func(Function parentFunc){
        parentFunc.addBB(this);
        this.parentFunc = parentFunc;
    }

    public Function getParentFunc() {
        return parentFunc;
    }

    public void setParentFunc(Function parentFunc) {
        this.parentFunc = parentFunc;
    }

    public void addInstr(Instr instr){
        this.instrList.add(instr);
    }

    public boolean lastInstrIsRet(){
        if(instrList.size() == 0){
            return false;
        }else {
            return instrList.get(instrList.size() - 1) instanceof RetInstr;
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(name.substring(1));
        sb.append(":\n");
        for (Instr instr : this.instrList){
            sb.append("\t");
            sb.append(instr.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

//    @Override
//    public int compare(BasicBlock o1, BasicBlock o2) {
//        return Integer.parseInt(o1.getName().substring(o1.getName().length()-1)) -
//                Integer.parseInt(o2.getName().substring(o2.getName().length()-1));
//    }

    @Override
    public void toMips() {
        new TextLabel(this.name.substring(1));
        for (Instr instr : this.instrList){
            instr.toMips();
        }
    }
}
