package mips.instr;

import mips.MipsModuleBuilder;
import type.MipsInstrType;

public class MipsInstr {
    protected MipsInstrType instrType;
    public MipsInstr() {
        MipsModuleBuilder.getInstance().mipsModule.addMipsInstr2Text(this);
    }
}
