/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: 协议解读者
 * @声明: copyrights reserved by 2007-2011
 *******************************************************/
package com.android.util.protocol.TUDP;


public interface IPacketParser
{	
	public byte getByte();
	public char getChar();
	public short getShort();
	public int getInt();
	public float getFloat();
	public long getLong();
	public double getDouble();
	public void getBytes(byte[] data);
	public String getString(int len);
	public void skipBytes(int len);
}
