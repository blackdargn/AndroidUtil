/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: ByteBuffer 协议组装者
 * @声明: copyrights reserved by 2007-2011
 *******************************************************/
package com.android.util.protocol.TUDP;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferCreator implements IPacketCreator
{
    private ByteBuffer buffer;
    // 默认为 UTF-8字节编码
    private String byte_code = "UTF-8";
    // 默认为 小端
    private ByteOrder byte_order = ByteOrder.LITTLE_ENDIAN;
    
    public ByteBufferCreator(int packMaxLen)
    {
        buffer = ByteBuffer.allocate(packMaxLen);
        buffer.order(byte_order);
    }
    
    public ByteBufferCreator(ByteOrder order, String code,int packMaxLen)
    {
        if(order != null) byte_order = order;
        if(code != null) byte_code = code;
        buffer = ByteBuffer.allocate(packMaxLen);
        buffer.order(byte_order);
    }
    
    @Override
    public void putByte(byte b)
    {
        buffer.put(b);
    }

    @Override
    public void putShort(short s)
    {
        buffer.putShort(s);
    }
    @Override
    public void putChar(char c)
    {
        buffer.putChar(c);
    }

    @Override
    public void putBytes(byte[] bytes)
    {
        buffer.put(bytes);
    }
    @Override
    public void putString(String str, int len)
    {
        buffer.put(PacketUtil.string2Byte(str, len, byte_code));
    }

    @Override
    public void putInt(int in)
    {
        buffer.putInt(in);
    }
    
    @Override
    public void putFloat(float f)
    {
        buffer.putFloat(f);
    }
    @Override
    public void putLong(long l)
    {
        buffer.putLong(l);
    }

    @Override
    public void putDouble(double d)
    {
        buffer.putDouble(d);
    }
    
    @Override
    public ByteBuffer getBuffer()
    {
        return buffer;
    }

    @Override
    public void clear()
    {
        buffer.clear();
    }
}
