/**
 * JLCode
 * SubSequeuTest.java
 * jlcode
 * 
 */
package com.android.test.algthm;


/*********************************************************
 * @author : zhaohua
 * @version : 2014-2-18
 * @see : 
 * @Copyright : copyrights reserved by personal 2007-2012
 **********************************************************/
public class SubSequeuTest
{

    /** 最大和子串*/
    public static void maxSubSum(int[] data) {
        
        int newBegin = 0,// 新序列的开始
            begin = 0, // 最大序列的开始
            end = 0,   // 最大序列的结束
            curSum = data[0],// 当前最大和
            sum = data[0];// 之前最大和
        int n = data.length;
        
        for(int i = 0; i <n ; i++) {
            curSum += data[i];
            // 更新最大和
            if(curSum > sum) {
                sum = curSum;
                end = i;
                begin = newBegin;
            }
            // 开始新的序列
            if(curSum < 0) {
                newBegin = i+1;
                curSum = 0;
            }
        }
        System.out.println("-->maxSum = "+ sum);
        for(int i = begin; i <= end; i++) {
            System.out.print("-->"+ data[i]);
        }
    }
    
    /** 最大公共子串*/
    public static void LCS(String s1, String s2) {
        
        char[] char1 = s1.toCharArray();
        char[] char2 = s2.toCharArray();
        int[][] matrix = new int[char1.length][char2.length];
        
        for(int i = 0; i <char1.length; i++) {
            for(int j = 0; j <char2.length; j++) {
                if(char1[i] == char2[j]) {
                    matrix[i][j] = 1;
                    if(i-1 >=0 && j-1 >=0) {
                        matrix[i][j] += matrix[i-1][j-1];
                    }
                }
            }
        }
        
        int maxLen = matrix[0][0];
        for(int i = 0; i <char1.length; i++) {
            for(int j = 0; j <char2.length; j++) {
                if(matrix[i][j] >= maxLen) {
                    maxLen = matrix[i][j];
                }
            }
        }
        System.out.println("-->max lcs = "+ maxLen + ":" );
        for(int i = 0; i <char1.length; i++) {
            for(int j = 0; j <char2.length; j++) {
                if(matrix[i][j] == maxLen) {
                    System.out.println("-->"+ s1.substring(i - maxLen +1, i+1));
                }
            }
        }
    }
    
    /** 最大公共子序列*/
    public static void LCSs(String s1, String s2) {
        char[] char1 = s1.toCharArray();
        char[] char2 = s2.toCharArray();
        int[][] matrix = new int[char1.length + 1][char2.length + 1];
        
        for(int i = 1; i <char1.length + 1; i++) {
            for(int j = 1; j <char2.length + 1; j++) {
                if(char1[i-1] == char2[j-1]) {
                    matrix[i][j] = 1 + matrix[i-1][j-1];
                }else {
                    matrix[i][j] = Math.max(matrix[i-1][j], matrix[i][j-1]);
                }
            }
        }
        System.out.println("lenght of LCS= "+matrix[char1.length][char2.length]);
        
        int p1 =char1.length ,p2 = char2.length;
        StringBuilder str = new StringBuilder();
        
        while(p1 >0 && p2 >0) {
            if(char1[p1-1] == char2[p2-1]) {
                str.insert(0, char1[p1-1]);
                p1--;
                p2--;
            }else
            // 左 > 上
            if(matrix[p1][p2-1] >= matrix[p1-1][p2]){
                p2--;
            }else {
                p1--;
            }
        }
        System.out.println(" LCSs= "+ str);
    }
    
    /** KMP子串模式匹配*/
    public static int KMP(char[] mStr, char[] pStr) {
        int p = 0;
        int m = 0;
        int[] next = getKMPNext(pStr);
        int mlen = mStr.length;
        int plen = pStr.length;
        
        while( m < mlen && p < plen) {
            if(p == -1 || mStr[m] == pStr[p]) {
                p++;
                m++;
            }else {
                p = next[p];
            }
        }
        
        if(p == plen) return m-plen;
        return -1;
    }
    
    /** KMP子串模式匹配Next,数组，其实是自己匹配自己*/
    public static int[] getKMPNext(char[] pStr) {
        int i = -1;
        int j = 0;
        int len = pStr.length;
        int[] next = new int[len];
        next[j] = -1;
        
        while(j < len-1) {
            if(i == -1 || pStr[i] == pStr[j]) {
                next[++j] = ++i;
            }else {
                i = next[i];
            }
        }
        return next;
    }
    
    public static void main(String[] args)
    {            
//        int[] data = SortTest.getData(100);
//        int[] data1 = { 2,-21 , 65, 24, -47, 2 , 13, -50, 1, 1, 3, 3, -5, 92, -88, 66, -33, 12};
//        int[] data2 = { -5, -4, -12, 0, -2, -4, -12, -13, -2, 0, -13,-6};
//        maxSubSum(data);
        
        String s1 = "ADECD", s2= "ECD";
        LCS(s1, s2);       
        LCSs(s1,s2);
        
        int p = KMP(s1.toCharArray(), s2.toCharArray());
        System.out.print("kmp = " + s1.substring(p, p+s2.length()));
    }
    
}
