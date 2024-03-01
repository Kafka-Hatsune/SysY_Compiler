package type;

public enum MipsInstrType {
    // R-R Cal
    ADDU("addu"), // add rd, rs, rt
    SUBU("subu"), // sub rd, rs, rt
    MULT("mult"),   // mult rs, rt
    DIV("div"), // div rs, rt
    SLT("slt"), // slt rd, rs, rt  rs小于rt置rd为1
    SLE("sle"),
    SGT("sgt"),
    SGE("sge"),
    SEQ("seq"),
    SNE("sne"),
    AND("and"), // and rd, rs, rt
    OR("or"),   // or rd, rs, rt
    XOR("xor"), // xor rd, rs, rt 异或
    SLL("sll"), // sll rd, rt, s  逻辑左移
    SRL("srl"), // srl rd, rt, s 逻辑右移
    SRA("sra"), // sra rd, rt, s 算数右移
    // R-I Cal
    ADDI("addi"),   // addi rt, rs, immediate
    ANDI("andi"),   // andi rt, rs, immediate
    ORI("ori"),     // ori rt, rs, immediate
    XORI("xori"),   // xori rt, rs, immediate
    SLTI("slti"), // slti rt, rs, immediate 小于立即数置1
    // branch
    BEQ("beq"),     // beq $t1,$t2,label
    BNE("bne"),     // bne $t1,$t2,label
    // jump
    J("j"), // j target
    JAL("jal"), // jal target
    JR("jr"),   //  jr rs
    // mem : load/store
    LW("lw"),   // lw rt, offset(base)  lw rt, label(offset)
    SW("sw"),   // sw rt, offset(base)
    // move
    MFHI("mfhi"),   // mfhi rd
    MFLO("mflo"),   // mflo rd
    MTHI("mthi"),   // mthi rs
    MTLO("mtlo"),   // mtlo rs
    // trap
    SYSCALL("syscall"), // syscall
    // others
    MOVE("move"),
    LABEL("label"),
    LI("li"),   // li $v0, 10
    LA("la");               // la $t1, label
    private String typeName;

    MipsInstrType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
