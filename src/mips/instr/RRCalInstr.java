package mips.instr;

import mips.Reg;
import type.MipsInstrType;

// add rd, rs, rt
// GPR[rd] <- GPR[rs]+GPR[rt]
public class RRCalInstr extends MipsInstr{
    private Reg rd; // 目标寄存器
    private Reg rs;
    private Reg rt;

    private int s;  // for sll sra srl

    public RRCalInstr(MipsInstrType instrType, Reg rd, Reg rs, Reg rt) {
        super();
        this.instrType = instrType;
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }
    // MULT DIV
    public RRCalInstr(MipsInstrType instrType, Reg rs, Reg rt) {
        super();
        this.instrType = instrType;
        this.rs = rs;
        this.rt = rt;
    }

    public RRCalInstr(MipsInstrType instrType, Reg rd, Reg rt, int s) {
        super();
        this.instrType = instrType;
        this.rd = rd;
        this.rt = rt;
        this.s = s;
    }

    @Override
    public String toString() {
        switch (this.instrType){
            case MULT, DIV -> {
                return instrType + " " + rs + ", " + rt;
            }
            case SLL, SRA, SRL -> {
                return instrType + " " + rd + ", " + rt + ", " + s;
            }
            default -> {
                return instrType + " " + rd + ", " + rs + ", " + rt;
            }
        }
    }
}
