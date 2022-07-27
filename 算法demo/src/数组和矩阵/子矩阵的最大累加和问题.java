package 数组和矩阵;

import java.util.Scanner;

/**
 * 描述
 * 给定一个矩阵matrix，其中的值有正、有负、有0，返回子矩阵的最大累加和
 * 例如，矩阵matrix为：
 * -90 48 78
 * 64 -40 64
 * -81 - 7 66
 * 其中，最大的累加和的子矩阵为：
 * 48 78
 * -40 64
 * -7 66
 * 所以返回累加和209。
 * 例如，matrix为：
 * -1 -1 -1
 * -1 2 2
 * -1 -1 -1
 * 其中，最大累加和的子矩阵为：
 * 2 2
 * 所以返回4
 *
 * 输入：
 * 3 3
 * -90 48 78
 * 64 -40 64
 * -81 -7 66
 *
 * 输出：
 * 209
 */
public class 子矩阵的最大累加和问题 {
    public static int maxSum(int[][] nums) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < nums.length; i++) {//第一层循环表示从第i行开始为起始行，向下累加每一行再求最大值，这样可以包含所有的结果
            int[] temp = new int[nums[0].length];
            for (int j = i; j < nums.length; j++) {
                int cur =0;//在第二层循环里定义当前累加和，每次向下累加一行累加和都要重置为0
                for (int k = 0; k < nums[0].length; k++) {
                    temp[k] += nums[j][k];//temp数组保存每一行当前列目前的累加和
                    cur += temp[k];//cur保存当前行的最大累加和
                    max = Math.max(max, cur);
                    cur = cur < 0 ? 0 : cur;
                }
            }
        }
        return max;
    }

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int n = scan.nextInt();
        int m = scan.nextInt();
        int[][] nums = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                nums[i][j] = scan.nextInt();
            }
        }
        System.out.println(maxSum(nums));
    }
}
