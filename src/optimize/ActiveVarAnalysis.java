package optimize;

import llvm.value.BasicBlock;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.BrInstr;
import llvm.value.user.instr.CallInstr;
import llvm.value.user.instr.GEPInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.LoadInstr;
import llvm.value.user.instr.RetInstr;
import llvm.value.user.instr.StoreInstr;
import llvm.value.user.instr.aluInstr.AluInstr;
import llvm.value.user.instr.libInstr.GetintInstr;
import llvm.value.user.instr.libInstr.PutchInstr;
import llvm.value.user.instr.libInstr.PutintInstr;
import llvm.value.user.instr.optimizeInstr.LLVMMoveInstr;
import optimize.Mem2Reg.CFGBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ActiveVarAnalysis {
    private Function function;
    private CFGBuilder cfgBuilder;

    private HashMap<BasicBlock, HashSet<Value>> inMap = new HashMap<>();
    private HashMap<BasicBlock, HashSet<Value>> outMap = new HashMap<>();
    private HashMap<BasicBlock, HashSet<Value>> useMap = new HashMap<>();
    private HashMap<BasicBlock, HashSet<Value>> defMap = new HashMap<>();

    public ActiveVarAnalysis(Function function, CFGBuilder cfgBuilder) {
        this.function = function;
        this.cfgBuilder = cfgBuilder;
        this.buildUseDef();
        this.buildInOut();
    }

    public HashMap<BasicBlock, HashSet<Value>> getInMap() {
        return inMap;
    }

    public HashMap<BasicBlock, HashSet<Value>> getOutMap() {
        return outMap;
    }

    public HashMap<BasicBlock, HashSet<Value>> getUseMap() {
        return useMap;
    }

    public HashMap<BasicBlock, HashSet<Value>> getDefMap() {
        return defMap;
    }

    public void buildUseDef() {
        for (BasicBlock BB : function.getBBList()) {
            HashSet<Value> def = new HashSet<>();
            HashSet<Value> use = new HashSet<>();
            inMap.put(BB, new HashSet<>());
            outMap.put(BB, new HashSet<>());
            defMap.put(BB, def);
            useMap.put(BB, use);
            for (Instr instr : BB.getInstrList()) {
                // AllocaInstr在ssa后不出现
                // Alu : <result> = add <ty> <op1>, <op2>
                // Call : %7 = call i32 @aaa(i32 %5, i32 %6)
                // load : %7 = load i32, i32* %6
                // 本身是def operands全部是use的指令
                Value defValue = instr.getDef();
                ArrayList<Value> useValues = instr.getUse();
                if(!use.contains(defValue)){
                    def.add(defValue);
                }
                for(Value useValue : useValues){
                    if(!def.contains(useValue)){
                        use.add(useValue);
                    }
                }
            }
        }
    }

    public void buildInOut() {
        boolean hasChange = true;
        while (hasChange) {
            hasChange = false;
            for (int i = function.getBBList().size() - 1; i >= 0; i--) {
                BasicBlock BB = function.getBBList().get(i);
                HashSet<Value> out = new HashSet<>();
                // out = 后继的in的并
                for (BasicBlock sucBB : cfgBuilder.getSucMap().get(BB)) {
                    out.addAll(inMap.get(sucBB));
                }
                outMap.put(BB, out);
                // in = (out - def) + use
                HashSet<Value> inOfBB = inMap.get(BB);
                HashSet<Value> ansInOfBB = new HashSet<>();
                ansInOfBB.addAll(out);
                ansInOfBB.removeAll(defMap.get(BB));
                ansInOfBB.addAll(useMap.get(BB));
                if (! ansInOfBB.equals(inOfBB)) {
                    inMap.put(BB, ansInOfBB);
                    hasChange = true;
                }
            }
        }
    }
}