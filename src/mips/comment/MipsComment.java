package mips.comment;

import llvm.value.Value;
import mips.instr.MipsInstr;

public class MipsComment extends MipsInstr {
    Value value;

    public MipsComment(Value value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        return "# " + value.toString();
    }
}
