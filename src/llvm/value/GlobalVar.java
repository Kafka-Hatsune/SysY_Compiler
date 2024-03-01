package llvm.value;

import AST.InitVal.InitVal;
import llvm.ModuleBuilder;
import llvm.type.LLVMArrayType;
import llvm.type.LLVMPointerType;
import llvm.type.LLVMType;
import mips.directives.Space;
import mips.directives.Word;
import mips.directives.DirectiveLabel;
import type.SymbolType;

import java.util.ArrayList;

// @b = dso_local global i32 7
// @a = dso_local global [10 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0]
// @c = dso_local global [5 x [5 x i32]] [[5 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0], [5 x i32] [i32 1, i32 2, i32 3, i32 4, i32 5], [5 x i32] zeroinitializer, [5 x i32] zeroinitializer, [5 x i32] zeroinitializer]
public class GlobalVar extends Value {
    InitVal initVal;

    public GlobalVar(LLVMType type, String name, InitVal initVal) {
        super(new LLVMPointerType(type), name); // GlobalVar本质是声明变量的地址
        // super(type, name);
        this.initVal = initVal;
        ModuleBuilder.getInstance().module.addGlobalVar(this);
    }

    @Override
    public String toString() {
        return name + " = dso_local global " + initVal.toLLVMString(((LLVMPointerType) type).getPtrValType());
    }

    @Override
    public void toMips() {
        // name @a
        new DirectiveLabel(this.name.substring(1));
        if (this.initVal.getType() == SymbolType.ARRAY_DIM_0) {
            new Word(this.initVal.getInit0());
        }
        // [5 x i32]
        else if (this.initVal.getType() == SymbolType.ARRAY_DIM_1) {
            new Word(this.initVal.getInit1());
            int remainEle = ((LLVMArrayType) ((LLVMPointerType) this.type).getPtrValType()).getLeftNum() - this.initVal.getInit1().size();
            if (remainEle > 0) {
                new Space(remainEle * 4);
            }
        }
        // [5 x [5 x i32]]
        else if (this.initVal.getType() == SymbolType.ARRAY_DIM_2) {
            ArrayList<ArrayList<Integer>> init2 = this.initVal.getInit2();
            for (ArrayList<Integer> arrayList : init2) {
                new Word(arrayList);
            }
            int remainArray = ((LLVMArrayType) ((LLVMPointerType) this.type).getPtrValType()).getLeftNum() - this.initVal.getInit2().size();
            int dim1Size = ((LLVMArrayType)((LLVMArrayType) ((LLVMPointerType) this.type).getPtrValType()).getRightType()).getLeftNum();
            if (remainArray > 0) {
                new Space(dim1Size * remainArray * 4);
            }
        }
        // 没有初始值的变量 应声明space
        else {
            LLVMType ptrType = ((LLVMPointerType) this.type).getPtrValType();
            if(ptrType.isInt32()){
                new Space(4);
            } else if (ptrType.isArray1()) {
                int dim = ((LLVMArrayType) ptrType).getLeftNum();
                new Space(dim * 4);
            } else if (ptrType.isArray2()){
                int dim1 = ((LLVMArrayType) ptrType).getLeftNum();
                int dim2 = ((LLVMArrayType)((LLVMArrayType) ptrType).getRightType()).getLeftNum();
                new Space(dim1 * dim2 * 4);
            }else {
                System.err.println("GlobalVar-toMips:未知的变量声明");
            }
        }
    }

    //    public GlobalVar(InitVal initVal, String name) {
//        super();
////        if(initVal.getType() == SymbolType.ARRAY_DIM_0){
////            this.type = LLVMBasicType.INT32;
////            this.name = name;
////        }
//
//    }
}
