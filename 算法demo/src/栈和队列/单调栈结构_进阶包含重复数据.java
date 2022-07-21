package 栈和队列;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * 描述
 * 给定一个可能含有重复值的数组 arr，找到每一个 i 位置左边和右边离 i 位置最近且值比 arr[i] 小的位置。返回所有位置相应的信息。
 * <p>
 * 输入描述：
 * 第一行输入一个数字 n，表示数组 arr 的长度。
 * <p>
 * 以下一行输出 n个数字，表示数组的值。
 * 输出描述：
 * 输出n行，每行两个数字 L 和 R，如果不存在，则值为-1，下标从0开始。
 * <p>
 * 输入：
 * 7
 * 3 4 1 5 6 2 7
 * <p>
 * 输出：
 * -1 2
 * 0 2
 * -1 -1
 * 2 5
 * 3 5
 * 2 -1
 * 5 -1
 */
public class 单调栈结构_进阶包含重复数据 {
    public static int[][] getNearLessRepeat(int[] array) {
        int[][] res = new int[array.length][2];
        //再有重复的情况下，每个栈里面存的就不再是一个数值，可能是一组重复的数值，所以需要用一个集合来存储并记录顺序
        Stack<List<Integer>> stack = new Stack<>();
        for (int i = 0; i < array.length; i++) {
            //如果要找到每一个 i 位置左边和右边离 i 位置最近且值比 arr[i] 小的位置，那么要求stack的值是单调递增的
            while (!stack.isEmpty() && array[stack.peek().get(0)] > array[i]) {
                //在stack不为空时，如果要加入的值破坏了单调递增的结构，那么就要弹出stack的最后一组值，直到stack的值是单调递增的
                List<Integer> popIndexs = stack.pop();
                //记录弹出的位置的左右边离i位置的最近的位置，左边就是弹出后栈顶的值，因为是递增的，右边就是i位置的值
                //因为现在每个栈记录的都有可能是一组数据，所以要拿到最靠近弹出位置的那组数据的下标，然后记录下来
                int index = stack.isEmpty() ? -1 : stack.peek().get(stack.peek().size() - 1);
                for (Integer x : popIndexs) {
                    res[x][0] = index;
                    res[x][1] = i;
                }
            }
            //在不破坏单调递增的结构的情况下，将i位置的值加入到栈中
            //加入前判断栈顶的元素与要加入的元素的值是否相同
            if (!stack.isEmpty() && array[stack.peek().get(0)] == array[i]) {
                stack.peek().add(i);
            } else {
                List<Integer> list = new ArrayList<>();
                list.add(i);
                stack.add(list);
            }
        }
        //遍历完数组后，如果栈不为空，那么就要弹出栈中的所有值，记录下来
        while (!stack.isEmpty()) {
            //在stack不为空时，如果要加入的值破坏了单调递增的结构，那么就要弹出stack的最后一组值，直到stack的值是单调递增的
            List<Integer> popIndexs = stack.pop();
            //记录弹出的位置的左右边离i位置的最近的位置，左边就是弹出后栈顶的值，因为是递增的，右边就是i位置的值
            //因为现在每个栈记录的都有可能是一组数据，所以要拿到最靠近弹出位置的那组数据的下标，然后记录下来
            int index = stack.isEmpty() ? -1 : stack.peek().get(stack.peek().size() - 1);
            for (Integer x : popIndexs) {
                res[x][0] = index;
                res[x][1] = -1;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = sc.nextInt();
        }
        int[][] res = getNearLessRepeat(array);
        StringBuilder sb=new StringBuilder();
        for(int[] t:res){
            sb.append(t[0]).append(" ").append(t[1]).append("\n");
        }
        System.out.println(sb.toString());
        sc.close();
        //用如下方法输出会超时
//        for (int i = 0; i < n; i++) {
//            System.out.println(res[i][0] + " " + res[i][1]);
//        }
    }
}
