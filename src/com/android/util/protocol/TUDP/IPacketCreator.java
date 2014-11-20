/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: 协议组装者
 * @声明: copyrights reserved by 2007-2011
 *******************************************************/
package com.android.util.protocol.TUDP;

public interface IPacketCreator
{
	public void putByte(byte b);
	public void putChar(char c);
	public void putShort(short s);
	public void putInt(int i);
	public void putFloat(float f);
    public void putLong(long l);
    public void putDouble(double d);
	public void putBytes(byte[] bytes);
	public void putString(String str, int len);
	public Object getBuffer();
	public void clear();
}
