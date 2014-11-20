/**
 * JLCode
 * SortTest.java
 * jlcode
 * 
 */
package com.android.test.algthm;

import java.util.Random;

/*********************************************************
 * @author : zhaohua
 * @version : 2014-2-16
 * @see : 
 * @Copyright : copyrights reserved by personal 2007-2012
 **********************************************************/
public class SortTest
{ 
    /**
     * 快排分区，选定基准，小的low区，大的high区
     * @param data
     * @param low
     * @param higth
     * @param ascend true:升序；false:降序
     * @return
     */
    public static int partition(int[] data, int low, int higth, boolean ascend) {
        // 选第一个为基准
        int pivot = data[low];
        // 分区
        while(low < higth) {
            // 先分高区，循环找出地区的位置，并调到地区
            while(low < higth && data[higth] > pivot == ascend) higth--;
            data[low] = data[higth];
            // 再分地区，循环找出高区的位置，并调到高区
            while(low < higth && data[low] <= pivot == ascend) low++;
            data[higth] = data[low];
        }
        // low即为基准的位置
        data[low] = pivot;
        return low;
    }
    
    /**
     * 快排
     * @param data
     * @param low
     * @param higth
     * @param ascend true:升序；false:降序
     */
    public static void quickSort(int[] data, int low, int higth, boolean ascend) {
        // 分区条件
        if(low < higth) {
            // 分区
            int loc = partition(data, low, higth, ascend);
            // 分区loc的左边
            quickSort(data, low, loc-1,ascend);
            // 分区loc的右边
            quickSort(data, loc+1, higth, ascend);
        }
    }
    
    /**
     * 快排，分区，平均O(nlogn),最坏O(n2)
     * @param data
     * @param ascend true:升序；false:降序
     */
    public static void quickSort(int[] data,boolean ascend) {
        quickSort(data, 0, data.length -1, ascend);
    }
      
    /**
     * 插入排序，向一个有序子列表中插入数值，构成新的有序子列表
     * @param data
     * @param dx 步长
     * @param ascend
     */
    public static void insertSortDx(int[] data, int dx, boolean ascend) {
        int temp = 0, j = 0, n = data.length;
        for(int i = dx; i < n; i++) {
            if(data[i] < data[i-dx]) {
                temp = data[i];
                j = i-dx;
                while( j >= 0 && data[j] > temp) {
                    data[j +dx] = data[j];
                    j-=dx;
                }
                data[j+dx] = temp;
            }
        }
    }
    
    /**
     * 插入排序
     * @param data
     * @param ascend
     */
    public static void insertSort2Dx(int[] data,  int dx, boolean ascend) {
        int n = data.length;
        for(int i=dx; i < n; i++) {
            for(int j =i-dx; j>=0 && data[j] >data[j+dx]; j-=dx) {
                swap2(data, j, j+dx);
            }
        }
    }
    
    /**
     * 插入排序
     * @param data
     * @param ascend
     */
    public static void insertSort2(int[] data, boolean ascend) {
        insertSort2Dx(data, 1, ascend);
    }
   
    /**
     * 插入排序，向一个有序子列表中插入数值，构成新的有序子列表
     * @param data
     * @param ascend
     */
    public static void insertSort(int[] data, boolean ascend) {
        insertSortDx(data, 1, ascend);
    }
    
    /**
     * 希尔排序，
     * @param data
     * @param ascend
     */
    public static void shellSort(int[] data, boolean ascend) {
        for(int dx= data.length/2; dx > 0; dx/=2) {
            insertSortDx(data, dx, ascend);
        }
    }
    
    /**
     * 希尔排序，
     * @param data
     * @param ascend
     */
    public static void shellSort2(int[] data, boolean ascend) {
        for(int dx = data.length/2 ; dx > 0; dx /=2) {
            insertSort2Dx(data, dx, ascend);
        }
    }
   
    /**
     * 基数排序
     * @param data
     * @param d
     */
    public static void radixSort(int[] data, int d) {
        
        int n = 1;
        int k = 0;
        int len = data.length;
        int[][] radixArray  = new int[len][len];
        int[] tempArray = new int[10];
        
        int radix = 0;
        
        while(n <= d) {
            // 放入
            for(int v : data) {
                radix = (v/n)%10;
                radixArray[radix][tempArray[radix]] = v;
                tempArray[radix]++;
            }
            // 取出
            for(int i = 0; i < tempArray.length; i++) {
                if(tempArray[i] != 0) {
                    for(int j =0 ; j<tempArray[i]; j++ ) {
                        data[k] = radixArray[i][j];
                        k++;
                    }
                    tempArray[i] = 0;
                }
            }
            
            k = 0;
            n *=10;
        }
    }
       
    /**
     * 归并排序，递归合并有序子序列
     * @param data
     */
    public static void mergeSort(int[] data) {
        mergeSort(data,0,data.length-1,new int[data.length]);
    }
    
    public static void mergeSort(int[] data, int first, int last, int[] sorted) {
        if(first < last) {
            // 取中间位
            int mid = (first + last)/2;
            // 排序前段
            mergeSort(data, first, mid, sorted);
            // 排序后段
            mergeSort(data, mid+1, last, sorted);
            // 合并前后段
            merge(data, first, mid, last, sorted);
        }
    }
    
    public static void merge(int[] data, int first, int mid, int last, int[] sorted) {
        // 前段指针
        int i = first;
        // 后端指针
        int j = mid + 1;
        // 有序指针
        int k = 0;
        
        // 找出前段 与 后段中有序元素，直到一个完
        while(i <= mid && j<=last) {
            if(data[i] < data[j]) {
                sorted[k++] = data[i++];
            }else {
                sorted[k++] = data[j++];
            }
        }
        // 前段续接
        while(i <= mid) {
            sorted[k++] = data[i++];
        }
        // 后段续接
        while(j <= last) {
            sorted[k++] = data[j++];
        }
        // 更新有序序列
        for(int n = 0; n < k; n++) {
            data[first + n] = sorted[n];
        }
    }
    
    /**
     * 冒泡排序，一步一步交换上升到有序位置
     * @param data
     */
    public static void bublleSort(int[] data) {
        int n = data.length;
        boolean flag = false;
        
        for(int i = 0; i < n; i++) {
            flag = false;
            for(int j = 1; j < n - i; j++) {
                if(data[j] < data[j-1]) {
                    swap2(data, j, j-1);
                    flag = true;
                }
            }
            if(!flag) break;
        }
    }
    
    /**
     * 冒泡排序，一步一步交换上升到有序位置
     * @param data
     */
    public static void bublleSort2(int[] data) {
        int n = data.length;
        int flag = n;
        int k =0;
        
        while(flag > 0) {
            k = flag;
            flag = 0;
            for(int j = 1; j < k; j++) {
                if(data[j] < data[j-1]) {
                    swap2(data, j, j-1);
                    flag = j;
                }
            }
        }
    }
    
    public static void swap(int[] data, int p, int q) {
        int temp = data[p];
        data[p] = data[q];
        data[q] = temp;
    }
    
    public static void swap2(int[] data, int p, int q) {
        data[p] ^= data[q];
        data[q] ^= data[p];
        data[p] ^= data[q];
    }
    
    /**
     * 简单选择排序，循环选择有序序列，放置到有序列表中
     * @param data
     */
    public static void selectSort(int[] data) {
        int minIndex = 0;
        int temp = 0;
        for(int i =0; i < data.length; i++) {
            minIndex = i;
            for(int j=i+1; j<data.length; j++) {
                if(data[j] < data[minIndex]) {
                    minIndex = j;
                }
            }
            if(minIndex != i) {
                temp = data[i];
                data[i] = data[minIndex];
                data[minIndex] = temp;
            }
        }
    }
    
    /**
     * 将节点i进行正确的下沉调整,左右子叶子为 2*i + 1, 2*i +2
     * @param data
     * @param i 调整的节点i
     * @param n 长度为n
     */
    public static void minHeapFixDown(int[] data, int i, int n) {
        
        int temp = data[i];
        // 左叶子
        int j = 2*i + 1;
        while(j < n) {
            // 从左右叶子中找出最小的节点
            if(j+1 < n && data[j+1] < data[j]) {
                j++;
            }
            // 无效调整
            if(data[j] > temp) {
                break;
            }
            data[i] = data[j];
            i = j;
            j = 2*i +1;
        }
        data[i] = temp;
    }
    
    /**
     * 删除最小堆的第一个节点
     * @param data
     * @param n
     */
    public static void minHeapDel(int[] data, int n) {
        int temp = data[0];
        data[0] = data[n-1];
        data[n-1] = temp;
        minHeapFixDown(data, 0, n-1);
    }
    
    /**
     * 将节点i进行正确的上升调整，父节点为 (i-1)/2
     * @param data
     * @param i
     */
    public static void minHeapFixUp(int[] data, int i) {
        int temp = data[i];
        // 父节点
        int j = (i-1)/2;
        while(j >=0 && i != 0) {
            if(data[j] <= temp) {
                break;
            }
            data[i] = data[j];
            i = j;
            j = (i-1)/2;
        }
        data[i] = temp;
    }
     
    /**
     * 最小堆添加节点i
     * @param data
     * @param i
     * @param num
     */
    public static void minHeapAdd(int[] data, int i, int num) {
        data[i] = num;
        minHeapFixUp(data, i);
    }
    
    /**
     * 初始化最小堆
     * @param data
     */
    public static void minHeapMake(int[] data) {
        for(int i = data.length / 2 -1 ; i >=0 ; i--) {
            minHeapFixDown(data, i, data.length);
        }
    }
    
    /**
     * 最小堆排序
     * @param data
     */
    public static void minHeapSort(int[] data) {
        minHeapMake(data);
        for(int n = data.length; n >1 ; n--) {
            minHeapDel(data, n);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////
    private static Random random;
    private static int total = 50000;
    private static int spec = 1;
    public static int[] getData(int num) {
        if(random == null) {
            random = new Random(System.currentTimeMillis());    
        }
        int[] data = new int[num];
        for(int i=0;  i<num;  i++) {
            data[i] = random.nextInt();
        }
        return data;
    }
    public static void main(String[] args)
    {
        int[] data = getData(total  * spec);        
        int[] data1 = { 2, 999999999, 65, 24, 47, 13, 50, 1, 1, 3, 3, 5,92, 88, 66, 33, 22445, 10001, 624159, 624158, 624155501 };
       
        long startTime = System.currentTimeMillis();
        boolean printable = false;
//        quickSort(data,false);
//        insertSort(data, true);
//        insertSort2(data, true);
//        radixSort(data, 1000000000);
//        mergeSort(data);
//        bublleSort2(data);
//        selectSort(data1);
//        shellSort(data, true);
//        shellSort2(data, true);
//        minHeapSort(data);
        
        if(printable) {
            for(int i : data) {
                System.out.println(i);
            }
        }
        System.out.println("-->"+ total * spec +" nums use time = " + (System.currentTimeMillis() -startTime ) + "ms");
    }

}
