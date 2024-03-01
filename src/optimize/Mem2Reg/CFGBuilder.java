package optimize.Mem2Reg;

import llvm.value.BasicBlock;
import llvm.value.user.Function;
import llvm.value.user.instr.BrInstr;
import llvm.value.user.instr.Instr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CFGBuilder {
    Function function;
    // 前继和后继
    private HashMap<BasicBlock, ArrayList<BasicBlock>> preMap = new HashMap<>();
    private HashMap<BasicBlock, ArrayList<BasicBlock>> sucMap = new HashMap<>();
    // 支配
    private HashMap<BasicBlock, ArrayList<BasicBlock>> domMap = new HashMap<>();
    private HashMap<BasicBlock, ArrayList<BasicBlock>> iDomMap = new HashMap<>();
    // dom tree
    private HashMap<BasicBlock, BasicBlock> parentNode = new HashMap<>();
    private HashMap<BasicBlock, ArrayList<BasicBlock>> childNodes = new HashMap<>();
    // Dominance Frontier
    private HashMap<BasicBlock, ArrayList<BasicBlock>> dfMap = new HashMap<>();

    public CFGBuilder(Function function) {
        this.function = function;
        initMap();
        initPreAndSuc();
        initDom();
        initIDom();
        initDF();
    }

    public void initMap() {
        for (BasicBlock BB : function.getBBList()) {
            preMap.put(BB, new ArrayList<>());
            sucMap.put(BB, new ArrayList<>());
            domMap.put(BB, new ArrayList<>());
            iDomMap.put(BB, new ArrayList<>());
            childNodes.put(BB, new ArrayList<>());
            dfMap.put(BB, new ArrayList<>());
        }
    }

    public void initPreAndSuc() {
        for (BasicBlock BB : function.getBBList()) {
            for (Instr instr : BB.getInstrList()) {
                if (instr instanceof BrInstr) {
                    if (instr.getOperandList().size() == 3) {
                        BasicBlock trueBB = (BasicBlock) instr.getOperand2();
                        BasicBlock falseBB = (BasicBlock) instr.getOperand3();
                        sucMap.get(BB).add(trueBB);
                        sucMap.get(BB).add(falseBB);
                        preMap.get(trueBB).add(BB);
                        preMap.get(falseBB).add(BB);
                    } else {
                        BasicBlock desBB = (BasicBlock) instr.getOperand1();
                        sucMap.get(BB).add(desBB);
                        preMap.get(desBB).add(BB);
                    }
                }
            }
        }
        // 将信息写到BB和function中？
    }

    public void initDom() {
        BasicBlock entry = function.getBBList().get(0);
        for (BasicBlock BB : function.getBBList()) {
            HashSet<BasicBlock> entry2BB = new HashSet<>();
            DFSEntry2BB(entry, BB, entry2BB);
            for (BasicBlock BB1 : function.getBBList()) {
                if (!entry2BB.contains(BB1)) {
                    domMap.get(BB).add(BB1);
                }
            }
        }
    }

    public void DFSEntry2BB(BasicBlock entry, BasicBlock BB, HashSet<BasicBlock> entry2BB) {
        if (entry == BB) {
            return;
        }
        entry2BB.add(entry);
        for (BasicBlock sucBB : sucMap.get(entry)) {
            if (!entry2BB.contains(sucBB)) {
                DFSEntry2BB(sucBB, BB, entry2BB);
            }
        }
    }

    // 判断BB1是否严格支配BB2
    public boolean isSDom(BasicBlock BB1, BasicBlock BB2) {
        return BB1 != BB2 && domMap.get(BB1).contains(BB2);
    }

    // 判断BB1是否直接支配BB2
    public boolean isIDom(BasicBlock BB1, BasicBlock BB2) {
        if (!isSDom(BB1, BB2)) {
            return false;
        }
        for (BasicBlock BB1Children : this.domMap.get(BB1)) {
            if (BB1Children == BB1) {
                continue;
            } else if (isSDom(BB1Children, BB2)) {
                return false;
            }
        }
        return true;
    }

    public void initIDom() {
        for (BasicBlock BB : function.getBBList()) {
            for (BasicBlock domOfBB : this.domMap.get(BB)) {
                if (isIDom(BB, domOfBB)) {
                    // 直接支配Map
                    this.iDomMap.get(BB).add(domOfBB);
                    // 构造支配树
                    this.parentNode.put(domOfBB, BB);
                    this.childNodes.get(BB).add(domOfBB);
                }
            }
        }
    }

    public void initDF() {
        ArrayList<BasicBlock> BBList = function.getBBList();
        // for(a,b) in CFG edges
        for (Map.Entry<BasicBlock, ArrayList<BasicBlock>> entry : sucMap.entrySet()) {
            BasicBlock a = entry.getKey();
            for (BasicBlock b : entry.getValue()) {
                BasicBlock x = a;
                while (!isSDom(x, b)) {
                    dfMap.get(x).add(b);
                    x = parentNode.get(x);
                }
            }
        }
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getDfMap() {
        return dfMap;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getPreMap() {
        return preMap;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getSucMap() {
        return sucMap;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getDomMap() {
        return domMap;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getiDomMap() {
        return iDomMap;
    }

    public HashMap<BasicBlock, BasicBlock> getParentNode() {
        return parentNode;
    }

    public HashMap<BasicBlock, ArrayList<BasicBlock>> getChildNodes() {
        return childNodes;
    }
}
