package llvm.value.user.instr;

import llvm.type.LLVMBasicType;
import llvm.type.LLVMType;
import llvm.value.Value;
import llvm.value.user.Function;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.JumpInstr;
import mips.instr.MemInstr;
import mips.instr.MoveInstr;
import mips.instr.RICalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;

// <result> = call [ret attrs] <ty> <fnptrval>(<function args>)


/*
define dso_local i32 @aaa(i32 %0, i32 %1){
  %3 = alloca i32
  %4 = alloca i32
  store i32 %0, i32* %3
  store i32 %1, i32* %4
  %5 = load i32, i32* %3
  %6 = load i32, i32* %4
  %7 = add nsw i32 %5, %6
  ret i32 %7
}
...
%7 = call i32 @aaa(i32 %5, i32 %6)
name = 'call' retType @
*/
public class CallInstr extends Instr {
    private String callFuncName;

    private LinkedHashMap<Object, Integer> funcStack;

    public CallInstr(LLVMType funcRetType, String name, Function function, ArrayList<Value> params) {
        // Call指令的LLVM类型由函数的返回类型决定
        super(funcRetType, name, LLVMInstrType.CALL);
        this.addOperands(function);
        for (Value param : params) {
            this.addOperands(param);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.getName(), ((Value) obj).getName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < this.operandList.size(); i++) {
            sb.append(this.operandList.get(i).getType());
            sb.append(" ");
            sb.append(this.operandList.get(i).getName());
            if (i < this.operandList.size() - 1) {
                sb.append(",");
            }
        }
        if (this.type == LLVMBasicType.VOID) {
            return "call " + type + " " + this.getOperand1().getName() +
                    "(" + sb.toString() + ")";
        } else {
            return name + " = call " + type + " " + this.getOperand1().getName() +
                    "(" + sb.toString() + ")";
        }
    }

    @Override
    public void toMips() {
        // TODO
        super.toMips();
        Function function = (Function) this.getOperand1();
        this.funcStack = new LinkedHashMap<>();
        int stackOffset = MipsModuleBuilder.getInstance().getStackPointer();
        // ------------------------------------ 保存现场 ------------------------------------ //
        // 调用前的寄存器现场, 如果寄存器已经分配 那么将它们压在栈上
        ArrayList<Reg> regs = new ArrayList<>(new HashSet<>(MipsModuleBuilder.getInstance().getValue2Reg().values()));
        for (int i = 0; i < regs.size(); i++) {
            Reg reg = regs.get(i);
            this.funcStack.put(reg, stackOffset);
            new MemInstr(MipsInstrType.SW, regs.get(i), stackOffset, Reg.SP);
            stackOffset -= 4;
        }
        // 把函数实参倒着压到栈上
        for (int i = this.operandList.size() - 1; i >= 1; i--) {
            Value value = this.operandList.get(i);
            Reg reg = MipsModuleBuilder.getInstance().loadValue2Reg(value, Reg.K0);
            reg = (reg == null) ? Reg.K0 : reg;
            // 函数实参必然已经存在了栈上 这里不需要在编译器中保存value 只需要在对应位置塞入对应函数实参值
            this.funcStack.put(value, stackOffset);
            new MemInstr(MipsInstrType.SW, reg, stackOffset, Reg.SP);
            stackOffset -= 4;
        }
        // 保存fp和ra到栈上
        new MemInstr(MipsInstrType.SW, Reg.SP, stackOffset, Reg.SP);
        this.funcStack.put(Reg.SP, stackOffset);
        stackOffset -= 4;
//        MipsModuleBuilder.getInstance().pushReg2Stack(Reg.SP);
        new MemInstr(MipsInstrType.SW, Reg.RA, stackOffset, Reg.SP);
        this.funcStack.put(Reg.RA, stackOffset);
        stackOffset -= 4;
//        MipsModuleBuilder.getInstance().pushReg2Stack(Reg.RA);
        // 让保存fp为当前未分配栈空间的起始位置 开始本函数栈帧
        // fp+4处是old ra, fp+8处是old fp, fp + 12 + index * 4(index从0开始)是对应参数
        // 注意fp存的是地址
        new RICalInstr(MipsInstrType.ADDI, Reg.SP, Reg.SP, stackOffset);

//        new MemInstr(MipsInstrType.LW, Reg.FP, MipsModuleBuilder.getInstance().getStackPointer(), Reg.SP);
        // ------------------------------------ 调用函数 ------------------------------------ //
        new JumpInstr(MipsInstrType.JAL, function.getName().substring(1));

        // ------------------------------------ 恢复现场 ------------------------------------ //
        // 先恢复ra 再恢复fp
        new MemInstr(MipsInstrType.LW, Reg.RA, 4, Reg.SP);
        new MemInstr(MipsInstrType.LW, Reg.SP, 8, Reg.SP);

        // 恢复寄存器环境
        for (int i = 0; i < regs.size(); i++) {
            Reg reg = regs.get(i);
            int offset = this.funcStack.get(reg);
            new MemInstr(MipsInstrType.LW, reg, offset, Reg.SP);
        }
        // 如果函数有返回值 检查该函数是否分配寄存器 否则存在栈上
        if (function.isIntFunc()) {
            if(MipsModuleBuilder.getInstance().getValue2Reg().containsKey(this)){
                Reg reg = MipsModuleBuilder.getInstance().getValue2Reg().get(this);
                new MoveInstr(reg, Reg.V0);
            } else {
                new MoveInstr(Reg.K0, Reg.V0);
                MipsModuleBuilder.getInstance().pushValue2Stack(Reg.K0, this);
            }
        }
    }

    @Override
    public Value getDef() {
        return this.type == LLVMBasicType.INT32 ? this : null;
    }

    @Override
    public ArrayList<Value> getUse() {
        return this.operandList;
    }
}
