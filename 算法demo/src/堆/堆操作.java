package 堆;

/**
 * @author 应涛
 * @date 2022/1/26
 * @function：
 */
public class 堆操作 {
    public static void swap(int arr[], int i, int j) {
        int c = arr[i];
        arr[i] = arr[j];
        arr[j] = c;
    }

    //上移操作
    public static void heapInsert(int arr[], int index) {
        while (arr[index] > arr[(index - 1) / 2]) {
            swap(arr, index, (index - 1) / 2);
            index = (index - 1) / 2;
        }
    }

    //下沉操作
    public static void heapify(int arr[], int index, int heapsize) {
        int left = index * 2 + 1;//左孩子位置
        while (left < heapsize) {
            //先找到孩子结点的较大的下标位置
            int largest = left + 1 < heapsize && arr[left + 1] > arr[left] ? left + 1 : left;
            //在比较largest和父节点，谁大就把下标给largest
            largest = arr[largest] > arr[index] ? largest : index;
            if (index == largest) break;
            swap(arr, index, largest);
            index = largest;//交换结束后largest作为新的index
            left = index * 2 + 1;
        }
    }

    //堆排序
    public static void heapSort(int[] arr) {
        if (arr == null || arr.length < 1)
            return;
        for (int i = 0; i < arr.length; i++) {
            heapInsert(arr, i);
        }
        int heapsize = arr.length - 1;
        swap(arr, 0, heapsize);
        while (heapsize > 0) {
            heapify(arr, 0, heapsize--);
            swap(arr, 0, heapsize);
        }
    }

    public static void main(String[] args) {
        int[] arr = {-4, 0, 7, 4, 9, -5, -1, 0, -7, -1};
        heapSort(arr);
        for (int i : arr) {
            System.out.println("i = " + i);
        }
    }
}