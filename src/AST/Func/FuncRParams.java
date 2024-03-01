package AST.Func;

import AST.Exp.CalExp.Exp;
import AST.Node;

import java.util.ArrayList;

// FuncRParams → Exp { ',' Exp }
public class FuncRParams extends Node {
    private ArrayList<Exp> exps;
    public FuncRParams(Node node) {
        super(node);
        exps = new ArrayList<>();
        for(Node child:this.getChildren()){
            if(child instanceof Exp){
                exps.add((Exp) child);
            }
        }
    }
    public int getExpsCount(){
        return exps.size();
    }

    // 形式: Exp1的Dims Exp2的Dims ...
    public ArrayList<Integer> getRParamsDims(){
        ArrayList<Integer> ans = new ArrayList<>();
        for (Node node : this.getChildren()){
            if(node instanceof Exp){
                ans.add(((Exp)node).getDim());
            }
        }
        return ans;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    @Override
    public boolean handleError(){
        return super.handleError();
    }
}
