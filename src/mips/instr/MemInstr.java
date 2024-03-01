package mips.instr;

import mips.Reg;
import type.MipsInstrType;

public class MemInstr extends MipsInstr {
    private Reg rt;
    private Reg base;
    private int offset;
    private String label;

    // lw rt, offset(base)
    public MemInstr(MipsInstrType instrType, Reg rt, int offset, Reg base) {
        super();
        this.instrType = instrType;
        this.rt = rt;
        this.base = base;
        this.offset = offset;
    }
    // lw rt, label(offset)     for array load
    public MemInstr(MipsInstrType instrType, Reg rt, String label, int offset) {
        super();
        this.instrType = instrType;
        this.rt = rt;
        this.offset = offset;
        this.label = label;
    }

    @Override
    public String toString() {
        // lw rt, label(offset)
        if(this.base == null){
            return instrType + " " + rt + ", " + label + "(" + offset + ")";
        }
        // lw rt, offset(base)
        else {
            return instrType + " " + rt + ", " + offset + "(" + base + ")";
        }
    }
}
