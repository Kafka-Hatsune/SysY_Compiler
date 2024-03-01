package mips.instr;

import mips.Reg;
import type.MipsInstrType;

public class RICalInstr extends MipsInstr{
    private Reg rt; // 目标寄存器
    private Reg rs;
    private int immediate;

    public RICalInstr(MipsInstrType instrType, Reg rt, Reg rs, int immediate) {
        super();
        this.instrType = instrType;
        this.rt = rt;
        this.rs = rs;
        this.immediate = immediate;
    }

    @Override
    public String toString() {
        return instrType + " " + rt + ", " + rs + ", " + immediate;
    }
}
