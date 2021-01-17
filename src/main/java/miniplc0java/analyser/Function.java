package miniplc0java.analyser;

import miniplc0java.instruction.Instruction;
import miniplc0java.util.Ty;
import miniplc0java.analyser.Variable;

import java.util.ArrayList;
import java.util.Stack;

public class Function {
    // 8个库函数可以在所有其他函数之前，放在目标代码中
    public String name;// 用于编译过程中在函数列表中检索
    public Ty returnType;// 可以是void
    public ArrayList<Variable> paramList = new ArrayList<>();// 参数列表
    // 函数的局部变量放多少个，要不先都给10个？
    public int offset;// 全局符号表偏移用于函数编号
    public int localOffset;// 函数表里的偏移(从1开始数)，用于call指令
    public int localCount = 0;// 函数局部变量占用slot数的最大值

    // 不必考虑和变量的重名问题
    // 存这个列表是用来计算分配空间的
    public ArrayList<SymbolEntry> localVars = new ArrayList<>();// 在函数体内，变量没有结束生命周期，可以在符号表找到

    public ArrayList<Instruction> instructions = new ArrayList<>();// 暂时存放函数指令的o0形式

    public Function(String name, int offset, int localOffset) {
        this.name = name;
        this.offset = offset;
        this.localOffset = localOffset;
    }

    public String getName() {
        return this.name;
    }

    /**
     * 初始化参数列表过程中，逐个添加参数
     *
     * @param v
     */
    public void addParamList(Variable v) {
        this.paramList.add(v);
    }

    /**
     * 设置返回类型
     * 因为在函数刚插入符号表，没有分析完参数列表时，还不知道返回类型
     * 但这个参数是必须的
     *
     * @param returnType
     */
    public void setReturnType(Ty returnType) {
        this.returnType = returnType;
    }

    public Ty getReturnType() {
        return this.returnType;
    }

    public void addInstruction(Instruction ins) {
        this.instructions.add(ins);
    }

    /**
     * 计算参数列表占的slot数
     *
     * @return
     */
    public int calParamSlot() {
        int slots = 0;// 一个slot占8字节
        // 此时返回值也已经事先加入到参数列表中了，所以slot不需要额外留值
        for (Variable v : paramList) {
            if (v.type == Ty.UINT)
                slots += 1;
            else if (v.type == Ty.DOUBLE)
                slots += 1;
            ;//some operations else
        }
        return slots;
    }
}
