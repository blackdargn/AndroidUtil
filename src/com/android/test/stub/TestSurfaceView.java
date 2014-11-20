package com.android.test.stub;

import java.lang.reflect.Field;
import java.util.Vector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.android.util.R;

public class TestSurfaceView extends Activity
{
    /** Called when the activity is first created. */
    Button btnSingleThread , btnDoubleThread;
    SurfaceView sfv;
    SurfaceHolder sfh;
    Vector<ImageUnit> imgList = new Vector<ImageUnit>();
    private boolean isStop = false;
    RelativeLayout one;
    private Animation anim;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_surfaceview);

        btnSingleThread = (Button) this.findViewById(R.id.Button01);
        btnDoubleThread = (Button) this.findViewById(R.id.Button02);
        btnSingleThread.setOnClickListener(new ClickEvent());
        btnDoubleThread.setOnClickListener(new ClickEvent());
        sfv = (SurfaceView) this.findViewById(R.id.SurfaceView01);
        sfh = sfv.getHolder();
        sfh.addCallback(new MyCallBack());// 自动运行surfaceCreated以及surfaceChanged
        
        anim = new Rotate3dAnimation(20, 100, 50, 50, 50, false);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }
    
    class ClickEvent implements View.OnClickListener
    {

        @Override
        public void onClick(View v)
        {
            btnSingleThread.startAnimation(anim);
            if (v == btnSingleThread)
            {
                new Load_DrawImage(0, 0).start();// 开一条线程读取并绘图
            } else if (v == btnDoubleThread)
            {
                new LoadImage().start();// 开一条线程读取
                new DrawImage(0, 100).start();// 开一条线程绘图
            }
        }
    }

    class MyCallBack implements SurfaceHolder.Callback
    {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                int height)
        {
            Log.i("Surface:", "Change");

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            Log.i("Surface:", "Create");
            isStop = false;
            // 用反射机制来获取资源中的图片ID和尺寸
            Field[] fields = R.drawable.class.getDeclaredFields();
            for (Field field : fields)
            {
                if (!"icon".equals(field.getName()))// 除了icon之外的图片
                {
                    int index = 0;
                    try
                    {
                        index = field.getInt(R.drawable.class);
                    } catch (IllegalArgumentException e)
                    {
                        e.printStackTrace();
                    } catch (IllegalAccessException e)
                    {
                        e.printStackTrace();
                    }
                    // 取得图像大小
                    Bitmap bmImg = BitmapFactory.decodeResource(getResources(), index);
                    if(bmImg != null)
                    {
                        ImageUnit oen = new ImageUnit();
                        oen.width  = bmImg.getWidth();
                        oen.height = bmImg.getHeight();
                        oen.resId  = index;
                        // 保存图片ID
                        imgList.add(oen);
                    }
                }
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            Log.i("Surface:", "Destroy");
            isStop = true;
        }

    }

    /**
     * 读取并显示图片的线程
     */
    class Load_DrawImage extends Thread
    {
        int x , y;
        int imgIndex = 0;

        public Load_DrawImage(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public void run()
        {
            while (true && !isStop)
            {
                ImageUnit preimg = null;
                if(imgIndex == 0)
                {
                    preimg = imgList.lastElement();
                }else
                {
                    preimg = imgList.get(imgIndex - 1);
                }
                ImageUnit img = imgList.get(imgIndex);
                int width = 0;
                int height = 0;
                if(preimg.bitmap != null)
                {
                    width = Math.max(preimg.width, img.width);
                    height = Math.max(preimg.height, img.height);
                }else
                {
                    width =  img.width;
                    height = img.height;
                }
                Canvas c = sfh.lockCanvas(new Rect(this.x, this.y, this.x + width, this.y + height));
                if(c != null)
                {
                c.drawColor(Color.BLACK);
                if(img.bitmap == null)
                {
                    img.bitmap = BitmapFactory.decodeResource(getResources(), img.resId);
                }
                if(img.bitmap != null)
                {
                    c.drawBitmap(img.bitmap, this.x, this.y, new Paint());
                }
                imgIndex++;
                if (imgIndex == imgList.size())
                {
                    imgIndex = 0;
                }
                // 更新屏幕显示内容
                sfh.unlockCanvasAndPost(c);
                }
            }
        }
    };

    /**
     * 只负责绘图的线程
     */
    class DrawImage extends Thread
    {
        int x , y;
        int imgIndex = 0;
        
        public DrawImage(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public void run()
        {
            while (true && !isStop)
            {
                ImageUnit preimg = null;
                if(imgIndex == 0)
                {
                    preimg = imgList.lastElement();
                }else
                {
                    preimg = imgList.get(imgIndex - 1);
                }
                ImageUnit img = imgList.get(imgIndex);
                int width = 0;
                int height = 0;
                if(preimg.bitmap != null)
                {
                    width = Math.max(preimg.width, img.width);
                    height = Math.max(preimg.height, img.height);
                }else
                {
                    width =  img.width;
                    height = img.height;
                }
                Canvas c = sfh.lockCanvas(new Rect(this.x, this.y, this.x + width, this.y + height));
                if(c != null)
                {
                c.drawColor(Color.BLACK);                
                if(img.bitmap != null)
                {
                    c.drawBitmap(img.bitmap, this.x, this.y, new Paint());
                }
                imgIndex++;
                if (imgIndex == imgList.size())
                {
                    imgIndex = 0;
                }
                // 更新屏幕显示内容
                sfh.unlockCanvasAndPost(c);
                }
            }
        }
    };

    /**
     * 只负责读取图片的线程
     */
    class LoadImage extends Thread
    {
        int imgIndex = 0;
        int count = 0;
        
        public void run()
        {
            while (true && !isStop)
            {
                ImageUnit img = imgList.get(imgIndex);
                if(img.bitmap == null)
                {
                    img.bitmap = BitmapFactory.decodeResource(getResources(), img.resId);
                    if(img.bitmap != null)
                    {
                        count++;
                    }
                }
                imgIndex++;
                if (imgIndex == imgList.size())
                {
                    // 如果到尽头则重新读取
                    imgIndex = 0;
                }
                if(count == imgList.size())
                {
                    
                    break;
                }
            }
        }
    };
    
    public static class ImageUnit
    {
        public int resId;
        public int width;
        public int height;
        public Bitmap bitmap;
    }
}