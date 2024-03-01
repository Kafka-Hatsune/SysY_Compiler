package mips.directives;

public class Space extends Directive{
    private int value;

    public Space(int value) {
        super();
        this.value = value;
    }

    @Override
    public String toString() {
        return "    .space "+value;
    }
}
