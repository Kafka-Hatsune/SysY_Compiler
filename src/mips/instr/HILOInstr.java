package mips.instr;

import mips.Reg;
import type.MipsInstrType;

public class HILOInstr extends MipsInstr{
    private Reg rt;

    public HILOInstr(MipsInstrType instrType, Reg rt) {
        super();
        this.instrType = instrType;
        this.rt = rt;
    }

    // mfhi rd
    @Override
    public String toString() {
        return this.instrType + " " + rt;
    }
}
