package 递归和动态规划;

import java.util.Scanner;

/**
 * 描述
 * 假设有排成一行的N个位置，记为1~N，开始时机器人在M位置，机器人可以往左或者往右走，如果机器人在1位置，那么下一步机器人只能走到2位置，如果机器人在N位置，那么下一步机器人只能走到N-1位置。规定机器人只能走k步，最终能来到P位置的方法有多少种。由于方案数可能比较大，所以答案需要对1e9+7取模。
 * 输入描述：
 * 输出包括一行四个正整数N（2<=N<=5000）、M(1<=M<=N)、K(1<=K<=5000)、P(1<=P<=N)。
 * 输出描述：
 * 输出一个整数，代表最终走到P的方法数对10^9+7取模后的值。
 *
 * 输入：
 * 5 2 3 3
 * 输出：
 * 3
 * 说明：
 * 1).2->1,1->2,2->3
 *
 * 2).2->3,3->2,2->3
 *
 * 3).2->3,3->4,4->3
 */
public class 机器人达到指定位置方法数_重点 {
    private static final int MOD = (int) (1E9 + 7);
    /**
     * 暴力递归
     * 只能在1~N位置，当前在cur位置，走完rest步后，停在p位置的方法数作为结果返回
     * @param n：位置为1~n，固定参数
     * @param cur：当前位置，可变参数
     * @param rest：剩余步数，可变参数
     * @param p：目标位置，固定参数
     * @return
     */
    public static int walk(int n, int cur, int rest, int p) {
        //如果没有剩余步数了，当前的cur位置即最后位置
        //如果最后停在了p上则之前的移动有效，反之则无效
        if(rest == 0) {
            return cur == p ? 1 : 0;
        }
        //如果还有rest步要走，此刻在1位置上，则只能走到2位置
        if(cur == 1) {
            return walk(n, 2, rest - 1, p);
        }
        //如果还有rest步要走，此刻在n位置上，则只能走到n-1位置
        if(cur == n) {
            return walk(n, n - 1, rest - 1, p);
        }
        //如果还有rest步要走，此刻在cur位置上，则可以走到cur+1或者cur-1位置
        return walk(n, cur + 1, rest - 1, p) + walk(n, cur - 1, rest - 1, p);
    }

    /**
     * 优化方案：动态规划
     * 前提条件：这个问题是无后效性的，即一个递归状态的返回值与怎么到达这个状态的路径无关（一般的问题都是无后效性的）
     * 1）找到什么可变参数可以代表一个递归状态，就是说那些参数一旦确定，返回值就确定了
     * 2）把可变参数的左右组合映射成一张表，几个参数就是几维
     * 3）最终答案是表中的哪个位置，在表中标出
     * 4）根据递归过程的base case，把这张表最简单、不需要依赖其他位置的那些位填好值
     * 5）分析表中普通位置的值要怎么计算得到，最终填完整张表
     * 6）填好表返回标记的那个位置的值即可
     * @param n:位置为1~n，固定参数
     * @param m：当前位置，可变参数
     * @param k：剩余步数，可变参数
     * @param p：目标位置，固定参数
     * @return
     */
    public static int walk2(int n, int m, int k, int p) {
        //!!定义dp数组时最好竖着定义，也就是base case的值最好是横着的第一排，有助于想象遍历的过程
        //相当于是让步数这种一次改变的变量做纵轴，位置这种不会改变的做横轴
        int[][] dp = new int[k+1][n+1];
        dp[0][p]=1;
        for (int i = 1; i <= k; i++) {
            for (int j = 1; j <= n; j++) {
                if(j==1){
                    /**
                     * //如果还有rest步要走，此刻在1位置上，则只能走到2位置
                     * if(cur == 1)
                     *     return walk(n, 2, rest - 1, p);
                     */
                    dp[i][j] = dp[i-1][2];
                }
                if(j==n){
                    dp[i][j] = dp[i-1][n-1];//同上
                }
                if(j>1&&j<n){
                    dp[i][j] = (dp[i-1][j-1]+dp[i-1][j+1])%MOD;
                }
            }
        }
        //返回的就是最开始的时候剩余步数下当前位置的值，这个值是一步一步逆推得到的
        return dp[k][m];
    }
        public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int m = sc.nextInt();
        int k = sc.nextInt();
        int p = sc.nextInt();
        System.out.println(walk2(n, m, k, p));
    }
}
