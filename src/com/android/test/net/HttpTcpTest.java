package com.android.test.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.SSLSocketFactory;

public class HttpTcpTest {

    public static final String LCLR = "\r\n";
    public static final String CHARSET = "utf-8";
    
    public static void doHttp(String url, String method, Map<String,String> params) throws UnknownHostException, IOException {
        
        URL requestUrl = new URL(url);
        int port = requestUrl.getPort();
        port = port == -1 ? 80 : port;
        String path = requestUrl.getPath() ;
        path = path.length() == 0 ? "/" : path;
        String host = requestUrl.getHost();
        
        Socket client = null;
        if(requestUrl.getProtocol().equalsIgnoreCase("https")) {
            client = SSLSocketFactory.getDefault().createSocket();
            port = 443;
        }else {
            client = new Socket();
        }
        client.connect(new InetSocketAddress(host, port));
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(),CHARSET));
        
        boolean isGet = method.equalsIgnoreCase("get") ;
        StringBuilder param = new StringBuilder();
        
        if(params != null) { 
            Set<Entry<String, String>> set = params.entrySet();
            Iterator<Entry<String, String>> itor = set.iterator();
            Entry<String, String> entry = null;
            while(itor.hasNext()) {
                entry = itor.next();
                if(param.length() > 0) {
                    param.append("&");
                }
                param.append(URLEncoder.encode(entry.getKey(), CHARSET) + "=" + URLEncoder.encode(entry.getValue(), CHARSET));
            }
        }
        // 是否是GET ，最大1024字节
        if(isGet && param.length() > 0) {
            path += "?" + param.toString();
        }
        // ----请求行----
        writer.write(method + " " + path + " " + "HTTP/1.1" + LCLR);
        // ----请求消息报头----
        writer.write("Host: " +requestUrl.getHost() + LCLR);
        writer.write("Connection: Close"+ LCLR);
        // 是否是Post
        if(!isGet && param.length() > 0) {
            writer.write("Content-Length: " + param.length() + LCLR);
            writer.write("Content-type: application/x-www-form-urlencoded" + LCLR);
        }
        // ----消息报头结束行-----
        writer.write(LCLR);
        // ----请求正文----
        if(!isGet && param.length() > 0) {
            writer.write(param.toString());
        }
        writer.flush();
        // 关闭输入通道
        client.shutdownOutput();
        
//        printResponse(client);
        
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.parseHeader(client.getInputStream());
        System.out.print(httpResponse.getHeader());
        httpResponse.parseBody(client.getInputStream());
        System.out.print(httpResponse.getBody());
        
        client.close();
    }
    
    public static void printResponse(Socket client) throws UnsupportedEncodingException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader( client.getInputStream(),CHARSET));
        String temp = null;
        while( (temp = reader.readLine()) != null) {
            System.out.println(temp);
        }
        reader.close();
    }
 
    public static class HttpRequest extends HttpResponse {
        
        String mMethod;
        String path;
        
        @Override
        protected void parseStatuLine(String line) {
            resCode =1;
            String[] split = line.split(" ");
            if(split!=null && split.length > 2) {
                mMethod = split[0];
                path = split[1];
            }
        }
        
        @Override
        public String getHeader() {
            return mMethod + " " + path + "\n" + super.getHeader();
        }
        
        public String getPath() {
            return path;
        }
    }
    
    public static class HttpResponse{
        protected int resCode;
        protected HashMap<String, String> headers = new HashMap<String, String>();
        protected StringBuilder body;
        
        public HttpResponse parseHeader(InputStream ins) throws IOException {
            StringBuilder line = new StringBuilder();
            char c = 0;
            
            while(true) {
                c = (char)ins.read();
                if(c == '\r') {
                    c = (char)ins.read();
                    if(c == '\n') {
                        if(line.length() == 0) {
                            break;
                        }
                        if(resCode == 0) {
                            parseStatuLine(line.toString());
                        }else {
                            parseHeaders(line.toString());
                        }
                        line.delete(0, line.length());
                        continue;
                    }
                }
                line.append(c);
            }
            return this;
        }
        
        public HttpResponse parseBody(InputStream ins) throws IOException {
            
            if(getHeader("Content-Range") != null) {
                fileDownload(ins);
            }
            if(body == null) {
                body = new StringBuilder();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins,CHARSET));
            String temp = null;
            while( (temp = reader.readLine()) != null) {
                body.append(temp);
            }
            return this;
        }
        
        public HttpResponse fileDownload(InputStream ins) throws IOException {
                // 下载文件
                int filesize = Integer.parseInt(getHeader("Content-Length"));
                String filePath = System.getProperty("user.dir") + File.separator + "file" + File.separator + "main-d.db";
                FileOutputStream fileins = new FileOutputStream(filePath);
                BufferedInputStream bins = new BufferedInputStream(ins);
                int count = 0, len = 0;
                byte[] data = new byte[1024];
                while( (len = bins.read(data) )> 0) {
                    count +=len;
                    fileins.write(data);
                    fileins.flush();
                    if(count >= filesize) {
                        break;
                    }
                }
            return this;
        }
               
        protected void parseStatuLine(String line) {
           String[] split = line.split(" ");
           if(split!=null && split.length > 2) {
               resCode = Integer.parseInt(split[1]);
           }
        }
        
        private void parseHeaders(String line) {
            int pos = line.indexOf(':');
            if(pos > 0) {
                headers.put(line.substring(0, pos), line.substring(pos + 2, line.length()));
            }
        }
        
        public String getHeader(String key) {
            return headers.get(key);
        }
        
        public int getStatuCode() {
            return resCode;
        }
        
        public String getBody() {
            return body != null ? body.toString() : null;
        }
        
        public String getHeader() {
            StringBuilder buf = new StringBuilder();
            buf.append( "status code : " + resCode +"\n");
            Set<Entry<String, String>> set = headers.entrySet();
            Iterator<Entry<String, String>> itor = set.iterator();
            Entry<String, String> entry = null;
            while(itor.hasNext()) {
                entry = itor.next();
                buf.append(entry.getKey() + ": " + entry.getValue() + "\n");
            }
            return buf.toString();
        }
    }
    
    public static void main(String[] args) {
        String url = "http://127.0.0.1:8088/file";
        String method = "POST";
        Map<String,String> params = new HashMap<String, String>();
        params.put("tn", "baiduhome_pg");
        params.put("ie","utf-8");
        params.put("bs","11");
        params.put("f","8");
        params.put("rsv_bp","1");
        params.put("rsv_spt","1");
        params.put("wd","zhaodehua");
        params.put("rsv_sug3","6");
        params.put("rsv_sug4","438");
        params.put("rsv_sug1","5");
        params.put("rsv_sug2","0");
        params.put("inputT","15");
        
        try {
            doHttp(url, method, params);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
