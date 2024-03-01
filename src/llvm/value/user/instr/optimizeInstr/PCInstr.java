package llvm.value.user.instr.optimizeInstr;

import llvm.type.LLVMBasicType;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import type.LLVMInstrType;

import java.util.ArrayList;

public class PCInstr extends Instr {

    private ArrayList<Value> dstList = new ArrayList<>();
    private ArrayList<Value> srcList = new ArrayList<>();

    private int indexOfBBInstrList;
    public PCInstr(String name) {
        super(LLVMBasicType.VOID, name);
        this.instrType = LLVMInstrType.PC;
    }

    public void addCopy(Value dst, Value src) {
        dstList.add(dst);
        srcList.add(src);
    }

    public int getIndexOfBBInstrList() {
        return indexOfBBInstrList;
    }

    public void setIndexOfBBInstrList(int indexOfBBInstrList) {
        this.indexOfBBInstrList = indexOfBBInstrList;
    }

    public ArrayList<Value> getDstList() {
        return dstList;
    }

    public ArrayList<Value> getSrcList() {
        return srcList;
    }

    @Override
    public String toString() {
        return "pc";
    }
}
