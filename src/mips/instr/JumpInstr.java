package mips.instr;

import mips.Reg;
import type.MipsInstrType;

public class JumpInstr extends MipsInstr{
    private String target;
    private Reg rs; // for jr ra;
    // j jal
    public JumpInstr(MipsInstrType instrType, String target) {
        super();
        this.instrType = instrType;
        this.target = target;
    }
    // jr
    public JumpInstr(MipsInstrType instrType, Reg rs) {
        super();
        this.instrType = instrType;
        this.rs = rs;
    }

    @Override
    public String toString() {
        switch (instrType){
            case JR -> {
                return this.instrType + " " + rs;
            }
            case J, JAL -> {
                return this.instrType + " " + target;
            }
            default -> {
                System.err.println("使用了未知的Jump指令类型");
                return null;
            }
        }
    }
}
