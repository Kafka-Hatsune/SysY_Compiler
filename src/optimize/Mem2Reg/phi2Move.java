package optimize.Mem2Reg;

import llvm.type.LLVMBasicType;
import llvm.value.BasicBlock;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.BrInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.optimizeInstr.LLVMMoveInstr;
import llvm.value.user.instr.optimizeInstr.PCInstr;
import llvm.value.user.instr.optimizeInstr.PhiInstr;
import utils.NameGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class phi2Move {
    Function function;
    CFGBuilder cfgBuilder;

    ArrayList<BasicBlock> phiBBList = new ArrayList<>();

    HashMap<BasicBlock, ArrayList<PCInstr>> pcBBMap = new HashMap<>();

    public phi2Move(Function function, CFGBuilder cfgBuilder) {
        this.function = function;
        this.cfgBuilder = cfgBuilder;
        for (BasicBlock BB : function.getBBList()) {
            if (BB.getInstrList().get(0) instanceof PhiInstr) {
                phiBBList.add(BB);
            }
        }
        phi2PC();
        PC2Move();
    }

    public void phi2PC() {
        for (BasicBlock BB : this.phiBBList) {
            ArrayList<BasicBlock> preBBList = cfgBuilder.getPreMap().get(BB);
            ArrayList<PCInstr> pcList = new ArrayList<>();
            for (BasicBlock preBB : preBBList) {
                PCInstr pc = new PCInstr(NameGen.getInstance().genLocalVarName(function));
                pcList.add(pc);
                // 只记录要加入的pc指令和其基本块 而不是真正加入 类似于虚指令
                if (cfgBuilder.getSucMap().get(preBB).size() == 1) {
                    insertPC2PreBB(pc, preBB);
                } else {
                    insertPC2MidBB(pc, preBB, BB);
                }
            }
            // 将phi的entry放入pc pcList的pc顺序与preBB一致与phi的operand的value顺序一致
            Iterator<Instr> iterator = BB.getInstrList().iterator();
            while (iterator.hasNext()) {
                Instr instr = iterator.next();
                if (instr instanceof PhiInstr) {
                    PhiInstr phiInstr = (PhiInstr) instr;
                    for (int i = 0; i < phiInstr.getOperandList().size(); i++) {
                        pcList.get(i).addCopy(phiInstr, phiInstr.getOperandList().get(i));
                    }
                    // 删除phi
                    iterator.remove();
                }

            }
        }

    }

    /**
     * 将每一个BB中的pc指令转换为move
     */
    public void PC2Move() {
        for (BasicBlock BB : pcBBMap.keySet()) {
            ArrayList<PCInstr> pcInstrs = pcBBMap.get(BB);
            for (PCInstr pc : pcInstrs) {
                ArrayList<LLVMMoveInstr> moveInstrs = singlePC2Move(pc);
                for (LLVMMoveInstr move : moveInstrs) {
                    BB.getInstrList().add(BB.getInstrList().size() - 1, move);
                }
            }
        }
    }

    public ArrayList<LLVMMoveInstr> singlePC2Move(PCInstr pc) {
        ArrayList<LLVMMoveInstr> ansList = new ArrayList<>();
        ArrayList<Value> dstList = pc.getDstList();
        ArrayList<Value> srcList = pc.getSrcList();
        for (int i = 0; i < dstList.size(); i++) {
            LLVMMoveInstr move = new LLVMMoveInstr(dstList.get(i), srcList.get(i));
            ansList.add(move);
        }
        // 并行->串行
        serializeMove(ansList);
        return ansList;
    }

    public void serializeMove(ArrayList<LLVMMoveInstr> moveList){
        // 等待加入序列开头的move指令
        ArrayList<LLVMMoveInstr> toAdd = new ArrayList<>();
        // 已经完成检查的src
        HashSet<Value> checkedSrc = new HashSet<>();
        for (int i = moveList.size() - 1; i >= 0; i--) {
            Value src = moveList.get(i).getOperand2();
            if(!(src instanceof Constant) && !checkedSrc.contains(src)){
                for (int j = 0; j < i; j++) {
                    Value dst = moveList.get(j).getOperand1();
                    if(dst == src){
                        Value midValue = new Value(LLVMBasicType.INT32, src.getName() + "_serial_tmp");
                        for(LLVMMoveInstr moveInstr: moveList){
                            if(moveInstr.getOperand2() == src){
                                moveInstr.getOperandList().set(1, midValue);
                            }
                        }
                        LLVMMoveInstr serialMove = new LLVMMoveInstr(midValue, src);
                        toAdd.add(serialMove);
                        checkedSrc.add(src);
                    }
                }
            }
        }
        // 将toAdd中的临时变量move加入序列开头
        for(LLVMMoveInstr moveInstr: toAdd){
            moveList.add(0, moveInstr);
        }
    }
    public void insertPC2PreBB(PCInstr pc, BasicBlock preBB) {
//        int index = preBB.getInstrList().size() - 1;
        // 经过死代码删除后,preBB的最后一条必然为br,之前的语句必然不是跳转语句
//        preBB.getInstrList().add(index, pc);
//        pc.setBB(preBB);
//        pc.setIndexOfBBInstrList(index);
        // 更新map
        if (this.pcBBMap.get(preBB) == null) {
            ArrayList<PCInstr> pcInstrs = new ArrayList<>();
            pcInstrs.add(pc);
            this.pcBBMap.put(preBB, pcInstrs);
        } else {
            this.pcBBMap.get(preBB).add(pc);
        }
    }

    public void insertPC2MidBB(PCInstr pc, BasicBlock preBB, BasicBlock BB) {
        // new midBB
        BasicBlock midBB = new BasicBlock(NameGen.getInstance().genBlockName(function));
        midBB.setParentFunc(function);
        function.getBBList().add(function.getBBList().indexOf(BB), midBB);
        // insert pc2midBB
//        midBB.addInstr(pc);
//        pc.setBB(midBB);
//        pc.setIndexOfBBInstrList(midBB.getInstrList().indexOf(pc));
        // modify pre and suc : from phi2PC-cfgBuilder.getSucMap().get(preBB).size() != 1
        BrInstr brInstr = (BrInstr) preBB.getInstrList().get(preBB.getInstrList().size() - 1);
        BasicBlock trueBB = (BasicBlock) brInstr.getOperand2();
        BasicBlock falseBB = (BasicBlock) brInstr.getOperand3();
        // modify pre2mid
        if (trueBB == BB) {
            brInstr.getOperandList().set(1, midBB);
        } else {
            brInstr.getOperandList().set(2, midBB);
        }
        // modify mid2BB
        BrInstr midBr2BB = new BrInstr();
        midBr2BB.setBB(midBB);
        midBr2BB.getOperandList().add(BB);
        midBB.addInstr(midBr2BB);
        // modify cfg
        cfgBuilder.getSucMap().get(preBB).set(cfgBuilder.getSucMap().get(preBB).indexOf(BB), midBB);
        cfgBuilder.getPreMap().get(BB).set(cfgBuilder.getPreMap().get(BB).indexOf(preBB), midBB);
        cfgBuilder.getPreMap().put(midBB, new ArrayList<>());
        cfgBuilder.getPreMap().get(midBB).add(preBB);
        cfgBuilder.getSucMap().put(midBB, new ArrayList<>());
        cfgBuilder.getSucMap().get(midBB).add(BB);
        // 更新map
        if (this.pcBBMap.get(midBB) == null) {
            ArrayList<PCInstr> pcInstrs = new ArrayList<>();
            pcInstrs.add(pc);
            this.pcBBMap.put(midBB, pcInstrs);
        } else {
            this.pcBBMap.get(midBB).add(pc);
        }
    }

}
