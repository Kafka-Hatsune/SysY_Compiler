package AST.Exp.CondExp;

import AST.Node;
import llvm.ModuleBuilder;
import llvm.type.LLVMBasicType;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.aluInstr.IcmpInstr;
import llvm.value.user.instr.ZextInstr;
import type.InstrIcmpCondType;
import utils.NameGen;

// RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
public class RelExp extends Node {
    public RelExp(Node node) {
        super(node);
    }

    @Override
    public boolean handleError(){
        return super.handleError();
    }

    /*
    * 返回i32或i1
    * */
    @Override
    public Value genIR() {
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        Value value = this.getChildren().get(0).genIR();
        for (int i = 1; i < this.getChildren().size(); i++) {
            Node node = this.getChildren().get(i);
            if(!value.getType().isInt32()){
                value = new ZextInstr(value.getType(), value, LLVMBasicType.INT32, NameGen.getInstance().genLocalVarName(curFunction));
            }
            Value operand2 = this.getChildren().get(++i).genIR();
            switch (node.getTerminal().getType()){
                // <
                case LSS ->{
                    value = new IcmpInstr(NameGen.getInstance().genLocalVarName(curFunction), InstrIcmpCondType.SLT, value, operand2);
                }
                // <=
                case LEQ -> {
                    value = new IcmpInstr(NameGen.getInstance().genLocalVarName(curFunction), InstrIcmpCondType.SLE, value, operand2);
                }
                // >
                case GRE -> {
                    value = new IcmpInstr(NameGen.getInstance().genLocalVarName(curFunction), InstrIcmpCondType.SGT, value, operand2);
                }
                // >=
                case GEQ -> {
                    value = new IcmpInstr(NameGen.getInstance().genLocalVarName(curFunction), InstrIcmpCondType.SGE, value, operand2);
                }
                default -> System.err.println("RelExp-genIR:未知的符号");
            }
        }
        return value;
    }
}
