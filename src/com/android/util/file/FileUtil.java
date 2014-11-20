/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: 文件处理相关方法
 * @声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * @author Administrator
 *
 */
public class FileUtil
{
    private static final String TAG = "FileUtil";
    private static DecimalFormat floatFormater = new DecimalFormat("0.00");
    
    /** 获取文件的MIME类型 */
    public static String getMIMEType(String file)
    {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = getFileExtension(file);
        return mimeTypeMap.getMimeTypeFromExtension(extension);
    }

    /** 获取文件的后缀 */
    public static String getFileExtension(String file)
    {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file);
        if (TextUtils.isEmpty(extension))
        {
            // getMimeTypeFromExtension() doesn't handle spaces in filenames nor
            // can it handle
            // urlEncoded strings. Let's try one last time at finding the
            // extension.
            int dotPos = file.lastIndexOf('.');
            if (0 <= dotPos)
            {
                extension = file.substring(dotPos + 1);
            }
        }
        return extension;
    }

    /** 判断SDcard是否存在 */
    public static boolean isSdcardExist()
    {
        return Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 如果SDcard存在，则返回SDcard上的普通文件的目录 否则，失败返回null
     */
    public static String makeDir(String dir)
    {
        if (!isSdcardExist())
        {
            return null;
        }
        File file = new File(dir);
        if (!file.exists())
        {
            if (!file.mkdirs())
            {
                Log.e(TAG, "--->create file dir fail!");
                return null;
            }
        }
        return dir;
    }

    public static File makeDirFile(String dir)
    {
        String dirPath = makeDir(dir);
        if (dirPath != null)
        {
            return new File(dirPath);
        }
        else
        {
            return null;
        }
    }

    /**
     * 文件命名并复制
     * 
     * @param src
     *            源文件
     * @param dest
     *            目标文件
     * @param recoverd
     *            是否覆盖
     * @throws IOException
     * @return -- 0 ：成功 1:与存在，提示询问 -- -1：参不对 -2：源不存在 -3：源不是文件 -4：源不能读 -5:目标不是文件
     *         -6:目标不可写 -7:读写异常
     * */
    public static int asSaveFile(String src, String dest, boolean recoverd)
            throws IOException
    {
        if (src == null || dest == null)
        {
            // 参数不对
            return -1;
        }
        File srcFile = new File(src);
        // 源不存在
        if (!srcFile.exists())
        {
            return -2;
        }
        // 源不是文件
        if (!srcFile.isFile())
        {
            return -3;
        }
        // 源不能读
        if (!srcFile.canRead())
        {
            return -4;
        }
        File destFile = new File(dest);
        // 目标文件已经存在，提示是否覆盖，或者 重命名
        if (destFile.exists())
        {
            if (recoverd)
            {
                // 覆盖
                destFile.delete();
            }
            else
            {
                // 询问 提示 重命名
                return 1;
            }
        }
        // 一定是不存在的
        destFile.createNewFile();
        // 目标不是文件
        if (!destFile.isFile())
        {
            return -5;
        }
        // 目标不可写
        if (!destFile.canWrite())
        {
            return -6;
        }
        FileInputStream fileIn = null;
        FileOutputStream fileOut = null;
        byte[] buffer = new byte[8192];
        int count = 0;
        // 开始复制文件
        try
        {
            fileIn = new FileInputStream(srcFile);
            fileOut = new FileOutputStream(destFile);
            while ((count = fileIn.read(buffer)) > 0)
            {
                fileOut.write(buffer, 0, count);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return -7;
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
            return -7;
        }
        finally
        {
            // 关闭 输入 输出 流
            if (fileIn != null)
                fileIn.close();
            if (fileOut != null)
                fileOut.close();
        }
        return 0;
    }

    /**
     * 将对象序列化
     * @param o
     * @return
     */
    public static byte[] objectToBytes(Object o)
    {
        byte[] bytes;
        ByteArrayOutputStream out = null;
        ObjectOutputStream sOut = null;
        try
        {
            out = new ByteArrayOutputStream();
            sOut = new ObjectOutputStream(out);
            sOut.writeObject(o);
            sOut.flush();
            bytes = out.toByteArray();
            return bytes;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                if (sOut != null)
                {
                    sOut.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
    /**
     * 反序列化
     * @param bytes
     * @return
     * @throws Exception
     */
    public static Object bytesToObject(byte[] bytes)
    {
        if(bytes == null) return null;
        // logger.debug("bytesToObject called ");
        // byte转object
        ByteArrayInputStream in = null ;
        ObjectInputStream sIn = null;
        try{
             in = new ByteArrayInputStream(bytes);
             sIn = new ObjectInputStream(in);
             return sIn.readObject();
        }catch(Exception e){
            e.printStackTrace();
        }finally
        {
            try{
                if(in != null)
                {
                    in.close();
                }
                if(sIn != null)
                {
                    sIn.close();
                }
            }catch(Exception e)
            {
                e.printStackTrace();
            }
        }
       

        return null;

    }

    /** 格式化文件的大小为MB，KB，B*/
    public static String formatByte(long bytes)
    {
        
        float r = (bytes+0.0f)/(1<<20);
        if(r > 1.0f)
        {
            // MB
            return floatFormater.format(r) + "MB";
        }else
        {
            r = (bytes+0.0f)/(1<<10);
            if(r > 1.0f)
            {
                // KB
                return floatFormater.format(r) + "KB";
            }else
            {
                // B
                return bytes + "B";
            }
        }
    }
    
    /** 格式化文件的大小为KM，M*/
    public static String formatDistance(float dist)
    {
        float r = dist / 1000;
        if (r > 1.0f) {
            // KM
            return floatFormater.format(r) + "KM";
        } else {
            // B
            return (int) dist + "M";
        }
    }
    
    /** 
     * 将指定的资源文件copy到指定file，如果file不存在的话，
     * file存在，则直接读取file的内容返回
     * */
    public static String mvRawToFile(Context context, int rawId, File file) {
        if(file == null) {
            return readRawString(context, rawId);
        }else
        if(!file.exists()) {
            String content = readRawString(context, rawId);
            writeFile(file, content);
            return content;
        }else {
            return readFile(file);
        }
    }
    
    public static String readRawString(Context context, int rawId) {
        String res = null;
        try {
            InputStream in = context.getResources().openRawResource(rawId);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            // 选择合适的编码，如果不调整会乱码
            res = EncodingUtils.getString(buffer, "UTF-8");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    
    public static String readAssertString(Context context, String file) {
        String res = null;
        try {
            InputStream in = context.getAssets().open(file);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            // 选择合适的编码，如果不调整会乱码
            res = EncodingUtils.getString(buffer, "UTF-8");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    
    public static boolean writeFile(File file, String content) {
        try {
            FileOutputStream fout = new FileOutputStream(file);
            byte[] bytes = content.getBytes();
            fout.write(bytes);
            fout.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static String readFile(File file) {
        String res = null;
        try {
            FileInputStream fin = new FileInputStream(file);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    /**
     * 获取新的文件，如果已存在则删除，创建新文件包括目录
     * @param file 文件全路径
     * @return 失败返回null
     */
    public static File newFile(String file)
    {
        if(file == null || file.trim().length() == 0) return null;
        File f = new File(file);       
        try
        {
            // 删除存在的
            if (f.exists())
            {
                f.delete();
            }
            // 创建父目录
            File parent = f.getParentFile();
            if(parent != null && !parent.exists())
            {
                parent.mkdirs();
            }
            // 创建文件
            if( f.createNewFile())
            {
                return f;
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 创建目录
     * @param dir 目录全路径
     * @return 失败返回null
     */
    public static File newDir(String dir)
    {
        if(dir == null || dir.trim().length() == 0) return null;
        File f = new File(dir);
        if(f.isDirectory() && !f.exists())
        {
             return f.mkdirs() ? f : null;
        }else
        {
            return f;
        }
    }

    /** 删除文件或者目录*/
    public static boolean delFDir(File fd)
    {
        if(fd == null)  return false;
        if(!fd.exists()) return true;
        if(!fd.isDirectory())
        {
            return fd.delete();
        }else
        {
            File[] files = fd.listFiles();
            if(files != null)
            {
                // 目录下有文件
                for (int i = 0; i < files.length; i++)
                {
                    if (files[i].isDirectory())
                    {
                        delFDir(files[i]);
                    }
                    else
                    {
                        files[i].delete();
                    }
                }
            }
            return fd.delete();
        }
    }

    public static String getExternDir(String dir)
    {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path += dir;
        return path;
    }
    
    public static boolean saveFile(String filename, InputStream ios)
    {
        if(ios == null) return false;
        File file = newFile(filename);
        if(file == null) return false;
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while( (len = ios.read(buffer)) != -1)
            {
                fos.write(buffer, 0, len);
            }
            return true;
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }finally
        {
            if(fos != null)
            {
                try
                {
                    fos.flush();
                    fos.close();
                    fos = null;
                } catch (IOException e)
                {
                    e.printStackTrace();
                }                
            }
        }
        return false;
    }
}
