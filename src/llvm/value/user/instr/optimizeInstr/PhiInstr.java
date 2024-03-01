package llvm.value.user.instr.optimizeInstr;

import llvm.type.LLVMBasicType;
import llvm.value.BasicBlock;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import type.LLVMInstrType;

import java.util.ArrayList;

/**
 * PhiInstr的def-use关系比较复杂
 */
public class PhiInstr extends Instr {
    // 要求operand的value严格对应preBBList的index
    public ArrayList<BasicBlock> preBBList;

    public Instr allocInstr;

    public PhiInstr(String name, ArrayList<BasicBlock> preBBList, Instr allocInstr) {
        super(LLVMBasicType.INT32, name);
        this.instrType = LLVMInstrType.PHI;
        // 使用0占位:在辅助测试库A中出现了局部变量未初始化但是使用
        for (BasicBlock preBB : preBBList) {
            this.operandList.add(Constant.ConstantZero);
        }
        this.preBBList = preBBList;
        this.allocInstr = allocInstr;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.preBBList.size(); i++) {
            sb.append("[");
            sb.append(this.operandList.get(i).getName());
            sb.append(", ");
            sb.append(this.preBBList.get(i).getName());
            sb.append("],");
        }
        sb.deleteCharAt(sb.length() - 1);
        return name + " = phi i32 " + sb.toString();
    }

    /**
     * @param value 从BB来则当前值设为value
     * @param BB    从哪个BB跳转过来
     */
    public void addEntry(Value value, BasicBlock BB) {
        int index = preBBList.indexOf(BB);
        this.operandList.set(index, value);
        // 维护phi的def-use 来进行后面对value的替换
        value.addUse(this);
    }

    @Override
    public Value getDef() {
        return this;
    }

    @Override
    public ArrayList<Value> getUse() {
        return new ArrayList<>();
    }
}
