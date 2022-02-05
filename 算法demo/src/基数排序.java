import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author 应涛
 * @date 2022/1/26
 * @function：
 */
public class 基数排序 {

    public static int getDigit(int x, int d) { //拿到第d位上的数字
        return ((x / ((int) Math.pow(10, d - 1))) % 10);
    }

    public static int maxbits(int[] arr) { //拿到数组中最大的位数
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            max = Math.max(max, arr[i]);
        }
        int res = 0;
        while (max != 0) {
            res++;
            max /= 10;
        }
        return res;
    }
    //从个位开始对每一位进行排序
    public static void radixSort(int[] arr, int l, int r, int bigit) {
        final int radix = 10;
        int i = 0, j = 0;
        //辅助空间存储
        int[] temp = new int[r - l + 1];
        for (int d = 1; d <= bigit; d++) {//有几位就进出几次
            int[] count = new int[radix];//存放每位数字出现的次数
            for (i = l; i <= r; i++) {
                count[getDigit(arr[i], d)]++;//统计次数
            }
            for (i = 1; i < radix; i++) {
                count[i] += count[i - 1]; //累计求和
            }
            for (i = r; i >= l; i--) { //从最后开始
                j = count[getDigit(arr[i], d)];//应该放在第几位
                temp[j-1]=arr[i];
                count[getDigit(arr[i], d)]--;//放完以后位子前移一个
            }
            for (i = l, j = 0; i <= r; i++, j++) {
                arr[i] = temp[j];
            }
        }
    }

    public static void main(String[] args) {
        int[] arr = {4, 0, 7, 4, 9, 5, 1, 0, 7, 1};
        radixSort(arr,0,arr.length-1,maxbits(arr));
        for (int i : arr) {
            System.out.println("i = " + i);
        }
    }
}