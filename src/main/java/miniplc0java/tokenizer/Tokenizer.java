package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;
import miniplc0java.error.ErrorCode;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
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
        if (Character.isDigit(peek)) {
            return lexUInt();
        } else if (Character.isAlphabetic(peek)) {
            return lexIdentOrKeyword();
        } else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexUInt() throws TokenizeError {
        StringBuilder digitStr = new StringBuilder();
        while (!it.isEOF() && Character.isDigit(it.peekChar())) {
            digitStr.append(it.nextChar());
        }
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        try {
            String s = digitStr.toString();
            Integer digit = Integer.valueOf(s);
            // Token 的 Value 应填写数字的值
            return new Token(TokenType.Uint, digit, it.previousPos(), it.currentPos());
        }
        catch(NumberFormatException e){
            throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        StringBuilder ident = new StringBuilder();
        while (!it.isEOF() && Character.isAlphabetic(it.peekChar())) {
            ident.append(it.nextChar());
        }
        String identStr = ident.toString();
        // -- 如果是关键字，则返回关键字类型的 token
        if (identStr.equals("begin"))
            return new Token(TokenType.Begin, "begin", it.previousPos(), it.currentPos());
        else if (identStr.equals("end"))
            return new Token(TokenType.End, "end", it.previousPos(), it.currentPos());
        else if (identStr.equals("var"))
            return new Token(TokenType.Var, "var", it.previousPos(), it.currentPos());
        else if (identStr.equals("const"))
            return new Token(TokenType.Const, "const", it.previousPos(), it.currentPos());
        else if (identStr.equals("print"))
            return new Token(TokenType.Print, "print", it.previousPos(), it.currentPos());
        // -- 否则，返回标识符
        else
            return new Token(TokenType.Ident, identStr, it.previousPos(), it.currentPos());
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.Plus, '+', it.previousPos(), it.currentPos());

            case '-':
                return new Token(TokenType.Minus, '-', it.previousPos(), it.currentPos());

            case '*':
                return new Token(TokenType.Mult, '*', it.previousPos(), it.currentPos());

            case '/':
                return new Token(TokenType.Div, '/', it.previousPos(), it.currentPos());

            case '=':
                return new Token(TokenType.Equal, '=', it.previousPos(), it.currentPos());

            case ';':
                return new Token(TokenType.Semicolon, ';', it.previousPos(), it.currentPos());

            case '(':
                return new Token(TokenType.LParen, '(', it.previousPos(), it.currentPos());

            case ')':
                return new Token(TokenType.RParen, ')', it.previousPos(), it.currentPos());

            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
