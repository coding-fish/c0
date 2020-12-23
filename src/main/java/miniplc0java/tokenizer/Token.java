package miniplc0java.tokenizer;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.TokenizeError;
import miniplc0java.util.Pos;
import miniplc0java.util.Ty;

import java.util.Objects;

public class Token {
    private TokenType tokenType;
    private Object value;
    private Pos startPos;
    private Pos endPos;

    public Token(TokenType tokenType, Object value, Pos startPos, Pos endPos) {
        this.tokenType = tokenType;
        this.value = value;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    // 绕过错误处理，不需要存储位置的构造方法
    public Token(TokenType tokenType, Object value) {
        this.tokenType = tokenType;
        this.value = value;
    }

    public Token(Token token) {
        this.tokenType = token.tokenType;
        this.value = token.value;
        this.startPos = token.startPos;
        this.endPos = token.endPos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Token token = (Token) o;
        return tokenType == token.tokenType && Objects.equals(value, token.value)
                && Objects.equals(startPos, token.startPos) && Objects.equals(endPos, token.endPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenType, value, startPos, endPos);
    }

    public String getValueString() {
        if (value instanceof Integer || value instanceof String || value instanceof Character) {
            return value.toString();
        }
        throw new Error("No suitable cast for token value.");
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public Ty getType() throws AnalyzeError {
        // ty -> IDENT
        // 调用这个的token都是IDENT（包括函数名）
        // 下面这三种是变量类型允许的所有范围，这三个不是关键字，因此他们的类似仍为IDENT
        // 需要从值中进行比较
        if (this.value.equals("int"))
            return Ty.UINT;
        else if (this.value.equals("double"))
            return Ty.DOUBLE;
        else if (this.value.equals("void"))
            return Ty.VOID;
            // 变量类型和函数的返回值类型应当仅限于上面三种
        else
            throw new AnalyzeError(ErrorCode.UnrecognizedType, this.startPos);
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Pos getStartPos() {
        return startPos;
    }

    public void setStartPos(Pos startPos) {
        this.startPos = startPos;
    }

    public Pos getEndPos() {
        return endPos;
    }

    public void setEndPos(Pos endPos) {
        this.endPos = endPos;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("Line: ").append(String.format("%3d", this.startPos.row)).append(' ');
        sb.append("Column: ").append(String.format("%3d", this.startPos.col)).append(' ');
        sb.append("Type: ").append(String.format("%8s", this.tokenType)).append(' ');
        sb.append("Value: ").append(this.value);
        return sb.toString();
    }

    public String toStringAlt() {
        return new StringBuilder().append("Token(").append(this.tokenType).append(", value: ").append(value)
                .append("at: ").append(this.startPos).toString();
    }

    public static TokenType tyToTokenType(Ty tt) {
        if (tt == Ty.UINT)
            return TokenType.UINT_LITERAL;
        // 只有变量声明通过类型检查后，才调用这个函数
        else if (tt == Ty.DOUBLE)
            return TokenType.DOUBLE_LITERAL;
        else
            return TokenType.STRING_LITERAL;
    }
}
