//package mips;
//
//import llvm.value.Constant;
//import llvm.value.Value;
//import mips.instr.LiInstr;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class RegsManager {
//    // 通过value查询对应的reg
//    HashMap<Value, Reg> value2Reg = new HashMap<>();
//    ArrayList<Reg> availableRegs = new ArrayList<>();
//
//    public RegsManager() {
//        for (Reg value : Reg.values()) {
//            if (value.ordinal() >= Reg.T0.ordinal() && value.ordinal() <= Reg.T7.ordinal()) {
//                availableRegs.add(value);
//            } else if (value.ordinal() >= Reg.S0.ordinal() && value.ordinal() <= Reg.T9.ordinal()) {
//                availableRegs.add(value);
//            }
//        }
//    }
//
//    public void refreshValue2Reg() {
//        this.value2Reg = new HashMap<>();
//        for (Reg reg : availableRegs) {
//            if (reg.getValue() != null) {
//                this.value2Reg.put(reg.getValue(), reg);
//            }
//        }
//    }
//
//    public Reg getRegOf(Value value) {
//        return value2Reg.get(value);
//    }
//
//    public void loadValue2Reg(Value value, Reg reg) {
//        if (value instanceof Constant) {
//            new LiInstr(Reg.V0, ((Constant) value).getValue());
//        }else if()
//    }
//
//    public Reg AllocReg() {
//        for (Reg reg : availableRegs) {
//            if (reg.isAvailable()) {
//                return reg;
//            }
//        }
//        // 说明都没写到栈上 随便挑一个(第一个/随机)写到栈上
//        Reg reg = availableRegs.get(0);
//        MipsModuleBuilder.getInstance().pushValue2Stack(reg);
//        return reg;
//    }
//}
