package AST.Block;

import AST.Node;
import llvm.ModuleBuilder;
import llvm.value.Value;

// Block → '{' { BlockItem } '}'
public class Block extends Node {
    public Block(Node node) {
        super(node);
    }

    @Override
    public boolean handleError() {
        return super.handleError();
    }

    public boolean hasBlockItem(){
        return this.getChildren().size() > 2;
    }

    @Override
    public Value genIR() {
        super.genIR();
        return null;
    }
}
