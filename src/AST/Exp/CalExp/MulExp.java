package AST.Exp.CalExp;

import AST.Node;
import llvm.ModuleBuilder;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.aluInstr.MulInstr;
import llvm.value.user.instr.aluInstr.SdivInstr;
import llvm.value.user.instr.aluInstr.SremInstr;
import utils.Calculable;
import utils.NameGen;

// MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
public class MulExp extends Node implements Calculable {
    private Integer value;

    public MulExp(Node node) {
        super(node);
    }

    public Integer calNode(Node node){
        if(node instanceof MulExp){
            return ((MulExp)node).calculate();
        } else if (node instanceof UnaryExp) {
            return ((UnaryExp)node).calculate();
        } else {
            System.err.println("未实现Calculable的Node");
            return null;
        }
    }
    @Override
    public Integer calculate() {
        value = calNode(this.getChildren().get(0));
        if (value == null){
            return null;
        }
        for (int i = 1; i < this.getChildren().size(); i++) {
            Node node = this.getChildren().get(i);
            switch (node.getTerminal().getType()) {
                case MULT -> {
                    Integer operand2 = calNode(this.getChildren().get(++i));
                    if(operand2 == null){
                        return null;
                    }else {
                        value *= operand2;
                    }
                }
                case DIV -> {
                    Integer operand2 = calNode(this.getChildren().get(++i));
                    if(operand2 == null){
                        return null;
                    }else {
                        value /= operand2;
                    }
                }
                case MOD -> {
                    Integer operand2 = calNode(this.getChildren().get(++i));
                    if(operand2 == null){
                        return null;
                    }else {
                        value %= operand2;
                    }
                }
                default -> System.err.println("MulExp-calculate:未知的符号");
            }
        }
        return value;
    }

    public Integer getDim() {
        return this.getChildren().get(0).getDim();
    }

    @Override
    public boolean handleError(){
        return super.handleError();
    }

    @Override
    public Value genIR(){
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        Value ans = this.getChildren().get(0).genIR();
        for (int i = 1; i < this.getChildren().size(); i++) {
            Node node = this.getChildren().get(i);
            switch (node.getTerminal().getType()){
                case MULT ->{
                    String name = NameGen.getInstance().genLocalVarName(curFunction);
                    Value operand2 = this.getChildren().get(++i).genIR();
                    ans = new MulInstr(name, ans, operand2);
                }
                case DIV -> {
                    String name = NameGen.getInstance().genLocalVarName(curFunction);
                    Value operand2 = this.getChildren().get(++i).genIR();
                    ans = new SdivInstr(name, ans, operand2);
                }
                case MOD -> {
                    String name = NameGen.getInstance().genLocalVarName(curFunction);
                    Value operand2 = this.getChildren().get(++i).genIR();
                    ans = new SremInstr(name, ans, operand2);
                }
                default -> System.err.println("MulExp-genIR:未知的符号");
            }
        }
        return ans;
    }
}
