/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-11-17
 * @描述: Google Maps API Web Services
 * 			1.Google Elevation API : 海拔
 * 			2.Google Geocoding API ：地理信息名称
 * @声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.location.google;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

/**
 * @author zhaohua
 *
 */
public class GoogleAPIManager
{
	private static final String url_elevation = "http://maps.google.com/maps/api/elevation/json?";
	private static final String url_geocode   = "http://maps.google.com/maps/api/geocode/json?";
	private static final String url_ditu_geo  = "http://ditu.google.cn/maps/geo?output=json&language=zh_CN&q=%s,%s";
	
	/**
	 * @see 获取 海拔信息
	 * @param loc
	 * @return null，则获取失败
	 */
	public static Double getEvevation(Location loc)
	{
		if(loc == null) return null;
		
		String url = buildEvevationPath(loc);
		
		String content = getContent(url);
		
		return paresEvevation(content);
	}

	/**
	 * @see 获取地址名称
	 * @param loc
	 * @return null，则获取失败
	 */
	public static String getAddress(Location loc)
	{
		if(loc == null) return null;
		
		String url = buildGeoCodePath(loc);
		
		String content = getContent(url);
		
		String address = paresGeoCode(content);
		
		return address;
	}
	
	public static Address getAddress(double lat, double lon)
	{
		String url = String.format(url_ditu_geo, lat, lon);
		
		String content = getContent(url);
		
		return paresGeoAddress(content);
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
			JSONObject status = data.getJSONObject("Status");
			int code = status.getInt("code");
			
			if(200 == code)
			{
				// OK
				JSONArray results = (JSONArray) data.get("Placemark");
				JSONObject result = (JSONObject)results.get(0);				
				Address adr = new Address();				
				JSONObject one = result.getJSONObject("AddressDetails").getJSONObject("Country").getJSONObject("AdministrativeArea");
				adr.province = one.getString("AdministrativeAreaName");
				JSONObject one2 = one.getJSONObject("Locality");
				adr.city = one2.getString("LocalityName");
				JSONObject one3 = null;
				try{
					one3 = one2.getJSONObject("DependentLocality");
					adr.county = one3.getString("DependentLocalityName");
				}catch (Exception e) 
				{
					e.printStackTrace();
				}
				try{
					if(one3 != null) adr.line = (String)one3.getJSONArray("AddressLine").get(0);
				}catch (Exception e) 
				{
					e.printStackTrace();
				}
				try{
					if(one3 != null) adr.route = one3.getJSONObject("Thoroughfare").getString("ThoroughfareName");
				}catch (Exception e) 
				{
					e.printStackTrace();
				}
				return adr;
			}else
			{
				// Fail
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
		
	/**
	 * @param content
	 * @return
	 */
	private static String paresGeoCode(String content)
	{
		if(content == null) return null;
		
		JSONObject data = null;
		try
		{
			data = new JSONObject(content);
			String status = (String) data.get("status");
			
			if("OK".equals(status))
			{
				// OK
				JSONArray results = (JSONArray) data.get("results");
				JSONObject result = (JSONObject)results.get(0);
				return result.getString("formatted_address");
			}else
			{
				// Fail
				return null;
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * @param content
	 * @return
	 */
	private static Double paresEvevation(String content)
	{
		if(content == null) return null;
		
		JSONObject data = null;
		try
		{
			data = new JSONObject(content);
			String status = (String) data.get("status");
			
			if("OK".equals(status))
			{
				// OK
				JSONArray results = (JSONArray) data.get("results");
				JSONObject result = (JSONObject)results.get(0);
				return result.getDouble("elevation");
			}else
			{
				// Fail
				return null;
			}
			
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @param url
	 * @return
	 */
	private static String getContent(String url)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		// 请求超时 
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		// 读取超
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
		
		try
		{
			HttpHost host = new HttpHost("maps.google.com");
			HttpResponse rsp = client.execute(host ,get);
			HttpEntity entity = rsp.getEntity();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
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
	 * @param loc
	 * @return
	 */
	private static String buildGeoCodePath(Location loc)
	{
		String path = url_geocode + "latlng="+loc.getLatitude()+","+loc.getLongitude()+"&sensor=true&language=zh_CN";
		return path;
	}
	
	/**
	 * @param loc
	 * @return
	 */
	private static String buildEvevationPath(Location loc)
	{
		String path = url_elevation + "locations="+ loc.getLatitude()+","+loc.getLongitude()+"&sensor=true";
		return path;
	}
	
	/** 地址实体*/
	public static class Address
	{
		/** 广东省*/
		public String province = "";
		/** 深圳市*/
		public String city = "";
		/** 福田区*/
		public String county = "";
		/** 福中三路*/
		public String route = "";
		/** 深圳市人民政府*/
		public String line = "";
		
		public String getFixAdr()
		{
			return province + city + county;
		}
		
		public String getActAdr()
		{
			return route + line ;
		}
	}
}
