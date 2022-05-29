package 递归和动态规划;

import java.util.Scanner;

/**
 * 描述
 * 给定数组arr，arr中所有的值都为正整数且不重复。每个值代表一种面值的货币，每种面值的货币可以使用任意张，再给定一个aim，代表要找的钱数，求组成aim的最少货币数。
 * 输入描述：
 * 输入包括两行，第一行两个整数n（0<=n<=1000）代表数组长度和aim（0<=aim<=5000），第二行n个不重复的正整数，代表arr数组。
 * <p>
 * 输出描述：
 * 输出一个整数，表示组成aim的最小货币数，无解时输出-1.
 * <p>
 * 输入：
 * 3 20
 * 5 2 3
 * 输出：
 * 4
 * 说明：
 * 20=5*4
 */
public class 换钱的最少货币数 {
    /**
     * 递归方法
     * 当前考虑的是面值arr[i]的情况，还剩rest需要找零
     * 如果返回值为-1，说明在使用arr[i..n-1]的情况下，不能找零rest，反之则可以
     *
     * @param arr：货币面值数组
     * @param i：当前货币面值数组下标
     * @param rest：剩余需要找的钱数
     * @return
     */
    public static int minCount(int[] arr, int i, int rest) {
        //base case
        //已经没有面值可以考虑，如果rest为0，则返回0，否则返回-1
        if (i == arr.length) {
            return rest == 0 ? 0 : -1;
        }
        //最少张数，初始值为-1，表示无法找零，在每个栈帧中都各自定义
        int res = -1;
        //依次尝试使用当前面值的1~k张，但不能超过rest
        for (int k = 0; k * arr[i] <= rest; k++) {
            //使用了k张，剩余的钱数为rest-k*arr[i]，交给剩下的面值去处理
            int next = minCount(arr, i + 1, rest - k * arr[i]);
            if (next != -1) {//说明后续过程有效
                res = res == -1 ? k + next : Math.min(res, k + next);
            }
        }
        return res;
    }

    /**
     * 动态规划
     * minCount(int[] arr, int i, int rest)返回的值就是dp[i][rest]
     * dp[i][rest]=Min{dp[i+1][rest-0*arr[i]]+0;dp[i+1][rest-1*arr[i]]+1;dp[i+1][rest-2*arr[i]]+2...dp[i+1][rest-k*arr[i]]+k}
     *
     * @param arr:货币面值数组
     * @param aim:需要找零的钱数
     * @return
     */
    public static int minCount2(int[] arr, int aim) {
        int n = arr.length;
        int[][] dp = new int[n + 1][aim + 1];
        //base case：设置最后一行的值，除了dp[n][0]为0，其他都为-1
        for (int i = 1; i <= aim; i++) {
            dp[n][i] = -1;
        }
        for (int i = n - 1; i >= 0; i--) {//从最后一行往前遍历
            for (int j = 0; j <= aim; j++) {//从左到右遍历
                dp[i][j] = -1;//先设置无效
                int min = Integer.MAX_VALUE;//设置最小值
                for (int k = 0; j - arr[i] * k >= 0; k++) {//开始遍历当前位置的下面一行的左边的部分
                    if (dp[i + 1][j - arr[i] * k] != -1) {//如果该位置有效则进行判断取最小值赋值
                        min = Math.min(min, dp[i + 1][j - arr[i] * k] + k);
                        dp[i][j] = min;
                    }
                }
            }
        }
        return dp[0][aim];
    }

    /**
     * 动态规划——优化
     * 之前的办法在每次求值的时候都要遍历一遍下面一行，优化后只需要依赖两个位置
     * minCount(int[] arr, int i, int rest)返回的值就是dp[i][rest]
     * dp[i][rest]=Min{dp[i+1][rest-0*arr[i]]+0;dp[i+1][rest-1*arr[i]]+1;dp[i+1][rest-2*arr[i]]+2...dp[i+1][rest-k*arr[i]]+k}
     * dp[i][rest-arr[i]]=Min{dp[i+1][rest-1*arr[i]]+0;dp[i+1][rest-2*arr[i]]+1;dp[i+1][rest-3*arr[i]]+2...dp[i+1][rest-k*arr[i]]+k-1}
     * 由上面两个公式可以推导出：
     * dp[i][rest]=Min{dp[i+1][rest-0*arr[i]]+0;dp[i][rest-arr[i]]+1} --只需要两个位置即可，一个下面的，一个左边的
     *
     * @param arr:货币面值数组
     * @param aim:需要找零的钱数
     * @return
     */
    public static int minCount3(int[] arr, int aim) {
        int n = arr.length;
        int[][] dp = new int[n + 1][aim + 1];
        //base case：设置最后一行的值，除了dp[n][0]为0，其他都为-1
        for (int i = 1; i <= aim; i++) {
            dp[n][i] = -1;
        }
        for (int i = n - 1; i >= 0; i--) {//从最后一行往前遍历
            for (int j = 0; j <= aim; j++) {//从左到右遍历
                dp[i][j] = dp[i + 1][j];//先设置成下面的值
                //如果左边的值不越界且有效
                if (j - arr[i] >= 0 && dp[i][j - arr[i]] != -1) {
                    if (dp[i][j] != -1) {//如果下面的值是有效的话取两个的最小值(不要忘记+1)
                        dp[i][j] = Math.min(dp[i][j], dp[i][j - arr[i]] + 1);
                    } else {//下面的无效的话直接赋左边的有效的值(不要忘记+1)
                        dp[i][j] = dp[i][j - arr[i]] + 1;
                    }
                }
            }
        }
        return dp[0][aim];
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int aim = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
//        System.out.println(minCount(arr, 0, aim));
//        System.out.println(minCount2(arr, aim));
        System.out.println(minCount3(arr, aim));
    }
}
