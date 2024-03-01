package optimize;

import llvm.value.BasicBlock;
import llvm.value.Module;
import llvm.value.user.Function;
import llvm.value.user.instr.BrInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.RetInstr;

import java.util.HashSet;
import java.util.Iterator;

public class DeleteDead {
    private Module llvmModule;

    public DeleteDead(Module module) {
        this.llvmModule = module;
        for (Function function : llvmModule.getFunctionList()) {
            for (BasicBlock BB : function.getBBList()) {
                deleteDeadCode(BB);
            }
        }
        for (Function function : llvmModule.getFunctionList()) {
            deleteDeadBB(function);
        }
    }

    // 删除ret和br之后的死代码
    public void deleteDeadCode(BasicBlock BB) {
        boolean isDead = false;
        Iterator<Instr> iterator = BB.getInstrList().iterator();
        while (iterator.hasNext()) {
            Instr instr = iterator.next();
            if (isDead) {
                iterator.remove();
            } else if (instr instanceof BrInstr || instr instanceof RetInstr) {
                isDead = true;
            }
        }
    }

    // 删除死代码块: function的entry块开始无法到达的BB
    // 通过dfs求entry块所在的极大联通分量
    public void deleteDeadBB(Function function) {
        BasicBlock entry = function.getBBList().get(0);
        HashSet<BasicBlock> visit = new HashSet<>();
        dfs(entry, visit);
        function.getBBList().removeIf(BB -> !visit.contains(BB));
    }

    public void dfs(BasicBlock basicBlock, HashSet<BasicBlock> visit) {
        visit.add(basicBlock);
        Instr instr = basicBlock.getInstrList().get(basicBlock.getInstrList().size() - 1);
        if (instr instanceof BrInstr) {
            if(instr.getOperandList().size() == 3 ){
                BasicBlock trueBB = (BasicBlock) instr.getOperand2();
                BasicBlock falseBB = (BasicBlock) instr.getOperand3();
                if (!visit.contains(trueBB)) {
                    dfs(trueBB, visit);
                }
                if (!visit.contains(falseBB)) {
                    dfs(falseBB, visit);
                }
            }else {
                BasicBlock desBB = (BasicBlock) instr.getOperand1();
                if (!visit.contains(desBB)) {
                    dfs(desBB, visit);
                }
            }

        }
    }

}
