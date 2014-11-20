package com.android.test.net;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import com.android.test.net.HttpTcpTest.HttpRequest;

public class HTTPTcpServer {

    
    public static void startServer() throws IOException {
        
        ServerSocket server = new ServerSocket();
        SocketAddress endpoint = new InetSocketAddress(8088);
        server.bind(endpoint);
        
        while(true) {
            
            Socket accpte = server.accept();
            
            HttpRequest request = new HttpRequest();
            request.parseHeader(accpte.getInputStream());
            request.parseBody(accpte.getInputStream());
            System.out.print(request.getHeader());
            System.out.print(request.getBody());
                       
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(accpte.getOutputStream(),HttpTcpTest.CHARSET));
            // ----状态行----
            writer.write("HTTP/1.1 " + 200 + " OK" + HttpTcpTest.LCLR);
            // ----响应消息报头----
            writer.write("Server:  tcp server" + HttpTcpTest.LCLR);
            writer.write("Connection: Close"+ HttpTcpTest.LCLR);

            boolean fileget = request.getPath().contains("file");
            if(fileget) {
                // 下载文件
                String filePath = System.getProperty("user.dir") + File.separator + "file" + File.separator + "main.db";
                File file = new File(filePath);
                writer.write("Content-Length: " + file.length() + HttpTcpTest.LCLR);
                writer.write("Content-Range: bytes " + "0-0/"+ file.length() + HttpTcpTest.LCLR);
            }
            // ----响应消息报头结束行----
            writer.write(HttpTcpTest.LCLR);
            writer.flush();
            // ----响应正文----
            if(fileget) {
                // 下载文件
                String filePath = System.getProperty("user.dir") + File.separator + "file" + File.separator + "main.db";
                File file = new File(filePath);
                
                FileInputStream fileous = new FileInputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(accpte.getOutputStream());
                byte[] data = new byte[1024];
                while(fileous.read(data) > 0) {
                    bos.write(data);
                    bos.flush();
                }
            }
            writer.flush();
            // 关闭输入流
            accpte.shutdownOutput();
            
            writer.close();
            accpte.close();
        }
        
    }
    
    public static void main(String[] args) {
        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
