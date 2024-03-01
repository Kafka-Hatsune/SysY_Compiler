package mips.instr;

import type.MipsInstrType;

public class SysCallInstr extends MipsInstr{
    public SysCallInstr() {
        super();
        this.instrType = MipsInstrType.SYSCALL;
    }

    @Override
    public String toString() {
        return "syscall";
    }
}
