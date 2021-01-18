package miniplc0java.instruction;

import miniplc0java.analyser.Analyser;
import miniplc0java.analyser.Function;
import miniplc0java.analyser.SymbolEntry;
import miniplc0java.error.CompileError;
import miniplc0java.tokenizer.Token;
import miniplc0java.util.Ty;

import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class Instruction {
    private Operation opt;//u8
    int x;  //u32 i32
    double y;
    long z; //u64 i64

    // 没有操作数
    public Instruction(Operation opt) {
        this.opt = opt;
        this.x = 0;
        this.y = 0.0;
    }

    // 一个操作数
    public Instruction(Operation opt, Object num) {
        this.opt = opt;
        if (num instanceof Integer) {
            this.x = (int) num;
            this.z = Long.valueOf(this.x);
        } else if (num instanceof Double) {
            this.y = (double) num;
            Double d = this.y;
//            System.out.println("double "+d);
            this.z = Double.doubleToLongBits(d);// double -> long
        } else if (num instanceof Long) {
            this.z = (long) num;
        }
//        else
//            this.x = (int) num;
    }

    // 缺省构造函数，可能用于占位，但实际没用上
    public Instruction() {
        this.opt = Operation.nop;
        this.x = 0;
        this.y = 0;
    }

    public static ArrayList<Byte> getByteBytes(int op) {
        byte opb = (byte) op;
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(opb);
        byte[] bytes = buffer.array();
        ArrayList<Byte> ret = new ArrayList<Byte>();
        for (byte b : bytes)
            ret.add(b);
        return ret;
    }

    /**
     * int 转 byte[]
     * 大端
     *
     * @param data int
     * @return bytes[]
     */
    public static ArrayList<Byte> getIntBytes(int data) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(data);
        byte[] bytes = buffer.array();
        ArrayList<Byte> ret = new ArrayList<Byte>();
        for (byte b : bytes)
            ret.add(b);
        return ret;
//        int length = 4;
//        ArrayList<Byte> bytesArr = new ArrayList<>();
//        int start = 8 * (length-1);
//        for(int i = 0 ; i < length; i++){
//            bytesArr.add((byte) (( data >> ( start - i * 8 )) & 0xFF ));
//        }
//        byte[] bytes =  listTobyte(bytesArr);
//        return bytes;
    }

    public static byte[] listTobyte(List<Byte> list) {
        if (list == null || list.size() < 0)
            return null;
        byte[] bytes = new byte[list.size()];
        int i = 0;
        Iterator<Byte> iterator = list.iterator();
        while (iterator.hasNext()) {
            bytes[i] = iterator.next();
            i++;
        }
        return bytes;
    }

    /**
     * long 转 byte[]
     * 大端
     *
     * @param data int
     * @return bytes[]
     */
    public static ArrayList<Byte> getLongBytes(long data) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(data);
        byte[] bytes = buffer.array();
        ArrayList<Byte> ret = new ArrayList<Byte>();
        for (byte b : bytes)
            ret.add(b);
        return ret;
    }

    /**
     * double字面量 转 byte[]
     * 大端
     *
     * @param data double
     * @return bytes[]
     */
    public static ArrayList<Byte> getDoubleBytes(double data) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putDouble(data);
        byte[] bytes = buffer.array();
        ArrayList<Byte> ret = new ArrayList<Byte>();
        for (byte b : bytes)
            ret.add(b);
        return ret;
    }

    /**
     * byte[] 转 String
     * o0的格式是以8字节为单位的字节流，这个函数直接决定输出文件的编码格式！
     * 实验表明，这个函数确实有问题！-8输出变成了0x3f3f3f3f
     *
     * @param bytes
     * @return
     */
    public static ArrayList<Byte> getString(ArrayList<Byte> bytes) {
//        String result = new String(bytes);
//        return result;
        return bytes;
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
        if (this.y != 0)
            return this.opt.name() + " " + this.y;
        else if (this.x != 0)
            return this.opt.name() + " " + this.x;
        else
            return this.opt.name() + " " + this.z;
    }

    private static List<Byte> long2bytes(int length, long target) {
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * (length - 1);
        for (int i = 0; i < length; i++) {
            bytes.add((byte) ((target >> (start - i * 8)) & 0xFF));
        }
        return bytes;
    }


    public boolean toString(List<Byte> output) {
        switch (this.opt) {
            case nop:
                return output.addAll(getString(getByteBytes(0x00)));
            case push:
                // push的操作数是u64类型
                output.addAll(getString(getByteBytes(0x01)));
                if (this.z != 0)
                    return output.addAll(getString(getLongBytes(this.z)));
                else
                    return output.addAll(long2bytes(8, this.z));
            case pop:
                return output.addAll(getString(getByteBytes(0x02)));
            case popn:
                output.addAll(getString(getByteBytes(0x03)));
                return output.addAll(getString(getIntBytes(this.x)));
            case dup:
                return output.addAll(getString(getByteBytes(0x04)));
            case loca:
                output.addAll(getString(getByteBytes(0x0a)));
                return output.addAll(getString(getIntBytes(this.x)));
            case arga:
                output.addAll(getString(getByteBytes(0x0b)));
                return output.addAll(getString(getIntBytes(this.x)));
            case globa:
                output.addAll(getString(getByteBytes(0xc)));
                return output.addAll(getString(getIntBytes(this.x)));
            case load8:
                return output.addAll(getString(getByteBytes(0x10)));
            case load16:
                return output.addAll(getString(getByteBytes(0x11)));
            case load32:
                return output.addAll(getString(getByteBytes(0x12)));
            case load64:
                return output.addAll(getString(getByteBytes(0x13)));
            case store8:
                return output.addAll(getString(getByteBytes(0x14)));
            case store16:
                return output.addAll(getString(getByteBytes(0x15)));
            case store32:
                return output.addAll(getString(getByteBytes(0x16)));
            case store64:
                return output.addAll(getString(getByteBytes(0x17)));
            case alloc:
                return output.addAll(getString(getByteBytes(0x18)));
            case free:
                return output.addAll(getString(getByteBytes(0x19)));
            case stackalloc:
                output.addAll(getString(getByteBytes(0x1a)));
                return output.addAll(getString(getIntBytes(this.x)));
            case addi:
                return output.addAll(getString(getByteBytes(0x20)));
            case subi:
                return output.addAll(getString(getByteBytes(0x21)));
            case muli:
                return output.addAll(getString(getByteBytes(0x22)));
            case divi:
                return output.addAll(getString(getByteBytes(0x23)));
            case addf:
                return output.addAll(getString(getByteBytes(0x24)));
            case subf:
                return output.addAll(getString(getByteBytes(0x25)));
            case mulf:
                return output.addAll(getString(getByteBytes(0x26)));
            case divf:
                return output.addAll(getString(getByteBytes(0x27)));
            case divu:
                return output.addAll(getString(getByteBytes(0x28)));
            case shl:
                return output.addAll(getString(getByteBytes(0x29)));
            case shr:
                return output.addAll(getString(getByteBytes(0x2a)));
            case and:
                return output.addAll(getString(getByteBytes(0x2b)));
            case or:
                return output.addAll(getString(getByteBytes(0x2c)));
            case xor:
                return output.addAll(getString(getByteBytes(0x2d)));
            case not:
                return output.addAll(getString(getByteBytes(0x2e)));
            // cmp.T命令：output.addAll(
            // 指令会在 lhsoutput.addAll( < rhs 时压入 -1, lhs > rhs 时压入 1,
            // lhs == routput.addAll(hs 时压入 0。
            // 浮点数无法比较时output.addAll(压入 0。
            case cmpi:
                return output.addAll(getString(getByteBytes(0x30)));
            case cmpu:
                return output.addAll(getString(getByteBytes(0x31)));
            case cmpf:
                return output.addAll(getString(getByteBytes(0x32)));
            case negi:
                return output.addAll(getString(getByteBytes(0x34)));
            case negf:
                return output.addAll(getString(getByteBytes(0x35)));
            case itof:
                return output.addAll(getString(getByteBytes(0x36)));
            case ftoi:
                return output.addAll(getString(getByteBytes(0x37)));
            case shrl:
                return output.addAll(getString(getByteBytes(0x38)));
            case setlt:
                return output.addAll(getString(getByteBytes(0x39)));
            case setgt:
                return output.addAll(getString(getByteBytes(0x3a)));
            case br:// 操作数可能是负数
                output.addAll(getString(getByteBytes(0x41)));
                return output.addAll((long2bytes(4, this.z)));// fixme
//            return output.addAll(getString(getIntBytes(this.x)));
            case brfalse:
                output.addAll(getString(getByteBytes(0x42)));
                return output.addAll(getString(getIntBytes(this.x)));
            case brtrue:
                output.addAll(getString(getByteBytes(0x43)));
                return output.addAll(getString(getIntBytes(this.x)));
            case call:
                output.addAll(getString(getByteBytes(0x48)));
                return output.addAll(getString(getIntBytes(this.x)));
            case ret:
                return output.addAll(getString(getByteBytes(0x49)));
            case callname:
                output.addAll(getString(getByteBytes(0x4a)));
                return output.addAll(getString(getIntBytes(this.x)));
            case scani:
                return output.addAll(getString(getByteBytes(0x50)));
            case scanc:
                return output.addAll(getString(getByteBytes(0x51)));
            case scanf:
                return output.addAll(getString(getByteBytes(0x52)));
            case printi:
                return output.addAll(getString(getByteBytes(0x54)));
            case printc:
                return output.addAll(getString(getByteBytes(0x55)));
            case printf:
                return output.addAll(getString(getByteBytes(0x56)));
            case prints:
                return output.addAll(getString(getByteBytes(0x57)));
            case println:
                return output.addAll(getString(getByteBytes(0x58)));
            default:// output.addAll(panic
                return output.addAll(getString(getByteBytes(0xfe)));
        }
    }

    public static byte[] addHead() {
        // magic-number and version in o0
        List<Byte> ret = new ArrayList<Byte>();
        ret.addAll(getString(getIntBytes(0x72303b3e)));
        ret.addAll(getString(getIntBytes(0x00000001)));
        return listTobyte(ret);
    }

    public static ArrayList<Token> globalVarTable = new ArrayList<Token>();
    public static Stack<SymbolEntry> symbolTable = new Stack<SymbolEntry>();

    public static byte[] addGlob() throws CompileError {
        List<Byte> ret = new ArrayList<Byte>();
        ret.addAll(getString(getIntBytes(globalVarTable.size())));// glob.count
        for (Token t : globalVarTable) {
            SymbolEntry s = Analyser.getSymbol(symbolTable, t.getValueString());
            // u64和f64大小相同!!!
            if ((s != null) && (s.getType() == Ty.UINT || s.getType() == Ty.DOUBLE)) {
                if (s.isConstant())
                    ret.addAll(getString(getByteBytes(1)));
                else
                    ret.addAll(getString(getByteBytes(0)));
                // 长度为8
                ret.addAll(getString(getIntBytes(8)));
                // 初始值为0
                ret.addAll(getString(getLongBytes(0)));
            } else//字符串 或 函数名
            {
                ret.addAll(getString(getByteBytes(1)));// is_const
                ret.addAll(getString(getIntBytes(t.getValueString().length())));// count
                String string = t.getValueString();
                char[] ch = string.toCharArray();// 转ascii
//                System.out.println("字符串长度为"+ch.length);
                for (int i = 0; i < ch.length; i++) {
                    ret.addAll(getString(getByteBytes(Integer.valueOf(ch[i]).byteValue())));
//                    System.out.print(Integer.toHexString(Integer.valueOf(ch[i]).intValue())+" ");
                }// value
            }
        }
        return listTobyte(ret);
    }

    public static ArrayList<Function> funcTable = new ArrayList<Function>();

    public static byte[] addFunc() {
        List<Byte> ret = new ArrayList<Byte>();
        ret.addAll(getString(getIntBytes(funcTable.size())));// func.count
        for (Function f : funcTable) {
            int offset = calcFuncOffset(f.getName());// 函数在globTable中的偏移
            ret.addAll(getString(getIntBytes(offset)));// name,好像没什么用
            if (f.returnType == Ty.VOID)
                ret.addAll(getString(getIntBytes(0)));// ret_slots
            else
                ret.addAll(getString(getIntBytes(1)));// 返回一个int
            ret.addAll(getString(getIntBytes(f.calParamSlot())));// param_slots
//        System.out.println(f.getName()+" "+f.localCount+" "+f.calParamSlot()+"->0/1");
            ret.addAll(getString(getIntBytes(f.localCount)));// loc_slots
            ret.addAll(getString(getIntBytes(f.instructions.size())));// body.count
            for (Instruction i : f.instructions) {
                i.toString(ret);// body items
            }
        }
        return listTobyte(ret);
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
