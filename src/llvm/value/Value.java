package llvm.value;

import llvm.type.LLVMType;
import llvm.value.user.User;
import mips.comment.MipsComment;

import java.util.ArrayList;
import java.util.Objects;

public class Value {
    protected LLVMType type;
    protected String name;
    protected ArrayList<Use> useList;  // 对应def-use, 保存着use关系

    public Value(LLVMType type, String name) {
        this.type = type;
        this.name = name;
        useList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addUse(User user){
        useList.add(new Use(user, this));
    }

    public ArrayList<Use> getUseList() {
        return useList;
    }

    public LLVMType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }


    public void toMips(){
        new MipsComment(this);
    }

    /**
     * 将this替换为newValue并维护原有this的Use关系为newValue
     * use关系维护
     * used的useList + user的operandList
     */
    public void replaceUse(Value newValue){
        ArrayList<User> users = new ArrayList<>();
        for(Use use:this.useList){
            users.add((User) use.getUser());
        }
        for(User user : users){
            user.replaceOperand(this, newValue);
            newValue.addUse(user);
        }
    }
}
