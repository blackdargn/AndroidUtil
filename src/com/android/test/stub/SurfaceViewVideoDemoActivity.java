package com.android.test.stub;
//package com.android.test;
//
//import android.app.Activity;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Bundle;
//import android.view.SurfaceHolder;
//import android.view.SurfaceHolder.Callback;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//
//import com.android.util.R;
//
//public class SurfaceViewVideoDemoActivity extends Activity implements
//        OnClickListener
//{
//
//    Button btnplay, btnstop, btnpause;
//
//    SurfaceView surfaceView;
//
//    MediaPlayer mediaPlayer;
//
//    int position;
//
//    public void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.test_video);
//        
//        btnplay = (Button) this.findViewById(R.id.btnplay);
//        btnstop = (Button) this.findViewById(R.id.btnplay);
//        btnpause = (Button) this.findViewById(R.id.btnplay);
//
//        btnstop.setOnClickListener(this);
//        btnplay.setOnClickListener(this);
//        btnpause.setOnClickListener(this);
//       
//        surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
//
//        // 设置SurfaceView自己不管理的缓冲区
//        surfaceView.getHolder()
//                .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        surfaceView.getHolder().addCallback(new Callback()
//        {
//            @Override
//            public void surfaceDestroyed(SurfaceHolder holder)
//            {
//
//            }
//
//            @Override
//            public void surfaceCreated(SurfaceHolder holder)
//            {
//                if (position >= 0)
//                {
//                    try
//                    {
//                        // 开始播放
//                        play();
//                        // 并直接从指定位置开始播放
//                        mediaPlayer.seekTo(position);
//                        position = 0;
//                    } catch (Exception e)
//                    {
//                        // TODO: handle exception
//                    }
//                }
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder holder, int format,
//                    int width, int height)
//            {
//
//            }
//        });
//    }
//
//    @Override
//    public void onClick(View v)
//    {
//        switch (v.getId())
//        {
//        case R.id.btnplay:
//            play();
//            break;
//
//        case R.id.btnpause:
//            if (mediaPlayer.isPlaying())
//            {
//                mediaPlayer.pause();
//            } else
//            {
//                mediaPlayer.start();
//            }
//            break;
//
//        case R.id.btnstop:
//            if (mediaPlayer.isPlaying())
//            {
//                mediaPlayer.stop();
//            }
//
//            break;
//        default:
//            break;
//        }
//
//    }
//
//    @Override
//    protected void onPause()
//    {
//        // 先判断是否正在播放
//        if (mediaPlayer.isPlaying())
//        {
//            // 如果正在播放我们就先保存这个播放位置
//            position = mediaPlayer.getCurrentPosition();
//            mediaPlayer.stop();
//        }
//        super.onPause();
//    }
//
//    private void play()
//    {
//        try
//        {
//            mediaPlayer = new MediaPlayer();
//            
//            mediaPlayer.reset();
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            // 设置需要播放的视频
////            mediaPlayer.setDataSource("/mnt/sdcard/welcome.mp4");
//            mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/raw/welcome"));
//            // 把视频画面输出到SurfaceView
//            mediaPlayer.setDisplay(surfaceView.getHolder());
//            mediaPlayer.prepare();
//            // 播放
//            mediaPlayer.start();
//        } catch (Exception e)
//        {
//            // TODO: handle exception
//        }
//    }
//}
