package llvm.value.user.instr;

import llvm.type.LLVMArrayType;
import llvm.type.LLVMBasicType;
import llvm.type.LLVMPointerType;
import llvm.type.LLVMType;
import llvm.value.Constant;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import mips.instr.HILOInstr;
import mips.instr.LiInstr;
import mips.instr.MemInstr;
import mips.instr.MoveInstr;
import mips.instr.RICalInstr;
import mips.instr.RRCalInstr;
import type.LLVMInstrType;
import type.MipsInstrType;

import java.util.ArrayList;
import java.util.Objects;

public class GEPInstr extends Instr {
    private LLVMType ty;    // 第一个索引所指向的类型

    /*
     * name: gep返回寄存器的name
     * ty: 同文档。第一个索引所指向的类型,比如i32, [5xi32], [3x[5xi32]]
     * ptrVal: 指针的value 也是gep的基址
     * offsets: 一组i32索引
     * */
    public GEPInstr(String name, LLVMType ty, Value ptrVal, ArrayList<Value> offsets) {
        super(LLVMBasicType.INT32, name, LLVMInstrType.GEP);
        this.ty = ty;
        // 只有一个索引 返回ty的ptr类型
        if (offsets.size() == 1) {
            this.type = new LLVMPointerType(ty);
        }
        // 每多一个索引 去掉一层ty的包装
        else {
            LLVMType realTy = ty;
            for (int i = 1; i < offsets.size(); i++) {
                realTy = ((LLVMArrayType) realTy).getRightType();
            }
            this.type = new LLVMPointerType(realTy);
        }
        this.addOperands(ptrVal);
        for (Value value : offsets) {
            addOperands(value);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.getName(), ((Value) obj).getName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" = getelementptr ");
        sb.append(ty);
        sb.append(", ");
        sb.append(this.getOperand1().getType());
        sb.append(" ");
        sb.append(this.getOperand1().getName());
        sb.append(", ");
        for (int i = 1; i < this.operandList.size(); i++) {
            sb.append(this.operandList.get(i).getType());
            sb.append(" ");
            sb.append(this.operandList.get(i).getName());
            if (i != this.operandList.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public boolean offsetsIsConstant() {
        for (int i = 1; i < this.operandList.size(); i++) {
            if (!(this.operandList.get(i) instanceof Constant)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据gep的ty解包过程来求地址
     */
    @Override
    public void toMips() {
        super.toMips();
        Value ptrValue = this.getOperand1();
//        // 直接将数组首地址压到栈上做临时变量
//        new MemInstr(MipsInstrType.SW, ptrReg, MipsModuleBuilder.getInstance().getStackPointer(), Reg.SP);
        LLVMType tyClone = ty;
        if (offsetsIsConstant()) {
            Reg ptrReg = MipsModuleBuilder.getInstance().loadValue2Reg(ptrValue, Reg.K0);
            ptrReg = (ptrReg == null) ? Reg.K0 : ptrReg;
            toMipsConstant(ptrReg);
        }
        // 由于寄存器数量不够(K0和K1) 将偏移全部算出后再将ptr和res加载到K0和K1
        // 计算过程中的阶段偏移全部在K0中
        else {
            // 将ptr的值加载到声明的空间中
            Reg ptrReg = MipsModuleBuilder.getInstance().loadValue2Reg(ptrValue, Reg.K0);
            ptrReg = (ptrReg == null) ? Reg.K0 : ptrReg;
            MipsModuleBuilder.getInstance().pushValue2Stack(ptrReg, this);

            Reg resReg = MipsModuleBuilder.getInstance().getRegOf(this);
            if(resReg == null){
                for (int i = 1; i < this.operandList.size(); i++) {
                    Value offset = this.operandList.get(i);
                    // 如果这一项是0 且不是解包的最后一项 跳过此次解包
                    if (offset instanceof Constant && offset == Constant.ConstantZero && !tyClone.isInt32()) {
                        tyClone = ((LLVMArrayType) tyClone).getRightType();
                        continue;
                    }
                    if (tyClone.isInt32()) {
                        Reg offsetReg = MipsModuleBuilder.getInstance().loadValue2Reg(offset, Reg.K1);
                        offsetReg = (offsetReg == null) ? Reg.K1 : offsetReg;
                        new RRCalInstr(MipsInstrType.SLL, Reg.K1, offsetReg, 2); // k1
                        MipsModuleBuilder.getInstance().loadValue2Reg(this, Reg.K0);   //t3
                        resReg = Reg.K0;
                        new RRCalInstr(MipsInstrType.ADDU, resReg, resReg, Reg.K1);
                        MipsModuleBuilder.getInstance().pushValue2Stack(resReg, this);
                        break;  // 已经不能再解包
                    } else if (tyClone.isArray1()) {
                        // 加载offset到K0
                        Reg offsetReg = MipsModuleBuilder.getInstance().loadValue2Reg(offset, Reg.K0);
                        offsetReg = (offsetReg == null) ? Reg.K0 : offsetReg;
                        // 加载moveSpace到K1
                        new LiInstr(Reg.K1, ((LLVMArrayType) tyClone).getLeftNum() * 4);
                        // offset * moveSpace
                        new RRCalInstr(MipsInstrType.MULT, offsetReg, Reg.K1);
                        new HILOInstr(MipsInstrType.MFLO, Reg.K0);
                        // 修改this的值
                        MipsModuleBuilder.getInstance().loadValue2Reg(this, Reg.K1);
                        resReg = Reg.K1;
                        new RRCalInstr(MipsInstrType.ADDU, resReg, resReg, Reg.K0);
                        MipsModuleBuilder.getInstance().pushValue2Stack(resReg, this);
                        // 下一次解包
                        tyClone = ((LLVMArrayType) tyClone).getRightType();
                    } else if (tyClone.isArray2()) {
                        // moveSpace
                        int dim1 = ((LLVMArrayType) tyClone).getLeftNum();
                        int dim2 = ((LLVMArrayType) ((LLVMArrayType) tyClone).getRightType()).getLeftNum();
                        Reg offsetReg = MipsModuleBuilder.getInstance().loadValue2Reg(offset, Reg.K0);
                        offsetReg = (offsetReg == null) ? Reg.K0 : offsetReg;
                        new LiInstr(Reg.K1, dim1 * dim2 * 4);
                        // offset * moveSpace
                        new RRCalInstr(MipsInstrType.MULT, offsetReg, Reg.K1);
                        new HILOInstr(MipsInstrType.MFLO, Reg.K0);
                        // 修改this的值
                        MipsModuleBuilder.getInstance().loadValue2Reg(this, Reg.K1);
                        resReg = Reg.K1;
                        new RRCalInstr(MipsInstrType.ADDU, resReg, resReg, Reg.K0);
                        MipsModuleBuilder.getInstance().pushValue2Stack(resReg, this);

                        tyClone = ((LLVMArrayType) tyClone).getRightType();
                    } else {
                        System.err.println("GEPInstr-toMips:未知的ty");
                    }
                }
            }else {
                new MoveInstr(resReg, ptrReg);
                for (int i = 1; i < this.operandList.size(); i++) {
                    Value offset = this.operandList.get(i);
                    // 如果这一项是0 且不是解包的最后一项 跳过此次解包
                    if (offset instanceof Constant && offset == Constant.ConstantZero && !tyClone.isInt32()) {
                        tyClone = ((LLVMArrayType) tyClone).getRightType();
                        continue;
                    }
                    if (tyClone.isInt32()) {
                        Reg offsetReg = MipsModuleBuilder.getInstance().loadValue2Reg(offset, Reg.K1);
                        offsetReg = (offsetReg == null) ? Reg.K1 : offsetReg;
                        new RRCalInstr(MipsInstrType.SLL, Reg.K1, offsetReg, 2); // k1
                        new RRCalInstr(MipsInstrType.ADDU, resReg, resReg, Reg.K1);
                        break;  // 已经不能再解包
                    } else if (tyClone.isArray1()) {
                        // 加载offset到K0
                        Reg offsetReg = MipsModuleBuilder.getInstance().loadValue2Reg(offset, Reg.K0);
                        offsetReg = (offsetReg == null) ? Reg.K0 : offsetReg;
                        // 加载moveSpace到K1
                        new LiInstr(Reg.K1, ((LLVMArrayType) tyClone).getLeftNum() * 4);
                        // offset * moveSpace
                        new RRCalInstr(MipsInstrType.MULT, offsetReg, Reg.K1);
                        new HILOInstr(MipsInstrType.MFLO, Reg.K0);
                        new RRCalInstr(MipsInstrType.ADDU, resReg, resReg, Reg.K0);
                        // 下一次解包
                        tyClone = ((LLVMArrayType) tyClone).getRightType();
                    } else if (tyClone.isArray2()) {
                        // moveSpace
                        int dim1 = ((LLVMArrayType) tyClone).getLeftNum();
                        int dim2 = ((LLVMArrayType) ((LLVMArrayType) tyClone).getRightType()).getLeftNum();
                        Reg offsetReg = MipsModuleBuilder.getInstance().loadValue2Reg(offset, Reg.K0);
                        offsetReg = (offsetReg == null) ? Reg.K0 : offsetReg;
                        new LiInstr(Reg.K1, dim1 * dim2 * 4);
                        // offset * moveSpace
                        new RRCalInstr(MipsInstrType.MULT, offsetReg, Reg.K1);
                        new HILOInstr(MipsInstrType.MFLO, Reg.K0);
                        // 修改this的值
                        new RRCalInstr(MipsInstrType.ADDU, resReg, resReg, Reg.K0);

                        tyClone = ((LLVMArrayType) tyClone).getRightType();
                    } else {
                        System.err.println("GEPInstr-toMips:未知的ty");
                    }
                }
            }
        }
    }

    /**
     * offsets全部为const的gep
     * 计算offset的大小
     */
    public void toMipsConstant(Reg ptrReg) {
        LLVMType tyClone = ty;
        int space = 0;
        for (int i = 1; i < this.operandList.size(); i++) {
            Constant offset = (Constant) this.operandList.get(i);
            space += 4 * tyClone.getEleNum() * offset.getValue();
            if (tyClone.isInt32()) {
                break;
            } else {
                tyClone = ((LLVMArrayType) tyClone).getRightType();
            }
        }
        // 写到栈上 注意栈分配的数组空间顺序是低地址->高地址
        if (MipsModuleBuilder.getInstance().getRegOf(this) == null) {
            Reg resReg = Reg.K0;
            new RICalInstr(MipsInstrType.ADDI, resReg, ptrReg, space);
            MipsModuleBuilder.getInstance().pushValue2Stack(Reg.K0, this);
        }
        // 写到分配的寄存器中
        else {
            Reg resReg = MipsModuleBuilder.getInstance().getRegOf(this);
            new RICalInstr(MipsInstrType.ADDI, resReg, ptrReg, space);
        }
    }


    @Override
    public ArrayList<Value> getUse() {
        return this.operandList;
    }

    @Override
    public Value getDef() {
        return this;
    }
}
