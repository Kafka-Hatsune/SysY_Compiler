package type;

public enum InstrIcmpCondType {
    EQ("eq"),
    NE("ne"),   // 不相等
    UGT("ugt"), // 无符号大于
    UGE("uge"), // 无符号大于等于
    ULT("ult"), // 无符号小于
    ULE("ule"), // 无符号小于等于
    // 常用
    SGT("sgt"), // 有符号大于
    SGE("sge"), // 有符号大于等于
    SLT("slt"), // 有符号小于
    SLE("sle"); // 有符号小于等于

    private String typeName;

    InstrIcmpCondType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
