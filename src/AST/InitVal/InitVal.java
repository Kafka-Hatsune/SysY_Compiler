package AST.InitVal;

import AST.Exp.CalExp.ConstExp;
import AST.Exp.CalExp.Exp;
import AST.Node;
import llvm.type.LLVMArrayType;
import llvm.type.LLVMType;
import type.SymbolType;
import type.SyntaxType;

import java.util.ArrayList;

public class InitVal extends Node {
    protected SymbolType type;
    protected Integer init0;
    protected ArrayList<Integer> init1;
    protected ArrayList<ArrayList<Integer>> init2;

    public InitVal(Node node) {
        super(node);
    }

    public InitVal(SyntaxType syntaxType) {
        super(syntaxType);
    }

    public Integer getInit0() {
        return init0;
    }

    public ArrayList<Integer> getInit1() {
        return init1;
    }

    public ArrayList<ArrayList<Integer>> getInit2() {
        return init2;
    }

    public SymbolType getType() {
        return type;
    }

    @Override
    public boolean handleError() {
        return super.handleError();
    }

    public boolean initValue() {
        if (this.getChildren().get(0) instanceof Exp || this.getChildren().get(0) instanceof ConstExp) {
            init0 = this.getChildren().get(0) instanceof Exp ? ((Exp) this.getChildren().get(0)).calculate() :
                    ((ConstExp) this.getChildren().get(0)).calculate();
            if (init0 != null) {
                this.type = SymbolType.ARRAY_DIM_0;
                return true;
            } else {
                return false;
            }
        } else {
            ArrayList<Integer> tmp1 = new ArrayList<>();
            ArrayList<ArrayList<Integer>> tmp2 = new ArrayList<>();
            for (Node node : this.getChildren()) {
                if (node instanceof InitVal) {
                    if (!((InitVal) node).initValue()) {
                        return false;
                    }
                    // 子结点是0维 父节点是1维
                    if (((InitVal) node).type == SymbolType.ARRAY_DIM_0) {
                        tmp1.add(((InitVal) node).init0);
                        this.type = SymbolType.ARRAY_DIM_1;
                    } else if (((InitVal) node).type == SymbolType.ARRAY_DIM_1) {
                        tmp2.add(((InitVal) node).init1);
                        this.type = SymbolType.ARRAY_DIM_2;
                    }
                }
            }
            if (this.type == SymbolType.ARRAY_DIM_1) {
                init1 = tmp1;
            } else {
                init2 = tmp2;
            }
            return true;
        }
    }

    public int getValueByIndex(ArrayList<Integer> dims) {
        if (dims.size() == 1) {
            int index = dims.get(0);
            return init1.get(index);
        } else if (dims.size() == 2) {
            int index1 = dims.get(0);
            int index2 = dims.get(1);
            return init2.get(index1).get(index2);
        } else {
            System.err.println("InitVal:查询的index不正确");
            return 0;
        }
    }

    public int getValueByIndex() {
        return init0;
    }

    public String toLLVMStringDim0(int value) {
        return "i32 " + value;
    }

    public String toLLVMStringDim1(LLVMType type, ArrayList<Integer> list) {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        // 是否为zeroinitializer
        if (list.stream().allMatch(element -> element.equals(0))) {
            sb.append(" zeroinitializer");
            return sb.toString();
        }
        sb.append(" [");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toLLVMStringDim0(list.get(i)));
            if (i != list.size() - 1) {
                sb.append(", ");
            }
        }
        for (int i = list.size(); i < ((LLVMArrayType) type).getLeftNum(); i++) {
            if (i == list.size()) {
                sb.append(", ");
            }
            sb.append("i32 0");
            if (i != ((LLVMArrayType) type).getLeftNum() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String toLLVMStringDim2(LLVMType type, ArrayList<ArrayList<Integer>> list) {
        LLVMType dim1Type = ((LLVMArrayType) type).getRightType();
        int leftNum = ((LLVMArrayType) type).getLeftNum();
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        // 是否为zeroinitializer
        boolean flag = true;
        for (ArrayList<Integer> i : list) {
            if (!i.stream().allMatch(element -> element.equals(0))) {
                flag = false;
            }
        }
        if (flag) {
            sb.append(" zeroinitializer");
            return sb.toString();
        }
        // 正常进行初始化
        sb.append(" [");
        for (int i = 0; i < list.size(); i++) {
            sb.append(toLLVMStringDim1(dim1Type, list.get(i)));
            if (i != list.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String toLLVMString(LLVMType type) {
        if (this.type == SymbolType.ARRAY_DIM_0) {
            return toLLVMStringDim0(init0);
        } else if (this.type == SymbolType.ARRAY_DIM_1) {
            return toLLVMStringDim1(type, init1);
        } else if (this.type == SymbolType.ARRAY_DIM_2) {
            return toLLVMStringDim2(type, init2);
        } else {
            return type + " zeroinitializer";
        }
    }

}
