package 栈和队列;

import java.util.Scanner;
import java.util.Stack;

/**
 * 描述
 * 用两个栈实现队列，支持队列的基本操作。
 * 输入描述：
 * 第一行输入一个整数N，表示对队列进行的操作总数。
 * <p>
 * 下面N行每行输入一个字符串S，表示操作的种类。
 * <p>
 * 如果S为"add"，则后面还有一个整数X表示向队列尾部加入整数X。
 * <p>
 * 如果S为"poll"，则表示弹出队列头部操作。
 * <p>
 * 如果S为"peek"，则表示询问当前队列中头部元素是多少。
 * 输出描述：
 * 对于每一个为"peek"的操作，输出一行表示当前队列中头部元素是多少。
 * <p>
 * 输入：
 * 6
 * add 1
 * add 2
 * add 3
 * peek
 * poll
 * peek
 * <p>
 * 输出：
 * 1
 * 2
 */
public class 由两个栈组成的队列 {
    public static void add(Stack in, Stack out, int x) {//添加数据
        in.push(x);
    }

    public static void poll(Stack in, Stack out) {//弹出数据,只有当out栈为空时才可以把in栈中的数据全部写入out栈
        if (out.isEmpty()) {
            while (!in.isEmpty()) {
                out.push(in.pop());
            }
            out.pop();
        } else {
            out.pop();
        }
    }

    public static void peek(Stack in, Stack out) {//询问当前队列中头部元素是多少
        if (out.isEmpty()) {
            while (!in.isEmpty()) {
                out.push(in.pop());
            }
        }
        System.out.println(out.peek());
    }

    public static void main(String[] args) {
        Stack<Integer> in = new Stack<Integer>();//放数据的栈
        Stack<Integer> out = new Stack<Integer>();//出数据的栈
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        for (int i = 0; i < n; i++) {
            String s = sc.next();
            if (s.equals("add")) {
                int x = sc.nextInt();
                add(in, out, x);
            } else if (s.equals("poll")) {
                poll(in, out);
            } else {
                peek(in, out);
            }
        }
    }
}
