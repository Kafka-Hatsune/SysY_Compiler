package AST.Def;

import AST.Exp.CalExp.ConstExp;
import AST.InitVal.ConstInitVal;
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
import symbol.ConstSymbol;
import symbol.SymbolManager;
import utils.NameGen;

import java.util.ArrayList;

// ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
// constDef一定有constInitVal 且 constInitVal一定可以编译时(存在符号表)得出
// b k
public class ConstDef extends Def {
    private ConstInitVal constInitVal;

    public ConstDef(Node parent) {
        super(parent);
        this.constInitVal = (ConstInitVal) this.getChildren().get(this.getChildren().size() - 1);
    }

    public ConstSymbol registerSymbol() {
        String name = this.ident.getValue();
        for (ConstExp constExp : this.constExps) {
            dimList.add(constExp.calculate());
        }
        constInitVal.initValue();
        return SymbolManager.MANAGER.registerConstSymbol(name, dim, dimList, constInitVal);
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
        ConstSymbol symbol = registerSymbol();
        LLVMType llvmType = getLLVMType();
        if (SymbolManager.MANAGER.isGlobal()) {
            String varName = NameGen.getInstance().genGlobalVarName();
            GlobalVar globalVar = new GlobalVar(llvmType, varName, constInitVal);
            symbol.setLlvmValue(globalVar);
            return globalVar;
        } else {
            String instr1Name = NameGen.getInstance().genLocalVarName(curFunction);
            Instr allocaInstr = new AllocaInstr(llvmType, instr1Name);
            symbol.setLlvmValue(allocaInstr);
            // 零维变量
            if (dim == 0) {
                Value value = new Constant(LLVMBasicType.INT32, constInitVal.getValueByIndex());
                Instr storeInstr = new StoreInstr(value, allocaInstr);
                return null;
            }
            // 一维数组
            else if (dim == 1) {
                for (int offset = 0; offset < constInitVal.getInit1().size(); offset++) {
                    Value llvmOffset = new Constant(LLVMBasicType.INT32, offset);
                    String gepName = NameGen.getInstance().genLocalVarName(curFunction);
                    ArrayList<Value> offsets = new ArrayList<>();
                    offsets.add(Constant.ConstantZero);
                    offsets.add(llvmOffset);
                    Instr gepInstr = new GEPInstr(gepName, llvmType, allocaInstr, offsets);
                    Value value = new Constant(LLVMBasicType.INT32, constInitVal.getInit1().get(offset));
                    Instr storeInstr = new StoreInstr(value, gepInstr);
                }
            } else if (dim == 2) {
                int cnt = 0;
                for (int offset1 = 0; offset1 < this.dimList.get(0); offset1++) {
                    ArrayList<Integer> dim1 = constInitVal.getInit2().get(offset1);
                    for (int offset2 = 0; offset2 < this.dimList.get(1); offset2++) {
                        ArrayList<Value> offsets = new ArrayList<>();
                        offsets.add(Constant.ConstantZero);
                        offsets.add(new Constant(LLVMBasicType.INT32, offset1));
                        offsets.add(new Constant(LLVMBasicType.INT32, offset2));
                        String gepName = NameGen.getInstance().genLocalVarName(curFunction);
                        Instr gepInstr = new GEPInstr(gepName, llvmType, allocaInstr, offsets);
                        Value value = new Constant(LLVMBasicType.INT32, dim1.get(offset2));
                        Instr storeInstr = new StoreInstr(value, gepInstr);
                    }
                }
            }
        }
        return null;
    }
}
