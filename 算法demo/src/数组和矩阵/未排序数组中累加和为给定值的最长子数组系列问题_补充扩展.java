package 数组和矩阵;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class 未排序数组中累加和为给定值的最长子数组系列问题_补充扩展 {
    public static void first(int[] arr, int k) {
        //给定一个无序数组arr，其中元素可正、可负、可0。求arr所有子数组中正数与负数个数相等的最长子数组的长度
        //只需要把arr中的元素按照正负0分类，正数按1，负数按-1，0不变，令k=0即可
        Map map = new HashMap();
        map.put(0, -1);//要考虑到从0开始的子数组情况
        int sum = 0;// 当前最长子数组的和
        int maxLen = 0;// 当前最长子数组的长度
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i] == 0 ? 0 : arr[i] > 0 ? 1 : -1;
            if (map.containsKey(sum - k)) {
                //如果存在sum-k，则说明sum-k在前面出现过，则说明以arr[i]结尾的子数组存在
                //而且记录的位置是最早出现的位置，是当前情况下的最长子数组
                maxLen = Math.max(maxLen, i - ((int) map.get(sum - k)));
            }
            if (!map.containsKey(sum)) {
                //如果当前表中不存在sum，则说明当前位置是第一次出现sum的位置，把当前位置记录下来
                map.put(sum, i);
            }
        }
        System.out.println(maxLen);
    }

    public static void second(int[] arr, int k) {
        //给定一个无序数组arr，其中元素只能是1或0。求arr所有的子数组中0和1个数相等的最长子数组的长度
        //只需要把arr中的元素分类，正数按1，0按-1，令k=0即可
        Map map = new HashMap();
        map.put(0, -1);//要考虑到从0开始的子数组情况
        int sum = 0;// 当前最长子数组的和
        int maxLen = 0;// 当前最长子数组的长度
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i] == 0 ? -1 : 1;
            if (map.containsKey(sum - k)) {
                //如果存在sum-k，则说明sum-k在前面出现过，则说明以arr[i]结尾的子数组存在
                //而且记录的位置是最早出现的位置，是当前情况下的最长子数组
                maxLen = Math.max(maxLen, i - ((int) map.get(sum - k)));
            }
            if (!map.containsKey(sum)) {
                //如果当前表中不存在sum，则说明当前位置是第一次出现sum的位置，把当前位置记录下来
                map.put(sum, i);
            }
        }
        System.out.println(maxLen);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        int k = 0;
//        first(arr, k);
        second(arr,k);
    }
}
