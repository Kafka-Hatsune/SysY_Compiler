package llvm.value;

import llvm.type.LLVMType;

import java.util.ArrayList;

public class FString extends Value{
    private ArrayList<String> putInstr;
    public FString(LLVMType type, String name) {
        super(type, name);
    }
}
