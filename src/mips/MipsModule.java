package mips;

import mips.directives.Directive;
import mips.instr.MipsInstr;

import java.util.ArrayList;

public class MipsModule {
    private ArrayList<Directive> directives;
    private ArrayList<MipsInstr> texts;

    public MipsModule() {
        this.directives = new ArrayList<>();
        this.texts = new ArrayList<>();
    }

    public void addDirective2Data(Directive directive) {
        this.directives.add(directive);
    }

    public void addMipsInstr2Text(MipsInstr mipsInstr) {
        this.texts.add(mipsInstr);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (Directive directive : this.directives) {
            sb.append(directive);
            sb.append("\n");
        }
        sb.append("\n");
        sb.append(".text\n");
        for (MipsInstr mipsInstr : this.texts) {
            sb.append(mipsInstr);
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }
}
