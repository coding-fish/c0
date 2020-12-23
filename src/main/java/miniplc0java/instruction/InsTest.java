package miniplc0java.instruction;

public class InsTest {
    public static void main() {
        byte[] b = Instruction.getDoubleBytes(1.0e1);
        for (int i = 0; i< b.length;i++)
        {
            System.out.println(b[i]);
        }
    }
    static{
        main();
    }
}