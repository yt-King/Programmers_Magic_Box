package 链表;
import java.util.*;

public class 打印两个升序链表的公共部分 {
        public static void main(String[] args) {
            Scanner scan = new Scanner(System.in);
            int n = scan.nextInt();
            int[] nums1 = new int[n];
            for (int i = 0; i < n; i++) {
                nums1[i] = scan.nextInt();
            }
            int m = scan.nextInt();
            int[] nums2 = new int[m];
            for (int i = 0; i < m; i++) {
                nums2[i] = scan.nextInt();
            }
            int index1 = 0, index2 = 0;
            while (index1 < n && index2 < m) {
                if (nums1[index1] == nums2[index2]) {
                    System.out.print(nums1[index1] + " ");
                    index1++;
                    index2++;
                } else if (nums1[index1] < nums2[index2]) {
                    index1++;
                } else {
                    index2++;
                }
            }
        }
}
