package com.android.test.algthm;

import java.util.Random;

/** 
 * 从N个随机整数中寻找和为1000的对数并打印出来
 * */
public class Com1000 {

    private static Random random;
    private static int total = 100000;
    private static int spec = 1;
   
    public static void main(String[] args) {
        random = new Random(System.currentTimeMillis());
        long startTime = System.currentTimeMillis();
        
        com();
        
        System.out.println("-->"+ total * spec +" nums use time = " + (System.currentTimeMillis() -startTime ) + "ms");
    }
    
    public static void com() {
            int[] data = getData(total  * spec);
            int len = data.length;
            for(int i = 0; i < len; i ++) {
                for(int j = i+1; j < len; j++) {
                    if(data[j] != 0 && data[i] + data[j] == 1000) {
                        System.out.println("-->" +data[i] + " + " + data[j] + " = " + (data[i] + data[j]));
                        data[j] = 0;
                        break;
                    }
                }
            }
    }
    
    public static void commy() {
        CNum[] model = new CNum[1000];
        for(int j = 0; j < spec ; j++) {
            int[] data = getData(total);
            int len = data.length;
            MNode node = null;
            CNum   cnode = null;
            // 添加进模型中
            for(int i = 0 ; i< len; i++) {
                node = new MNode(data[i]);
                cnode = model[node.getIndex()];
                if(cnode == null) {
                    cnode = new CNum();
                    model[node.getIndex()] = cnode;
                }
                cnode.add(node);
            }
            // 在模型中进行匹配
            len = model.length;
            CNum cnode1 = null, cnode2 = null;
            for(int i =0; i < len ; i++) {
                cnode1 = model[i];
                cnode2 = model[(1000-i)%1000];
                if(cnode1 != null && cnode2 != null) {
                    cnode1.match(cnode2);
                }
            }
        }
    }
    
    public static int[] getData(int num) {
        int[] data = new int[num];
        for(int i=0;  i<num;  i++) {
            data[i] = random.nextInt();
        }
        int i = 0;
        data[i++] = 1000;
        data[i++] = 0;
        data[i++] = 1;
        data[i++] = 999;
        data[i++] = -1;
        data[i++] = 1001;
        data[i++] = 3;
        data[i++] = 997;
        data[i++] = -10;
        data[i++] = 1010;
        data[i++] = 2000;
        data[i++] = -1000;
        data[i++] = 6;
        data[i++] = 994;
        data[i++] = -3000;
        data[i++] = 4000;
        data[i++] = 1234;
        data[i++] = -234;
        
        return data;
    }
    
    static class MNode{
        /** 1000倍数*/
        int n;
        /** 余数 [0-999]*/
        short fact;
        /** 链表后一个*/
        MNode next;
        
        int num;
        
        public MNode(int num) {
            this.num = num;
            n = num/1000;
            fact = (short)(num%1000);
            if(fact < 0) {
                fact = (short)((fact + 1000)%1000);
                n -=1;
            }
        }
        
        public short getIndex() {
            return fact;
        }
        
        public boolean isTop() {
            return num > 0;
        }
        
        public int getN() {
            return n;
        }
        
        /**
         *  ==0 ; 正好匹配
         *  < 0  ;  停止匹配
         *  > 0  ;  继续匹配
         * */
        public int isTBMatch(MNode node) {
            if(fact ==0 ) {
                return n + node.n -1;
            }else {
                return n + node.n;
            }
        }
    }
    
    static class CNum{
        /** 正的n按照升序排列*/
        MNode top;
        /** 负的n按照降序排列*/
        MNode bottom;
        
        public void add(MNode node) {
            if(node.isTop()) {
                addTop(node);
            }else {
                addBottom(node);
            }
        }
        
        public void addTop(MNode node) {
            if(top == null) {
                top = node;
            }else {
                MNode p1 = top;
                MNode p2 = top;
                
                while(true) {
                    if(p2 == null) {
                        // 末尾
                        p1.next = node;
                        break;
                    }else
                    if(p2.getN() >= node.getN()) {
                        // 找到位置
                        node.next = p2;
                        if(p1 == p2) {
                            top = node;
                        }else {
                            p1.next = node;                          
                        }
                        break;
                    }else {
                        // 下一位
                        p1 = p2;
                        p2 = p1.next;
                    }
                }
            }
        }
        
        public void addBottom(MNode node) {
            if(bottom == null) {
                bottom = node;
            }else {
                MNode p1 = bottom;
                MNode p2 = bottom;
                
                while(true) {
                    if(p2 == null) {
                        // 末尾
                        p1.next = node;
                        break;
                    }else
                    if(p2.getN() <= node.getN()) {
                        // 找到位置
                        node.next = p2;
                        if(p1 == p2) {
                            bottom = node;
                        }else {
                            p1.next = node;                          
                        }
                        break;
                    }else {
                        // 下一位
                        p1 = p2;
                        p2 = p1.next;
                    }
                }
            }
        }
    
        public void match(CNum cnode) {
            matchTB(this, cnode);
            if(this != cnode) {
                matchTB(cnode, this);
                matchT0T0(cnode, this);
            }
        }
        
        public void matchTB(CNum topNode, CNum bottomNode) {
            MNode t1  =topNode.top,  t2 = t1;
            MNode b1 =bottomNode.bottom ,b2 = b1;
            int match = 0;
            
            while(t2 != null) {
                match = 1;
                while(b2 != null) {
                    match = t2.isTBMatch(b2);
                    if(match ==0 ) {
                        System.out.println("-->" + t2.num + " + " + b2.num + " = " + (t2.num + b2.num));
                        if(b1 == b2) {
                            bottomNode.bottom = b1.next;
                            b1.next = null;
                            b1 =bottomNode.bottom;
                            b2 = b1;
                        }else {
                            b1.next = b2.next;
                            b2.next = null;
                            b2 = b1.next;
                        }
                        break;
                    }else
                    if(match > 0) {
                        b1 = b2;
                        b2= b1.next;
                    }else {
                        break;
                    }
                }
                if(match == 0) {
                    if(t1 == t2) {
                        topNode.top = t1.next;
                        t1.next = null;
                        
                        t1  =topNode.top;
                        t2 = t1;
                    }else {
                        t1.next = t2.next;
                        t2.next = null;
                        t2 = t1.next;
                    }
                }else {
                    t1 = t2;
                    t2 = t1.next;
                }
            }
        }
        
        public void matchT0T0(CNum topNode, CNum bottomNode) {
           if(topNode.top.n == 0 && topNode.top.n == bottomNode.top.n) {
               System.out.println("-->" + topNode.top.num + " + " + bottomNode.top.num + " = " + (topNode.top.num + bottomNode.top.num));
               topNode.top = topNode.top.next;
               bottomNode.top = bottomNode.top.next;
           }
        }
    }
}