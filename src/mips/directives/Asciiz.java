package mips.directives;

public class Asciiz extends Directive{
    private String name;
    private String content;

    public Asciiz(String name, String content) {
        super();
        this.name = name;
        this.content = content;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
