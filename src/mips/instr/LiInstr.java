package mips.instr;

import mips.Reg;
import type.MipsInstrType;

public class LiInstr extends MipsInstr {
    Reg reg;
    int number;

    public LiInstr(Reg reg, int number) {
        super();
        this.instrType = MipsInstrType.LI;
        this.reg = reg;
        this.number = number;
    }

    @Override
    public String toString() {
        return "li " + reg + ", " + number;
    }
}
