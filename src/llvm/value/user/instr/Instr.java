package llvm.value.user.instr;

import llvm.ModuleBuilder;
import llvm.type.LLVMType;
import llvm.value.BasicBlock;
import llvm.value.Value;
import llvm.value.user.User;
import type.LLVMInstrType;

import java.util.ArrayList;

public class Instr extends User {
    protected LLVMInstrType instrType;
    protected BasicBlock BB;    // 指令所在的BB

    public Instr(LLVMType type, String name, LLVMInstrType instrType) {
        super(type, name);
        this.instrType = instrType;
        ModuleBuilder.getInstance().addInstr2CurBB(this);
    }

    // constructor for instr which can't be inserted directly
    public Instr(LLVMType type, String name) {
        super(type, name);
    }

    public BasicBlock getBB() {
        return BB;
    }

    public void setBB(BasicBlock BB) {
        this.BB = BB;
    }


    // 优化相关
    // 一条指令只会出现一个def
    public Value getDef(){
        return this;
    }

    // 一条指令可能出现多个use
    public ArrayList<Value> getUse(){
        return new ArrayList<>();
    }
}
