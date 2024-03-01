package mips.instr;

import mips.Reg;
import type.MipsInstrType;

public class MoveInstr extends MipsInstr{
    private Reg des;
    private Reg src;

    public MoveInstr(Reg des, Reg src) {
        super();
        this.des = des;
        this.src = src;
        this.instrType = MipsInstrType.MOVE;
    }

    @Override
    public String toString() {
        return "move " + des + ", " + src;
    }
}
