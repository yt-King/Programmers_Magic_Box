package 链表;

/**
 * @author 应涛
 * @date 2022/2/10
 * 不考虑空间的话直接把链表压栈，然后从头开始比对
 * 不使用额外空间的话考虑快慢指针，然后逆转后半段链表进行比对
 */
public class 判断回文链表 {
    //链表结构,给定的是一个单链表
    static class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }
    }


     static boolean isPalindrome(ListNode head) {
        if (head == null) {
            return true;
        }

        // 找到前半部分链表的尾节点并反转后半部分链表
        ListNode firstHalfEnd = endOfFirstHalf(head); //中点位置（靠前）
        ListNode secondHalfStart = reverseList(firstHalfEnd.next);

        // 判断是否回文
        ListNode p1 = head;
        ListNode p2 = secondHalfStart;
        boolean result = true;
        while (result && p2 != null) {
            if (p1.val != p2.val) {
                result = false;
            }
            p1 = p1.next;
            p2 = p2.next;
        }

        // 还原链表并返回结果
        firstHalfEnd.next = reverseList(secondHalfStart);
        return result;
    }

    static ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode curr = head;
        while (curr != null) {
            ListNode nextTemp = curr.next;
            curr.next = prev;
            prev = curr;
            curr = nextTemp;
        }
        return prev;
    }

    static ListNode endOfFirstHalf(ListNode head) {
        ListNode fast = head;
        ListNode slow = head;
        while (fast.next != null && fast.next.next != null) {
            fast = fast.next.next;
            slow = slow.next;
        }
        return slow;
    }

    public static void main(String[] args) {
        ListNode head = new ListNode(1);
        head.next=new ListNode(2);
        head.next.next=new ListNode(1);
        System.out.println("isPalindrome(head) = " + isPalindrome(head));
    }
}
