package 数组和矩阵;

import java.util.LinkedList;
import java.util.Scanner;

/**
 * 描述
 * 有一个整型数组arr和一个大小为w的窗口从数组的最左边滑到最右边，窗口每次向右边滑一个位置，求每一种窗口状态下的最大值。（如果数组长度为n，窗口大小为w，则一共产生n-w+1个窗口的最大值）
 * 输入描述：
 * 第一行输入n和w，分别代表数组长度和窗口大小
 * 第二行输入n个整数X_i，表示数组中的各个元素
 * 输出描述：
 * 输出一个长度为n-w+1的数组res，res[i]表示每一种窗口状态下的最大值
 * <p>
 * 输入：
 * 8 3
 * 4 3 5 4 3 3 6 7
 * 输出：
 * 5 5 5 4 6 7
 * 说明：
 * 例如，数组为[4，3，5，4，3，3，6，7]，窗口大小为3时：
 * <p>
 * [4 3 5] 4 3 3 6 7        窗口中最大值为5
 * <p>
 * 4 [3 5 4] 3 3 6 7        窗口中最大值为5
 * <p>
 * 4 3 [5 4 3] 3 6 7        窗口中最大值为5
 * <p>
 * 4 3 5 [4 3 3] 6 7        窗口中最大值为4
 * <p>
 * 4 3 5 4 [3 3 6] 7        窗口中最大值为6
 * <p>
 * 4 3 5 4 3 [3 6 7]        窗口中最大值为7
 * <p>
 * 输出的结果为{5 5 5 4 6 7}
 */
public class 生成窗口最大值数组 {
    /**
     * 首先生成双端队列qmax。其次，在窗口滑动时，判断队首元素是否为刚刚退出滑动窗口的元素，是则将其弹出队首。
     * 然后在窗口初始化或滑动的时候，判断新元素与qmax队尾元素的大小（qmax为空则直接放入新元素）。
     * 如果新元素大，则qmax队尾元素一直poll，直到qmax为空或者队尾元素大于等于新元素，再将新元素放入qmax队尾。
     * @param args
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int w = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        //用来保存成为最大窗口的元素
        int[] result = new int[arr.length - w + 1];
        int index = 0;
        //用链表从当双向队列。
        LinkedList<Integer> temp = new LinkedList<>();
        //刚才演示的时候，我i直接从i = w-1那里开始演示了。
        for (int i = 0; i < arr.length; i++) {
            //如果队列不为空，并且存放在队尾的元素小于等于当前元素，那么
            //队列的这个元素就可以弹出了，因为他不可能会是窗口最大值。
            //【当前元素】指的是窗口向右移动的时候新加入的元素。
            while (!temp.isEmpty() && arr[temp.peekLast()] <= arr[i]) {
                temp.pollLast();//把队尾元素弹出
            }
            //把【当前元素】的下边加入到队尾
            temp.addLast(i);
            //如果队首的元素不在窗口范围内，则弹出
            if (temp.peekFirst() == i - w) {
                temp.pollFirst();//
            }
            if (i >= w - 1) {
                //由于队首存放的是最大值，所以队首总是对应窗口的最大值元素
                result[index++] = arr[temp.peekFirst()];
            }
        }
        for (int i = 0; i < result.length; i++) {
            System.out.println(result[i] + " ");
        }
    }
}
