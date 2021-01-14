package miniplc0java.analyser;

import miniplc0java.util.Ty;

public class SymbolEntry {
    String name;// 变量名
    Ty type;// 变量类型,或函数的返回值类型
    boolean isConstant;
    boolean isInitialized;
    int stackOffset;
    int depth;// 嵌套层次
    Object value;// 由于在o0中进行栈上赋值，因此先存起来
    int offset;// 对全局变量有意义

    public SymbolEntry(String name, Ty type, boolean isConstant, boolean isDeclared, int stackOffset, int depth) {
        this.name = name;
        this.type = type;
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;
        this.depth = depth;
        this.value = null;// 声明变量未赋值属于UB，因此不予赋初值
    }

    public String getName() {
        return this.name;
    }

    public Ty getType() {
        return this.type;
    }

    public int getDepth() {
        return this.depth;
    }

    public void setType(Ty type) {
        this.type = type;
    }

    /**
     * @return the stackOffset
     */
    public int getStackOffset() {
        return stackOffset;
    }

    /**
     * @return the isConstant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * @param isConstant the isConstant to set
     */
    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    /**
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized, Object value) {
        this.value = value;
        this.isInitialized = isInitialized;
    }

    /**
     * @param stackOffset the stackOffset to set
     */
    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }
}
