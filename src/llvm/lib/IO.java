package llvm.lib;

public enum IO {
    // input
    GETINT32("declare i32 @getint()"),
    // output
    PUT_INT_32("declare void @putint(i32)"),
    PUT_CH("declare void @putch(i32)"),
    PUT_STR("declare void @putstr(i8*)");
    private String content;

    IO(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return this.content;
    }
}
