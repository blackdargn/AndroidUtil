/*******************************************************
 * @作者: zhaodh
 * @日期: 2011-12-19
 * @描述: 图片相关方法
 * @声明: copyrights reserved by personal 2007-2011
 *******************************************************/
package com.android.util.image;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.android.util.system.AppHelper;
import com.android.util.system.Log;
import com.android.util.system.Logger;

public class ImageUtil
{

    /** 将图片变为灰度图 */
    public static Bitmap getGreyImage(Bitmap old)
    {
        int width, height;
        height = old.getHeight();
        width = old.getWidth();
        Bitmap n = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(old.copy(Bitmap.Config.ARGB_8888, true));
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(n, 0, 0, paint);
        return n;
    }

    /** 获得带倒影的图片 */
    public static Bitmap getReflectionImageWithOrigin(Bitmap bitmap)
    {
        final int reflectionGap = 4;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2,
                width, height / 2, matrix, false);
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                (height + height / 2), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapWithReflection);
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint defaultPaint = new Paint();
        canvas.drawRect(0, height, width, height + reflectionGap, defaultPaint);
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,
                0x00ffffff, TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
                + reflectionGap, paint);
        return bitmapWithReflection;
    }
    
    /**
     * 按照指定的品质与尺寸压缩图片并保存文件
     * @param fromFile
     * @param toFile
     * @param width
     * @param height
     * @param quality
     */
    public static boolean transImage(String fromFile,String toFile, int width, int height, int quality)
    {       
        return scaleImage(fromFile, toFile, width, height);
    }
    
    /** 保存图片到指定文件*/
    public static boolean saveImag(String imgFile, Bitmap bitmap, int quot)
    {
        File file = new File(imgFile);
        BufferedOutputStream dos = null;
        try
        {
            dos = new BufferedOutputStream(new FileOutputStream(file));
            return bitmap.compress(CompressFormat.JPEG, quot, dos);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }finally
        {
            if(dos != null)
            {
                try
                {
                    dos.flush();
                    dos.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                dos = null;
            }
        }
        return false;
    }
    
    // 压缩图片到指定的大小KB
    public static boolean compressImage(Bitmap image, int fileSizeKB, String imgFile) 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        try {
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);        
            //循环判断如果压缩后图片是否大于fileSizeKB,大于继续压缩
            while ( baos.toByteArray().length / 1024 > fileSizeKB)
            {         
                //重置baos即清空baos
                baos.reset();
                //每次都减少10
                options -= 5;
                //这里压缩options%，把压缩后的数据存放到baos中
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);           
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }finally
        {          
            try
            {
                baos.reset();
                baos.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            baos = null;
        }        
        return saveImag(imgFile, image, options);
    }
    
    /** 按固定长度缩放*/
    public static boolean scaleImageFixwh(Bitmap bitmap, String toFile, int width, int height)
    {      
        // 按固定缩放
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float scaleWidth = (float) width / bitmapWidth;
        float scaleHeight = (float) height / bitmapHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizeBitmap = null;
        try {
             resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
             return compressImage(resizeBitmap, 100, toFile);
        }catch(Exception e)
        {
            e.printStackTrace();
        }finally
        {
            if (resizeBitmap != null && !resizeBitmap.isRecycled())
            {
                // 记得释放资源，否则会内存溢出
                resizeBitmap.recycle();
                resizeBitmap = null;
            }
        }
        return false;
    }
    
    /** 按比例压缩后*/
    public static boolean scaleImage(String fromFile, String toFile, int width, int height)
    {
        // 压缩处理
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fromFile, opts);
        long pixs = opts.outWidth * opts.outHeight;
        if( pixs < width * height)
        {
             // 指定太大无法压缩，原图返回
             return false;
        }
        opts.inJustDecodeBounds = false;
        int sample = 1;
        // >8M = 10M
        if(pixs >= 8*1E6)
        {
            sample = 2;
        }else
        // >6M = 8M
        if(pixs >= 6*1E6)
        {
            sample = 2;
        }else
        // >5M = 6M
        if(pixs >= 5*1E6)
        {
            sample = 2;
        }else
        // <5M 4M 2M
        {
            sample = 1;
        }
        //设置缩放比例
        opts.inSampleSize = sample;
        Bitmap bitmap = null;
        try {
             while(true)
             {
                 try {
                     // 缩放直到不没异常为止
                     bitmap = BitmapFactory.decodeFile(fromFile, opts);
                     break;
                 }catch(OutOfMemoryError e)
                 {
                     e.printStackTrace();
                     opts.inSampleSize++;
                 }
             }
             return scaleImageFixwh(bitmap, toFile, width, height);
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally
        {
            if (bitmap != null && !bitmap.isRecycled())
            {
                // 记得释放资源，否则会内存溢出
                bitmap.recycle();
                bitmap = null;
            }
        }
        return false;
    }
    
    /**
     * 图片效果叠加
     * @param bmp 限制了尺寸大小的Bitmap
     * @param overlay 叠加的Bitmap
     * @return
     */
    public static Bitmap overlay(Bitmap bmp, Bitmap overlay)
    {
        long start = System.currentTimeMillis();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        
        // 对边框图片进行缩放
        int w = overlay.getWidth();
        int h = overlay.getHeight();
        float scaleX = width * 1F / w;
        float scaleY = height * 1F / h;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        
        Bitmap overlayCopy = Bitmap.createBitmap(overlay, 0, 0, w, h, matrix, true);
        
        int pixColor = 0;
        int layColor = 0;
        
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int pixA = 0;
        
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int newA = 0;
        
        int layR = 0;
        int layG = 0;
        int layB = 0;
        int layA = 0;
        
        final float alpha = 0.5F;
        
        int[] srcPixels = new int[width * height];
        int[] layPixels = new int[width * height];
        bmp.getPixels(srcPixels, 0, width, 0, 0, width, height);
        overlayCopy.getPixels(layPixels, 0, width, 0, 0, width, height);
        
        int pos = 0;
        for (int i = 0; i < height; i++)
        {
            for (int k = 0; k < width; k++)
            {
                pos = i * width + k;
                pixColor = srcPixels[pos];
                layColor = layPixels[pos];
                
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                pixA = Color.alpha(pixColor);
                
                layR = Color.red(layColor);
                layG = Color.green(layColor);
                layB = Color.blue(layColor);
                layA = Color.alpha(layColor);
                
                newR = (int) (pixR * alpha + layR * (1 - alpha));
                newG = (int) (pixG * alpha + layG * (1 - alpha));
                newB = (int) (pixB * alpha + layB * (1 - alpha));
                layA = (int) (pixA * alpha + layA * (1 - alpha));
                
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));
                newA = Math.min(255, Math.max(0, layA));
                
                srcPixels[pos] = Color.argb(newA, newR, newG, newB);
            }
        }
        
        bitmap.setPixels(srcPixels, 0, width, 0, 0, width, height);
        long end = System.currentTimeMillis();
        Log.d("may", "overlayAmeliorate used time="+(end - start));
        return bitmap;
    }
    
    /**
     * 组合涂鸦图片和源图片
     * @param src 源图片
     * @param watermark 涂鸦图片
     * @return
     */
    public Bitmap doodle(Bitmap src, Bitmap watermark)
    {
        // 创建一个新的和SRC长度宽度一样的位图
        Bitmap newb = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(newb);
        // 在 0，0坐标开始画入原图片src
        canvas.drawBitmap(src, 0, 0, null);
        // 涂鸦图片画到原图片中间位置
        canvas.drawBitmap(watermark, (src.getWidth() - watermark.getWidth()) / 2, (src.getHeight() - watermark.getHeight()) / 2, null); 
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        
        watermark.recycle();
        watermark = null;
        
        return newb;
    }
    
    /**
     * 图片区域合成
     * @param src
     * @param marker
     * @param fillColor
     * @return
     */
    public static Bitmap overlayFill(Bitmap src, Bitmap marker, int fillColor)
    {
        long start = System.currentTimeMillis();
        int width = marker.getWidth();
        int height = marker.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        
        // 对原图片进行缩放
        int w = src.getWidth();
        int h = src.getHeight();
        float scaleX = width * 1F / w;
        float scaleY = height * 1F / h;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        
        Bitmap srcCopy = Bitmap.createBitmap(src, 0, 0, w, h, matrix, true);
        
        int pixColor = 0;
        int layColor = 0; 
        int size = width * height;
        int[] srcPixels = new int[size];
        int[] layPixels = new int[size];
        
        srcCopy.getPixels(srcPixels, 0, width, 0, 0, width, height);
        marker.getPixels(layPixels, 0, width, 0, 0, width, height);
        
        int pos = 0;
        int roundColor = 0;
        int rowFirst = 0,rowLast = 0, colFirst = 0, colLast = 0;
        boolean left =false;
        
        for (int i = 0; i < height; i++)
        {     
            left = false;
            for (int k = 0; k < width; k++)
            {
                pos = i * width + k;
                pixColor = srcPixels[pos];
                layColor = layPixels[pos];
                
                if(layColor == fillColor)
                {
                    if(roundColor == 0)
                    {
                        roundColor = layPixels[ (i-1) * width + k];
                    }
                    layPixels[pos] = pixColor;
                    if(rowFirst == 0)
                    {
                        if(layPixels[pos-1] != fillColor
                           && layPixels[pos+1] != fillColor
                           && layPixels[(i-1) * width + k] != fillColor)
                        {
                            rowFirst = i;
                        }
                    }
                    if(rowLast == 0)
                    {
                        if(layPixels[pos-1] != fillColor
                                && layPixels[pos+1] != fillColor
                                && layPixels[(i+1) * width + k] != fillColor)
                             {
                            rowLast = i;
                             }
                    }
                    if(colFirst == 0)
                    {
                        if(layPixels[pos-1] != fillColor
                                && layPixels[(i-1) * width + k] != fillColor
                                && layPixels[(i+1) * width + k] != fillColor)
                             {
                                colFirst = k;
                             }
                    }
                    if(colLast == 0)
                    {
                        if(layPixels[pos+1] != fillColor
                                && layPixels[(i-1) * width + k] != fillColor
                                && layPixels[(i+1) * width + k] != fillColor)
                             {
                                colLast = k;
                             }
                    }
                    if(rowFirst == i)
                    {
                        int col1 = k - 1;
                        while(true)
                        {
                            if(layPixels[(i+1) * width + col1] != fillColor)
                            {
                                col1 += 1;
                                break;
                            }else
                            {
                                col1--; 
                            }
                        }
                        float alphaDx = (k-col1)/1.0f;
                        int dx = 1;
                        int pos1 = i * width + col1;
                        for(; col1 < k; col1++)
                        {
                            jcMake( pos1, 1- alphaDx*dx, srcPixels[pos1], roundColor, layPixels);
                            dx++;
                            pos1++;
                        }
                        
                        col1 = k + 1;
                        while(true)
                        {
                            if(layPixels[(i+1) * width + col1] != fillColor)
                            {
                                col1 -= 1;
                                break;
                            }else
                            {
                                col1++; 
                            }
                        }
                        dx = 1;
                        pos1 = i * width + col1;
                        for(; col1 > k; col1--)
                        {
                            jcMake( pos1, 1- alphaDx*dx, srcPixels[pos1], roundColor, layPixels);
                            dx++;
                            pos1--;
                        }
                    }else
                    if(rowLast == i)
                    {
                        int col1 = k - 1;
                        while(true)
                        {
                            if(layPixels[(i-1) * width + col1] != fillColor)
                            {
                                col1 += 1;
                                break;
                            }else
                            {
                                col1--; 
                            }
                        }
                        float alphaDx = (k-col1)/1.0f;
                        int dx = 1;
                        int pos1 = i * width + col1;
                        for(; col1 < k; col1++)
                        {
                            jcMake( pos1, 1- alphaDx*dx, srcPixels[pos1], roundColor, layPixels);
                            dx++;
                            pos1++;
                        }
                        
                        col1 = k + 1;
                        while(true)
                        {
                            if(layPixels[(i-1) * width + col1] != fillColor)
                            {
                                col1 -= 1;
                                break;
                            }else
                            {
                                col1++; 
                            }
                        }
                        dx = 1;
                        pos1 = i * width + col1;
                        for(; col1 > k; col1--)
                        {
                            jcMake( pos1, 1- alphaDx*dx, srcPixels[pos1], roundColor, layPixels);
                            dx++;
                            pos1--;
                        }
                    }else
                    if(colFirst == k)
                    {
                        int row1 = i - 1;
                        while(true)
                        {
                            if(layPixels[row1 * width + k + 1] != fillColor)
                            {
                                row1 += 1;
                                break;
                            }else
                            {
                                row1--; 
                            }
                        }
                        float alphaDx = (i-row1)/1.0f;
                        int dx = 1;
                        int pos1 = 0;     
                        for(; row1 < i; row1++)
                        {
                            pos1 = row1 * width + k;
                            jcMake( pos1, 1- alphaDx*dx, srcPixels[pos1], roundColor, layPixels);
                            dx++;
                        }
                        
                        row1 = i + 1;
                        while(true)
                        {
                            if(layPixels[row1 * width + k+1] != fillColor)
                            {
                                row1 -= 1;
                                break;
                            }else
                            {
                                row1++; 
                            }
                        }
                        dx = 1;
                        pos1 = row1 * width + k;
                        for(; row1 > i; row1--)
                        {
                            pos1 = row1 * width + k;
                            jcMake( pos1, 1- alphaDx*dx, srcPixels[pos1], roundColor, layPixels);
                            dx++;
                        }
                    }else
                    if(colLast  == k)
                    {
                        int row1 = i - 1;
                        while(true)
                        {
                            if(layPixels[row1 * width + k - 1] != fillColor)
                            {
                                row1 += 1;
                                break;
                            }else
                            {
                                row1--; 
                            }
                        }
                        float alphaDx = (i-row1)/1.0f;
                        int dx = 1;
                        int pos1 = 0;                       
                        for(; row1 < i; row1++)
                        {
                            pos1 = row1 * width + k;
                            jcMake( pos1, 1- alphaDx*dx, srcPixels[pos1], roundColor, layPixels);
                            dx++;
                        }
                        
                        row1 = i + 1;
                        while(true)
                        {
                            if(layPixels[row1 * width + k - 1] != fillColor)
                            {
                                row1 -= 1;
                                break;
                            }else
                            {
                                row1++; 
                            }
                        }
                        dx = 1;
                        pos1 = row1 * width + k;
                        for(; row1 > i; row1--)
                        {
                            pos1 = row1 * width + k;
                            jcMake( pos1, 1- alphaDx*dx, srcPixels[pos1], roundColor, layPixels);
                            dx++;
                        }
                    }else
                    {
                        if(!left)
                        {
                            if((k-1)== colFirst)
                            {
                                left = true;
                            }else
                            {
                                jcMake(pos - 1, 0.6f, srcPixels[pos-1], roundColor, layPixels);
                                left = true;
                            }
                        }else
                        if(layPixels[pos+1] != fillColor)
                        {
                            if((k+1) == colLast)
                            {
                                break;
                            }else
                            {
                                jcMake(pos + 1, 0.6f, srcPixels[pos+1], roundColor, layPixels);
                            }
                        }
                    }
                }
            }
        }
        
        bitmap.setPixels(layPixels, 0, width, 0, 0, width, height);
        long end = System.currentTimeMillis();
        Log.d("may", "overlayAmeliorate used time="+(end - start));
        return bitmap;
    }  
    
    private static void jcMake(int pos, float alpha, int pixColor, int layColor, int[] layPixels)
    {
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        
        int newR = 0;
        int newG = 0;
        int newB = 0;
        
        int layR = 0;
        int layG = 0;
        int layB = 0;
        
        // 锯齿模糊处理
        pixR = Color.red(pixColor);
        pixG = Color.green(pixColor);
        pixB = Color.blue(pixColor);
        
        layR = Color.red(layColor);
        layG = Color.green(layColor);
        layB = Color.blue(layColor);
        
        newR = (int) (layR * alpha + pixR * (1 - alpha));
        newG = (int) (layG * alpha + pixG * (1 - alpha));
        newB = (int) (layB * alpha + pixB * (1 - alpha));
        
        newR = Math.min(255, Math.max(0, newR));
        newG = Math.min(255, Math.max(0, newG));
        newB = Math.min(255, Math.max(0, newB));
        
        layPixels[pos] = Color.argb(255, newR, newG, newB);
    }
    
    public static Bitmap decodeFile(String url, boolean isInSample, int requiredSize){
        if(url == null || url.trim().length() == 0) return null;
        boolean http = URLUtil.isHttpUrl(url);
        File f = null;
        if(http){
            // 网络文件
            String filename = String.valueOf(url.hashCode()) + ".jpg";
            f = AppHelper.getAppFiledImgFile(filename);
            if(f == null) return null;
        }else{
            // 本地文件
            f = new File(url);
        }
        // delete invalid image
        if(f.exists() && f.length() == 0)
        {
            f.delete();
        }
        // first from local file
        Bitmap b = decodeFile(f,isInSample, requiredSize);
        if (b != null) return b;
        // from web
        if(http){
            HttpURLConnection conn = null;
            try
            {
                Bitmap bitmap = null;
                if(loadFileFromUrl(url, f)){
                    bitmap = decodeFile(f, isInSample, requiredSize);
                }
                return bitmap;
            }catch (Exception ex)
            {
                ex.printStackTrace();
                return null;
            }finally{
                if(conn != null){
                    conn.disconnect();
                    conn = null;
                }
            }
        }else
        {
            return null;
        }
    }
    
    public static boolean loadFileFromUrl(String url, File file){
        HttpURLConnection conn = null;
        try
        {
            URL imageUrl = new URL(url);
            conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(10000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(file);
            CopyStream(is, os);
            os.close();
            return true;
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }finally{
            if(conn != null){
                conn.disconnect();
                conn = null;
            }
        }
    }
    
    private static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size = 1024;
        try
        {
            byte[] bytes = new byte[buffer_size];
            for (;;)
            {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) break;
                os.write(bytes, 0, count);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 比例缩小图片
     * @param f
     * @param isInSample
     * @param requiredSize
     * @return
     */
    private static Bitmap decodeFile(File f, boolean isInSample, int requiredSize)
    {
        if(!f.exists() || f.length() == 0) return null;
        try
        {
            int scale = 1;
            if(isInSample)
            {
                // decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(f), null, o);
    
                // Find the correct scale value. It should be the power of 2.
                int REQUIRED_SIZE = requiredSize;
                int width_tmp = o.outWidth, height_tmp = o.outHeight;               
                while (true)
                {
                    if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) break;
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }
            }
            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bmp = null;
            int retry = 3;
            while(bmp == null && retry > 0) {
                try {
                    bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
                }catch(OutOfMemoryError e) {
                    Logger.d("--> OutOfMemoryError ");
                    System.gc();  
                    System.runFinalization();
                    scale *= 2;
                    o2.inSampleSize = scale;
                }catch(Exception e) {
                    Logger.d("--> "+ e.getLocalizedMessage());
                    scale *= 2;
                    o2.inSampleSize = scale;
                }
                retry --;
            }
            return bmp;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }catch(OutOfMemoryError e){
            e.printStackTrace();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /** 保存图片到指定文件*/
    public static boolean saveImag(String imgFile, Bitmap bitmap, CompressFormat format)
    {
        return saveImag(imgFile, bitmap, format, 100);
    }
    
    /** 保存图片到指定文件*/
    public static boolean saveImag(String imgFile, Bitmap bitmap, CompressFormat format, int quot)
    {
        File file = new File(imgFile);
        BufferedOutputStream dos = null;
        try
        {
            dos = new BufferedOutputStream(new FileOutputStream(file));
            return bitmap.compress(format, quot, dos);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }catch(OutOfMemoryError e){
            e.printStackTrace();
            System.gc();  
            System.runFinalization();
        }finally
        {
            if(dos != null)
            {
                try
                {
                    dos.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                dos = null;
            }
        }
        return false;
    }
    
    /** 获得圆角图片 */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    
    /** 获得圆角图片缩放到指定宽与高 */
    public static Bitmap getRoundedCornerBitmap(int width, int heigth, Bitmap bitmap, float roundPx) {
        // 对原图片进行缩放
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        float scaleX = width * 1F / w;
        float scaleY = heigth * 1F / h;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);        
        Bitmap srcCopy = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);      
        return getRoundedCornerBitmap(srcCopy, roundPx);
    }
    
    /**
     * @author baibin
     * 
     * @see 合成相框方法。会根据相框宽度拉伸或者压缩来源图片。GlobalConstant.java中有常用形式的偏移量。
     * 
     * @param bitmap 需要处理的图像
     * @param outImg 相框
     * @param offsetX 相框与图像叠加的偏移量。X轴方向。
     * @return 合成后的图像
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap ,Bitmap outImg, int x, int y, float density) {
        
        if(bitmap == null || outImg == null) return null;
        
        int offsetX = (int)(x * density + 0.5f);
        int offsetY = (int)(y * density + 0.5f);
        
        int width = outImg.getWidth();
        int height = outImg.getHeight();
        float roundPx = (width - offsetX*2)/2;
        
        //创建画布。以相框大小为基准。
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
         
        //剪裁圆形区域
        final int color = 0xff424242;
        final Paint paint = new Paint();        
        final Rect dst = new Rect(offsetX, offsetY, width - offsetX, width - offsetX*2 + offsetY);  //需要最终大小的尺寸
        final RectF rectF = new RectF(dst);
        // 以原图的中心点，目标图的尺寸
        int left = 0;
        int rigth = 0;
        int top = 0;
        int bottm = 0;
        int ox = bitmap.getWidth() /2;
        int oy = bitmap.getHeight() /2;
        // 以原图最短的边为住
        int corpWidth = Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2;
        left = ox - corpWidth;
        rigth = ox + corpWidth;
        top = oy - corpWidth;
        bottm = oy + corpWidth;
        final Rect src = new Rect(Math.max(left, 0), Math.max(top, 0), Math.min(rigth, bitmap.getWidth()), Math.min(bottm, bitmap.getHeight()));
        paint.setAntiAlias(true);
        
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        
        //添加相框
        final Rect outR = new Rect(0, 0, width, height);
        final Paint outP = new Paint();
        outP.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
        canvas.drawBitmap(outImg, outR, outR, outP);
        
        return output;
    }
    
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap ,Bitmap outImg, int offsetX, float density)
    {
        return getRoundedCornerBitmap( bitmap , outImg, offsetX, 3, density);
    }
    
    /** 
     * 按照指定分辨率等比例压缩图片
     * 如果没要压缩的必要，则不执行任何操作
     * 成功写入到toFile，才返回true，其他情况返回false。
     * */
    public static boolean smallBitmap(String imgFile, String toFile, int reqWidth, int reqHeight) {
        if(TextUtils.isEmpty(imgFile) || TextUtils.isEmpty(toFile) || reqWidth <=0 || reqHeight <= 0) {
            return false;
        }
        Bitmap bmp = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imgFile, options);
            
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;
            
            if (height > reqHeight || width > reqWidth) {
                final int heightRatio = Math.round((float) height/ (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            if( inSampleSize == 1) {
                // 不必要压缩
                return false;
            }
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;                   
            bmp = BitmapFactory.decodeFile(imgFile, options);
            return saveImag(toFile, bmp, CompressFormat.JPEG, 80);
        }catch(OutOfMemoryError er) {
            er.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            if(bmp != null) {
                bmp.recycle();
                bmp = null;
            }
        }
        return false;
    }
    
    /** 以中心点裁剪圆角图片*/
    public static Bitmap cropRoundImage(int cropWidth, int cropHeigth, Bitmap bitmap, float roundPx) {
        if(cropWidth <= 0 || cropWidth <=0 || bitmap== null) {
            return bitmap;
        }
        // 以原图的中心点，目标图的尺寸
        int left = 0;
        int rigth = 0;
        int top = 0;
        int bottm = 0;
        int ox = bitmap.getWidth() /2;
        int oy = bitmap.getHeight() /2;
        // 以原图最短的边为住
        int corpWidth = Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2;
        left = ox - corpWidth;
        rigth = ox + corpWidth;
        top = oy - corpWidth;
        bottm = oy + corpWidth;
        final Rect src = new Rect(Math.max(left, 0), Math.max(top, 0), Math.min(rigth, bitmap.getWidth()), Math.min(bottm, bitmap.getHeight()));
        final Rect dst = new Rect(0, 0, cropWidth, cropHeigth);
        final RectF rectF = new RectF(dst);
        // 达到最大边框，才进行裁剪
//        if( !(0.8*cropWidth <= src.width()  &&  0.8*cropHeigth <= src.height())) {
//            return bitmap;
//        }
        final Paint paint = new Paint();
        final int color = 0xff424242;      
        Bitmap output = Bitmap.createBitmap(cropWidth, cropWidth, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawARGB(0, 0, 0, 0);         
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        
        return output;
    }
    
    /** 获取视频文件第一帧的缩略图*/
    public static Bitmap createVedioThumbnail(String videoFile, int width, int height) {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoFile, Images.Thumbnails.MINI_KIND);
        if(thumb != null) {
            return ThumbnailUtils.extractThumbnail(thumb, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);             
        }
        return thumb;
    }
    
    /** 生成视频文件第一帧的缩略图的文件*/
    public static String createVedioThumbnailFile(String videoFile, int width, int height) {
        String imgFile = videoFile +".jpg";
        if(!new File(imgFile).exists()) {
            // 不存在
            Bitmap thumb = createVedioThumbnail(videoFile,width,height);
            if(thumb != null) {            
                if(saveImag(imgFile, thumb, CompressFormat.PNG)) {
                    return imgFile;
                }
            }
            return null;
        }else {
            // 存在
            return imgFile;
        }
    }
    
    /** COPY bitmap*/
    public static Bitmap copyBitmap(Bitmap src) {
        if(src == null) return null;
        try {
            Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawBitmap(src, 0, 0, null);
            return output;
        }catch(OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }
}