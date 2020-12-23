package miniplc0java.analyser;

import miniplc0java.util.Ty;

public class Variable {
    public String name;// 其实全局栈式符号表应该就够了，不过存一下没坏处
    public boolean isConst;// 参数都看成普通变量
    public Ty type;
    public Object value;
    public int offset;// 局部变量在运行栈上的偏移

    public Variable(String name, boolean isConst, Ty type) {
        this.name = name;
        this.isConst = isConst;
        this.type = type;
//        this.value = value;
    }
}
