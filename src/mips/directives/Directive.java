package mips.directives;

import mips.MipsModuleBuilder;

public class Directive {
    public Directive() {
        MipsModuleBuilder.getInstance().mipsModule.addDirective2Data(this);
    }
}
