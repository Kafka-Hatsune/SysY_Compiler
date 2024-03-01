package AST.Exp.CalExp;

import AST.Node;
import llvm.ModuleBuilder;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.aluInstr.AddInstr;
import llvm.value.user.instr.aluInstr.SubInstr;
import utils.Calculable;
import utils.NameGen;

// AddExp → MulExp | AddExp ('+' | '−') MulExp
public class AddExp extends Node implements Calculable {
    private Integer value;

    public AddExp(Node node) {
        super(node);
    }


    public Integer calNode(Node node){
        if(node instanceof AddExp){
            return ((AddExp)node).calculate();
        } else if (node instanceof MulExp) {
            return ((MulExp)node).calculate();
        } else {
            System.err.println("未实现Calculable的Node");
            return null;
        }
    }

    public Integer calculate(){
        value = calNode(this.getChildren().get(0));
        if (value == null){
            return null;
        }
        for (int i = 1; i < this.getChildren().size(); i++) {
            Node node = this.getChildren().get(i);
            switch (node.getTerminal().getType()){
                case PLUS ->{
                    Integer operand2 = calNode(this.getChildren().get(++i));
                    if(operand2 == null){
                        return null;
                    }else {
                        value += operand2;
                    }
                }
                case MINU -> {
                    Integer operand2 = calNode(this.getChildren().get(++i));
                    if(operand2 == null){
                        return null;
                    }else {
                        value -= operand2;
                    }
                }
                default -> System.err.println("AddExp-calculate:未知的符号");
            }
        }
        return value;
    }

    // 假定 所有MulExp的dim相同 不做检查
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
                case PLUS ->{
                    String name = NameGen.getInstance().genLocalVarName(curFunction);
                    Value operand2 = this.getChildren().get(++i).genIR();
                    ans = new AddInstr(name, ans, operand2);
                }
                case MINU -> {
                    String name = NameGen.getInstance().genLocalVarName(curFunction);
                    Value operand2 = this.getChildren().get(++i).genIR();
                    ans = new SubInstr(name, ans, operand2);
                }
                default -> System.err.println("AddExp-genIR:未知的符号");
            }
        }
        return ans;
    }
}
