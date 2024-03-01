package llvm.value.user;

import llvm.type.LLVMType;
import llvm.value.Value;

import java.util.ArrayList;

public class User extends Value {
    protected ArrayList<Value> operandList;   // 对应use-def,保存着这个User使用的Value列表

    public User(LLVMType type, String name) {
        super(type, name);
        this.operandList = new ArrayList<>();
    }

    public void addOperands(Value value) {
        // use-def
        operandList.add(value);
        // def-use
        value.addUse(this);
    }

    public ArrayList<Value> getOperandList() {
        return operandList;
    }

    public Value getOperand1() {
        return operandList.get(0);
    }

    public Value getOperand2() {
        return operandList.get(1);
    }

    public Value getOperand3() {
        return operandList.get(2);
    }

    public void replaceOperand(Value oldValue, Value newValue){
        int index;
        while((index = operandList.indexOf(oldValue))!=-1){
            operandList.set(index, newValue);
        }
    }
}
