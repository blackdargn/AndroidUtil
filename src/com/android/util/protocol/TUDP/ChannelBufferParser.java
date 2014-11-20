/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: Netty ChannelBuffer 协议解读者
 * @声明: copyrights reserved by 2007-2011
 *******************************************************/
package com.android.util.protocol.TUDP;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class ChannelBufferParser implements IPacketParser
{	
	private ChannelBuffer dataBuffer;
	// 默认为 UTF-8字节编码
    private String byte_code = "UTF-8";
    
	public ChannelBufferParser(ChannelBuffer body)
	{
		this.dataBuffer = body;		
	}
	
	public ChannelBufferParser(ChannelBuffer body,String code)
    {
        this.dataBuffer = body; 
        if(code != null) byte_code = code;
    }
	
	public ChannelBufferParser(byte[] body, int skipLen, ByteOrder order, String code)
	{
		ByteBuffer buf = ByteBuffer.wrap(body,skipLen,body.length-skipLen);
		buf.order(order == null ? ByteOrder.LITTLE_ENDIAN : order);
		this.dataBuffer = ChannelBuffers.wrappedBuffer(buf);
		if(code != null) byte_code = code;
	}
	
	@Override
	public byte getByte()
	{
		return dataBuffer.readByte();
	}
	@Override
	public char getChar()
	{
		return dataBuffer.readChar();
	}
	@Override
	public short getShort()
	{
		return dataBuffer.readShort();
	}
	@Override
	public int getInt()
	{
		return dataBuffer.readInt();
	}
	@Override
	public float getFloat()
	{
		return dataBuffer.readFloat();
	}
	@Override
	public long getLong()
	{
		return dataBuffer.readLong();
	}
	@Override
	public double getDouble()
	{
		return dataBuffer.readDouble();
	}
	@Override
	public void getBytes(byte[] data)
	{
		dataBuffer.readBytes(data);
	}
	@Override
	public String getString(int len)
	{		
		byte[] dest  = new byte[len];
		dataBuffer.readBytes(dest);		
		return PacketUtil.byte2String(dest, byte_code);
	}
	@Override
	public void skipBytes(int len)
	{
		dataBuffer.skipBytes(len);
	}
}
