package type;

public enum LLVMInstrType {
    ADD("add"),
    SUB("sub"),
    MUL("mul"),
    SDIV("sdiv"),   // 有符号除法
    SREM("srem"),   // 有符号取模
    ICMP("icmp"),
    AND("and"),
    OR("or"),
    CALL("call"),
    ALLOCA("alloca"),
    LOAD("load"),
    STORE("store"),
    GEP("getelementptr"),
    ZEXT("zext"),   // to
    TRUNC("trunc"),     // to
    BR("br"),
    RET("ret"),
    // 库函数
    LIB("lib"),
    // 优化使用
    PHI("phi"),
    PC("pc"),
    MOVE("move"),
    MULH("mulh"),
    SRL("srl");
    private String typeName;

    LLVMInstrType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
