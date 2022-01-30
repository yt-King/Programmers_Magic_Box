package 堆;

import javax.swing.*;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author 应涛
 * @date 2022/1/26
 * @function：
 */
public class K范围内有序的排序问题 {
    public static void sortArrDistanceLessK(int[] arr, int k) {
        PriorityQueue<Integer> heap = new PriorityQueue<>(new comp());
        int index = 0;
        for (; index < Math.min(arr.length, k); index++) {
            heap.add(arr[index]);
        }
        int i = 0;
        for (; index < arr.length; i++) {
            arr[i] = heap.poll();
            heap.add(arr[index++]);
        }
        while (!heap.isEmpty())
            arr[i++] = heap.poll();
    }

    static class comp implements Comparator<Integer> {//比较器
        @Override
        public int compare(Integer o1, Integer o2) {
            return o2-o1;
        }
    }

    public static void main(String[] args) {
        int[] arr = {-4, 0, 7, 4, 9, -5, -1, 0, -7, -1};
        sortArrDistanceLessK(arr, arr.length);
        for (int i : arr) {
            System.out.println("i = " + i);
        }
    }
}