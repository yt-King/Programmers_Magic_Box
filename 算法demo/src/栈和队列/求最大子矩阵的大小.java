package 栈和队列;

import java.util.Scanner;
import java.util.Stack;

/**
 * 描述
 * 给定一个整型矩阵 map，其中的值只有 0 和 1 两种，求其中全是 1 的所有矩形区域中，最大的矩形区域里 1 的数量。
 * 输入描述：
 * 第一行输入两个整数 n 和 m，代表 n*m 的矩阵
 * 接下来输入一个 n*m 的矩阵
 * 输出描述：
 * 输出其中全是 1 的所有矩形区域中，最大的矩形区域里 1 的数量。
 * 示例1
 * 输入：
 * 1 4
 * 1 1 1 0
 *
 * 输出：
 * 3
 *
 * 说明：
 * 最大的矩形区域有3个1，所以返回3
 */
public class 求最大子矩阵的大小 {
    public static int maxArea(int[][] map) {
        int max = 0;
        int[] height = new int[map[0].length];//以当前这一行为底，每一列的高度
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                height[j] = map[i][j] == 0 ? 0 : height[j] + 1;//如果是0，高度就是0，如果是1，高度就加1
            }
            max = Math.max(max, maxByStack(height));
        }
        return max;
    }

    /**
     * 使用单调栈的方式在每一行中找到最大的矩形区域 O(n)复杂度
     * @param height
     * @return
     */
    public static int maxByStack(int[] height) {
        Stack<Integer> stack = new Stack<>();
        int max = 0;
        for (int i = 0; i < height.length; i++) {
            while (!stack.isEmpty() && height[stack.peek()] >= height[i]) {//相等的时候也要弹出
                //popIndex为当前位置，找到他两边离他最近的比他小的元素，这个元素就是矩形的左右边界
                int popIndex = stack.pop();
                int left = stack.isEmpty() ? -1 : stack.peek();
                //左边界就是栈顶元素，右边界就是遍历到的元素
                int curArea = height[popIndex] * (i - left - 1);//当前矩形区域的面积
                max = Math.max(max, curArea);
            }
            stack.push(i);
        }
        while (!stack.isEmpty()) {
            int popIndex = stack.pop();
            int left = stack.isEmpty() ? -1 : stack.peek();
            int curArea = height[popIndex] * (height.length - left - 1);
            max = Math.max(max, curArea);
        }
        return max;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();
        int m = scan.nextInt();
        int[][] map = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                map[i][j] = scan.nextInt();
            }
        }
        System.out.println(maxArea(map));
    }
}
