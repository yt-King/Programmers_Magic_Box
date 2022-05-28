package 数组和矩阵;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * 给定一个无序数组arr, 其中元素可正、可负、可0。给定一个整数k，求arr所有子数组中累加和为k的最长子数组长度
 *
 * 输入：
 * 5 0
 * 1 -2 1 1 1
 *
 * 输出：
 * 3
 */
public class 未排序数组中累加和为给定值的最长子数组长度_含负数 {
    /**
     * 关键在于通过一个sum[]数组来记录每个位置的累加和，然后通过一个map来记录每个累加和第一次出现的位置
     * 这样arr[i..j]的累加和就可以通过sum[j] - sum[i]来计算，这是本题的核心
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int k = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        Map map = new HashMap();
        map.put(0, -1);//要考虑到从0开始的子数组情况
        int sum = 0;// 当前最长子数组的和
        int maxLen = 0;// 当前最长子数组的长度
        for (int i = 0; i < n; i++) {
            sum += arr[i];
            if(map.containsKey(sum-k)){
                //如果存在sum-k，则说明sum-k在前面出现过，则说明以arr[i]结尾的子数组存在
                //而且记录的位置是最早出现的位置，是当前情况下的最长子数组
                maxLen = Math.max(maxLen, i-((int)map.get(sum-k)));
            }
            if (!map.containsKey(sum)) {
                //如果当前表中不存在sum，则说明当前位置是第一次出现sum的位置，把当前位置记录下来
                map.put(sum, i);
            }
        }
        System.out.println(maxLen);
    }
}
