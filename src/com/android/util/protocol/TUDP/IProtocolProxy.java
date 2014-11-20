package com.android.util.protocol.TUDP;

import android.util.Log;

public abstract class IProtocolProxy
{
    private static final String TAG = "IProtocolProxy";
    private ProtocolFactory factory;
    
    public abstract boolean start();
    public abstract void stop();
    public abstract void send(OutPacket p);
    public abstract boolean reset(String serverAddr, int serverPort);
    
    public final void onReceived(InPacket packet)
    {
        if(factory !=null)
        {
            factory.onReceived(packet);
        }else
        {
            Log.e(TAG,"call this methid must init ProtocolFactory!");
        }
    }
    
    void setFactory(ProtocolFactory factory)
    {
        this.factory = factory;
    }
}
