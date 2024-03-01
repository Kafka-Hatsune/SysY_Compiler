import AST.Node;
import IO.Output;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenStream;
import llvm.ModuleBuilder;
import llvm.value.Module;
import mips.MipsModule;
import mips.MipsModuleBuilder;
import parser.Parser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) throws IOException {
//        String arg = "icg-llvm-optimize";
        String arg = "icg-mips-optimize";
//        String arg = "icg-llvm";
//        String arg = "icg-mips";
        // 使用命令行参数
        if (args.length > 0) {
            arg = args[0];
        }
        Output out = new Output("output.txt", "error.txt", "llvm_ir.txt", "mips.txt");
        PushbackInputStream reader = new PushbackInputStream(new FileInputStream("testfile.txt"));
        Lexer lexer = new Lexer(reader);
        ArrayList<Token> tokens = lexer.getTokenList();
        TokenStream tokenStream = new TokenStream(tokens);

        // 解析参数
        switch (arg) {
            case "la" -> out.output.printTokens(tokens);
            case "sa" -> {
                Parser parser = new Parser(tokenStream);
                Node AST = parser.parseCompUnit();
                out.output.printSyntax(AST);
            }
            case "eh" -> {
                Parser parser = new Parser(tokenStream);
                Node AST = parser.parseCompUnit();
                AST.handleError();
                out.output.printErrorLogs();
            }
            case "icg-llvm" -> {
                Parser parser = new Parser(tokenStream);
                Node AST = parser.parseCompUnit();
                AST.handleError();
                if (out.output.hasError()) {
                    out.output.printErrorLogs();
                } else {
                    AST.genIR();
                    Module module = ModuleBuilder.getInstance().module;
                    out.output.printLLVM(module);
                }
            }
            case "icg-mips" -> {
                Parser parser = new Parser(tokenStream);
                Node AST = parser.parseCompUnit();
                AST.handleError();
                if (out.output.hasError()) {
                    out.output.printErrorLogs();
                } else {
                    AST.genIR();
                    Module module = ModuleBuilder.getInstance().module;
                    out.output.printLLVM(module);
                    module.toMips();
                    MipsModule mipsModule = MipsModuleBuilder.getInstance().mipsModule;
                    out.output.printMips(mipsModule);
                }
            }
            case "icg-llvm-optimize"->{
                Parser parser = new Parser(tokenStream);
                Node AST = parser.parseCompUnit();
                AST.handleError();
                if (out.output.hasError()) {
                    out.output.printErrorLogs();
                } else {
                    AST.genIR();
                    Module module = ModuleBuilder.getInstance().module;
                    module.Optimize();
                    out.output.printLLVM(module);
                }
            }
            case "icg-mips-optimize"->{
                Parser parser = new Parser(tokenStream);
                Node AST = parser.parseCompUnit();
                AST.handleError();
                if (out.output.hasError()) {
                    out.output.printErrorLogs();
                } else {
                    AST.genIR();
                    Module module = ModuleBuilder.getInstance().module;
                    out.output.printBeforeOptLLVM(module);
                    module.Optimize();
                    out.output.printAfterOptLLVM(module);
                    out.output.printLLVM(module);
                    module.toMips();
                    MipsModule mipsModule = MipsModuleBuilder.getInstance().mipsModule;
                    out.output.printMips(mipsModule);
                }
            }
        }
    }
}
