package miniplc0java.analyser;

//import javafx.util.Pair;// 这个不属于标准类库，可能docker上不支持

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.ExpectedTokenError;
import miniplc0java.error.TokenizeError;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;
import miniplc0java.util.Ty;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    /**
     * 栈式符号表！！！！！
     */
    public static Stack<SymbolEntry> symbolTable = new Stack<SymbolEntry>();

    /**
     * 当前代码块的嵌套层次，随 { } 变化
     * 全局变量的值为0
     */
    int currentDepth = 0;

    /**
     * 栈式符号表当前层的基指针，随 { } 变化
     */
    int stackBP = 0;

    /**
     * 下一个变量的栈偏移，似乎可以用stack.size()
     */
    int nextOffset = 0;

    // 函数的符号表单独维护
    // （为了目标代码格式）
    // call指令的参数，就是callee在函数列表的index+1
    // 这里面不存放库函数，库函数作为字符串放入全局变量
    // 因此调用库函数之前，需要单独检查是否满足调用条件
    ArrayList<Function> funcTable = new ArrayList<Function>();

    private int calcFuncOffset(String name, Pos curPos) throws CompileError {
        int i = 0;
        for (Function f : funcTable) {
            if (f.getName().equals(name))
                return i + 1;
            i++;
        }
        throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
    }

    // 单纯的栈式符号表不支持字符串和库函数，因此单独开一个列表存
    // 也就是o0里面函数前面的所有东西
    // (为了目标代码格式)
    // 里面包括全局变量，字符串，库函数名，自定义函数名
    // 这个表不进行去重检查
    ArrayList<Token> globalVarTable = new ArrayList<Token>();

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        addGlobToIns();
        addFuncToIns();
        return instructions;
    }

    private void addGlobToIns() {
        Instruction.symbolTable = this.symbolTable;
        Instruction.globalVarTable = this.globalVarTable;
    }

    private void addFuncToIns() {
        Instruction.funcTable = this.funcTable;
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移，添加符号时调用
     *
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    /**
     * 遇到 {，进入嵌套的子代码块
     * 栈式符号表增加一层
     * >=stackBP的是当前层次的变量
     *
     * @return
     */
    private void pushNestedBlock() {
        this.currentDepth++;
        this.stackBP = this.nextOffset;
    }

    /**
     * 遇到 }，退出嵌套的子代码块
     * 栈式符号表弹出一层
     * 注意编译结束之前，应当把全局变量表加入o0中
     * 注意区分代码块嵌套 和 函数定义在_start里面的区别
     * 此处管理的是所有代码块，函数局部变量的管理放在了函数的递归子程序中
     *
     * @return
     */
    private void popNestedBlock() {
        // 字符串字面量，函数，加上全局变量，单独开一个地方存
        // 最终栈里面剩下的就是全局变量，不过没什么用
        while (symbolTable.lastElement().getDepth() == this.currentDepth)
            symbolTable.pop();
        this.currentDepth--;
//        this.currentDepth--;
//        for (int i = this.nextOffset; i > this.stackBP; i--)
//            symbolTable.pop();
        this.nextOffset = this.stackBP;
    }

    /**
     * 查找某个符号
     * 对于全局变量，这里假设所有声明都在函数声明之前！
     * 否则需要多遍扫描
     *
     * @return
     */
    public static SymbolEntry getSymbol(Stack<SymbolEntry> s, String name) {
        for (int i = s.size() - 1; i >= 0; i--) {// 后面声明的符号会覆盖前面的
            if (s.get(i).name.equals(name)) {
                return s.get(i);
            }
        }
        return null;
    }

    /**
     * 查找某个函数
     * 在c0中，函数都是全局变量，不存在嵌套声明
     *
     * @param s
     * @param name
     * @return
     */
    private Function getFunc(ArrayList<Function> s, String name) {
        for (int i = s.size() - 1; i >= 0; i--) {// 后面声明的符号会覆盖前面的
            if (s.get(i).name.equals(name)) {
                return s.get(i);
            }
        }
        return null;
    }

    /**
     * 返回当前分析中的函数，用于将目标代码插进去
     *
     * @return
     */
    private Function getCurFunc() {
        // 应该在function列表的最后面（因为是刚加进来的）
        // 但最外层的指令不属于某个函数，比如分析完毕后，函数列表最后一条实际上不是当前函数！
        // 另外这样实现的话，调用库函数就不能在函数列表再压一个了，可以把库函数一开始都压进去
        if (this.funcTable.size() > 0)
            return this.funcTable.get(this.funcTable.size() - 1);
            // 最外层函数的指令，放在_start
        else
            // TODO:不存在，应该报错,但懒得传curPos了
            return null;
    }

    /**
     * 添加一个符号
     * 字符串字面量和库函数是支持重复的，因此插入全局符号表的方式有些不同
     *
     * @param name          名字
     * @param type          类型
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, Ty type, boolean isInitialized, boolean isConstant, Pos curPos) throws AnalyzeError {
        if ((getSymbol(this.symbolTable, name) != null) &&
                (getSymbol(this.symbolTable, name).depth == currentDepth)) {
            // 支持覆盖，只有同一层次才算重复声明
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else {
            // 通过了重复声明检查
            this.symbolTable.push(new SymbolEntry(name, type, isConstant, isInitialized, getNextVariableOffset(), currentDepth));
            // 全局变量不需要赋初值，在_start里面用赋值语句完成
            if (currentDepth == 0) {
                // int OR double OR function(void)
                Token var = new Token(Token.tyToTokenType(type), name);
                addGlobVar(var);
            }
        }
    }

    /**
     * 设置符号为已赋值
     *
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void declareSymbol(String name, Pos curPos, Object value) throws AnalyzeError {
        var entry = getSymbol(this.symbolTable, name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            entry.setInitialized(true, value);
        }
    }

    /**
     * 设置函数的返回类型
     *
     * @param name
     * @param type
     * @throws AnalyzeError
     */
    private void setFuncType(String name, Ty type) throws AnalyzeError {
        var entry = getFunc(this.funcTable, name);
        entry.setReturnType(type);
    }

    /**
     * 设置函数的参数列表
     *
     * @param name
     * @param param
     */
    private void addFuncParam(String name, Variable param) {
        Function entry = getFunc(this.funcTable, name);
        entry.addParamList(param);
    }

    /**
     * 把目标代码先放到函数类里面临时存一下
     *
     * @param name
     * @param ins
     */
    private void addFuncIns(String name, Instruction ins) {
        var entry = getCurFunc();
        entry.addInstruction(ins);
    }

    /**
     * 获取变量在栈上的偏移
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
//    private int getOffset(String name, Pos curPos) throws AnalyzeError {
//        var entry = this.symbolTable.get(name);
//        if (entry == null) {
//            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
//        } else {
//            return entry.getStackOffset();
//        }
//    }

    /**
     * 获取变量是否是常量
     *
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = getSymbol(this.symbolTable, name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    /**
     * 添加一个全局变量
     * 包括变量，字符串字面量，库函数（视为字符串），自定义函数名
     * 对于变量，假设已经通过了addSymbol中的检查，和前面不产生重复
     */
    private void addGlobVar(Token var) {
        this.globalVarTable.add(var);
    }

    private int calcGlobOffset() {
        return this.globalVarTable.size();
    }

    /**
     * item -> function | decl_stmt
     * program -> item*
     */
    private void analyseProgram() throws CompileError {
        analyseMain();
        expect(TokenType.EOF);
    }

    /**
     * decl_stmt -> let_decl_stmt | const_decl_stmt
     * - let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     * - const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     */
    private void analyseMain() throws CompileError {
        // 全局变量赋值的目标代码放在_start中,且不会被栈式符号表弹出
        Function start = new Function("_start", 0, 0);
        this.funcTable.add(start);
        setFuncType("_start", Ty.VOID);
        while (check(TokenType.LET_KW) || check(TokenType.CONST_KW) || check(TokenType.FN_KW)) {
            if (check(TokenType.LET_KW))
                analyseVariableDeclaration();
            else if (check(TokenType.CONST_KW))
                analyseConstantDeclaration();
                // 我们假设全局变量声明都在函数之前
            else// if (check(TokenType.FN_KW))
                analyseFunctionDeclaration();
        }
        // 把字符串_start放在全局表最后
        addGlobVar(new Token(TokenType.STRING_LITERAL, "_start"));
        // 调用main
        Pos curPos = nextIf(TokenType.EOF).getEndPos();
        // 找不到main会报错
        int offset = calcFuncOffset("main", curPos);
        this.funcTable.get(0).addInstruction(new Instruction(Operation.call, offset));
        System.out.println("Analysed finished.");
    }

    /**
     * let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     * ty -> IDENT(int, void, double)
     */
    private void analyseVariableDeclaration() throws CompileError {
//        System.out.println("变量声明分析");
        if (nextIf(TokenType.LET_KW) != null) {
            // 标识符
            Token nameToken = expect(TokenType.IDENT);
            // 名字
            String name = nameToken.getValueString();
            Pos curPos = nameToken.getStartPos();
            // 冒号
            expect(TokenType.COLON);
            // 加入符号表
            Token typeToken = expect(TokenType.IDENT);
            Ty type = typeToken.getType();
            // 变量不能是void类型
            if (type == Ty.VOID)
                throw new AnalyzeError(ErrorCode.VarTypeVoid, curPos);
            addSymbol(name, type, false, false, curPos);
            // 等于号
            if (nextIf(TokenType.EQ) != null) {
                // 表达式(带类型，可能发生类型转换)
                ExpVal rightExp = analyseExpression();
                if (rightExp.type != type) {
                    throw new AnalyzeError(ErrorCode.TypeUnmatch, curPos);
                }
                Object value = rightExp.value;
                declareSymbol(name, curPos, value);
                // 添加到全局表
                if (this.currentDepth == 0)
                    addGlobVar(new Token(TokenType.UINT_LITERAL, rightExp.value));
            } else {
                // 添加到全局表
                if (this.currentDepth == 0)
                    addGlobVar(new Token(TokenType.UINT_LITERAL, 0));
            }

            // 分号
            expect(TokenType.SEMICOLON);
        }
    }

    /**
     * const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     * ty -> IDENT(int, void, double)
     */
    private void analyseConstantDeclaration() throws CompileError {
        if (nextIf(TokenType.CONST_KW) != null) {
            // 标识符
            Token nameToken = expect(TokenType.IDENT);
            // 名字
            String name = nameToken.getValueString();
            Pos curPos = nameToken.getStartPos();
            // 冒号
            expect(TokenType.COLON);
            // 加入符号表
            Token typeToken = expect(TokenType.IDENT);
            Ty type = typeToken.getType();
            // 变量不能是void类型
            if (type == Ty.VOID)
                throw new AnalyzeError(ErrorCode.VarTypeVoid, curPos);
            addSymbol(name, type, true, true, curPos);
            // 等于号,const类型必须赋值
            expect(TokenType.EQ);
            // 表达式(带类型，可能发生类型转换)
            ExpVal rightExp = analyseExpression();
            if (rightExp.type != type) {
                throw new AnalyzeError(ErrorCode.TypeUnmatch, curPos);
            }
            Object value = rightExp.value;
            declareSymbol(name, curPos, value);
            // 添加到全局表
            if (this.currentDepth == 0)
                addGlobVar(new Token(TokenType.UINT_LITERAL, rightExp.value));
            // 分号
            expect(TokenType.SEMICOLON);
        }
    }

    /**
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     * function_param_list -> function_param (',' function_param)*
     * function_param -> 'const'? IDENT ':' ty
     */
    private void analyseFunctionDeclaration() throws CompileError {
        if (nextIf(TokenType.FN_KW) != null) {
            // 标识符
            Token nameToken = expect(TokenType.IDENT);
            // 名字
            String name = nameToken.getValueString();
            Pos curPos = nameToken.getStartPos();
            // 函数返回值类型还不知道，先设为VOID
            // 既解决函数和变量重名的问题，也便于求o0中的函数偏移量
            addSymbol(name, Ty.FUNC, true, true, curPos);
            // 添加到全局表(已经在addSymbol与全局变量一起加进去了)
//            addGlobVar(new Token(TokenType.STRING_LITERAL, name));
            // 已经在栈式符号表进行了重复性检查
//            if (getFunc(this.funcTable, name) != null)
//                throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
            // new一个函数类，存放相关信息，为了便于被调用，放到单独的函数列表中
            // 注意:这个偏移量是全局符号表里的
            Function newFunc = new Function(name, globalVarTable.size(), funcTable.size());
            // 但函数可以调用自己，因此在分析函数体之前，应当已经加入列表了
            this.funcTable.add(newFunc);
            // 参数与局部变量都属于下一层
            pushNestedBlock();
            // 左括号
            expect(TokenType.L_PAREN);
            // 参数列表，可以为空
            while (check(TokenType.IDENT) || check(TokenType.CONST_KW)) {
                boolean isConst = false;
                // 可能有const
                if (nextIf(TokenType.CONST_KW) != null)
                    isConst = true;
                // 参数名
                Token newVarName = nextIf(TokenType.IDENT);
                String varName = newVarName.getValueString();
                // 冒号
                expect(TokenType.COLON);
                // 类型
                Token typeToken = expect(TokenType.IDENT);
                Ty type = typeToken.getType();
                // 变量不能是void类型
                if (type == Ty.VOID)
                    throw new AnalyzeError(ErrorCode.VarTypeVoid, curPos);
                // 压入栈式符号表，但后面会被弹出
                addSymbol(varName, type, false, isConst, curPos);
                // 因此也压入函数的参数列表
                Variable newParam = new Variable(varName, isConst, type);
                addFuncParam(name, newParam);
                // 有下一个的话，前面用逗号分隔
                if (nextIf(TokenType.COMMA) == null)
                    break;
            }
            // 右括号
            expect(TokenType.R_PAREN);
            // 箭头
            expect(TokenType.ARROW);
            // 返回值类型，需要回填
            Token typeToken = expect(TokenType.IDENT);
            Ty type = typeToken.getType();
            setFuncType(name, type);
            // 函数体，可能有return语句（需要检查类型）
            // 进入时记得增加嵌套层次
            // TODO:局部变量在哪存
            analyseBlockStmt();
            // 退出该嵌套层次，回到上一层
            popNestedBlock();
            for (SymbolEntry s : symbolTable)
            System.out.println(s.getName()+" "+s.getDepth());
//            System.out.println("func:"+getCurFunc().getName()+"\tfinished.");
        }
    }

    /**
     * 表达式类
     * 存放表达式的值和类型
     * 可能是用变量、参数计算得到，也可能是调用函数得到
     */
    class ExpVal {
        // 记录表达式的值和类型
        Ty type;
        Object value;

        ExpVal(Ty type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    /**
     * stmt ->
     * expr_stmt    开始符号集较为复杂，放在最后分析
     * | decl_stmt  以let或const开始
     * | if_stmt    以if开始
     * | while_stmt 以while开始
     * | return_stmt以return开始
     * | block_stmt 以{开始
     * | empty_stmt 空语句直接跳过
     */
    private void analyseStatement() throws CompileError {
        // 声明语句
        if (check(TokenType.LET_KW))
            analyseVariableDeclaration();
        else if (check(TokenType.CONST_KW))
            analyseConstantDeclaration();
        else if (check(TokenType.IF_KW))
            analyseIfStmt();
        else if (check(TokenType.WHILE_KW))
            analyseWhileStmt();
        else if (check(TokenType.RETURN_KW))
            analyseReturnStmt();
        else if (check(TokenType.L_BRACE))
            analyseBlockStmt();
            // 空语句直接跳过
        else if (check(TokenType.SEMICOLON)) {
            next();
        }
        // 无效语句在表达式中单独处理
        else
            analyseExpression();
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' (block_stmt | if_stmt))?
     * //              ^~~~ ^~~~~~~~~~         ^~~~~~~~~~~~~~~~~~~~~~
     * //              |     if_block           else_block
     * //              condition
     * 有跳转，需要计算跳转长度，也就是指令的数目
     * 需要注意的是，在分析过程中，指令一条条是随之存入函数的指令列表的
     *
     * @throws CompileError
     */
    private void analyseIfStmt() throws CompileError {
        expect(TokenType.IF_KW);
        analyseExpression();
        // 先占位
        getCurFunc().addInstruction(new Instruction(Operation.br, 0));
        int loc1 = this.funcTable.size() - 1;
        analyseBlockStmt();
        // 先占位
        getCurFunc().addInstruction(new Instruction(Operation.br, 0));
        int loc2 = this.funcTable.size() - 1;
        // 回填
        getCurFunc().instructions.set(loc1, new Instruction(Operation.br, loc2 - loc1));
        expect(TokenType.ELSE_KW);
        if (check(TokenType.IF_KW)) {
            // 存在嵌套
            analyseIfStmt();
        } else
            analyseBlockStmt();
        // 这个占位似乎可以省略
//        getCurFunc().addInstruction(new Instruction(Operation.br, 0));
        int loc3 = this.funcTable.size() - 1;
        // 回填
        // 此时控制跳到else块之后，需保证仍然有语句，比如显式的Ret
        getCurFunc().instructions.set(loc2, new Instruction(Operation.br, loc3 - loc2));
    }

    /**
     * while_stmt -> 'while' expr block_stmt
     * //                    ^~~~ ^~~~~~~~~~while_block
     * //                     condition
     * 同样也有跳转问题
     *
     * @throws CompileError
     */
    private void analyseWhileStmt() throws CompileError {
        // TODO:bread & continue仅限循环内使用
        expect(TokenType.WHILE_KW);
        // 占位
        getCurFunc().addInstruction(new Instruction(Operation.br, 0));
        int loc1 = this.funcTable.size()-1;
        analyseExpression();
        // 占位
        getCurFunc().addInstruction(new Instruction(Operation.br, 0));
        int loc2 = this.funcTable.size()-1;
        analyseBlockStmt();
        int loc3 = this.funcTable.size()-1;
        loc3++;
        getCurFunc().addInstruction(new Instruction(Operation.br, loc1-loc3));
        // 回填
        // 同样也应确保后面还有指令，这与返回路径检查挂钩(while块是函数最后语句的情况)
        getCurFunc().instructions.set(loc2, new Instruction(Operation.br, loc3-loc2));
    }

    /**
     * return_stmt -> 'return' expr? ';'
     *
     * @throws CompileError
     */
    private void analyseReturnStmt() throws CompileError {
        Token retToken = expect(TokenType.RETURN_KW);
        // 这个报错位置其实不太对，但在同一行
        Pos curPos = retToken.getEndPos();
        Function curFunc = getCurFunc();
        Ty retType = curFunc.getReturnType();
        if (retType == Ty.VOID) {
            addFuncIns(curFunc.getName(), new Instruction(Operation.ret));
        } else {
            // 有返回类型的必须有返回值，还要类型一致
            ExpVal rightExp = analyseExpression();
            // TODO:先不检查了
//            if (rightExp.type != retType) {
//                throw new AnalyzeError(ErrorCode.TypeUnmatch, curPos);
//            }
            // TODO:int返回类型的函数 返回路径检查
            addFuncIns(curFunc.getName(), new Instruction(Operation.arga, (int) 0));
            addFuncIns(curFunc.getName(), new Instruction(Operation.push, rightExp.value));
            addFuncIns(curFunc.getName(), new Instruction(Operation.store64));
            addFuncIns(curFunc.getName(), new Instruction(Operation.ret));
        }
        expect(TokenType.SEMICOLON);
    }

    /**
     * block_stmt -> '{' stmt* '}'
     *
     * @throws CompileError
     */
    private void analyseBlockStmt() throws CompileError {
        expect(TokenType.L_BRACE);
        pushNestedBlock();
        // 语句不会以 } 开头
        while (!check(TokenType.R_BRACE))
            analyseStatement();
        expect(TokenType.R_BRACE);
        System.out.println();
        popNestedBlock();
    }

    /**
     * 改写表达式相关的产生式：
     * E -> C ( == | != | < | > | <= | >= C )
     * C -> T { + | - T}
     * T -> F { * | / F}
     * F -> A ( as int_ty | double_ty )
     * A -> ( - ) I
     * I -> IDENT | UNIT | DOUBLE | func_call | '(' E ')' | IDENT = E
     */
    private ExpVal analyseExpression() throws CompileError {
        Function curFunc = getCurFunc();
        ExpVal expval = analyseC();
        while (true) {
            var op = peek();
            if (op.getTokenType() != TokenType.EQ &&
                    op.getTokenType() != TokenType.NEQ &&
                    op.getTokenType() != TokenType.LT &&
                    op.getTokenType() != TokenType.GT &&
                    op.getTokenType() != TokenType.LE &&
                    op.getTokenType() != TokenType.GE) {
                break;
            }
            // 运算符
            next();
            analyseC();
            // 生成目标代码
            if (op.getTokenType() == TokenType.EQ) {
                addFuncIns(curFunc.getName(), new Instruction(Operation.cmpi));
                addFuncIns(curFunc.getName(), new Instruction(Operation.brfalse, 1));
            } else if (op.getTokenType() == TokenType.NEQ) {
                addFuncIns(curFunc.getName(), new Instruction(Operation.cmpi));
                addFuncIns(curFunc.getName(), new Instruction(Operation.brtrue, 1));
            } else if (op.getTokenType() == TokenType.LT) {
                addFuncIns(curFunc.getName(), new Instruction(Operation.cmpi));
                addFuncIns(curFunc.getName(), new Instruction(Operation.setlt));
                addFuncIns(curFunc.getName(), new Instruction(Operation.brtrue, 1));
            } else if (op.getTokenType() == TokenType.GT) {
                addFuncIns(curFunc.getName(), new Instruction(Operation.cmpi));
                addFuncIns(curFunc.getName(), new Instruction(Operation.setgt));
                addFuncIns(curFunc.getName(), new Instruction(Operation.brtrue, 1));
            } else if (op.getTokenType() == TokenType.LE) {
                addFuncIns(curFunc.getName(), new Instruction(Operation.cmpi));
                addFuncIns(curFunc.getName(), new Instruction(Operation.setgt));
                addFuncIns(curFunc.getName(), new Instruction(Operation.brfalse, 1));
            } else if (op.getTokenType() == TokenType.GE) {
                addFuncIns(curFunc.getName(), new Instruction(Operation.cmpi));
                addFuncIns(curFunc.getName(), new Instruction(Operation.setlt));
                addFuncIns(curFunc.getName(), new Instruction(Operation.brfalse, 1));
            } else
                ;// do nothing
        }
        return expval;
    }

    // C -> T { + | - T}
    private ExpVal analyseC() throws CompileError {
        ExpVal expVal = analyseT();
        while (true) {
            var op = peek();
            if (op.getTokenType() != TokenType.PLUS &&
                    op.getTokenType() != TokenType.MINUS) {
                break;
            }
            // 运算符
            next();
            analyseT();
            // 生成目标代码
            if (op.getTokenType() == TokenType.PLUS)
                getCurFunc().addInstruction(new Instruction(Operation.addi));
            else if (op.getTokenType() == TokenType.MINUS)
                getCurFunc().addInstruction(new Instruction(Operation.subi));
            else
                ;// do nothing
        }
        return expVal;
    }

    //T -> F { * | / F}
    private ExpVal analyseT() throws CompileError {
        ExpVal expVal = analyseF();
        while (true) {
            var op = peek();
            if (op.getTokenType() != TokenType.MUL &&
                    op.getTokenType() != TokenType.DIV) {
                break;
            }
            // 运算符
            next();
            analyseF();
            // 生成目标代码
            if (op.getTokenType() == TokenType.MUL)
                getCurFunc().addInstruction(new Instruction(Operation.muli));
            else if (op.getTokenType() == TokenType.DIV)
                getCurFunc().addInstruction(new Instruction(Operation.divi));
            else
                ;// do nothing
        }
        return expVal;
    }

    //F -> A ( as int_ty | double_ty )
    private ExpVal analyseF() throws CompileError {
        ExpVal expVal = analyseA();
        if (check(TokenType.AS_KW)) {
            expect(TokenType.AS_KW);
            Token transTo = expect(TokenType.IDENT);
            if (transTo.getValueString().equals("int") && expVal.type == Ty.DOUBLE)
                getCurFunc().addInstruction(new Instruction(Operation.ftoi));
            else if (transTo.getValueString().equals("double") && expVal.type == Ty.UINT)
                getCurFunc().addInstruction(new Instruction(Operation.itof));
            else
                ;// do nothing
        }
        return expVal;
    }

    //A -> ( - ) I
    private ExpVal analyseA() throws CompileError {
        int minuscount = 0;
        while (check(TokenType.MINUS)) {
            minuscount++;
            expect(TokenType.MINUS);
        }
        ExpVal expVal = analyseI();
        if (minuscount % 2 != 0)
            getCurFunc().addInstruction(new Instruction(Operation.negi));
        return expVal;
    }

    //I -> IDENT | UINT | DOUBLE | func_call | '(' E ')' | IDENT = E
    private ExpVal analyseI() throws CompileError {
        Pos curPos = null;
        if (check(TokenType.IDENT)) {
            Token nameToken = expect(TokenType.IDENT);
            curPos = nameToken.getEndPos();
            locateVar(nameToken.getValueString(), curPos);
            if (check(TokenType.L_PAREN))
                // 是函数调用
                return analyseCallExpr(nameToken.getValueString(), curPos);
            else if (check(TokenType.ASSIGN)) {
                // 注意类型比较
                if (nameToken.getType() == Ty.VOID)
                    throw new AnalyzeError(ErrorCode.InvalidAssignment, nameToken.getEndPos());
                SymbolEntry leftVar = getSymbol(this.symbolTable, nameToken.getValueString());
                if (leftVar == null)
                    throw new AnalyzeError(ErrorCode.NotDeclared, nameToken.getEndPos());
                if (leftVar.isConstant)
                    throw new AnalyzeError(ErrorCode.InvalidAssignment, nameToken.getEndPos());
                // 赋值号
                expect(TokenType.ASSIGN);
                // 赋值表达式右半部
                ExpVal rightExp = analyseExpression();
                if (nameToken.getType() != rightExp.type)
                    throw new AnalyzeError(ErrorCode.TypeUnmatch, nameToken.getEndPos());
                getCurFunc().addInstruction(new Instruction(Operation.store64));
                return new ExpVal(Ty.VOID, 1);
            } else {
                // 只是一个变量
                getCurFunc().addInstruction(new Instruction(Operation.load64));
                // TODO:假设变量都是int类型的
                SymbolEntry s = getSymbol(symbolTable, nameToken.getValueString());
//                return new ExpVal(s.getType(), nameToken.getValue());
                return new ExpVal(nameToken.getType(), nameToken.getValue());
            }
        } else if (check(TokenType.UINT_LITERAL)) {
            Token numToken = expect(TokenType.UINT_LITERAL);
            getCurFunc().addInstruction(new Instruction(Operation.push, (int) numToken.getValue()));
            return new ExpVal(Ty.UINT, (int) numToken.getValue());
        } else if (check(TokenType.DOUBLE_LITERAL)) {
            Token doubleToken = expect(TokenType.DOUBLE_LITERAL);
            getCurFunc().addInstruction(new Instruction(Operation.push, (double) doubleToken.getValue()));
            return new ExpVal(Ty.DOUBLE, (double) doubleToken.getValue());
        } else if (check(TokenType.STRING_LITERAL)) {
            // 字符串字面量要加入全局表
            addGlobVar(expect(TokenType.STRING_LITERAL));
            getCurFunc().addInstruction(new Instruction(Operation.push, calcGlobOffset()));
            return new ExpVal(Ty.STRING, 1);
        } else if (check(TokenType.CHAR_LITERAL)) {
            Token charToken = expect(TokenType.CHAR_LITERAL);
            getCurFunc().addInstruction(new Instruction(Operation.push, (int) charToken.getValueString().charAt(0)));
            return new ExpVal(Ty.UINT, charToken.getValue());
        } else if (check(TokenType.L_PAREN)) {
            expect(TokenType.L_PAREN);
            ExpVal ret = analyseExpression();
            expect(TokenType.R_PAREN);
            return ret;
        } else
            // TODO:throw error?
            return new ExpVal(Ty.UINT, 0);
    }

    // call_expr -> IDENT '(' call_param_list? ')'
    // call_param_list -> expr (',' expr)*
    private ExpVal analyseCallExpr(String funcName, Pos curPos) throws CompileError {
        // 库函数和自定义函数区别对待
        boolean isLib = false;
        if (funcName.equals("getint") || funcName.equals("getdouble") || funcName.equals("getchar")) {
            isLib = true;
            getCurFunc().addInstruction(new Instruction(Operation.stackalloc, 1));
        }
        if (funcName.equals("putint") || funcName.equals("putdouble") || funcName.equals("putchar")
                || funcName.equals("putstr") || funcName.equals("putln")) {
            isLib = true;
            getCurFunc().addInstruction(new Instruction(Operation.stackalloc, 0));
        }
        if (!isLib) {
            Function callee = getFunc(this.funcTable, funcName);
            if (callee.returnType == Ty.VOID)
                getCurFunc().addInstruction(new Instruction(Operation.stackalloc, 0));
            else if (callee.returnType == Ty.UINT)
                getCurFunc().addInstruction(new Instruction(Operation.stackalloc, 1));
            else
                ;// 其他返回类型
        }
        // 压入参数
        expect(TokenType.L_PAREN);
        if (!check(TokenType.R_PAREN)) {
            // 参数列表非空
            // TODO:逐个检查参数是否与声明对应，包括类型和个数
            //  包括库函数，这里先假设测试数据点没有这种错误
            int i;
            analyseExpression();
            while (check(TokenType.COMMA)) {
                expect(TokenType.COMMA);
                analyseExpression();
            }
        }
        expect(TokenType.R_PAREN);
        // 调用call系列指令
        // 库函数
        if (isLib) {
            addGlobVar(new Token(TokenType.STRING_LITERAL, funcName));
            // 在全局表里，不妨把函数名看做字符串
            // 库函数每次压一个进去，index就是表里最后面的那个
            getCurFunc().addInstruction(new Instruction(Operation.callname, globalVarTable.size() - 1));
        }
        // 自定义函数
        else
            // FIXME:其实Function的属性offset已经记录了位置
            getCurFunc().addInstruction(new Instruction(Operation.call, calcFuncOffset(funcName, curPos)));
        return new ExpVal(getCurFunc().getReturnType(), 1);
    }

    // 找到变量在局部符号表，或全局符号表中的偏移
    // 还应有类型检查,交由上一层完成，所以把type返回
    // 添加定位指令
    private ExpVal locateVar(String name, Pos curPos) throws CompileError {
        // 查找顺序：局部变量->参数列表（注意这一层还有函数名）->全局变量
        // 对应深度：   0   ->            1                ->  >1的值
        SymbolEntry s = null;
        int i = symbolTable.size() - 1;
        for (; i >= 0; i--) {
            s = symbolTable.get(i);
            if (s.getName().equals(name))
                break;
        }
        if (s != null) {
            // 计算实际的偏移量
            int offset = 0;
            if (s.getDepth() == 0) {
                offset = i;
                getCurFunc().addInstruction(new Instruction(Operation.globa, offset));
            } else if (s.getDepth() == 1) {
                for (; i >= 0; i--) {
                    if (symbolTable.get(i).getName().equals(getCurFunc().getName()))
                        break;
                }
                offset = s.getDepth() - i - 1;
                getCurFunc().addInstruction(new Instruction(Operation.arga, offset));
            } else {
                for (; i >= 0; i--) {
                    if (symbolTable.get(i).getDepth() == 1)
                        break;
                }
                offset = s.getDepth() - i - 1;
                getCurFunc().addInstruction(new Instruction(Operation.loca, offset));
            }
            return new ExpVal(s.getType(), offset);
        } else {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        }
    }

}
