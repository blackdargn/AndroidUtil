package com.android.util.location.baidu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONException;
import org.json.JSONObject;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-9-7
 * @see : 百度API管理器
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class BDAPIManager
{
    private static final String url_ditu_geo  
    = "http://api.map.baidu.com/geocoder?output=json&location=%s,%s&key=%s";
    
    public static Address reverseGeocode(double lat, double lon, String key) 
        throws JSONException, ClientProtocolException, IOException
    {
        String url = String.format(url_ditu_geo, lat, lon, key);        
        String content = getContent(url);
        return paresGeoAddress(content);
    }

    /**
     * @param url
     * @return
     */
    private static String getContent(String url)
    {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        get.setHeader("Accept-Encoding", "gzip");
        get.setHeader("Accept-Language", "zh");
        // 请求超时 
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        // 读取超
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        try
        {
            HttpResponse rsp = client.execute(get);
            HttpEntity entity = rsp.getEntity();
            
            InputStream instream = null; 
            Header contentEncoding = rsp.getFirstHeader("Content-Encoding");
            if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) 
            {
                instream = new GZIPInputStream(entity.getContent());
            }else
            {
                instream = entity.getContent();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(instream));
            
            StringBuffer sb = new StringBuffer();
            String result = br.readLine();
            
            while (result != null)
            {
                sb.append(result);
                result = br.readLine();
            }
            
            return sb.toString();
        }
        catch (ClientProtocolException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * @param content
     * @return
     */
    private static Address paresGeoAddress(String content)
    {
        if(content == null) return null;
        
        JSONObject data = null;
        try
        {
            data = new JSONObject(content);
            String status = data.getString("status");
            if(status.equalsIgnoreCase("OK"))
            {
                JSONObject result = data.getJSONObject("result");               
                Address adr = new Address();                
                adr.address = result.getString("formatted_address");
                JSONObject addressComponent = result.getJSONObject("addressComponent");
                adr.city =  addressComponent.getString("city");
                adr.county = addressComponent.getString("district");
                adr.province = addressComponent.getString("province");
                adr.route = addressComponent.getString("street");
                adr.line = addressComponent.getString("street_number");
                return adr;
            }else
            {
                return null;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
}
