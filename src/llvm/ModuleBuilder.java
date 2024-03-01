package llvm;

import llvm.value.BasicBlock;
import llvm.value.Module;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.Instr;
import utils.NameGen;

public class ModuleBuilder {
    private final static ModuleBuilder moduleBuilder = new ModuleBuilder();

    // 某些需要创建BB的AST结点需要获取当前func
    private Function curFunction;
    // 当前的BasicBlock 一个Function可以有多个BB Instr插入对应的BB
    private BasicBlock curBB;

    private BasicBlock afterBB; // break的desBB

    private BasicBlock forStmt2BB; // continue的desBB

    public static ModuleBuilder getInstance(){
        return moduleBuilder;
    }

    public Module module;
    private ModuleBuilder() {
        module = new Module();
    }
    public Function getCurFunction() {
        return curFunction;
    }
    public void initFunction(Function curFunction){
        // 开始一个新的命名空间
        NameGen.getInstance().resetCntInFunction(curFunction);
        // set curFunction
        this.curFunction = curFunction;
        // new BB
        BasicBlock BB = new BasicBlock(NameGen.getInstance().genBlockName(curFunction), curFunction);
        // set BB to Function
        this.curBB = BB;
    }

//    public void initFuncParams(){
//        this.curFunction.initParams();
//    }

    public BasicBlock getCurBB() {
        return curBB;
    }

    public void setCurBB(BasicBlock curBB) {
        this.curBB = curBB;
    }

    public BasicBlock getAfterBB() {
        return afterBB;
    }

    public void setAfterBB(BasicBlock afterBB) {
        this.afterBB = afterBB;
    }

    public BasicBlock getForStmt2BB() {
        return forStmt2BB;
    }

    public void setForStmt2BB(BasicBlock forStmt2BB) {
        this.forStmt2BB = forStmt2BB;
    }

    public void addInstr2CurBB(Instr instr){
        curBB.addInstr(instr);
        instr.setBB(curBB);
    }

    public void addParam2CurFunc(Value value) {
        curFunction.addParam(value);
    }
}
