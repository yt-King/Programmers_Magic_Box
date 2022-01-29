package 递归;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author 应涛
 * @date 2022/1/26
 * @function：
 */
public class 归并求逆序对 {
    public static int process(int arr[], int l, int r){
        if(l==r) return 0;
        int mid=(l+r)/2;
        return process(arr,l,mid)+process(arr,mid+1,r)+merge(arr,l,mid,r);
    }
    public static int merge(int[] arr, int l, int mid, int r){
        int[] help = new int[r-l+1];
        int p1=l;
        int i=0;
        int p2=mid+1;
        int res=0;
        while(p1<=mid && p2<=r){
            res+= arr[p1]>arr[p2]?(r-p2+1):0;
            help[i++]= arr[p1]>arr[p2]?arr[p1++]:arr[p2++];//如果改成>=结果会比正确的少,重点
        }
        while(p1<=mid){
            help[i++]=arr[p1++];
        }
        while(p2<=r){
            help[i++]=arr[p2++];
        }
        for ( i = 0; i < help.length; i++) {
            arr[l+i]=help[i];
        }
        return res;
    }
    public static void main(String[] args) {
        int[] arr={1,3,2,3,1};
        System.out.println("process(arr,0,4) = " + process(arr, 0, arr.length-1));
    }
}
