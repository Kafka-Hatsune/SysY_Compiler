package AST.Def;

import AST.Exp.CalExp.ConstExp;
import AST.InitVal.VarInitVal;
import AST.Node;
import llvm.ModuleBuilder;
import llvm.type.LLVMBasicType;
import llvm.type.LLVMType;
import llvm.value.Constant;
import llvm.value.GlobalVar;
import llvm.value.Value;
import llvm.value.user.Function;
import llvm.value.user.instr.AllocaInstr;
import llvm.value.user.instr.GEPInstr;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.StoreInstr;
import symbol.SymbolManager;
import symbol.VarSymbol;
import utils.NameGen;

import java.util.ArrayList;

//VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
public class VarDef extends Def {
    private VarInitVal initVal;

    public VarDef(Node parent) {
        super(parent);
        if (this.getChildren().get(this.getChildren().size() - 1) instanceof VarInitVal) {
            this.initVal = (VarInitVal) this.getChildren().get(this.getChildren().size() - 1);
        }
    }

    public VarSymbol registerSymbol() {
        String name = this.ident.getValue();
        for (ConstExp constExp : this.constExps) {
            dimList.add(constExp.calculate());
        }
        return SymbolManager.MANAGER.registerVarSymbol(name, dim, dimList);
    }

    @Override
    public boolean handleError() {
        boolean check;
        // 检查b错误
        check = checkRedefine();
        if (check) {
            registerSymbol();
        }
        check &= super.handleError();
        return check;
    }

    @Override
    public Value genIR() {
        Function curFunction = ModuleBuilder.getInstance().getCurFunction();
        VarSymbol symbol = registerSymbol();
        LLVMType llvmType = getLLVMType();
        // 全局变量的初始值一定可以编译时得出
        if (SymbolManager.MANAGER.isGlobal()) {
            String varName = NameGen.getInstance().genGlobalVarName();
            // 全局变量没有初始化值
            if (initVal == null) {
                VarInitVal zeroInit = new VarInitVal();
                GlobalVar globalVar = new GlobalVar(llvmType, varName, zeroInit);
                symbol.setLlvmValue(globalVar);
                return globalVar;
            }
            // 全局变量有初始化值
            else {
                initVal.initValue();
                symbol.setInitVal(initVal);
                GlobalVar globalVar = new GlobalVar(llvmType, varName, initVal);
                symbol.setLlvmValue(globalVar);
                return globalVar;
            }
        }
        // 局部变量的初始值需要生成LLVM Value
        else {
            String instr1Name = NameGen.getInstance().genLocalVarName(curFunction);
            Instr allocaInstr = new AllocaInstr(llvmType, instr1Name);
            symbol.setLlvmValue(allocaInstr);
            // 如果初始值不为空还需要进行Store的生成
            if (initVal != null) {
                ArrayList<Value> llvmValues = initVal.initLLVMValue();
                // 零维变量
                if (dim == 0) {
                    Value value = llvmValues.get(0);
                    Instr instr2 = new StoreInstr(value, allocaInstr);
                    return null;
                }
                // 一维数组
                else if (dim == 1) {
                    int offset = 0;
                    for (Value value : llvmValues) {
                        ArrayList<Value> offsets = new ArrayList<>();
                        offsets.add(Constant.ConstantZero);
                        offsets.add(new Constant(LLVMBasicType.INT32, offset));
                        String gepName = NameGen.getInstance().genLocalVarName(curFunction);
                        Instr gepInstr = new GEPInstr(gepName, llvmType, allocaInstr, offsets);
                        Instr storeInstr = new StoreInstr(value, gepInstr);
                        offset++;
                    }
                } else if (dim == 2) {
                    int cnt = 0;
                    for (int offset1 = 0; offset1 < this.dimList.get(0); offset1++) {
                        for (int offset2 = 0; offset2 < this.dimList.get(1); offset2++) {
                            if (cnt < llvmValues.size()) {
                                ArrayList<Value> offsets = new ArrayList<>();
                                offsets.add(Constant.ConstantZero);
                                offsets.add(new Constant(LLVMBasicType.INT32, offset1));
                                offsets.add(new Constant(LLVMBasicType.INT32, offset2));
                                String gepName = NameGen.getInstance().genLocalVarName(curFunction);
                                Instr gepInstr = new GEPInstr(gepName, llvmType, allocaInstr, offsets);
                                Instr storeInstr = new StoreInstr(llvmValues.get(cnt++), gepInstr);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
