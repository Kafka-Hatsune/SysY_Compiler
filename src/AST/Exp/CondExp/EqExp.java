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

// EqExp → RelExp | EqExp ('==' | '!=') RelExp
public class EqExp extends Node {
    public EqExp(Node node) {
        super(node);
    }
    @Override
    public boolean handleError(){
        return super.handleError();
    }

    /*
    * 返回i32 或 i1
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
            if(!operand2.getType().isInt32()){
                operand2 = new ZextInstr(operand2.getType(), operand2, LLVMBasicType.INT32, NameGen.getInstance().genLocalVarName(curFunction));
            }
            switch (node.getTerminal().getType()){
                // ==
                case EQL ->{
                    value = new IcmpInstr(NameGen.getInstance().genLocalVarName(curFunction), InstrIcmpCondType.EQ, value, operand2);
                }
                // !=
                case NEQ -> {
                    value = new IcmpInstr(NameGen.getInstance().genLocalVarName(curFunction), InstrIcmpCondType.NE, value, operand2);
                }
                default -> System.err.println("EqExp-genIR:未知的符号");
            }
        }
        return value;
    }
}
