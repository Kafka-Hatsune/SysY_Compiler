package AST.InitVal;

import AST.Node;

// ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
public class ConstInitVal extends InitVal {
    public ConstInitVal(Node node) {
        super(node);
    }

}
