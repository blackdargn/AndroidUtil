/*******************************************************
 * @作者: zhaohua
 * @日期: 2011-11-22
 * @描述: 音频管理器
 * @声明: copyrights reserved by Petfone 2007-2011
 *******************************************************/
package com.android.util.media;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

import com.android.util.component.PhoneStatReceiver;
import com.android.util.file.FileUtil;
import com.android.util.thread.Executable;
import com.android.util.thread.TNotifyListener;
import com.android.util.thread.ThreadHelper;

/**
 * @author zhaohua
 * 
 */
public class MediaManager
{
    private static final String TAG = "RecoderManager";

    /** 临时文件的前缀 */
    public static final String FILE_PREFIX = "pf_";

    /** 音频文件的通用后缀 */
    public static final String VOICE_SUFIX = ".amr";

    /** 音频文件的最大长度 大约 280kB <= 10 分钟 */
    public static final int VOICE_FILE_MAX_SIZE = 250000;
    public static final int VOICE_FILE_MAX_DURATION = 120900;
    
    private MediaRecorder mediaRecorder;

    private MediaPlayer mediaPlayer;

    /** Audio管理器，用了控制音量 */
    private AudioManager audioMgr;

    private Context context;

    private AtomicBoolean isRecodeReady = new AtomicBoolean(false);

    private AtomicBoolean isPendStop = new AtomicBoolean(false);

    private File myRecAudioFile;
    /** 声音池,用于播放较短的提示音*/
    private SoundPool pool;
    private ConcurrentHashMap<Integer, Integer> soundMaps;
    /** 最后一次响铃*/
    private ConcurrentHashMap<Integer, Long> ringTimeMaps;
    
    private MediaManager(Context context)
    {
        this.context = context;
        // 播放自定义铃音，就是音乐
        audioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        pool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 0);
        soundMaps = new ConcurrentHashMap<Integer, Integer>();
        ringTimeMaps = new ConcurrentHashMap<Integer, Long>();
    }
    
    private static MediaManager instance;

    public static MediaManager getInstace(Context context)
    {
        if (context == null) return null;
        if (instance == null)
        {
            instance = new MediaManager(context);
        }
        return instance;
    }
    
    /** 设置最大音量*/
    void setMaxVoum() {
        // 获取最大音乐音量
        int maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume,AudioManager.FLAG_PLAY_SOUND);
    }

    /** 开始录音 */
    private boolean startRecord(String dir, final OnRecoderListener listener)
    {
        if (isRecodeReady.get()) return true;
        myRecAudioFile = null;
        if (dir == null) return false;
        // 获取保存文件的根目录
        File recAudioDir = FileUtil.makeDirFile(dir);
        if (recAudioDir == null) return false;
        /* 建立录音档 */
        try
        {
            myRecAudioFile = File.createTempFile(FILE_PREFIX, VOICE_SUFIX, recAudioDir);
        } catch (IOException e)
        {
            e.printStackTrace();
            Log.e(TAG, "--->create voice save file fail!");
            return false;
        }

        try
        {
            mediaRecorder = new MediaRecorder();
            /* 设定录音来源为麦克风 */
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setMaxFileSize(VOICE_FILE_MAX_SIZE);
            mediaRecorder.setMaxDuration(VOICE_FILE_MAX_DURATION);
            mediaRecorder.setOutputFile(myRecAudioFile.getAbsolutePath());

            mediaRecorder.prepare();
            mediaRecorder.start();

            mediaRecorder.setOnErrorListener(new OnErrorListener()
                {
                    @Override
                    public void onError(MediaRecorder mr, int what, int extra)
                    {
                        if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN)
                        {
                            // 录音中途错误
                            stopRecord();
                            // 处理异常
                            if (listener != null)
                            {
                                listener.onError();
                            }
                        }
                    }
                });

            mediaRecorder.setOnInfoListener(new OnInfoListener()
                {
                    @Override
                    public void onInfo(MediaRecorder mr, int what, int extra)
                    {
                        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED
                           ||what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
                        {
                            // 录音文件 到达最大文件限制
                            stopRecord();
                            // 处理异常
                            if (listener != null)
                            {
                                listener.onMaxReached();
                            }
                        }
                    }
                });

            isRecodeReady.set(true);
            if (isPendStop.get())
            {
                // 延迟关闭
                stopRecord();
            }

            Log.d(TAG, "--->startRecord");
            return true;
        } catch (IllegalStateException e)
        {
            Log.e(TAG, "--->prepare MediaRecorder fail!");
            e.printStackTrace();
        } catch (IOException e)
        {
            Log.e(TAG, "--->prepare MediaRecorder fail!");
            e.printStackTrace();
        } catch (Exception e)
        {
            Log.e(TAG, "--->start MediaRecorder fail!");
            e.printStackTrace();
        }
        return false;
    }

    /** 是否正常正在录音 */
    public boolean isRecoding()
    {
        return isRecodeReady.get();
    }

    /** 释放录音资源 */
    private synchronized void release()
    {
        if (mediaRecorder != null)
        {
            // 结束使用，释放资源,重置状态
            mediaRecorder.release();
            mediaRecorder = null;
            isRecodeReady.set(false);
            isPendStop.set(false);
        }              
    }
    
    public void releaseRing() {
        if(pool != null){
            pool.release();
            pool = null;
            
            soundMaps.clear();
            ringTimeMaps.clear();
        }
    }

    /** 停止录音 */
    public boolean stopRecord()
    {
        if (mediaRecorder == null) return true;

        if (isRecodeReady.get())
        {
            /* 停止录音 */
            try
            {
                mediaRecorder.setOnErrorListener(null);
                mediaRecorder.setOnInfoListener(null);
                mediaRecorder.stop();
                mediaRecorder.reset();

                Log.d(TAG, "--->stopRecord");

                return true;
            } catch (IllegalStateException e)
            {
                Log.e(TAG, "--->stopRecord MediaRecorder fail!");
                e.printStackTrace();
            } catch (RuntimeException e)
            {
                Log.e(TAG, "--->stopRecord MediaRecorder fail!");
                e.printStackTrace();
            } catch (Exception e)
            {
                Log.e(TAG, "--->stopRecord MediaRecorder fail!");
                e.printStackTrace();
            } finally
            {
                release();
            }
        } else
        {
            isPendStop.set(true);
        }

        return false;
    }

    /** 获取录音后的文件 */
    public File getRecordFile()
    {
        return myRecAudioFile;
    }

    /** 是否时有效的录音文件 */
    public boolean isValidRecord()
    {
        if (myRecAudioFile != null)
        {
            // 最小音频文件限制
            if (myRecAudioFile.length() > 500)
            {
                return true;
            } else
            {
                myRecAudioFile.delete();
                myRecAudioFile = null;
                return false;
            }
        } else
        {
            return false;
        }
    }

    /** 录音监听器，处于UI主线程 */
    public static interface OnRecoderListener
    {
        /** 录音出错异常处理 */
        public void onError();

        /** 录音文件上限异常处理 */
        public void onMaxReached();
    }

    // ///////////////////////////// record event life///////////////////////////////
    /** 停止录音 */
    public void stopRecordAsyn(final TNotifyListener<Boolean> listener)
    {
        ThreadHelper.executeWithCallback(new Executable<Boolean>() {
            @Override
            public Boolean execute() throws Exception {
                return stopRecord();
            }
        }, listener);
    }
    
    /** 停止录音 */
    public void stopRecord(final TNotifyListener<Boolean> listener)
    {
         boolean r = stopRecord();
         if(listener != null) {
             listener.notify(r, true);
         }
    }

    /** 开始录音 */
    public boolean startRecordAsyn(final String dir,
            final TNotifyListener<Boolean> listener,
            final OnRecoderListener recordListener)
    {
        ThreadHelper.executeWithCallback(new Executable<Boolean>() {
            @Override
            public Boolean execute() throws Exception {
                return startRecord(dir, recordListener);
            }
        }, listener);
        return true;
    }

    /**
     * 后台播放音频
     * 
     * @param filePath
     * @param listener
     * @return
     */
    public boolean palyAudioAsyn(String filePath,
            CommonAudioPlayListener listener)
    {
        mediaPlayer = new MediaPlayer();

        if (listener != null)
        {
            listener.setMediaPlayer(mediaPlayer);
            mediaPlayer.setOnCompletionListener(listener);
            mediaPlayer.setOnErrorListener(listener);
            mediaPlayer.setOnPreparedListener(listener);
        }

        try
        {
            mediaPlayer.setDataSource(filePath);           
        } catch (Exception e)
        {
            e.printStackTrace();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.e(TAG, "语音文件有误，无法播放！");
            return false;
        }

        mediaPlayer.prepareAsync();

        return true;
    }
    /**
     * 播放项目目录下载的文件
     * @param context
     * @param fd
     * @param listener
     */
    public  void playAudioMessage(
			AssetFileDescriptor fd, CommonAudioPlayListener listener) 
    {
			mediaPlayer = new MediaPlayer();
			 if (listener != null)
		      {
		            listener.setMediaPlayer(mediaPlayer);
		            mediaPlayer.setOnCompletionListener(listener);
		            mediaPlayer.setOnErrorListener(listener);
		            mediaPlayer.setOnPreparedListener(listener);
		      }

			try {
				mediaPlayer.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(), fd.getLength());
				mediaPlayer.prepare();
			} catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			} catch (IllegalStateException e)
			{
				e.printStackTrace();
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			mediaPlayer.start();
	}
    
    /**
     * 播放系统资源的铃音
     */
    void palyRingAsyn(final int mediaRID)
    {
        ThreadHelper.executeWithCallback(new Executable<Void>() {
            @Override
            public Void execute() throws Exception {
                MediaPlayer mediaPlayer0 = MediaPlayer.create(context,mediaRID);
                if (mediaPlayer0 == null)
                {
                    Log.e(TAG, "--->palySystemRing error");
                    return null;
                }
                mediaPlayer0.setOnCompletionListener(new CommonAudioPlayListener(mediaPlayer0));
                mediaPlayer0.start();
                Log.d(TAG, "--->palySystemRing");
                return null;
            }
        }, new TNotifyListener<Void>());
    }
    
    /**
     * 播放系统提示音
     * @param mediaRID
     */
    public synchronized void playMsgNotify(final int mediaRID)
    {
        // 电话是活动的就返回
        if(PhoneStatReceiver.isPhoneActive()) return;
        boolean isPlay = true;
        if(ringTimeMaps.containsKey(mediaRID)) {
            if( System.currentTimeMillis() - ringTimeMaps.get(mediaRID) < 3000) {
                isPlay = false;
            }
        }
        ringTimeMaps.put(mediaRID, System.currentTimeMillis());
        if( !isPlay ) return;
        
    	int soundId = -1;
    	final boolean play = isPlay;
    	if(pool == null) pool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 0);
    	if(soundMaps.containsKey(mediaRID))
    	{
    		soundId = soundMaps.get(mediaRID);   		
    		playOrder(soundId, mediaRID, play);
    	}else
    	{
    		pool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					if(status == 0){
					    soundMaps.put(mediaRID, sampleId);
						playOrder(sampleId, mediaRID, play);					
					}
				}
			});
    	    pool.load(context, mediaRID, 1);
    	}
    }
    
    /** 按顺序时间间隔播放*/
    private void playOrder(int sampleId, int mediaId, boolean isPlay) {
        if(pool == null) return;
        /**播放音频，
        第二个参数为左声道音量;
        第三个参数为右声道音量;
        第四个参数为优先级；
        第五个参数为循环次数，0不循环，-1循环;
        第六个参数为速率，速率    最低0.5最高为2，1代表正常速度
        */
        if(isPlay) {
            int statu = pool.play(sampleId, 1, 1, 0, 0, 1);
            if(statu == 0) {
                soundMaps.remove(mediaId);
            }
        }
    }

    /**
     * 调节音量，
     * 
     * @param addAble
     *            true：增加; false:减少
     */
    public void adjustVolume(boolean addAble)
    {
        audioMgr.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                addAble ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER,
                0);
    }
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * 音频播放监听器
     * 
     * @author zhaohua
     */
    public static class CommonAudioPlayListener implements
            OnCompletionListener , MediaPlayer.OnErrorListener ,
            OnPreparedListener
    {
        protected MediaPlayer mediaPlayerO;

        public CommonAudioPlayListener()
        {

        }

        public void setMediaPlayer(MediaPlayer mp)
        {
            mediaPlayerO = mp;
        }

        public CommonAudioPlayListener(MediaPlayer mp)
        {
            mediaPlayerO = mp;
        }

        @Override
        public void onPrepared(MediaPlayer mp)
        {
            if (mediaPlayerO != null)
            {
                mediaPlayerO.start();
            }
        }

        public boolean onError(MediaPlayer mp, int what, int extra)
        {
            if (mediaPlayerO != null)
            {
                mediaPlayerO.release();
                mediaPlayerO = null;
            }
            Log.e(TAG, "语音文件有误，播放失败！");
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp)
        {
            if (mediaPlayerO != null)
            {
                mediaPlayerO.release();
                mediaPlayerO = null;
            }
        }
        
        public void stopPlay()
        {
            if (mediaPlayerO != null)
            {
                try {
                    mediaPlayerO.stop();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayerO.release();
                mediaPlayerO = null;
            }
        }
    }
}
