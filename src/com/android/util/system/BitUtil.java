package com.android.util.system;

/** 字节移位操作工具*/
public class BitUtil {

    public static long MASK_4F = +0x7FFFFFFFFFFFFFFFl;
    public static long MASK_FF = +0xFFFFFFFFFFFFFFFFl;
    public static long MASK_00 = +0x0000000000000000l;
    
    /**
     * 参数非法条件
     * start <0 || len <1 || start + len >= 64 || data < 0
     * @param data  64位 > 0
     * @param start  [0,64)
     * @param len    start + len < 64
     * @return
     */
    public static long read(long data, int start, int len) throws Exception{
        if(start <0 || len <1 || start + len >= 64 || data < 0) throw new Exception("Invalid Param!");
        return ( (data>>start) & ( MASK_4F >> (63-len)));
    }
    
    /**
     * 参数非法条件
     * start <0 || len <1 || start + len >= 64 || data < 0 ||  value <0 ||  value > ((1<<len) - 1)
     * @param data   64位 
     * @param start   [0,64)
     * @param len     start + len < 64
     * @param value  
     * @return
     * @throws Exception
     */
    public static long save(long data, int start, int len, int value) throws Exception{
        if(start <0 || len <1 || start + len >= 64 || data < 0 ||  value <0 ||  value > ((1<<len) - 1)) throw new Exception("Invalid Param!");
        return ((value & (MASK_4F >> (63-len))) << start) | (data & ((MASK_FF << (start + len)) | (MASK_4F>>(63- start))));
    }
       
    /**
     * 将index位置为1
     * @param bits
     * @param index
     */
    public static int setBit(int[] bits, int index) throws IndexOutOfBoundsException {
        if(index < 0 || index > bits.length*32) throw new IndexOutOfBoundsException("index = " + index);
        return bits[index>>5] |= 1<<(index&0x1F);
    }
    
    /**
     * 将index位重置为0
     * @param bits
     * @param index
     */
    public static int resetBit(int[] bits, int index) throws IndexOutOfBoundsException{
        if(index < 0 || index > bits.length*32) throw new IndexOutOfBoundsException("index = " + index);
        return bits[index>>5] &= ~(1<<(index&0x1F));
    }
    
    /**
     * 获取index位的值
     * @param bits
     * @param index
     */
    public static int getBit(int[] bits, int index) throws IndexOutOfBoundsException{
        if(index < 0 || index > bits.length*32) throw new IndexOutOfBoundsException("index = " + index);
        return ( bits[index>>5] >> (index&0x1F) ) & 1;
    }
    
    /**
     * 获取奇偶的指定步长的掩码，如0xAAAAAAAA,0xCCCCCCCC等。
     * @param dx
     * @param isdouble
     * @return
     */
    public static int getMask(int dx, boolean isdouble) {
        int dxMask = -1 >>>(32 -dx);
        int mask = 0;
        int start = isdouble ? dx : 0;
        for(; start <32; start+=2*dx) {
            mask |= (dxMask << start);
        }
        return mask;
    }
    
    /**
     * 逆序2进制
     * @param num
     * @return
     */
    public static int reverserBinary(int num) {
        int dx = 1;
        while(dx < 32) {
            num = ((num & getMask(dx, true)) >> dx) | (( num & getMask(dx, false)) << dx);
            dx<<=1;
        }
        return num;
    }
    
    /**
     * 获取2进制1的个数
     * @param num
     * @return
     */
    public static int get1Num1(int num) {
        int count = 0;
        for(int i = 0; i < 32; i++) {
            count += (num>>>i)&1;
        }
        return count;
    }
    
    /**
     * 获取2进制1的个数
     * @param num
     * @return
     */
    public static int get1Num2(int num) {
        int dx = 1;
        while(dx < 32) {
            num = ((num & getMask(dx, true)) >>> dx) + (( num & getMask(dx, false)));
            dx<<=1;
        }
        return num;
    }
    
    /**
     * 判断奇偶数，最后位为1则奇数
     * @param num
     * @return
     */
    public static boolean isDouble(int num) {
        return (num & 1) == 0;
    }

    /**
     * 符号变更
     * @param num
     * @return
     */
    public static int reverserSign(int num) {
        return ~num + 1;
    }
    
    /**
     * 高地位交换
     * @param num
     * @return
     */
    public static int reverserLH(int num) {
       return (num >>> 16) | (num << 16);
    }
    
    /**
     * 取绝对值
     * @param num
     * @return
     */
    public static int abs(int num) {
        // 正数，则为0，负数则为-1
        int i = num>>31;
//        return i==0 ? num : (~num + 1);
        return (num^i) - i;
    }
    
    public static void main(String[] args) {
        long data = 0;
        try {
//            data = save(data, 0, 2, 3);
//            System.out.println("##data = " + read(data, 0, 2));
//            data = save(data, 2, 2, 2);
//            System.out.println("##data = " + read(data, 2, 2));
//            data = save(data, 4, 4, 12);
//            System.out.println("##data = " + read(data, 4, 4));
//            data = save(data, 8, 3, 7);
//            System.out.println("##data = " + read(data, 8, 3));
//            data = save(data, 11, 8, 100);
//            System.out.println("##data = " + read(data, 11, 8));
//            data = save(data, 19, 1, 1);
//            System.out.println("##data = " + read(data, 19, 1));
            int[] bits = new int[2];
            int num = -34520;
            System.out.println( Integer.toBinaryString(num) + "reverserLH("+num+") = " +  Integer.toBinaryString(reverserLH(num)));
            System.out.println( "setBit(2) = " + setBit(bits,2));
            System.out.println( "getBit(2) = " + getBit(bits,5));
            System.out.println( "resetBit(2) = " + resetBit(bits,2));
            System.out.println( "isDouble(3) = " + isDouble(3));
            System.out.println( "reverserSign(-2) = " + reverserSign(-2));
            System.out.println( "abs(-2) = " + abs(-2));
            System.out.println( Integer.toHexString(getMask(4, false)));
            
            System.out.println( Integer.toBinaryString(num) + "-->" +Integer.toBinaryString(reverserBinary(num)));
            
            System.out.println( Integer.toBinaryString(num) + "-->1 size = " +get1Num1(num));
            System.out.println( Integer.toBinaryString(num) + "-->1 size = " +get1Num2(num));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}