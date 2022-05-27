package 数组和矩阵;

import java.util.Scanner;

/**
 * 描述
 * 给定一个数组arr，该数组无序，但每个值均为正数，再给定一个正数k。求arr的所有子数组中所有元素相加和为k的最长子数组的长度
 * 例如，arr = [1, 2, 1, 1, 1], k = 3
 * 累加和为3的最长子数组为[1, 1, 1]，所以结果返回3
 * [要求]
 * 时间复杂度为O(n)O(n)，空间复杂度为O(1)O(1)
 * <p>
 * 输入描述：
 * 第一行两个整数N, k。N表示数组长度，k的定义已在题目描述中给出
 * 第二行N个整数表示数组内的数
 * 输出描述：
 * 输出一个整数表示答案
 * <p>
 * 输入：
 * 5 3
 * 1 2 1 1 1
 * 输出：
 * 3
 */
public class 未排序正数数组中累加和为给定值的最长子数组的长度 {
    /**
     *注意子数组的概念，因为是连续的一段你，所以一般都可以用一个左右指针的思路进行解题
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int k = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        int left = 0;// 左指针
        int right = 0;// 右指针
        int res = 0;
        int sum = 0;// 当前最长子数组的和
        while (right < n) {
            if (sum + arr[right] == k) {
                res = Math.max(res, right - left + 1);
                sum += arr[right];
                right++;
            } else if (sum +arr[right]< k) {
                sum += arr[right];
                right++;
            } else {
                sum -= arr[left];
                left++;
            }
        }
        System.out.println(res);
    }
}
