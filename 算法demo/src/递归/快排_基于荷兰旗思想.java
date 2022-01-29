package 递归;

/**
 * @author 应涛
 * @date 2022/1/26
 * @function：
 */
public class 快排_基于荷兰旗思想 {
    public static void swap(int arr[], int i, int j) {
        int c = arr[i];
        arr[i] = arr[j];
        arr[j] = c;
    }

    public static void quickSort(int arr[], int l, int r) {
        if (l < r) {
            swap(arr, l + (int) (Math.random() * (r - l + 1)), r);
            int[] p = partition(arr, l, r);
            quickSort(arr, l, p[0] - 1);
            quickSort(arr, p[1] + 1, r);
        }
    }

    public static int[] partition(int arr[], int l, int r) {
        int less = l - 1;//小于区域的有边界
        int more = r;//大于区域的左边界，r作为划分值用于判断
        while (l < more) {//l代表当前位置的数，跟r上的划分值作比较
            if (arr[l] < arr[r]) {
                swap(arr, ++less, l++);//重点
            } else if (arr[l] > arr[r]) {
                swap(arr, l, --more);//重点
            } else {
                l++;
            }
        }
        swap(arr, more, r);
        return new int[]{less + 1, more};
    }


    public static void main(String[] args) {
        int[] arr = {-4,0,7,4,9,-5,-1,0,-7,-1};
        quickSort(arr, 0, arr.length - 1);
        for (int i : arr) {
            System.out.println("i = " + i);
        }
    }
}
