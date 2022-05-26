package 数组和矩阵;

import java.util.Scanner;

/**
 * 同不重复打印排序数组中相加和为给定值的所有二元组，解法就是在外面再加一层循环
 * 需要注意的是不重复打印arr中所有相加和为k的严格升序的三元组
 */
public class 不重复打印排序数组中相加和为给定值的所有三元组 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int k = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        for (int i = 0; i < n; i++) {
            int left = i + 1;
            int right = n - 1;
            if (i > 0 && arr[i] == arr[i - 1]) {
                continue;
            }
            while (left < right) {
                if (arr[i] + arr[left] + arr[right] == k) {
                    if ((left == 0 || arr[left] != arr[left - 1]) && arr[i] < arr[left] && arr[left] < arr[right]) {//判断是否有重复,且保证升序
                        System.out.println(arr[i] + " " + arr[left] + " " + arr[right]);
                    }
                    left++;
                    right--;
                } else if (arr[i] + arr[left] + arr[right] < k) {
                    left++;
                } else {
                    right--;
                }
            }
        }
    }
}
