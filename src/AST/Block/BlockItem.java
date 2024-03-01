package AST.Block;

import AST.Node;

// BlockItem → Decl | Stmt
public class BlockItem extends Node {
    public BlockItem(Node node) {
        super(node);
    }

    @Override
    public boolean handleError() {
        return super.handleError();
    }
}
