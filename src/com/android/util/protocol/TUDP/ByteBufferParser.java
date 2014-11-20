/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: ByteBuffer 协议解读者
 * @声明: copyrights reserved by 2007-2011
 *******************************************************/
package com.android.util.protocol.TUDP;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferParser implements IPacketParser
{
    private ByteBuffer buffer;
    // 默认为 UTF-8字节编码
    private String byte_code = "UTF-8";
    
    public ByteBufferParser(ByteBuffer body)
    {
        buffer = body;
    }
    
    public ByteBufferParser(byte[] body, int skipLen, ByteOrder order, String code)
    {
        buffer = ByteBuffer.wrap(body,skipLen,body.length-skipLen);
        buffer.order(order == null ? ByteOrder.LITTLE_ENDIAN : order);
        if(code != null) byte_code = code;
    }
    
    @Override
    public byte getByte()
    {
        return buffer.get();
    }

    @Override
    public char getChar()
    {
        return buffer.getChar();
    }
    @Override
    public short getShort()
    {
        return buffer.getShort();
    }

    @Override
    public int getInt()
    {
        return buffer.getInt();
    }

    @Override
    public float getFloat()
    {
        return buffer.getFloat();
    }

    @Override
    public long getLong()
    {
        return buffer.getLong();
    }

    @Override
    public double getDouble()
    {
        return buffer.getDouble();
    }

    @Override
    public void getBytes(byte[] data)
    {
        buffer.get(data);
    }

    @Override
    public String getString(int len)
    {
        byte[] dest  = new byte[len];
        buffer.get(dest);
        return PacketUtil.byte2String(dest, byte_code);
    }

    @Override
    public void skipBytes(int len)
    {
        buffer.position(buffer.position() + len);
    }
}
