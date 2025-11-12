import java.util.Scanner;

public class ArithmeticDemo {
    // Declare native methods
    public native int add(int a, int b);
    public native int subtract(int a, int b);
    public native int multiply(int a, int b);
    public native float divide(int a, int b);

    // Load the DLL
    static {
        System.loadLibrary("arithmeticDemo");
    }

    public static void main(String[] args) {
        ArithmeticDemo obj = new ArithmeticDemo();
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter first number: ");
        int a = sc.nextInt();
        System.out.print("Enter second number: ");
        int b = sc.nextInt();

        System.out.println("Addition = " + obj.add(a, b));
        System.out.println("Subtraction = " + obj.subtract(a, b));
        System.out.println("Multiplication = " + obj.multiply(a, b));
        System.out.println("Division = " + obj.divide(a, b));

        sc.close();
    }
}
