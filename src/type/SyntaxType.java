package type;

public enum SyntaxType {
    COMP_UNIT("CompUnit"),
    DECL("Decl"),
    CONST_DECL("ConstDecl"),
    CONST_DEF("ConstDef"),
    CONST_INIT_VAL("ConstInitVal"),
    VAR_DECL("VarDecl"),
    VAR_DEF("VarDef"),
    INIT_VAL("InitVal"),
    FUNC_DEF("FuncDef"),
    MAIN_FUNC_DEF("MainFuncDef"),
    FUNC_F_PARAMS("FuncFParams"),
    FUNC_F_PARAM("FuncFParam"),
    BLOCK("Block"),
    BLOCK_ITEM("BlockItem"),
    STMT("Stmt"),
    FOR_STMT("ForStmt"),
    NUMBER("Number"),
    EXP("Exp"),
    COND("Cond"),
    LVAL("LVal"),
    PRIMARY_EXP("PrimaryExp"),
    UNARY_EXP("UnaryExp"),
    FUNC_R_PARAMS("FuncRParams"),
    MUL_EXP("MulExp"),
    ADD_EXP("AddExp"),
    REL_EXP("RelExp"),
    EQ_EXP("EqExp"),
    LAND_EXP("LAndExp"),
    LOR_EXP("LOrExp"),
    CONST_EXP("ConstExp"),
    B_TYPE("BType"),
    FUNC_TYPE("FuncType"),
    UNARY_OP("UnaryOp"),

    TOKEN("token");

    private String typeName;

    SyntaxType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
