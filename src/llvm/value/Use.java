package llvm.value;

public class Use {
    private Value user;
    private Value used;

    public Use(Value user, Value used) {
        this.user = user;
        this.used = used;
    }

    public Value getUser() {
        return user;
    }
}
