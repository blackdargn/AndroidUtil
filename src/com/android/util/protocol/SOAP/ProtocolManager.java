package com.android.util.protocol.SOAP;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-10
 * @see : 协议管理类
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class ProtocolManager
{
    private ThreadPoolExecutor threadPool;
    private static ProtocolManager instance;
    public static ProtocolManager getProtocolManager()
    {
        if(instance == null)
        {
            instance = new ProtocolManager();
        }
        return instance;
    }
    private ProtocolManager()
    {
        threadPool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
    }
    
    public boolean submitAction(SoapAction<?> action)
    {
        if(action == null || !action.isValid()) return false;
        try {
            Future<?> furture = threadPool.submit(new ActionTask(action));
            action.setFuture(furture);
            return true;
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    private class ActionTask implements Runnable
    {
        private SoapAction<?> action;
        
        public ActionTask(SoapAction<?> action)
        {
            this.action = action;
        }
        
        @Override
        public void run()
        {
            SoapObject soapObject = new SoapObject(action.getTargetNameSpace(), action.getMethod());
            if(action.hasParams())
            {
                action.putParams(soapObject);
            }
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(action.VERSION);
            envelope.setOutputSoapObject(soapObject);
            HttpTransportSE httpTranstation=new HttpTransportSE(action.getWsdl());
            try
            {
                httpTranstation.call(action.getSoapAction(), envelope);
                Object result = envelope.getResponse();
                action.onFinish(result.toString());
                return;
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (XmlPullParserException e)
            {
                e.printStackTrace();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
            action.onError();
        }
    }
}
