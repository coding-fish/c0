package miniplc0java.instruction;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.Function;
import miniplc0java.analyser.SymbolEntry;
import miniplc0java.error.CompileError;
import miniplc0java.tokenizer.Token;
import miniplc0java.util.Ty;

import java.util.ArrayList;
import java.util.Objects;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Stack;
import java.util.logging.Logger;

public class Instruction {
    private Operation opt;//u8
    int x;  //u32 i32
    double y; //u64 i64

    // 没有操作数
    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = 0;
        this.y = 0;
    }

    // 一个操作数
    public Instruction(Operation opt, Object num) {
        this.opt = opt;
        if (num instanceof Integer)
            this.x = (int) num;
        else if (num instanceof Double)// TODO:// 似乎，没有这个类型的操作数?
            this.y = (double) num;
//        else
//            this.x = (int) num;
    }

    // 缺省构造函数，可能用于占位，但实际没用上
    public Instruction() {
        this.opt = Operation.nop;
        this.x = 0;
        this.y = 0;
    }

    public static byte[] getByteBytes(int op) {
        byte opb = (byte) op;
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(opb);
        byte[] bytes = buffer.array();
        return bytes;
    }

    /**
     * int 转 byte[]
     * 大端
     *
     * @param data int
     * @return bytes[]
     */
    public static byte[] getIntBytes(int data) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(data);
        byte[] bytes = buffer.array();
        return bytes;
    }

    /**
     * long 转 byte[]
     * 大端
     *
     * @param data int
     * @return bytes[]
     */
    public static byte[] getLongBytes(long data) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(data);
        byte[] bytes = buffer.array();
        return bytes;
    }

    /**
     * double字面量 转 byte[]
     * 大端
     *
     * @param data double
     * @return bytes[]
     */
    public static byte[] getDoubleBytes(double data) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putDouble(data);
        byte[] bytes = buffer.array();
        return bytes;
    }

    /**
     * byte[] 转 String
     * o0的格式是以8字节为单位的字节流，这个函数直接决定输出文件的编码格式！
     *
     * @param bytes
     * @return
     */
    public static String getString(byte[] bytes) {
        String result = new String(bytes);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public String debug() {
        return this.opt.name()+" "+this.x+" "+this.y;
    }

    @Override
    public String toString() {
        switch (this.opt) {
            case nop:
                return getString(getByteBytes(0x00));
            case push:
                return getString(getByteBytes(0x01)) + getString(getDoubleBytes(this.y));
            case pop:
                return getString(getByteBytes(0x02));
            case popn:
                return getString(getByteBytes(0x03)) + getString(getIntBytes(this.x));
            case dup:
                return getString(getByteBytes(0x04));
            case loca:
                return getString(getByteBytes(0x0a)) + getString(getIntBytes(this.x));
            case arga:
                return getString(getByteBytes(0x0b)) + getString(getIntBytes(this.x));
            case globa:
                return getString(getByteBytes(0xc)) + getString(getIntBytes(this.x));
            case load8:
                return getString(getByteBytes(0x10));
            case load16:
                return getString(getByteBytes(0x11));
            case load32:
                return getString(getByteBytes(0x12));
            case load64:
                return getString(getByteBytes(0x13));
            case store8:
                return getString(getByteBytes(0x14));
            case store16:
                return getString(getByteBytes(0x15));
            case store32:
                return getString(getByteBytes(0x16));
            case store64:
                return getString(getByteBytes(0x17));
            case alloc:
                return getString(getByteBytes(0x18));
            case free:
                return getString(getByteBytes(0x19));
            case stackalloc:
                return getString(getByteBytes(0x1a)) + getString(getIntBytes(this.x));
            case addi:
                return getString(getByteBytes(0x20));
            case subi:
                return getString(getByteBytes(0x21));
            case muli:
                return getString(getByteBytes(0x22));
            case divi:
                return getString(getByteBytes(0x23));
            case addf:
                return getString(getByteBytes(0x24));
            case subf:
                return getString(getByteBytes(0x25));
            case mulf:
                return getString(getByteBytes(0x26));
            case divf:
                return getString(getByteBytes(0x27));
            case divu:
                return getString(getByteBytes(0x28));
            case shl:
                return getString(getByteBytes(0x29));
            case shr:
                return getString(getByteBytes(0x2a));
            case and:
                return getString(getByteBytes(0x2b));
            case or:
                return getString(getByteBytes(0x2c));
            case xor:
                return getString(getByteBytes(0x2d));
            case not:
                return getString(getByteBytes(0x2e));
            // cmp.T命令：
            // 指令会在 lhs < rhs 时压入 -1, lhs > rhs 时压入 1,
            // lhs == rhs 时压入 0。
            // 浮点数无法比较时压入 0。
            case cmpi:
                return getString(getByteBytes(0x30));
            case cmpu:
                return getString(getByteBytes(0x31));
            case cmpf:
                return getString(getByteBytes(0x32));
            case negi:
                return getString(getByteBytes(0x34));
            case negf:
                return getString(getByteBytes(0x35));
            case itof:
                return getString(getByteBytes(0x36));
            case ftoi:
                return getString(getByteBytes(0x37));
            case shrl:
                return getString(getByteBytes(0x38));
            case setlt:
                return getString(getByteBytes(0x39));
            case setgt:
                return getString(getByteBytes(0x3a));
            case br:
                return getString(getByteBytes(0x41)) + getString(getIntBytes(this.x));
            case brfalse:
                return getString(getByteBytes(0x42)) + getString(getIntBytes(this.x));
            case brtrue:
                return getString(getByteBytes(0x43)) + getString(getIntBytes(this.x));
            case call:
                return getString(getByteBytes(0x48)) + getString(getIntBytes(this.x));
            case ret:
                return getString(getByteBytes(0x49));
            case callname:
                return getString(getByteBytes(0x4a)) + getString(getIntBytes(this.x));
            case scani:
                return getString(getByteBytes(0x50));
            case scanc:
                return getString(getByteBytes(0x51));
            case scanf:
                return getString(getByteBytes(0x52));
            case printi:
                return getString(getByteBytes(0x54));
            case printc:
                return getString(getByteBytes(0x55));
            case printf:
                return getString(getByteBytes(0x56));
            case prints:
                return getString(getByteBytes(0x57));
            case println:
                return getString(getByteBytes(0x58));
            default:// panic
                return getString(getByteBytes(0xfe));
        }
    }

    public static String addHead() {
        // magic-number and version in o0
        return getString(getIntBytes(0x72303b3e)) + getString(getIntBytes(0x00000001));
    }

    public static ArrayList<Token> globalVarTable = new ArrayList<Token>();
    public static Stack<SymbolEntry> symbolTable = new Stack<SymbolEntry>();

    public static String addGlob() throws CompileError {
        // 应该用StringBuilder,懒得改了
        String ret = new String();
        ret += getString(getIntBytes(globalVarTable.size()));// glob.count
        for (Token t : globalVarTable) {
            SymbolEntry s = Analyser.getSymbol(symbolTable, t.getValueString());
            if ((s != null) && (s.getType() == Ty.UINT)) {
                if (s.isConstant())
                    ret += getString(getByteBytes(1));
                else
                    ret += getString(getByteBytes(0));
                // 长度为8
                ret += getString(getIntBytes(8));
                // 初始值为0
                ret += getString(getLongBytes(0));
            } else//字符串 或 函数名
            {
                ret += getString(getByteBytes(1));// is_const
                ret += getString(getIntBytes(t.getValueString().length()));// count
                String string = t.getValueString();
                char[] ch = string.toCharArray();// 转ascii
                for (int i = 0; i < ch.length; i++) {
                    ret += getString(getIntBytes(Integer.valueOf(ch[i]).intValue()));
                }// value
            }
        }
        return ret;
    }

    public static ArrayList<Function> funcTable = new ArrayList<Function>();

    public static String addFunc() {
        String ret = new String();
        ret += getString(getIntBytes(funcTable.size()));// func.count
        for (Function f : funcTable) {
            int offset = calcFuncOffset(f.getName());// 函数在globTable中的偏移
            ret += getString(getIntBytes(offset));// name,好像没什么用
            if (f.returnType == Ty.VOID)
                ret += getString(getIntBytes(0));// ret_slots
            else
                ret += getString(getIntBytes(1));// 返回一个int
            ret += getString(getIntBytes(f.calParamSlot()));// param_slots
            ret += getString(getIntBytes(100));// TODO:loc_slots
            ret += getString(getIntBytes(f.instructions.size()));// body.count
            for (Instruction i : f.instructions) {
                ret += i.toString();// body items
            }
        }
        return ret;
    }

    private static int calcFuncOffset(String name) {
        // 函数名在全局表中的偏移
        for (int i = 0; i < Instruction.globalVarTable.size(); i++) {
            if (Instruction.globalVarTable.get(i).getValueString().equals(name))
                return i;
        }
        return -1;// 随便整的，不存在这种情况
    }
}
