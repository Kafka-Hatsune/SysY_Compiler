package llvm.value.user.instr;

import llvm.type.LLVMBasicType;
import llvm.type.LLVMType;
import llvm.value.Value;
import mips.MipsModuleBuilder;
import mips.Reg;
import type.LLVMInstrType;

import java.util.ArrayList;
import java.util.Objects;

//<result> = zext <ty> <value> to <ty2>
public class ZextInstr extends Instr {

    private LLVMType originType;

    public ZextInstr(LLVMType type1, Value value, LLVMType type2, String name) {
        super(type2, name, LLVMInstrType.ZEXT);
        this.addOperands(value);
        this.originType = type1;
    }
    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.getName(), ((Value) obj).getName());
    }
    @Override
    public String toString() {
        Value value = this.getOperand1();
        return name + " = zext " + originType.toString() + " " +
                value.getName() + " to " + type;
    }

    // MIPS中不需要考虑位宽，Zext转Mips只需要将value的寄存器/栈内容绑定到this
    @Override
    public void toMips() {
        super.toMips();
        Value value = this.getOperand1();
        // 查询寄存器表是否存在相关寄存器
        Reg valueReg = MipsModuleBuilder.getInstance().getRegOf(value);
        // 存在栈上
        if(valueReg == null){
            int offset = MipsModuleBuilder.getInstance().getValue2Offset().get(value);
            MipsModuleBuilder.getInstance().getValue2Offset().put(this, offset);
        }
        // 在寄存器中 : 直接替换原value,因为llvm中原value被Zext后不会再被使用
        else {
            MipsModuleBuilder.getInstance().getValue2Reg().put(this, valueReg);
        }
    }

    @Override
    public Value getDef() {
        return null;
    }


}
