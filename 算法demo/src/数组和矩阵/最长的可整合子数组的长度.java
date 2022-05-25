package 数组和矩阵;

import java.util.*;

/**
 * 描述
 * 先给出可整合数组的定义：如果一个数组在排序之后，每相邻两个数的差的绝对值都为1，或者该数组长度为1，则该数组为可整合数组。例如，[5, 3, 4, 6, 2]排序后为[2, 3, 4, 5, 6]，符合每相邻两个数差的绝对值都为1，所以这个数组为可整合数组
 * 给定一个数组arr, 请返回其中最大可整合子数组的长度。例如，[5, 5, 3, 2, 6, 4, 3]的最大可整合子数组为[5, 3, 2, 6, 4]，所以请返回5
 * <p>
 * <p>
 * 注意：本题中子数组的定义是数组中连续的一段区间，例如 [1,2,3,4,5] 中 [2,3,4] 是子数组,[2,4,5] 和 [1,3] 不是子数组
 *
 * 输入：
 * 7
 * 5 5 3 2 6 4 3
 * 输出：
 * 5
 */
public class 最长的可整合子数组的长度 {
    public static void main(String[] args) {
        /**
         * 思路：判断一个数组是否是可整合数组，可以判断一个数组中是否有重复元素，若没有并且最大值减去最小值在加1就是数组长度，
         * 那么就可以确定该数组是整合数组。
         */
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        if (n == 0) {
            System.out.println(0);
            return;
        }
        if (n == 1) {
            System.out.println(1);
            return;
        }
        int maxLen = 0; //最大长度
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < n; i++) {
            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;
            for (int j = i; j < n; j++) {
                if (set.contains(arr[j])) {
                    break;//如果已经包含了，则不需要继续，将下一个数作为开头
                }
                set.add(arr[j]);
                max = Math.max(max, arr[j]);
                min = Math.min(min, arr[j]);
                if (max - min == j - i) {
                    maxLen = Math.max(maxLen, j - i + 1);
                }
            }
            // 每次以一个新的数字作为子数组开头的时候，都要先清空set
            set.clear();
        }
        System.out.println(maxLen);
    }
}
