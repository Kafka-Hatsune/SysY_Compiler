package mips;

import llvm.value.Constant;
import llvm.value.GlobalVar;
import llvm.value.Value;
import llvm.value.user.Function;
import mips.instr.LaInstr;
import mips.instr.LiInstr;
import mips.instr.MemInstr;
import mips.instr.RICalInstr;
import type.MipsInstrType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MipsModuleBuilder {

    private final static MipsModuleBuilder mipsModuleBuilder = new MipsModuleBuilder();
    public MipsModule mipsModule;
    // 记录value存在栈上的相对于sp当前位置的偏移
    HashMap<Value, Integer> value2Offset = new HashMap<>();
    HashMap<Value, Reg> value2Reg = new HashMap<>();    // 寄存器现场: 保存着当前available中的寄存器和哪个value关联
    ArrayList<Reg> availableRegs = new ArrayList<>();   // Global:记录哪些寄存器是函数栈可用的寄存器
    Function curFunction;
    private int stackPointer;

    public MipsModuleBuilder() {
        this.mipsModule = new MipsModule();
        this.stackPointer = 0;
        for (Reg value : Reg.values()) {
            if (value.ordinal() >= Reg.T0.ordinal() && value.ordinal() <= Reg.T7.ordinal()) {
                availableRegs.add(value);
            } else if (value.ordinal() >= Reg.S0.ordinal() && value.ordinal() <= Reg.T9.ordinal()) {
                availableRegs.add(value);
            }
        }
    }

    public static MipsModuleBuilder getInstance() {
        return mipsModuleBuilder;
    }

    public int getStackPointer() {
        return stackPointer;
    }

    public void allocStack(int space) {
        this.stackPointer -= space;
    }

    public HashMap<Value, Reg> getValue2Reg() {
        return value2Reg;
    }

    public HashMap<Value, Integer> getValue2Offset() {
        return value2Offset;
    }

    /**
     * 编译进入某个函数
     * 将编译器记录的函数栈设为新的: 栈偏移为0 没有value记录在栈上
     * 查看是否有寄存器分配方案(优化步骤)
     */
    public void enterFunc(Function function) {
        this.curFunction = function;
        this.value2Reg = function.getValue2Reg() != null ? function.getValue2Reg() : new HashMap<>();
        this.stackPointer = 0;
        this.value2Offset = new HashMap<>();
    }


    // 查询寄存器表中Value对应的寄存器
    public Reg getRegOf(Value value) {
        return value2Reg.get(value);
    }

    /**
     * 内存管理核心方法:
     * 如果value存在寄存器 返回寄存器
     * 否则将value的值加载到参数reg中
     * value的值可能存在于多种环境中
     */
    public Reg loadValue2Reg(Value value, Reg reg) {
        // 是常数 li加载
        if (value instanceof Constant) {
            if (value == Constant.ConstantZero || ((Constant) value).getValue() == 0) {
                return Reg.ZERO;
            } else {
                new LiInstr(reg, ((Constant) value).getValue());
            }
        }
        // 当前value是Function的形参:从sp/fp + 8起始位置加载
        else if (curFunction.isFuncFParam(value)) {
            int paramIndex = curFunction.getFuncFParamIndex(value);
            int offset = 12 + paramIndex * 4;
            new MemInstr(MipsInstrType.LW, reg, offset, Reg.SP);
        }
        // 寄存器组中存在对应值
        else if (value2Reg.containsKey(value)) {
            return value2Reg.get(value);
        }
        // 存在函数栈上 lw加载
        else if (value2Offset.containsKey(value)) {
            int offset = value2Offset.get(value);
            new MemInstr(MipsInstrType.LW, reg, offset, Reg.SP);
        }
        // 是全局label la加载
        else if (value instanceof GlobalVar) {
            new LaInstr(reg, value.getName().substring(1));
        }
        // 编译的顺序和执行的顺序不一致 此时不在栈上说明有可能还未编译到定义点 在栈上声明好空间load就行
        //
        else {
            allocValueInStack(value);
            int offset = value2Offset.get(value);
            new MemInstr(MipsInstrType.LW, reg, offset, Reg.SP);
            System.out.println("loadValue2Reg:value编译顺序不同");
        }
        return null;
    }

    // ---------------------------- 栈管理 ---------------------------- //

    /**
     * 将Reg存入value在栈上对应的空间 没有空间创建空间
     * @param reg 提供给这次push的寄存器
     * @param value 对应的需要在栈上记录/查询的value的值
     */
    public void pushValue2Stack(Reg reg, Value value) {
        // 栈上对于reg对应的局部变量已有空间 使用reg将值存到局部变量对应的空间里
        if (value2Offset.containsKey(value)) {
            int offset = value2Offset.get(value);
            new MemInstr(MipsInstrType.SW, reg, offset, Reg.SP);
        }
        // 栈上对于reg对应的局部变量没有空间 声明空间后在编译器记录局部变量的地址 然后存值
        else {
            allocValueInStack(value);
            int offset = value2Offset.get(value);
            new MemInstr(MipsInstrType.SW, reg, offset, Reg.SP);
        }
    }

    /**
     * 在栈上给value准备空间
     */
    public void allocValueInStack(Value value) {
        value2Offset.put(value, stackPointer);
        this.stackPointer -= 4;
    }


}
