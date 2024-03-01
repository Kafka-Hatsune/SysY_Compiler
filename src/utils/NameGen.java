package utils;

import llvm.ModuleBuilder;
import llvm.value.user.Function;

import java.util.HashMap;

public class NameGen {
    private final static NameGen nameGen = new NameGen();
    public final String GLOBAL_PREFIX = "@";
    public final String LOCAL_PREFIX = "%";
    public final String BLOCK_PREFIX = "block";

    // 有些Value并不需要名字?
    public final String Placeholder = "UnNamed";

    private int gVarCnt;
    private HashMap<Function, Integer> lValOfFuncCntMap;
    private HashMap<Function, Integer> bbOfFuncCntMap;

    public NameGen() {
        this.gVarCnt = 0;
        lValOfFuncCntMap = new HashMap<>();
        bbOfFuncCntMap = new HashMap<>();
    }

    public static NameGen getInstance() {
        return nameGen;
    }

    public void resetCntInFunction(Function function) {
        this.lValOfFuncCntMap.put(function, 0);
        this.bbOfFuncCntMap.put(function, 0);
    }

    public String genLocalVarName(Function function) {
        int curCnt = this.lValOfFuncCntMap.get(function);
        String name = LOCAL_PREFIX + function.getName().substring(1) + "_" + "var" + curCnt;
        this.lValOfFuncCntMap.replace(function, curCnt + 1);
        return name;
    }

    public String genGlobalVarName() {
        return GLOBAL_PREFIX + "global_var" + gVarCnt++;
    }

    // 在text中使用时, 不带%; 在br中使用时, 带%
    public String genBlockName(Function function) {
        int curCnt = this.bbOfFuncCntMap.get(function);
        String name = LOCAL_PREFIX + function.getName().substring(1) + "_" + BLOCK_PREFIX + curCnt;
        this.bbOfFuncCntMap.replace(function, curCnt + 1);
        return name;
    }

    public String genFuncName(String oriFuncName) {
        return GLOBAL_PREFIX + oriFuncName;
    }
}
