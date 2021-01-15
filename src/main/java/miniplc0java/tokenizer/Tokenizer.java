package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;
import miniplc0java.util.Pos;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        // peek 是下一个字符
        if (Character.isDigit(peek)) {
            return lexNum();
        } else if (Character.isAlphabetic(peek) || (peek == '_')) {
            return lexIdentOrKeyword();
        } else if (peek == '\"') {
            return lexString();
        } else if (peek == '\'') {
            return lexChar();
        } else if (peek == '/') {
            it.nextChar();
            if (it.peekChar() == '/') {
                // 跳过单行注释
                skipComment();
                return nextToken();
            } else {
                return new Token(TokenType.DIV, '/', it.currentPos(), it.currentPos());// 是除号诶！
            }
        } else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexNum() throws TokenizeError {
        StringBuilder digitStr = new StringBuilder();
        Pos startPos = it.currentPos();
        while (!it.isEOF() &&
                ( Character.isDigit(it.peekChar()) ||
                        it.peekChar() == '.' ||
                        it.peekChar() == 'e' ||
                        it.peekChar() == 'E') )
        {
            digitStr.append(it.nextChar());
        }
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        try {
            String s = digitStr.toString();
            if (s.contains(".")) {
                double digitD = Double.parseDouble(s);
                return new Token(TokenType.DOUBLE_LITERAL, digitD, startPos, it.currentPos());
            } else {
                // 默认无符号整数
                Integer digitI = Integer.valueOf(s);
                return new Token(TokenType.UINT_LITERAL, digitI, startPos, it.currentPos());
            }
        } catch (ArithmeticException e) {
            throw new TokenizeError(ErrorCode.IntegerOverflow, startPos);
        } catch (NumberFormatException e) {
            throw new TokenizeError(ErrorCode.InvalidInput, startPos);
        }
        // 双精度浮点数
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        StringBuilder ident = new StringBuilder();
        Pos startPos = it.currentPos();
        // 假设Character.isAlphabetic()不能识别下划线
        while (!it.isEOF() &&
                (Character.isDigit(it.peekChar()) || Character.isLetter(it.peekChar())
                        || it.peekChar() == '_')) {
            ident.append(it.nextChar());
        }
        String identStr = ident.toString();
        // -- 如果是关键字，则返回关键字类型的 token
        if (identStr.equals("fn"))
            return new Token(TokenType.FN_KW, "fn", startPos, it.currentPos());
        else if (identStr.equals("let"))
            return new Token(TokenType.LET_KW, "let", startPos, it.currentPos());
        else if (identStr.equals("const"))
            return new Token(TokenType.CONST_KW, "const", startPos, it.currentPos());
        else if (identStr.equals("as"))
            return new Token(TokenType.AS_KW, "as", startPos, it.currentPos());
        else if (identStr.equals("while"))
            return new Token(TokenType.WHILE_KW, "while", startPos, it.currentPos());
        else if (identStr.equals("if"))
            return new Token(TokenType.IF_KW, "if", startPos, it.currentPos());
        else if (identStr.equals("else"))
            return new Token(TokenType.ELSE_KW, "else", startPos, it.currentPos());
        else if (identStr.equals("return"))
            return new Token(TokenType.RETURN_KW, "return", startPos, it.currentPos());
        else if (identStr.equals("break"))
            return new Token(TokenType.BREAK_KW, "break", startPos, it.currentPos());
        else if (identStr.equals("continue"))
            return new Token(TokenType.CONTINUE_KW, "continue", startPos, it.currentPos());
            // -- 否则，返回标识符，包括int, void, double
        else
            return new Token(TokenType.IDENT, identStr, startPos, it.currentPos());
    }

    private Token lexString() throws TokenizeError{
        StringBuilder strLiteral = new StringBuilder();
        Pos startPos = it.currentPos();
        it.nextChar();
        while (!it.isEOF() && (it.peekChar() != '\"')) {
            // 转移字符按俩字符原样输出即可,如\n仍输出\n
//            System.out.println(Integer.valueOf(it.peekChar()).intValue());
            if (it.peekChar() == '\\') {
                it.nextChar();
                switch (it.peekChar()) {
                    case '\\': {
                        strLiteral.append('\\');
                        strLiteral.append('\\');
                        break;
                    }
                    case '\"': {
                        strLiteral.append('\\');
                        strLiteral.append('\"');
                        break;
                    }
                    case '\'': {
                        strLiteral.append('\\');
                        strLiteral.append('\'');
                        break;
                    }
                    case 'n': {
                        strLiteral.append('\\');
                        strLiteral.append('n');
                        break;
                    }
                    case 'r': {
                        strLiteral.append('\\');
                        strLiteral.append('r');
                        break;
                    }
                    case 't': {
                        strLiteral.append('\\');
                        strLiteral.append('t');
                        break;
                    }
                    default:
                        // 字符串里不允许 " 和 单独的\
                        throw new TokenizeError(ErrorCode.InvalidInput, startPos);
                }
                it.nextChar();
            }
            else
                strLiteral.append(it.nextChar());
        }
        if (it.peekChar() == '\"') {
            it.nextChar();
            return new Token(TokenType.STRING_LITERAL, strLiteral, startPos, it.currentPos());
        }
        else
            throw new TokenizeError(ErrorCode.InvalidInput, startPos);
    }

    private Token lexChar() throws TokenizeError {
        // 注意：字符字面量的类型是int！
        Pos startPos = it.currentPos();
        it.nextChar();
        if (it.peekChar() == '\\') {
            // 字符字面量必须为 '\[\"'nrt]'
            it.nextChar();
            char transformedChar = it.nextChar();
            if (it.nextChar() != '\'') {
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            }
            switch (transformedChar) {
                case '\\':
                    return new Token(TokenType.CHAR_LITERAL, (int)('\\'), startPos, it.currentPos());
                case '\"':
                    return new Token(TokenType.CHAR_LITERAL, (int)('\"'), startPos, it.currentPos());
                case '\'':
                    return new Token(TokenType.CHAR_LITERAL, (int)('\''), startPos, it.currentPos());
                case 'n':
                    return new Token(TokenType.CHAR_LITERAL, (int)('\n'), startPos, it.currentPos());
                case 'r':
                    return new Token(TokenType.CHAR_LITERAL, (int)('\r'), startPos, it.currentPos());
                case 't':
                    return new Token(TokenType.CHAR_LITERAL, (int)('\t'), startPos, it.currentPos());
                default:
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            }
        } else {
            // 字符字面量必须为 '[^'\\]
            // 字符字面量的类型是int
            int charLiteral = it.nextChar();
            if (it.nextChar() != '\'') {
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
            }
            return new Token(TokenType.CHAR_LITERAL, charLiteral, startPos, it.currentPos());
        }
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        Pos startPos = it.currentPos();
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', startPos, it.currentPos());
            case '-': {
                if (it.peekChar() == '>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", startPos, it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', startPos, it.currentPos());
            }
            case '*':
                return new Token(TokenType.MUL, '*', startPos, it.currentPos());
            case '/':
                return new Token(TokenType.DIV, '/', startPos, it.currentPos());
            case '=': {
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", startPos, it.currentPos());
                } else
                    return new Token(TokenType.ASSIGN, '=', startPos, it.currentPos());
            }
            case '!': {
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", startPos, it.currentPos());
                } else
                    throw new TokenizeError(ErrorCode.InvalidInput, startPos);
            }
            case '<': {
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", startPos, it.currentPos());
                } else
                    return new Token(TokenType.LT, '<', startPos, it.currentPos());
            }
            case '>': {
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", startPos, it.currentPos());
                } else
                    return new Token(TokenType.GT, '>', startPos, it.currentPos());
            }
            case '(':
                return new Token(TokenType.L_PAREN, '(', startPos, it.currentPos());
            case ')':
                return new Token(TokenType.R_PAREN, ')', startPos, it.currentPos());
            case '{':
                return new Token(TokenType.L_BRACE, '{', startPos, it.currentPos());
            case '}':
                return new Token(TokenType.R_BRACE, '}', startPos, it.currentPos());
            case ',':
                return new Token(TokenType.COMMA, ',', startPos, it.currentPos());
            case ':':
                return new Token(TokenType.COLON, ':', startPos, it.currentPos());
            case ';':
                return new Token(TokenType.SEMICOLON, ';', startPos, it.currentPos());
            default:
                // 不认识这个输入
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    private void skipComment() {
        while (it.peekChar() != '\n') {
            it.nextChar();
        }
    }
}
