package llvm.value.user;

import llvm.ModuleBuilder;
import llvm.type.LLVMBasicType;
import llvm.type.LLVMType;
import llvm.value.BasicBlock;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.instr.AllocaInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.RetInstr;
import llvm.value.user.instr.StoreInstr;
import mips.MipsModule;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.TextLabel;
import utils.NameGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Function extends User {

    private ArrayList<Value> paramList;
    private ArrayList<BasicBlock> BBList;

    private HashMap<Value, Reg> value2Reg;

    // type = retType
    public Function(LLVMType funcRetType, String name) {
        super(funcRetType, name);
        this.BBList = new ArrayList<>();
        this.paramList = new ArrayList<>();
        ModuleBuilder.getInstance().module.addFunction(this);
    }

    public HashMap<Value, Reg> getValue2Reg() {
        return value2Reg;
    }

    public void setValue2Reg(HashMap<Value, Reg> value2Reg) {
        this.value2Reg = value2Reg;
    }

    public ArrayList<BasicBlock> getBBList() {
        return BBList;
    }

    public void addBB(BasicBlock bb) {
        this.BBList.add(bb);
    }

    public void addParam(Value value) {
        this.paramList.add(value);
    }

    public ArrayList<Value> getParamList() {
        return paramList;
    }

    public boolean isIntFunc(){
        return this.type.equals(LLVMBasicType.INT32);
    }

    public boolean isFuncFParam(Value value){
        return this.paramList.contains(value);
    }

    public int getFuncFParamIndex(Value value){
        return this.paramList.indexOf(value);
    }
    // define dso_local i32 @main()
    // define dso_local void @set1(i32 %0, i32* %1)
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local " + this.type + " " + name);
        sb.append("(");
        for (int i = 0; i < this.paramList.size(); i++) {
            sb.append(this.paramList.get(i).toString());
            if(i != this.paramList.size() - 1){
                sb.append(", ");
            }
        }
        sb.append(")");
        sb.append("{\n");
        for (BasicBlock block : BBList){
            sb.append(block.toString());
        }
        sb.append("}");
        return sb.toString();
    }

    public void addRetIfAbsent() {
        BasicBlock curBB = ModuleBuilder.getInstance().getCurBB();
        if(!curBB.lastInstrIsRet()){
            if(this.type == LLVMBasicType.VOID){
                new RetInstr();
            } else if (this.type == LLVMBasicType.INT32) {
                new RetInstr(Constant.ConstantZero);
            }
        }
    }

    @Override
    public void toMips() {
        // TODO
        new TextLabel(this.name.substring(1));
        MipsModuleBuilder.getInstance().enterFunc(this);
        // TODO param?
        this.BBList.sort(new Comparator<BasicBlock>() {
            @Override
            public int compare(BasicBlock o1, BasicBlock o2) {
                Pattern pattern = Pattern.compile(".*block(\\d+)");
                Matcher matcher = pattern.matcher(o1.getName());
                matcher.find();
                int o1Num =Integer.parseInt(matcher.group(1));
                matcher = pattern.matcher(o2.getName());
                matcher.find();
                int o2Num =Integer.parseInt(matcher.group(1));
                return o1Num - o2Num;
            }
        });
        for(BasicBlock BB : this.BBList){
            BB.toMips();
        }
    }
}
