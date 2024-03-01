package mips.instr;

import mips.Reg;
import type.MipsInstrType;

public class LaInstr extends MipsInstr {
    private Reg reg;
    private String label;

    public LaInstr(Reg reg, String label) {
        super();
        this.instrType = MipsInstrType.LA;
        this.reg = reg;
        this.label = label;
    }

    @Override
    public String toString() {
        return "la " + reg + ", " + label;
    }
}
