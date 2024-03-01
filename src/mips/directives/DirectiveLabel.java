package mips.directives;

import mips.instr.MipsInstr;

public class DirectiveLabel extends Directive{
    String name;

    public DirectiveLabel(String name) {
        super();
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ":";
    }
}
