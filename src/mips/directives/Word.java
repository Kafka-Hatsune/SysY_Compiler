package mips.directives;

import java.util.ArrayList;

public class Word extends Directive{
    private int value;
    private ArrayList<Integer> values;

    // a: .word 2
    public Word(int value) {
        super();
        this.value = value;
    }

    public Word(ArrayList<Integer> values) {
        super();
        this.values = values;
    }

    //
    @Override
    public String toString() {
        if(values != null){
            StringBuilder sb = new StringBuilder();
            sb.append("    .word ");
            for(Integer integer : values){
                sb.append(integer);
                sb.append(",");
            }
            return sb.toString();
        }else {
            return "    .word " + value;
        }

    }
}
