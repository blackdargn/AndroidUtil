/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: Netty ChannelBuffer 协议组装者
 * @声明: copyrights reserved by 2007-2011
 *******************************************************/
package com.android.util.protocol.TUDP;
import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class ChannelBufferCreator implements IPacketCreator
{
	private ChannelBuffer buffer;
	// 默认为 UTF-8字节编码
	private String byte_code = "UTF-8";
	// 默认为 小端
	private ByteOrder byte_order = ByteOrder.LITTLE_ENDIAN;
	
	public ChannelBufferCreator(int packMaxLen)
	{			    
	    buffer = ChannelBuffers.dynamicBuffer(byte_order, packMaxLen);
	}
	
	public ChannelBufferCreator(ByteOrder order, String code, int packMaxLen)
    {
	    if(order != null) byte_order = order;
	    if(code != null) byte_code = code;
        buffer = ChannelBuffers.dynamicBuffer(byte_order, packMaxLen);
    }
	
	@Override
	public void putByte(byte b) 
	{
		buffer.writeByte(b);
	}
	@Override
	public void putChar(char c)
	{
		buffer.writeByte(c);
	}
	@Override
	public void putBytes(byte[] bytes)
	{
		buffer.writeBytes(bytes);
	}
	@Override
	public void putString(String str, int len)
	{		
		putBytes(PacketUtil.string2Byte(str, len, byte_code));
	}
	@Override
	public void putShort(short in) 
	{
		buffer.writeShort(in);
	}
	@Override
	public void putInt(int in) 
	{
		buffer.writeInt(in);
	}
	@Override
	public void putFloat(float f) 
	{
		buffer.writeFloat(f);
	}
	@Override
	public void putLong(long l) 
	{
		buffer.writeLong(l);
	}
	@Override
    public void putDouble(double d)
    {
        buffer.writeDouble(d);
    }
	@Override
	public ChannelBuffer getBuffer()
	{
		return buffer;
	}
	@Override
	public void clear()
	{
		buffer.clear();
	}
}
