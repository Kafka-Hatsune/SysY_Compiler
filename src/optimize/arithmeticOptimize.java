package optimize;

import llvm.type.LLVMBasicType;
import llvm.value.BasicBlock;
import llvm.value.Constant;
import llvm.value.Value;
import llvm.value.user.instr.Instr;
import llvm.value.user.instr.aluInstr.SdivInstr;
import llvm.value.user.instr.optimizeInstr.MulHInstr;
import llvm.value.user.instr.optimizeInstr.SrlInstr;
import utils.NameGen;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.ListIterator;

public class arithmeticOptimize {
    BasicBlock BB;

    public arithmeticOptimize(BasicBlock BB) {
        this.BB = BB;
        modOptimize();
        // 不知道为啥是错的
//        divOptimize();
        mulOptimize();
    }

    public void modOptimize() {

    }

    public void divOptimize() {
        BigInteger maxM = BigInteger.valueOf(2).pow(30);
        BigInteger maxMMulD = BigInteger.valueOf(2).pow(63);
        ListIterator<Instr> iterator = BB.getInstrList().listIterator();
        while (iterator.hasNext()) {
            Instr instr = iterator.next();
            if (instr instanceof SdivInstr) {
                // 被除数
                Value dividend = instr.getOperand1();
                // 除数
                Value divisor = instr.getOperand2();
                if (divisor instanceof Constant && ((Constant) divisor).getValue() > 0) {
                    BigInteger d = new BigInteger(String.valueOf(((Constant) divisor).getValue()));
                    int l;
                    BigInteger m = null;
                    for (l = 0; l < 31; l++) {
                        BigInteger lowerBound = BigInteger.valueOf(2).pow(32 + l);
                        BigInteger upperBound = lowerBound.add(BigInteger.valueOf(2).pow(l));
                        // 向上取整:检查余数
                        BigInteger lowerBoundDivD = lowerBound.divide(d);
                        if (!lowerBound.mod(d).equals(BigInteger.ZERO)) {
                            lowerBoundDivD = lowerBoundDivD.add(BigInteger.ONE);
                        }
                        // divide默认向下取整
                        BigInteger upperBoundDivD = upperBound.divide(d);
                        // upper >= lower说明有答案
                        if (upperBoundDivD.compareTo(lowerBoundDivD) >= 0 && lowerBoundDivD.compareTo(maxM) <= 0 ) {
                            m = lowerBoundDivD;
                            break;
                        }
                    }
                    if (m != null) {
                        int index = BB.getInstrList().indexOf(instr);
                        iterator.remove();
                        Value mValue = new Constant(LLVMBasicType.INT32, m.intValue());
                        Instr mulHInstr = new MulHInstr(NameGen.getInstance().genLocalVarName(BB.getParentFunc()), mValue, dividend);
                        // BB.getInstrList().add(index, mulHInstr);
                        iterator.add(mulHInstr);
                        // srl的Value是原来div结果的value 这里二者同名即可
                        Instr srlInstr = new SrlInstr(NameGen.getInstance().genLocalVarName(BB.getParentFunc()), mulHInstr, l);
                        // BB.getInstrList().add(index+1, srlInstr);
                        iterator.add(srlInstr);
                        instr.replaceUse(srlInstr);
                    }
                }
            }
        }

    }

    public void mulOptimize() {

    }

}
