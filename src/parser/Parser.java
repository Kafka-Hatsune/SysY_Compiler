package parser;

import AST.Block.Block;
import AST.Block.BlockItem;
import AST.CompUnit;
import AST.Const.ConstDecl;
import AST.Decl;
import AST.Def.ConstDef;
import AST.Def.VarDef;
import AST.Exp.CalExp.AddExp;
import AST.Exp.CalExp.ConstExp;
import AST.Exp.CalExp.Exp;
import AST.Exp.CalExp.LVal;
import AST.Exp.CalExp.MulExp;
import AST.Exp.CalExp.Number;
import AST.Exp.CalExp.PrimaryExp;
import AST.Exp.CalExp.UnaryExp;
import AST.Exp.CondExp.Cond;
import AST.Exp.CondExp.EqExp;
import AST.Exp.CondExp.LAndExp;
import AST.Exp.CondExp.LOrExp;
import AST.Exp.CondExp.RelExp;
import AST.Exp.UnaryOp;
import AST.ForStmt;
import AST.Func.FuncDef;
import AST.Func.FuncFParam;
import AST.Func.FuncFParams;
import AST.Func.FuncRParams;
import AST.Func.FuncType;
import AST.Func.MainFuncDef;
import AST.InitVal.ConstInitVal;
import AST.InitVal.VarInitVal;
import AST.Node;
import AST.Stmt;
import AST.Var.BType;
import AST.Var.VarDecl;
import IO.Output;
import error.Error;
import lexer.Token;
import lexer.TokenStream;
import type.ErrorType;
import type.StmtType;
import type.SyntaxType;
import type.TokenType;

public class Parser {
    private TokenStream tokenStream;

    private int line;

    public Parser(TokenStream tokenStream) {
        this.tokenStream = tokenStream;
        this.line = 1;
    }


    public void parseCurToken(Node fNode, TokenType expected) {
        Token token = tokenStream.getCurToken();
        this.line = token.getLine();
        Node tmpNode = new Node(token);
        if (expected != null && tmpNode.getTerminal().getType() != expected) {
            // 错误行号 : 前一个非终结符所在行号。
            int preLine = tokenStream.getPreTokenLine();
            switch (expected) {
                case SEMICN ->
                        Output.output.addErrorMsg(new Error(preLine, ErrorType.i, fNode.getSyntaxType() + "缺少';'"));
                case RPARENT ->
                        Output.output.addErrorMsg(new Error(preLine, ErrorType.j, fNode.getSyntaxType() + "缺少')'"));
                case RBRACK ->
                        Output.output.addErrorMsg(new Error(preLine, ErrorType.k, fNode.getSyntaxType() + "缺少']'"));
                default -> System.err.println("出现了预料之外的语法错误");
            }
            return;
        }
        tokenStream.next();
        fNode.addChild(tmpNode);
    }

    public Node parseCompUnit() {
        Node node = new Node(SyntaxType.COMP_UNIT);
        node.setStartLine(tokenStream.getCurToken().getLine());
        while (true) {
            if (tokenStream.isEOF()) {
                break;
            } else if (tokenStream.peek(1).getType() == TokenType.MAINTK) {
                node.addChild(parseMainFuncDef());
            } else if (tokenStream.peek(2).getType() == TokenType.LPARENT) {
                node.addChild(parseFuncDef());
            } else {
                node.addChild(parseDecl());
            }
        }
        node.setEndLine(this.line);
        return new CompUnit(node);
    }

    public Node parseMainFuncDef() {
        Node node = new Node(SyntaxType.MAIN_FUNC_DEF);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // int
        parseCurToken(node, TokenType.INTTK);
        // main
        parseCurToken(node, TokenType.MAINTK);
        // '('
        parseCurToken(node, TokenType.LPARENT);
        // ')'
        parseCurToken(node, TokenType.RPARENT);
        // Block
        node.addChild(parseBlock());
        node.setEndLine(this.line);
        return new MainFuncDef(node);
    }

    private Node parseBlock() {
        Node node = new Node(SyntaxType.BLOCK);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // '{'
        parseCurToken(node, TokenType.LBRACE);
        //  { BlockItem }
        while (tokenStream.getCurToken().getType() != TokenType.RBRACE) {
            node.addChild(parseBlockItem());
        }
        // '}'
        parseCurToken(node, TokenType.RBRACE);
        node.setEndLine(this.line);
        return new Block(node);
    }

    private Node parseBlockItem() {
        Node node = new Node(SyntaxType.BLOCK_ITEM);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // Decl | Stmt
        if (tokenStream.getCurToken().getType() == TokenType.CONSTTK ||
                tokenStream.getCurToken().getType() == TokenType.INTTK) {
            node.addChild(parseDecl());
        } else {
            node.addChild(parseStmt());
        }
        node.setEndLine(this.line);
        return new BlockItem(node);
    }

    private Node parseStmt() {
        Node node = new Node(SyntaxType.STMT);
        StmtType type = null;
        node.setStartLine(tokenStream.getCurToken().getLine());
        switch (tokenStream.getCurToken().getType()) {
            // 无Exp
            case SEMICN -> {
                parseCurToken(node, TokenType.SEMICN);
                type = StmtType.EXP_STMT;
            }
            // Block
            case LBRACE -> {
                node.addChild(parseBlock());
                type = StmtType.BLOCK_STMT;
            }
            //  'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            case IFTK -> {
                parseCurToken(node, TokenType.IFTK);
                parseCurToken(node, TokenType.LPARENT);
                node.addChild(parseCond());
                parseCurToken(node, TokenType.RPARENT);
                node.addChild(parseStmt());
                if (tokenStream.getCurToken().getType() == TokenType.ELSETK) {
                    parseCurToken(node, TokenType.ELSETK);
                    node.addChild(parseStmt());
                }
                type = StmtType.IF_STMT;
            }
            // 'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
            case FORTK -> {
                parseCurToken(node, TokenType.FORTK);
                parseCurToken(node, TokenType.LPARENT);
                if (tokenStream.getCurToken().getType() != TokenType.SEMICN) {
                    node.addChild(parseForStmt());
                }
                parseCurToken(node, TokenType.SEMICN);
                if (tokenStream.getCurToken().getType() != TokenType.SEMICN) {
                    node.addChild(parseCond());
                }
                parseCurToken(node, TokenType.SEMICN);
                if (tokenStream.getCurToken().getType() != TokenType.RPARENT) {
                    node.addChild(parseForStmt());
                }
                parseCurToken(node, TokenType.RPARENT);
                node.addChild(parseStmt());
                type = StmtType.FOR_STMT;
            }
            // 'break' ';'
            case BREAKTK -> {
                parseCurToken(node, TokenType.BREAKTK);
                parseCurToken(node, TokenType.SEMICN);
                type = StmtType.BREAK_STMT;
            }
            // 'continue' ';'
            case CONTINUETK -> {
                parseCurToken(node, TokenType.CONTINUETK);
                parseCurToken(node, TokenType.SEMICN);
                type = StmtType.CONTINUE_STMT;
            }
            // 'return' [Exp] ';'
            case RETURNTK -> {
                parseCurToken(node, TokenType.RETURNTK);
                // TODO 缺少分号
                TokenType tokenType = tokenStream.getCurToken().getType();
                if (tokenType == TokenType.PLUS || tokenType == TokenType.MINU || tokenType == TokenType.NOT ||
                        tokenType == TokenType.IDENFR || tokenType == TokenType.LPARENT || tokenType == TokenType.INTCON) {
                    node.addChild(parseExp());
                }
                parseCurToken(node, TokenType.SEMICN);
                type = StmtType.RETURN_STMT;
            }
            // 'printf''('FormatString{','Exp}')'';'
            case PRINTFTK -> {
                parseCurToken(node, TokenType.PRINTFTK);
                parseCurToken(node, TokenType.LPARENT);
                parseCurToken(node, TokenType.STRCON);
                while (tokenStream.getCurToken().getType() == TokenType.COMMA) {
                    parseCurToken(node, TokenType.COMMA);
                    node.addChild(parseExp());
                }
                parseCurToken(node, TokenType.RPARENT);
                parseCurToken(node, TokenType.SEMICN);
                type = StmtType.PRINTF_STMT;
            }
            default -> {
                // 区分逻辑
                tokenStream.save();
                Node exp = parseExp();
                // Exp ;
                if (tokenStream.getCurToken().getType() == TokenType.SEMICN) {
                    node.addChild(exp);
                    parseCurToken(node, TokenType.SEMICN);
                    type = StmtType.EXP_STMT;
                } else if (tokenStream.getCurToken().getType() == TokenType.ASSIGN) {
                    // LVal '=' 'getint' '(' ')' ';'
                    // LVal '=' 'getint' '(' ')'
                    if (tokenStream.peek(1).getType() == TokenType.GETINTTK) {
                        tokenStream.load();
                        node.addChild(parseLVal());
                        parseCurToken(node, TokenType.ASSIGN);
                        parseCurToken(node, TokenType.GETINTTK);
                        parseCurToken(node, TokenType.LPARENT);
                        parseCurToken(node, TokenType.RPARENT);
                        parseCurToken(node, TokenType.SEMICN);
                        type = StmtType.GETINT_STMT;
                    }
                    // LVal '=' Exp ';'
                    // LVal '=' Exp
                    else {
                        tokenStream.load();

                        node.addChild(parseLVal());
                        parseCurToken(node, TokenType.ASSIGN);
                        node.addChild(parseExp());
                        parseCurToken(node, TokenType.SEMICN);
                        type = StmtType.LVAL_STMT;
                    }
                }
                // Exp
                else {
                    type = StmtType.EXP_STMT;
                    node.addChild(exp);
                    Output.output.addErrorMsg(new Error(exp.getStartLine(), ErrorType.i, "Stmt 缺少';'"));
                }
            }
        }
        node.setEndLine(this.line);
        return new Stmt(node, type);
    }

    private Node parseCond() {
        Node node = new Node(SyntaxType.COND);
        node.setStartLine(tokenStream.getCurToken().getLine());
        node.addChild(parseLOrExp());
        node.setEndLine(this.line);
        return new Cond(node);
    }

    private Node parseLOrExp() {
        Node node = new Node(SyntaxType.LOR_EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        node.addChild(parseLAndExp());
        while (tokenStream.getCurToken().getType() == TokenType.OR) {
            node.adjustExp(SyntaxType.LOR_EXP);
            parseCurToken(node, TokenType.OR);
            node.addChild(parseLAndExp());

        }
        node.setEndLine(this.line);
        return new LOrExp(node);
    }

    private Node parseLAndExp() {
        Node node = new Node(SyntaxType.LAND_EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        node.addChild(parseEqExp());
        while (tokenStream.getCurToken().getType() == TokenType.AND) {
            node.adjustExp(SyntaxType.LAND_EXP);
            parseCurToken(node, TokenType.AND);
            node.addChild(parseEqExp());
        }
        node.setEndLine(this.line);
        return new LAndExp(node);
    }

    private Node parseEqExp() {
        Node node = new Node(SyntaxType.EQ_EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        node.addChild(parseRelExp());
        // 一个AddExp的Children要么三个Node 要么一个Node
        while (tokenStream.getCurToken().getType() == TokenType.EQL ||
                tokenStream.getCurToken().getType() == TokenType.NEQ) {
            node.adjustExp(SyntaxType.EQ_EXP);
            if (tokenStream.getCurToken().getType() == TokenType.EQL) {
                parseCurToken(node, TokenType.EQL);
            } else if (tokenStream.getCurToken().getType() == TokenType.NEQ) {
                parseCurToken(node, TokenType.NEQ);
            }
            node.addChild(parseRelExp());
        }
        node.setEndLine(this.line);
        return new EqExp(node);
    }

    private Node parseRelExp() {
        Node node = new Node(SyntaxType.REL_EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        node.addChild(parseAddExp());
        while (tokenStream.getCurToken().getType() == TokenType.LSS ||
                tokenStream.getCurToken().getType() == TokenType.LEQ ||
                tokenStream.getCurToken().getType() == TokenType.GRE ||
                tokenStream.getCurToken().getType() == TokenType.GEQ) {
            node.adjustExp(SyntaxType.REL_EXP);
            switch (tokenStream.getCurToken().getType()) {
                case LSS -> {
                    parseCurToken(node, TokenType.LSS);
                }
                case LEQ -> {
                    parseCurToken(node, TokenType.LEQ);
                }
                case GRE -> {
                    parseCurToken(node, TokenType.GRE);
                }
                case GEQ -> {
                    parseCurToken(node, TokenType.GEQ);
                }
                default -> {
                }
            }
            node.addChild(parseAddExp());
        }
        node.setEndLine(this.line);
        return new RelExp(node);
    }


    private Node parseForStmt() {
        Node node = new Node(SyntaxType.FOR_STMT);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // LVal
        node.addChild(parseLVal());
        // '='
        parseCurToken(node, TokenType.ASSIGN);
        // Exp
        node.addChild(parseExp());
        node.setEndLine(this.line);
        return new ForStmt(node);
    }


    public Node parseFuncDef() {
        Node node = new Node(SyntaxType.FUNC_DEF);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // FuncType
        node.addChild(parseFuncType());
        // ident
        parseCurToken(node, TokenType.IDENFR);
        // '('
        parseCurToken(node, TokenType.LPARENT);
        // [FuncFParams]
        if (tokenStream.getCurToken().getType() == TokenType.INTTK) {
            node.addChild(parseFuncFParams());
        }
        // ')'
        parseCurToken(node, TokenType.RPARENT);
        // Block
        node.addChild(parseBlock());
        node.setEndLine(this.line);
        return new FuncDef(node);
    }

    private Node parseFuncType() {
        Node node = new Node(SyntaxType.FUNC_TYPE);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // 'void'
        if (tokenStream.getCurToken().getType() == TokenType.VOIDTK) {
            parseCurToken(node, TokenType.VOIDTK);
        }
        // 'int'
        else if (tokenStream.getCurToken().getType() == TokenType.INTTK) {
            parseCurToken(node, TokenType.INTTK);
        }
        node.setEndLine(this.line);
        return new FuncType(node);
    }

    private Node parseFuncFParams() {
        Node node = new Node(SyntaxType.FUNC_F_PARAMS);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // FuncFParam
        node.addChild(parseFuncFParam());
        // { ',' FuncFParam }
        while (tokenStream.getCurToken().getType() == TokenType.COMMA) {
            parseCurToken(node, TokenType.COMMA);
            node.addChild(parseFuncFParam());
        }
        node.setEndLine(this.line);
        return new FuncFParams(node);
    }

    private Node parseFuncFParam() {
        Node node = new Node(SyntaxType.FUNC_F_PARAM);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // BType
        node.addChild(parseBType());
        // Ident
        parseCurToken(node, TokenType.IDENFR);
        // ['[' ']' { '[' ConstExp ']' }]
        if (tokenStream.getCurToken().getType() == TokenType.LBRACK) {
            parseCurToken(node, TokenType.LBRACK);
            parseCurToken(node, TokenType.RBRACK);
            while (tokenStream.getCurToken().getType() == TokenType.LBRACK) {
                parseCurToken(node, TokenType.LBRACK);
                node.addChild(parseConstExp());
                parseCurToken(node, TokenType.RBRACK);
            }
        }
        node.setEndLine(this.line);
        return new FuncFParam(node);
    }

    public Node parseDecl() {
        Node node = new Node(SyntaxType.DECL);
        node.setStartLine(tokenStream.getCurToken().getLine());
        if (tokenStream.getCurToken().getType() == TokenType.CONSTTK) {
            node.addChild(parseConstDecl());
        } else if (tokenStream.getCurToken().getType() == TokenType.INTTK) {
            node.addChild(parseVarDecl());
        }
        node.setEndLine(this.line);
        return new Decl(node);
    }

    private Node parseVarDecl() {
        Node node = new Node(SyntaxType.VAR_DECL);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // BType
        node.addChild(parseBType());
        // varDef
        node.addChild(parseVarDef());
        // {',', varDef}
        while (tokenStream.getCurToken().getType() == TokenType.COMMA) {
            parseCurToken(node, TokenType.COMMA);
            node.addChild(parseVarDef());
        }
        // ;
        parseCurToken(node, TokenType.SEMICN);
        node.setEndLine(this.line);
        return new VarDecl(node);
    }

    // Ident { '[' ConstExp ']' } | | Ident { '[' ConstExp ']' } '=' InitVal
    private Node parseVarDef() {
        Node node = new Node(SyntaxType.VAR_DEF);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // ident
        parseCurToken(node, TokenType.IDENFR);
        // {'['  ConstExp  ']'}
        while (tokenStream.getCurToken().getType() == TokenType.LBRACK) {
            // '['
            parseCurToken(node, TokenType.LBRACK);
            // ConstExp
            node.addChild(parseConstExp());
            // ']'
            parseCurToken(node, TokenType.RBRACK);
        }
        // ['='  InitVal]
        if (tokenStream.getCurToken().getType() == TokenType.ASSIGN) {
            // '='
            parseCurToken(node, TokenType.ASSIGN);
            // InitVal
            node.addChild(parseInitVal());
        }
        node.setEndLine(this.line);
        return new VarDef(node);
    }

    private Node parseInitVal() {
        Node node = new Node(SyntaxType.INIT_VAL);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // '{' [InitVal {',' InitVal}]'}'
        if (tokenStream.getCurToken().getType() == TokenType.LBRACE) {
            // '{'
            parseCurToken(node, TokenType.LBRACE);
            if (tokenStream.getCurToken().getType() != TokenType.RBRACE) {
                // InitVal
                node.addChild(parseInitVal());
                // {',' InitVal}
                while (tokenStream.getCurToken().getType() == TokenType.COMMA) {
                    // ','
                    parseCurToken(node, TokenType.COMMA);
                    // InitVal
                    node.addChild(parseInitVal());
                }
            }
            // '}'
            parseCurToken(node, TokenType.RBRACE);
        }
        // Exp
        else {
            node.addChild(parseExp());
        }
        node.setEndLine(this.line);
        return new VarInitVal(node);
    }

    public Node parseConstDecl() {
        Node node = new Node(SyntaxType.CONST_DECL);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // 'const'
        parseCurToken(node, TokenType.CONSTTK);
        // 'BType'
        node.addChild(parseBType());
        // 'constDef'
        node.addChild(parseConstDef());
        // {',' ConstDef}
        while (tokenStream.getCurToken().getType() == TokenType.COMMA) {
            parseCurToken(node, TokenType.COMMA);
            node.addChild(parseConstDef());
        }
        // ';'
        parseCurToken(node, TokenType.SEMICN);
        node.setEndLine(this.line);
        return new ConstDecl(node);
    }

    private Node parseConstDef() {
        Node node = new Node(SyntaxType.CONST_DEF);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // ident
        parseCurToken(node, TokenType.IDENFR);
        // {'['  ConstExp  ']'}
        while (tokenStream.getCurToken().getType() == TokenType.LBRACK) {
            parseCurToken(node, TokenType.LBRACK);
            node.addChild(parseConstExp());
            parseCurToken(node, TokenType.RBRACK);
        }
        // '='
        parseCurToken(node, TokenType.ASSIGN);
        // ConstInitVal
        node.addChild(parseConstInitVal());
        node.setEndLine(this.line);
        return new ConstDef(node);
    }

    private Node parseConstInitVal() {
        Node node = new Node(SyntaxType.CONST_INIT_VAL);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if (tokenStream.getCurToken().getType() == TokenType.LBRACE) {
            parseCurToken(node, TokenType.LBRACE);
//            // [ConstInitVal { ',' ConstInitVal}]
            if (tokenStream.getCurToken().getType() != TokenType.RBRACE) {
                node.addChild(parseConstInitVal());
//              // { ',' ConstInitVal}
                while (tokenStream.getCurToken().getType() == TokenType.COMMA) {
                    parseCurToken(node, TokenType.COMMA);
                    node.addChild(parseConstInitVal());
                }
            }
            parseCurToken(node, TokenType.RBRACE);
        }
        // ConstExp
        else {
            node.addChild(parseConstExp());
        }
        node.setEndLine(this.line);
        return new ConstInitVal(node);
    }


    private Node parseBType() {
        Node node = new Node(SyntaxType.B_TYPE);
        node.setStartLine(tokenStream.getCurToken().getLine());
        parseCurToken(node, TokenType.INTTK);
        node.setEndLine(this.line);
        return new BType(node);
    }

    private Node parseConstExp() {
        Node node = new Node(SyntaxType.CONST_EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        node.addChild(parseAddExp());
        node.setEndLine(this.line);
        return new ConstExp(node);
    }

    private Node parseAddExp() {
        Node node = new Node(SyntaxType.ADD_EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        node.addChild(parseMulExp());
        // 一个AddExp的Children要么三个Node 要么一个Node
        while (tokenStream.getCurToken().getType() == TokenType.PLUS ||
                tokenStream.getCurToken().getType() == TokenType.MINU) {
            node.adjustExp(SyntaxType.ADD_EXP);
            if (tokenStream.getCurToken().getType() == TokenType.PLUS) {
                parseCurToken(node, TokenType.PLUS);
            } else if (tokenStream.getCurToken().getType() == TokenType.MINU) {
                parseCurToken(node, TokenType.MINU);
            }
            node.addChild(parseMulExp());
        }
        node.setEndLine(this.line);
        return new AddExp(node);
    }

    private Node parseMulExp() {
        Node node = new Node(SyntaxType.MUL_EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        node.addChild(parseUnaryExp());

        while (tokenStream.getCurToken().getType() == TokenType.MULT ||
                tokenStream.getCurToken().getType() == TokenType.DIV ||
                tokenStream.getCurToken().getType() == TokenType.MOD) {
            node.adjustExp(SyntaxType.MUL_EXP);
            if (tokenStream.getCurToken().getType() == TokenType.MULT) {
                parseCurToken(node, TokenType.MULT);
            } else if (tokenStream.getCurToken().getType() == TokenType.DIV) {
                parseCurToken(node, TokenType.DIV);
            } else if (tokenStream.getCurToken().getType() == TokenType.MOD) {
                parseCurToken(node, TokenType.MOD);
            }
            node.addChild(parseUnaryExp());
        }
        node.setEndLine(this.line);
        return new MulExp(node);
    }

    private Node parseUnaryExp() {
        Node node = new Node(SyntaxType.UNARY_EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        //  UnaryOp UnaryExp
        if (tokenStream.getCurToken().getType() == TokenType.PLUS ||
                tokenStream.getCurToken().getType() == TokenType.MINU ||
                tokenStream.getCurToken().getType() == TokenType.NOT) {
            node.addChild(parseUnaryOp());
            node.addChild(parseUnaryExp());
        }
        //  Ident '(' [FuncRParams] ')'
        else if (tokenStream.getCurToken().getType() == TokenType.IDENFR &&
                tokenStream.peek(1).getType() == TokenType.LPARENT) {
            parseCurToken(node, TokenType.IDENFR);
            parseCurToken(node, TokenType.LPARENT);
            // TODO 缺少 )
            TokenType type = tokenStream.getCurToken().getType();
            if (type == TokenType.PLUS || type == TokenType.MINU || type == TokenType.NOT ||
                    type == TokenType.IDENFR || type == TokenType.LPARENT || type == TokenType.INTCON) {
                node.addChild(parseFuncRParams());
            }
            parseCurToken(node, TokenType.RPARENT);
        }
        // PrimaryExp
        else {
            node.addChild(parsePrimaryExp());
        }
        node.setEndLine(this.line);
        return new UnaryExp(node);
    }

    private Node parseFuncRParams() {
        Node node = new Node(SyntaxType.FUNC_R_PARAMS);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // Exp
        node.addChild(parseExp());
        // { ',' Exp }
        while (tokenStream.getCurToken().getType() == TokenType.COMMA) {
            parseCurToken(node, TokenType.COMMA);
            node.addChild(parseExp());
        }
        node.setEndLine(this.line);
        return new FuncRParams(node);
    }

    private Node parsePrimaryExp() {
        Node node = new Node(SyntaxType.PRIMARY_EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        //  '(' Exp ')'
        if (tokenStream.getCurToken().getType() == TokenType.LPARENT) {
            parseCurToken(node, TokenType.LPARENT);
            node.addChild(parseExp());
            parseCurToken(node, TokenType.RPARENT);
        }
        // LVal
        else if (tokenStream.getCurToken().getType() == TokenType.IDENFR) {
            node.addChild(parseLVal());
        }
        // Number
        else {
            node.addChild(parseNumber());
        }
        node.setEndLine(this.line);
        return new PrimaryExp(node);
    }

    private Node parseNumber() {
        Node node = new Node(SyntaxType.NUMBER);
        node.setStartLine(tokenStream.getCurToken().getLine());
        parseCurToken(node, TokenType.INTCON);
        node.setEndLine(this.line);
        return new Number(node);
    }

    private Node parseLVal() {
        Node node = new Node(SyntaxType.LVAL);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // ident
        parseCurToken(node, TokenType.IDENFR);
        // {'[' Exp ']'}
        while (tokenStream.getCurToken().getType() == TokenType.LBRACK) {
            parseCurToken(node, TokenType.LBRACK);
            node.addChild(parseExp());
            parseCurToken(node, TokenType.RBRACK);
        }
        node.setEndLine(this.line);
        return new LVal(node);
    }

    private Node parseExp() {
        Node node = new Node(SyntaxType.EXP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        node.addChild(parseAddExp());
        node.setEndLine(this.line);
        return new Exp(node);
    }

    private Node parseUnaryOp() {
        Node node = new Node(SyntaxType.UNARY_OP);
        node.setStartLine(tokenStream.getCurToken().getLine());
        // parseUnaryExp()做了检查 此处不做检查
        //  '+' | '−' | '!'
        parseCurToken(node, null);
        node.setEndLine(this.line);
        return new UnaryOp(node);
    }


}
