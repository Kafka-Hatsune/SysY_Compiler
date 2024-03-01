package optimize;

import llvm.value.BasicBlock;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.CallInstr;
import llvm.value.user.instr.GEPInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.LoadInstr;
import llvm.value.user.instr.aluInstr.AluInstr;
import llvm.value.user.instr.libInstr.GetintInstr;
import llvm.value.user.instr.optimizeInstr.LLVMMoveInstr;
import llvm.value.user.instr.optimizeInstr.PhiInstr;
import mips.Reg;
import optimize.Mem2Reg.CFGBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class RegAllocation {
    private Function function;
    private CFGBuilder cfgBuilder;

    private ActiveVarAnalysis activeVarAnalysis;

    private HashMap<Reg, Value> reg2Value = new HashMap<>();
    private HashMap<Value, Reg> value2Reg = new HashMap<>();

    private LinkedHashMap<Reg, Boolean> allocatedRegs = new LinkedHashMap<>();

    public RegAllocation(Function function, CFGBuilder cfgBuilder, ActiveVarAnalysis activeVarAnalysis) {
        this.function = function;
        this.cfgBuilder = cfgBuilder;
        this.activeVarAnalysis = activeVarAnalysis;
        for (Reg reg : Reg.values()) {
            if (reg.ordinal() >= Reg.T0.ordinal() && reg.ordinal() <= Reg.T9.ordinal()) {
                allocatedRegs.put(reg, false);
            }
        }
        BasicBlock entry = function.getBBList().get(0);
        regAlloc(entry);
    }

    public HashMap<Value, Reg> getValue2Reg() {
        return value2Reg;
    }

    public void regAlloc(BasicBlock BB) {
        HashMap<Value, Instr> likelyEndOfValue = new HashMap<>();
        HashSet<Value> defineInstr = new HashSet<>();
        HashSet<Value> activeEndValue = new HashSet<>();
        // 找到BB的每个value最后一次被使用的地方 它们可能是这个value的活跃区间终止处
        for (Instr instr : BB.getInstrList()) {
            ArrayList<Value> use = instr.getUse();
            for (Value useValue : use) {
                likelyEndOfValue.put(useValue, instr);
            }
        }
        // 分析该基本块内部的活跃区间
        for (Instr instr : BB.getInstrList()) {
            // 不给它们分配寄存器
            if(instr instanceof LLVMMoveInstr || instr instanceof PhiInstr){
                continue;
            }
            // 是定义语句(排除alloc数组语句)
            Value def = instr.getDef();
            if (def != null) {
                defineInstr.add(def);
                allocRegFor(def);
            }
            // 对于那些活跃区间终止点在该基本块内的value 可以在分配后直接释放它们的寄存器
            for (Value value : instr.getUse()) {
                // 不给它们分配寄存器
                if(value instanceof LLVMMoveInstr || value instanceof PhiInstr){
                    continue;
                }
                if (likelyEndOfValue.get(value) == instr &&
                        !activeVarAnalysis.getOutMap().get(BB).contains(value)) {
                    releaseRegFor(value);
                    activeEndValue.add(value);
                }
            }

        }
        // 分析该基本块的直接支配基本块的活跃变量
        for (BasicBlock child : cfgBuilder.getChildNodes().get(BB)) {
            // 将在该基本块中活跃,在该直接支配基本块中不活跃的变量的分配关系保留
            HashMap<Reg, Value> notActive = new HashMap<>();
            for (Map.Entry<Reg, Value> entry : reg2Value.entrySet()) {
                if (!activeVarAnalysis.getInMap().get(child).contains(entry.getValue())) {
                    notActive.put(entry.getKey(), entry.getValue());
                }
            }
            for (Value value : notActive.values()) {
                releaseRegFor(value);
            }
            regAlloc(child);
            // 恢复删除的寄存器分配映射
            for(Map.Entry<Reg, Value> entry: notActive.entrySet()){
                Reg reg = entry.getKey();
                Value value = entry.getValue();
                reg2Value.put(reg, value);
                allocatedRegs.put(reg, true);
            }
        }
        // 结束掉当前搜索基本块的活跃区间
        for (Value value : defineInstr) {
            if (value2Reg.containsKey(value)) {
                releaseRegFor(value);
            }

        }
        // dfs返回时保持原环境:将从前驱传过来的活跃变量恢复
        for (Value value : activeEndValue) {
            if (value2Reg.containsKey(value) && !defineInstr.contains(value)) {
                reg2Value.put(value2Reg.get(value), value);
                allocatedRegs.put(value2Reg.get(value), true);
            }
        }
    }

    public Reg allocRegFor(Value value) {
        for (Map.Entry<Reg, Boolean> entry : allocatedRegs.entrySet()) {
            if (!entry.getValue()) {
                entry.setValue(true);
                reg2Value.put(entry.getKey(), value);
                value2Reg.put(value, entry.getKey());
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 只释放reg2Value的值,不释放value2Reg的值
     * 活跃区间的结束代表这个value对应的寄存器可用
     */
    public void releaseRegFor(Value value) {
        if (!reg2Value.containsValue(value)) {
            return;
        }
        Reg reg = value2Reg.get(value);
        reg2Value.remove(reg);
        allocatedRegs.put(reg, false);
    }
}
