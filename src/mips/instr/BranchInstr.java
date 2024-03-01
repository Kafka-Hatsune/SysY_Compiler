package mips.instr;

import mips.Reg;
import type.MipsInstrType;

// beq $t1,$t2,label
public class BranchInstr extends MipsInstr {
    private Reg t1;
    private Reg t2;
    private String label;

    public BranchInstr(MipsInstrType instrType, Reg t1, Reg t2, String label) {
        super();
        this.instrType = instrType;
        this.t1 = t1;
        this.t2 = t2;
        this.label = label;
    }

    // beq $t1, $t2, label
    @Override
    public String toString() {
        return this.instrType + " " + t1 + ", " + t2 + ", " + label;
    }
}
