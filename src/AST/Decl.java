package AST;

// Decl → ConstDecl | VarDecl
public class Decl extends Node{
    public Decl(Node node) {
        super(node);
    }

    @Override
    public boolean handleError(){
        return super.handleError();
    }
}
