package optimize;

import IO.Output;
import llvm.value.BasicBlock;
import llvm.value.Module;
import llvm.value.Value;
import llvm.value.user.Function;
import mips.Reg;
import optimize.Mem2Reg.CFGBuilder;
import optimize.Mem2Reg.Mem2Phi;
import optimize.Mem2Reg.phi2Move;

import java.io.IOException;
import java.util.HashMap;

public class Optimizer {
    Module llvmModule;
    public Optimizer(Module module) {
        this.llvmModule = module;
    }

    public void optimize() {
        // 死代码删除
        new DeleteDead(llvmModule);
        HashMap<Function, CFGBuilder> functionCFGMap = new HashMap<>();
        // 以function为单位进行ssa转换
        for (Function function: llvmModule.getFunctionList()) {
            CFGBuilder cfgBuilder = new CFGBuilder(function);
            functionCFGMap.put(function, cfgBuilder);
            new Mem2Phi(function, cfgBuilder);
        }

        // phi2move
        for (Function function: llvmModule.getFunctionList()) {
            CFGBuilder cfgBuilder = functionCFGMap.get(function);
            new phi2Move(function, cfgBuilder);
        }
        HashMap<Function, ActiveVarAnalysis> functionDefUseMap = new HashMap<>();
        // 活跃变量分析
        for (Function function: llvmModule.getFunctionList()) {
            CFGBuilder cfgBuilder = functionCFGMap.get(function);
            ActiveVarAnalysis activeVarAnalysis =  new ActiveVarAnalysis(function, cfgBuilder);
            functionDefUseMap.put(function, activeVarAnalysis);
        }

        // reg分配
        for (Function function: llvmModule.getFunctionList()) {
            CFGBuilder cfgBuilder = functionCFGMap.get(function);
            ActiveVarAnalysis activeVarAnalysis = functionDefUseMap.get(function);
            RegAllocation regAllocation = new RegAllocation(function, cfgBuilder, activeVarAnalysis);
            HashMap<Value, Reg> value2Reg = regAllocation.getValue2Reg();
            function.setValue2Reg(value2Reg);
            System.out.println(function.getName());
            System.out.println(value2Reg);
        }

//        // 乘除法优化
//        for (Function function: llvmModule.getFunctionList()) {
//            for(BasicBlock BB:function.getBBList()){
//                new arithmeticOptimize(BB);
//            }
//        }
    }

}
