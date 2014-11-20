package com.android.util.protocol.SOAP;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Future;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;

import android.text.TextUtils;

import com.android.util.system.MyApplication;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-8-10
 * @see : webservice soap action base
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public abstract class SoapAction<T>
{
    public static enum ACTION_TYPE
    {
        ACTION_SYSTEM, ACTION_COMMON, ACTION_SHOP
    }

    public final int VERSION = SoapEnvelope.VER12;

    private String wsdl;

    private String targetNameSpace;

    private String method;

    private HashMap<String, Object> params;
    
    private JSONObject infoJsonObject;
    
    private ActionListener<T> listener;

    private Future<?> future;

    private T result;
    private boolean isLoading = false;
    
    protected abstract T parseJson(String response) throws Exception;

    public SoapAction(ACTION_TYPE type,String method)
    {
        if (type == ACTION_TYPE.ACTION_SYSTEM)
        {
            wsdl = "http://info.dg11185.com/services/systemService?wsdl";
            targetNameSpace = "http://system.shake.mobile.api.zx.dg11185.com";
        } else if (type == ACTION_TYPE.ACTION_COMMON)
        {
            wsdl = "http://info.dg11185.com/services/commonService?wsdl";
            targetNameSpace = "http://common.shake.mobile.api.zx.dg11185.com";
        } else
        {
            wsdl = "http://info.dg11185.com/services/shopService?wsdl";
            targetNameSpace = "http://shop.shake.mobile.api.zx.dg11185.com";
        }
        this.method = method;
    }

    public void setActionListener(ActionListener<T> listener)
    {
        this.listener = listener;
    }

    public void setFuture(Future<?> furture)
    {
        this.future = furture;
    }

    public void cancel(boolean mayInterruptIfRunning)
    {
        if (future != null && !future.isCancelled())
        {
            future.cancel(mayInterruptIfRunning);
        }
    }
    
    public boolean isLoading()
    {
        return isLoading;
    }

    public void onFinish(String response)
    {
        isLoading = false;
        try {            
            if(response != null && !response.equalsIgnoreCase("anyType{}"))
            {
                // 有结果
                result = parseJson(response);
            }else
            {
                // 返回空值
                result = null;
            }
            if (listener != null)
            {
                MyApplication.getContext().mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.onSucceed(result);
                    }
                });
            }
        }catch(Exception e)
        {
            e.printStackTrace();
            onError();
        }
    }

    public void onError()
    {
        isLoading = false;
        if (listener != null)
        {
            MyApplication.getContext().mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    listener.onError(1);
                }
            });
        }
    }

    /**
     * @return the wsdl
     */
    public String getWsdl()
    {
        return wsdl;
    }

    /**
     * @return the targetNameSpace
     */
    public String getTargetNameSpace()
    {
        return targetNameSpace;
    }

    /**
     * @return the method
     */
    public String getMethod()
    {
        return method;
    }

    public String getSoapAction()
    {
        return targetNameSpace + method;
    }

    /** 添加 SOAP协议的参数 */
    protected void addParam(String param, Object value)
    {
        if (param == null || param.trim().length() == 0 || value == null) { return; }
        if (params == null)
        {
            params = new HashMap<String, Object>();
        }
        params.put(param, value);
    }
    
    /** 添加JSON协议的参数 
     *  如果 value 为null，则移除 这个参数*/
    protected boolean addJsonParam(String param, Object value)
    {
        if (param == null || param.trim().length() == 0) { return false; }
        if(infoJsonObject == null)
        {
            infoJsonObject = new JSONObject();
        }
        try
        {
            if( value != null)
            {
                infoJsonObject.put(param, value);
            }else
            {
                infoJsonObject.remove(param);
            }
            return true;
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    
    /** 获取JSON协议的参数 */
    protected Object getJsonParam(String param)
    {
    	if(infoJsonObject == null)
        {
            infoJsonObject = new JSONObject();
            return null;
        }
        try
        {
            if(infoJsonObject.has(param))
            {
                return infoJsonObject.get(param);
            }else
            {
                return null;
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
     
    public boolean isValid()
    {
        return !TextUtils.isEmpty(wsdl) 
                && !TextUtils.isEmpty(targetNameSpace)
                && !TextUtils.isEmpty(method);
    }

    public boolean hasParams()
    {
        return  (infoJsonObject!=null) || (params != null && !params.isEmpty());
    }

    public void putParams(SoapObject soapObject)
    {
        isLoading = true;
        if(infoJsonObject != null)
        {
            addParam("infoJson", infoJsonObject.toString());
        }
        if (params != null && !params.isEmpty())
        {
            Set<String> itor = params.keySet();
            for (String param : itor)
            {
                soapObject.addProperty(param, params.get(param));
            }
        }
    }
  
    public interface ActionListener<T>
    {
        /** 成功 ，result != null,则有数据，否则返回空值*/
        public void onSucceed(T result);

        /** 错误 ，父类有 Toast 提示,如不需要提示，则子类复写该方法 */
        public void onError(int resultCode);
    }
}
