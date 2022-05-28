package 递归和动态规划;

import java.util.Scanner;

/**
 * 描述
 * 给定一个 n * m 的矩阵 a，从左上角开始每次只能向右或者向下走，最后到达右下角的位置，路径上所有的数字累加起来就是路径和，输出所有的路径中最小的路径和。
 * 输入描述：
 * 第一行输入两个整数 n 和 m，表示矩阵的大小。
 * <p>
 * 接下来 n 行每行 m 个整数表示矩阵。
 * 输出描述：
 * 输出一个整数表示答案。
 */
public class 矩阵的最小路径和_压缩空间方法 {
    //常规dp
    public static int minPathSum(int[][] arr) {
        int m = arr.length;
        int n = arr[0].length;
        int[][] dp = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (i == 0 && j == 0) {
                    dp[i][j] = arr[i][j];
                } else if (i == 0) {
                    dp[i][j] = dp[i][j - 1] + arr[i][j];
                } else if (j == 0) {
                    dp[i][j] = dp[i - 1][j] + arr[i][j];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j], dp[i][j - 1]) + arr[i][j];
                }
            }
        }
        return dp[m - 1][n - 1];
    }

    /**
     * 优化：压缩空间
     *
     * @param arr
     */
    public static int minPathSum2(int[][] arr) {
        int n = arr[0].length;
        int[] dp = new int[n];
        dp[0] = arr[0][0];
        for (int i = 1; i < n; i++) {
            dp[i] = dp[i - 1] + arr[0][i];
        }
        for (int i = 1; i < arr.length; i++) {
            dp[0] += arr[i][0];
            for (int j = 1; j < n; j++) {
                dp[j] = Math.min(dp[j], dp[j - 1]) + arr[i][j];
            }
        }
        return dp[n - 1];
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int m = sc.nextInt();
        int n = sc.nextInt();
        int[][] arr = new int[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                arr[i][j] = sc.nextInt();
            }
        }
        System.out.println(minPathSum2(arr));
    }
}
