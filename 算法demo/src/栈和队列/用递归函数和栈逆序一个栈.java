package 栈和队列;

import java.util.Scanner;
import java.util.Stack;

/**
 * 描述
 * 一个栈依次压入1,2,3,4,5，那么从栈顶到栈底分别为5,4,3,2,1。将这个栈转置后，从栈顶到栈底为1,2,3,4,5，
 * 也就是实现栈中元素的逆序，但是只能用递归函数来实现，不能用其他数据结构。
 * 输入描述：
 * 输入数据第一行一个整数N为栈中元素的个数。
 * <p>
 * 接下来一行N个整数
 * 表示一个栈依次压入的每个元素。
 * 输出描述：
 * 输出一行表示栈中元素逆序后的栈顶到栈底的每个元素
 * <p>
 * 输入：
 * 5
 * 1 2 3 4 5
 * <p>
 * 输出：
 * 1 2 3 4 5
 */
public class 用递归函数和栈逆序一个栈 {
    public static int getLast(Stack<Integer> s) {//获取栈底元素
        /**
         * cur用来保存栈顶元素，每进一个栈帧就保存当前的栈顶元素，为空时返回的就是栈底元素
         * 栈底元素用last保存，每退出一个栈帧last原封不动的返回，cur重新入栈
         * 最后返回last，栈内的其余元素都不变
         */
        int cur = s.pop();
        if (s.isEmpty()) {
            return cur;
        } else {
            int last = getLast(s);
            s.push(cur);
            return last;
        }
    }

    public static void reverse(Stack<Integer> s) {
        /**
         * 当栈为空时结束递归，不为空时拿到栈底元素，用cur保存，进入下一个栈帧
         * 从最后一个栈帧往外退出时，先进去的是最后一个cur（也就是最后的栈底==栈顶元素），完成反转
         */
        if (s.isEmpty()) {
            return;
        } else {
            int cur = getLast(s);
            reverse(s);
            s.push(cur);
        }
    }

    public static void main(String[] args) {
        Stack<Integer> s = new Stack<>();
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        for (int i = 0; i < n; i++) {
            s.push(sc.nextInt());
        }
        reverse(s);
        while (!s.isEmpty()) {
            System.out.print(s.pop() + " ");
        }
    }
}
