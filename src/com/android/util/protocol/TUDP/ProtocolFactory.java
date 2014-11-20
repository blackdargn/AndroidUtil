/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: 协议工厂，负责协议正常运作
 * @声明: copyrights reserved by 2007-2011
 *******************************************************/
package com.android.util.protocol.TUDP;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.android.util.system.Log;
import com.android.util.thread.CycledThread;

public class ProtocolFactory
{
    public static final String TAG = "ProtocolFactory";
    /** 协议代理接口 */
    private IProtocolProxy proxy;
    private PacketManager packetManager;
    /** 运转线程 */
    private CycledThread sendThread;
    /** 待发实时堆栈，及时响应用户最新的请求 */
    private Stack<OutPacket> realStack = new Stack<OutPacket>();
    /** 待发后台队列，后台操作请求，如文件等*/
    private ConcurrentLinkedQueue<OutPacket> delayQueue = new ConcurrentLinkedQueue<OutPacket>();
    /** 重发队列, 单线程访问 */
    private Queue<OutPacket> resendQueue = new LinkedList<OutPacket>();
    /** 状态映射，用于重发与接收状态判定*/
    private ConcurrentHashMap<Integer, OutPacket> statusMap = new ConcurrentHashMap<Integer, OutPacket>();
    
    /** 最大重发次数*/
    public static final int MAX_RESENT_NUM = 3;
    /** 重发超时时间，可根据网络质量而定*/
    public static final int TIMEOUT_RESENT = 3000;
    /** 最大超时时间，可根据网络质量而定*/
    public static final int MAX_TIMEOUT = 9000;
    
    public ProtocolFactory(IProtocolProxy proxy, PacketManager packetManager)
    {
        this.proxy = proxy;
        this.packetManager = packetManager;
        this.proxy.setFactory(this);
    }
    
    /** 启动 网络服务模块 */
    public boolean start()
    {
        // 重置
        reset();
        // 启动服务代理
        if(proxy.start())
        {
            // 启动发送线程
            sendThread = new CycledThread(sendTask,50);
            sendThread.start();
            return true;
        }else
        {
            return false;
        }
    }
    
    /** 停止网络服务模块 */
    public void stop()
    {
        // 停止发送线程
        if(sendThread != null)
        {
            sendThread._stop();
            sendThread = null;
        }
        // 停止协议服务
        if(proxy != null)
        {
            proxy.stop();
        }
    }
    
    /** 是否正在运行 */
    public boolean isRunning()
    {
        return sendThread != null;
    }
    
    /** 重置服务端 */
    public synchronized boolean resetServerPort(String serverAddr, int serverPort)
    {
        return proxy.reset(serverAddr, serverPort);
    }
    
    /**添加到发送队列*/
    public void addSendQueue(OutPacket outpacket)
    {
        if (proxy == null)
        {
            Log.e(TAG, " IProtocolProxy is null!");
            return;
        }
        if (outpacket == null)
        {
            Log.e(TAG, "packet is null!");
            return;
        }
        
        if(outpacket.isReal())
        {
            realStack.push(outpacket);
        }else
        {
            delayQueue.offer(outpacket);
        }
        // 启动睡眠的发送线程,如果已经睡眠的话
        if(sendThread != null)
        {
            sendThread._resume();
        }
    }
      
    /** 超时处理*/
    public void onTimeOut(OutPacket p)
    {
        // 交与上层PacketManager处理
        packetManager.onTimeoutPacket(p);
        p.dump();
    }
    
    /** 接收处理*/
    public void onReceived(InPacket p)
    {
        // 更新状态，如果有的话
        if(statusMap.containsKey(p.getReplyKey()))
        {
            OutPacket op = statusMap.get(p.getReplyKey());
            op.isReplyed.set(true);
        }
        // 交与上层PacketManager处理
        packetManager.onReceivedPacket(p);
    }
    
    /** 发送任务*/  
    private Runnable sendTask = new Runnable()
    {
        @Override
        public void run()
        {       
            // 预先处理状态映射
            if(!statusMap.isEmpty())
            {               
                // 主要是处理重发 、超时、 已接收，完后，进入下一阶段
                Iterator<OutPacket> it = statusMap.values().iterator();
                while(it.hasNext())
                {
                    OutPacket p = it.next();
                    // 是否已回复
                    if(p.isReplyed.get())
                    {
                        // 已回复，移除
                        it.remove();
                    }else
                    // 是否超时
                    if(p.isTimeOutable(MAX_TIMEOUT, MAX_RESENT_NUM))
                    {
                        it.remove();
                        // 报告超时
                        onTimeOut(p);
                    }else
                    // 没有超时，则重发
                    if(p.isReSentable(TIMEOUT_RESENT))
                    {
                        it.remove();
                        // 待发送队列
                        resendQueue.offer(p);
                    }else
                    {
                        // do nothing ...
                    }
                }
            }else
            // 没有状态处理
            {
                
            }
            // 优先发送实时栈中的协议包
            if( !realStack.isEmpty())
            {               
                send(realStack.pop());
            }else
            {               
                // 再处理重发队列
                if(!resendQueue.isEmpty())
                {
                    send(resendQueue.poll());
                }else
                {
                    // 再处理延时发送队列
                    if( !delayQueue.isEmpty())
                    {
                        send(delayQueue.poll());
                    }else
                    {
                        // 没有发送处理
                        
                    }
                }
            }
            // 没有任何处理，则暂停线程, 直到有发送进入唤醒线程
            if( statusMap.isEmpty() && realStack.isEmpty() 
             && resendQueue.isEmpty() && delayQueue.isEmpty())
            {               
                sendThread._pause();
            }
        }
    };
    
    /**发送实时包*/
    private void send(OutPacket outpacket)
    {
        // 回复设定
        if(outpacket.isNeedReply())
        {
            statusMap.put(outpacket.getCmd(), outpacket);
        }
        // 没有收到回复的则发送
        if( !outpacket.isReplyed.get() )
        {
            // 发送通道
            proxy.send(outpacket);
        }
    }
    
    /** 重置状态 */
    private void reset()
    {
        realStack.clear();
        delayQueue.clear();
        resendQueue.clear();
        statusMap.clear();
    }
}
