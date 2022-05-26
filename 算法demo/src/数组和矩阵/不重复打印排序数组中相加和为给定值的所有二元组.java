package 数组和矩阵;

import java.util.Scanner;

/**
 * 描述
 * 给定排序数组arr和整数k，不重复打印arr中所有相加和为k的不降序二元组
 * 例如, arr = [-8, -4, -3, 0, 1, 2, 4, 5, 8, 9], k = 10，打印结果为：
 * 1, 9
 * 2, 8
 * [要求]
 * 时间复杂度为O(n)O(n)，空间复杂度为O(1)O(1)
 * 输入描述：
 * 第一行有两个整数n, k
 * 接下来一行有n个整数表示数组内的元素
 * 输出描述：
 * 输出若干行，每行两个整数表示答案
 * 按二元组从小到大的顺序输出(二元组大小比较方式为每个依次比较二元组内每个数)
 * <p>
 * 输入：
 * 10 10
 * -8 -4 -3 0 1 2 4 5 8 9
 * 输出：
 * 1 9
 * 2 8
 */
public class 不重复打印排序数组中相加和为给定值的所有二元组 {
    /**
     * 思路：
     * 利用排序的特点，用左右指针不断向中间压缩的方式实现，注意要判断重复
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int k = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        int left = 0, right = n - 1; //左右指针指向数组两侧
        while (left < right) {
            if (arr[left] + arr[right] == k) {
                if (left == 0 || arr[left] != arr[left - 1]) {//判断是否有重复
                    System.out.println(arr[left] + " " + arr[right]);
                }
                left++;
                right--;
            } else if (arr[left] + arr[right] > k) {
                right--;
            } else {
                left++;
            }
        }
    }
}
