package IO;

import AST.Node;
import error.Error;
import error.ErrorLogs;
import lexer.Token;
import llvm.value.Module;
import mips.MipsModule;
import type.SyntaxType;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Output {
    public static final Output output = new Output();
    public ErrorLogs errorLogs;
    public FileOutputStream outFile;

    public FileOutputStream llvmIRFile;

    public FileOutputStream mipsOutFile;

    public FileOutputStream beforeOptllvmFile;
    public FileOutputStream afterOptllvmFile;

    private ArrayList<SyntaxType> excludeSyntax = new ArrayList<>(Arrays.asList(SyntaxType.BLOCK_ITEM, SyntaxType.B_TYPE, SyntaxType.DECL));

    public Output() {
    }

    public Output(String outfilePath, String errorFilePath, String llvmFilePath, String mipsFilePath) throws FileNotFoundException {
        output.outFile = new FileOutputStream(outfilePath);
        output.errorLogs = new ErrorLogs(errorFilePath);
        output.llvmIRFile = new FileOutputStream(llvmFilePath);
        output.mipsOutFile = new FileOutputStream(mipsFilePath);
        output.beforeOptllvmFile = new FileOutputStream("testfilei_20376348_路涛_优化前中间代码.txt");
        output.afterOptllvmFile = new FileOutputStream("testfilei_20376348_路涛_优化后中间代码.txt");
    }

    public <T> boolean isIn(T value, ArrayList<T> array) {
        for (T element : array) {
            if (element.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public void printTokens(ArrayList<Token> tokens) throws IOException {
        for (Token token : tokens) {
            String string = token.toString();
            this.outFile.write(string.getBytes());
        }
    }

    public void printSyntax(Node node) throws IOException {
        if (node.getChildren() != null) {
            for (Node child : node.getChildren()) {
                printSyntax(child);
            }
        }
        // 访问当前节点
        if (node.getSyntaxType() != null && !isIn(node.getSyntaxType(), excludeSyntax)) {
            this.outFile.write(node.toString().getBytes());
        }
    }

    public boolean hasError() {
        return this.errorLogs.hasErrorLogs();
    }

    public void printErrorLogs() throws IOException {
        this.errorLogs.printErrorLogs();
    }

    public void addErrorMsg(Error error) {
        this.errorLogs.addErrorMsg(error);
    }

    public void printLLVM(Module module) throws IOException {
        this.llvmIRFile.write(module.toString().getBytes());
    }

    public void printBeforeOptLLVM(Module module) throws IOException {
        this.beforeOptllvmFile.write(module.toString().getBytes());
    }

    public void printAfterOptLLVM(Module module) throws IOException {
        this.afterOptllvmFile.write(module.toString().getBytes());
    }

    public void printMips(MipsModule module) throws IOException {
        this.mipsOutFile.write(module.toString().getBytes());
    }

}
