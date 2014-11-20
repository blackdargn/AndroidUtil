package com.android.util.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import com.android.util.system.Logger;
import com.android.util.system.MyApplication;
import com.android.util.thread.ImageTAsyncTask;

/*********************************************************
 * @author : zhaohua
 * @version : 2012-12-18
 * @see : 列表的图片加载器
 * @Copyright : copyrights reserved by personal 2007-2011
 **********************************************************/
public class ListViewImgLoder
{
    private View        mParentView;
    private BitmapCache mCache;
    
    public ListViewImgLoder(View parent)
    {
        mParentView = parent;
        mCache    = new BitmapCache(parent.getContext());
    }
    
    public void destory()
    {
        ImageTAsyncTask.cancalAll();
        mCache.evictAll();
    }
    
    public void loadImage(Imager imgae)
    {
        if(imgae == null || !imgae.isValid()) return;
        
        Bitmap bmp  = mCache.getB(imgae.getMemKey());
        if(bmp != null && !bmp.isRecycled())
        {
            // 已缓存
            if(imgae.imageView != null)
            {
                imgae.imageView.setImageBitmap(bmp);
            }else
            {
                imgae.notifyComplete(bmp);
            }
        }else
        {
            // 未缓存
            if(imgae.imageView != null)
            {
                imgae.imageView.setTag(imgae.getMemUrl());
                imgae.imageView.setImageResource(imgae.stubId);
            }
            loadTask(imgae);
        }
    }
    
    /** 在非主线程时调用 */
    public void loadImageinUI(final Imager imgae)
    {
        mParentView.post(new Runnable()
        {
            @Override
            public void run()
            {
                imgae.imageView = (ImageView)mParentView.findViewWithTag(imgae.getMemUrl());
                loadImage(imgae);
            }
        });
    }
    
    /** 该对象是否有缓存*/
    public boolean isCached(Imager imgae)
    {
        if(imgae == null) return false;
        Bitmap bmp  = mCache.getB(imgae.getMemKey());
        return (bmp != null && !bmp.isRecycled());
    }
    
    /** 加载图像*/
    private void loadTask(final Imager image)
    {
        if(image == null) return;
        new ImageTAsyncTask<Void, Void, Imager>(image.getMemKey() +"")
            {   
                @Override
                protected Imager doInBackground(Void... params)
                {                    
                    Bitmap bmp = ImageUtil.decodeFile(image.getFileUrl(), image.isInSample, image.requiredSize);
                    if(bmp != null && mCache != null)
                    {
                        Bitmap bmp1 = bmp;
                        try {
                            // 边框处理
                            if(image.frameRid > 0)
                            {
                                Bitmap outImg = BitmapFactory.decodeResource(mParentView.getResources(), image.frameRid);
                                if(outImg != null)
                                {
                                    bmp1 = ImageUtil.getRoundedCornerBitmap(bmp, outImg, image.offsetX, MyApplication.getContext().getDensity());
                                    bmp.recycle();
                                    bmp = null;
                                    
                                    outImg.recycle();
                                    outImg = null;
                                }
                            }else
                            // 无边框，可能有圆角处理
                            if(image.offsetX > 0)
                            {
                                bmp1 = ImageUtil.cropRoundImage(image.requiredSize, image.requiredSize, bmp, image.offsetX);
                                if(bmp1 != bmp) {
                                    bmp.recycle();
                                    bmp = null;
                                }
                            }
                            Bitmap bmp2 = bmp1;
                            // 灰度处理
                            if(image.isMakeGray())
                            {
                                bmp2 = ImageUtil.getGreyImage(bmp1);
                                bmp1.recycle();
                                bmp1 = null;
                            }
                            mCache.put(image.getMemKey(), bmp2);
                        }catch(OutOfMemoryError e) {
                            Logger.d("--->OutOfMemoryError");
                            System.gc();  
                            System.runFinalization();
                        }
                    }
                    return image;
                }
                
                protected void onPostExecute(Imager result)
                {
                    if(mCache != null && result != null) {
                        Bitmap bmp = mCache.getB(result.getMemKey());
                        View view  = mParentView.findViewWithTag(result.getMemUrl());
                        view = view == null ? result.imageView : view;
                        if(bmp != null && !bmp.isRecycled())
                        {
                            ImageView imagView = (ImageView)view;
                            if(imagView != null) imagView.setImageBitmap(bmp);
                            result.notifyComplete(bmp);
                        }
                    }
                };
            }.execute();
    }
    
    public static class Imager
    {
        /** 真实的图片地址*/
        private String url;
        /** 内存中的图片地址，用于区分不通的缩略*/
        private String memUrl;
        /** 内存中的图片地址Key*/
        private int   memKey;
        /** 视图*/
        public ImageView imageView;
        /** 局部默认图片*/
        public int stubId;
        /** 是否缩略*/
        public boolean isInSample = true;
        /** 缩略尺寸*/
        public int requiredSize = 70;
        /** 是否是灰度图*/
        private boolean isGray = false;
        /** 完成通知器*/
        private OnLoadFinishedListener completeListener;
        /** 正圆偏移*/
        public int offsetX;
        /** 外框资源id*/
        public int frameRid;  
        /** 索引ID*/
        public long indexId; 
        
        /** 默认压缩的图*/
        public Imager(String url,ImageView imageView)
        {
            init(0, url,imageView,requiredSize,false,stubId, 0, 0);
        }
        
        /** 指定压缩的图，当requiredSize无效时，不压缩*/
        public Imager(String url,ImageView imageView, int requiredSize, int stubId)
        {
            init(0, url, imageView, requiredSize, false,stubId, 0, 0);
        }
        
        /** 指定压缩的图，当requiredSize无效时，不压缩*/
        public Imager(String url,ImageView imageView, int requiredSize, int offset, int stubId)
        {
            init(0, url, imageView, requiredSize, false,stubId, offset, 0);
        }
        
        /** 指定压缩的图，当requiredSize无效时，不压缩, 且指定是否灰度图*/
        public Imager(String url,ImageView imageView, int requiredSize, boolean isGray, int stubId)
        {
            init(0, url, imageView, requiredSize, isGray, stubId, 0, 0);
        }
        
        /** 指定压缩的图，当requiredSize无效时，不压缩, 且指定是否灰度图*/
        public Imager(String url,ImageView imageView, OnLoadFinishedListener listener, int requiredSize, boolean isGray, int stubId)
        {
            init(0, url, imageView, requiredSize, isGray, stubId, 0, 0);
            completeListener = listener;
        }
        
        /** 有边框的，指定压缩的图，当requiredSize无效时，不压缩, 且指定是否灰度图*/
        public Imager(long indexId, String url,ImageView imageView, OnLoadFinishedListener listener, int requiredSize, boolean isGray, int stubId,int offsetX, int frameRid)
        {
            init(indexId, url, imageView, requiredSize, isGray, stubId, offsetX, frameRid);
            completeListener = listener;
        }
        
        /** 初始化*/
        private void init(long indexId,String url,ImageView imageView, int requiredSize, boolean isGray, 
                int stubId, int offsetX, int frameRid)
        {
            this.url = url;
            this.indexId = indexId;
            this.imageView = imageView;
            this.requiredSize = requiredSize;
            this.isGray = isGray;
            this.stubId = stubId;
            this.offsetX = offsetX;
            this.frameRid = frameRid;
            this.isInSample = requiredSize <=0 ? false : true;          
            this.memUrl = isInSample ? ("["+ requiredSize +"]" + url) : url;
            this.memUrl = isGray ? ("[g]" + memUrl) : memUrl;
            this.memUrl = frameRid > 0 ? ("["+frameRid+"]" + memUrl) : memUrl;
            this.memKey = memUrl == null ? ((int)indexId) : memUrl.hashCode();
        }
        
        /** 获取原始文件的URL*/
        public String getFileUrl()
        {
            return url;
        }
        
        /** 获取内存缓存文件的URL*/
        public String getMemUrl()
        {
            return memUrl;
        }
        
        /** 获取内存存储Key*/
        public long getMemKey()
        {
            return memKey;
        }
        
        /** 是否合法*/
        public boolean isValid()
        {
            return !(url == null);
        }
        
        /** 是否需要进行灰度处理*/
        public boolean isMakeGray()
        {
            return isGray;
        }
        
        /** 返回默认的系统头型资源id*/
        public int getStubId()
        {
            return stubId;
        }
        
        public Imager setIndexId(long index) {
            this.indexId = index;
            return this;
        }
        
        public Imager setOnLoadFinishedListener(OnLoadFinishedListener listener) {
            completeListener = listener;
            return this;
        }
        
        /** 载入完成通知*/
        public void notifyComplete(Bitmap bmp)
        {
            if(completeListener != null)
            {
                completeListener.onFinish(indexId,bmp);
            }
        }
    }
    
    public static interface OnLoadFinishedListener
    {
        public void onFinish(long indexId, Bitmap bmp);
    }
}