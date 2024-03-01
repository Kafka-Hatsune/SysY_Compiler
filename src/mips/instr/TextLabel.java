package mips.instr;

import type.MipsInstrType;

public class TextLabel extends MipsInstr {
    String name;

    public TextLabel(String name) {
        super();
        this.instrType = MipsInstrType.LABEL;
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ":";
    }
}
