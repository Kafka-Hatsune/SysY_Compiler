package optimize.Mem2Reg;

import llvm.value.BasicBlock;
import llvm.value.Constant;
import llvm.value.Use;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.AllocaInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.LoadInstr;
import llvm.value.user.instr.optimizeInstr.PhiInstr;
import llvm.value.user.instr.StoreInstr;
import utils.NameGen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class Mem2Phi {

    Function function;
    CFGBuilder cfgBuilder;

    private Instr curAllocInstr;
    private ArrayList<Instr> useInstrList;  // load了alloc变量的instr
    private ArrayList<Instr> defInstrList;  // store了alloc变量的instr
    private ArrayList<BasicBlock> defBBList; //
    private ArrayList<BasicBlock> useBBList;
    private Stack<Value> stack;

    public Mem2Phi(Function function, CFGBuilder cfgBuilder) {
        this.function = function;
        this.cfgBuilder = cfgBuilder;
        // 遍历每个函数的所有alloc指令(变量声明) 将对应的赋值变为ssa形式
        ArrayList<Instr> allocInstrs = new ArrayList<>();
        for (BasicBlock BB : function.getBBList()) {
            for (Instr instr : BB.getInstrList()) {
                if (instr instanceof AllocaInstr && ((AllocaInstr) instr).getPtrType().isInt32()) {
                    allocInstrs.add(instr);
                }
            }
        }
        BasicBlock entryBB = this.function.getBBList().get(0);
        for (Instr instr : allocInstrs) {
            alloc2SSA(instr, entryBB);
        }
    }

    public void alloc2SSA(Instr instr, BasicBlock entryBB) {
        findDefAndUse(instr);
        insertPhi();
        rename(entryBB);
    }

    public void findDefAndUse(Instr instr) {
        this.curAllocInstr = instr;
        this.useBBList = new ArrayList<>();
        this.defBBList = new ArrayList<>();
        this.useInstrList = new ArrayList<>();
        this.defInstrList = new ArrayList<>();
        this.stack = new Stack<>();

        for (Use use : instr.getUseList()) {
            Instr user = (Instr) use.getUser();
            // user来自于被删除的BB 无效
            if(!function.getBBList().contains(user.getBB())){
                continue;
            }
            if (user instanceof StoreInstr) {
                defInstrList.add(user);
                if (!defBBList.contains(user.getBB())) {
                    defBBList.add(user.getBB());
                }
            } else if (user instanceof LoadInstr) {
                useInstrList.add(user);
                if (!useBBList.contains(user.getBB())) {
                    useBBList.add(user.getBB());
                }
            }
        }
    }

    public void insertPhi() {
        HashSet<BasicBlock> F = new HashSet<>(); // set of BB where phi of curAllocInstr is added
        Stack<BasicBlock> W = new Stack<>(); // set of BB contains def of V
        for (BasicBlock bb : defBBList) {
            W.push(bb);
        }
        while (!W.isEmpty()) {
            BasicBlock X = W.pop();
            for (BasicBlock Y : cfgBuilder.getDfMap().get(X)) {
                if (!F.contains(Y)) {
                    insertPhi2BB(Y);
                    F.add(Y);
                    if (!defBBList.contains(Y)) {
                        W.push(Y);
                    }
                }
            }
        }
    }

    public void insertPhi2BB(BasicBlock BB) {
        Function BBFunc = BB.getParentFunc();
        String name = NameGen.getInstance().genLocalVarName(BBFunc);
        Instr phi = new PhiInstr(name, cfgBuilder.getPreMap().get(BB), this.curAllocInstr);
        BB.getInstrList().add(0, phi);
        phi.setBB(BB);
        // phi是对原有value的def
//        defInstrList.add(phi);
    }

    /**
     * 从BB块开始 将alloc store load 变为ssa形式
     * 对于一遍
     * 删除alloc
     *
     * @param BB
     */
    public void rename(BasicBlock BB) {
        int toPop = 0;
        Iterator<Instr> iterator = BB.getInstrList().iterator();
        while (iterator.hasNext()) {
            Instr instr = iterator.next();
            if (instr == curAllocInstr) {
                iterator.remove();
            } else if (instr instanceof StoreInstr && defInstrList.contains(instr)) {
                Value defValue = ((StoreInstr) instr).getOperand1();
                stack.push(defValue);
                toPop++;
                iterator.remove();
            } else if (instr instanceof PhiInstr && ((PhiInstr)instr).allocInstr == curAllocInstr) {
                stack.push(instr);
                toPop++;
            } else if (instr instanceof LoadInstr && useInstrList.contains(instr)) {
                if(!stack.isEmpty()){
                    Value useValue = stack.peek();
                    instr.replaceUse(useValue);
                }else {
                    Value useValue = Constant.ConstantZero;
                    instr.replaceUse(useValue);
//                    System.err.println("111");
                }
                iterator.remove();
            }
        }
        // 按遍处理 当前每个后继块
        // 若存在当前alloc的phi 则必为第一条
        for (BasicBlock sucBB : cfgBuilder.getSucMap().get(BB)) {
            Instr instr = sucBB.getInstrList().get(0);
            if (instr instanceof PhiInstr && ((PhiInstr)instr).allocInstr == curAllocInstr) {
                if (!stack.isEmpty()) {
//                    System.err.println(curAllocInstr);
                    Value newValue = stack.peek();
                    ((PhiInstr) instr).addEntry(newValue, BB);
                }else {
                    Value newValue = Constant.ConstantZero;
                    ((PhiInstr) instr).addEntry(newValue, BB);
                }

            }
        }

        for (BasicBlock iDomBB : cfgBuilder.getChildNodes().get(BB)) {
            rename(iDomBB);
        }
        // 将该次dfs时压入stack的数据全部弹出
        for (int i = 0; i < toPop; i++) {
            stack.pop();
        }
    }
}
