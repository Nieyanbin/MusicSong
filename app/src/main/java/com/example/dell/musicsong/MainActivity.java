package com.example.dell.musicsong;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.Since;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    //歌词
private Handler handler=new Handler(){
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        SongBean obj = (SongBean) msg.obj;
        String lrcContent = obj.getLrcContent();
        //解析歌词构造器
        ILrcBuilder builder = new DefaultLrcBuilder();
        //解析歌词返回LrcRow集合
        List<LrcRow> rows = builder.getLrcRows(lrcContent);
        //将得到的歌词集合传给mLrcView用来展示
        mLrcView.setLrc(rows);


        //设置自定义的LrcView上下拖动歌词时监听
        mLrcView.setListener(new ILrcViewListener() {
            //当歌词被用户上下拖动的时候回调该方法,从高亮的那一句歌词开始播放
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (mPlayer != null) {
                    Log.d(TAG, "onLrcSeeked:" + row.time);
                    mPlayer.seekTo((int) row.time);
                }
            }
        });

    }
};
    //开始播放歌曲并同步展示歌词
    private Handler handler1=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SingBean obj = (SingBean) msg.obj;
            String file_link = obj.getBitrate().getFile_link();
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(file_link);
                //准备播放歌曲监听
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    //准备完毕
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                        if(mTimer == null){
                            mTimer = new Timer();
                            mTask = new LrcTask();
                            mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration);
                        }
                    }
                });
                //歌曲播放完毕监听
                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        stopLrcPlay();
                    }
                });
                //准备播放歌曲
                mPlayer.prepare();
                //开始播放歌曲
                mPlayer.start();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    public final static String TAG = "MainActivity";

    //自定义LrcView，用来展示歌词
    ILrcView mLrcView;
    //更新歌词的频率，每秒更新一次
    private int mPalyTimerDuration = 1000;
    //更新歌词的定时器
    private Timer mTimer;
    //更新歌词的定时任务
    private TimerTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取自定义的LrcView
        setContentView(R.layout.activity_main);
        mLrcView=(ILrcView)findViewById(R.id.lrcView);
        //解析
        jiexi();
        jiexigequ();

    }

    private void jiexigequ() {
        String url="http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=877578";
        OkHttpClient okHttpClient=new OkHttpClient.Builder().addInterceptor(new HttpInterceptor()).build();
        Request request=new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            //成功
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Gson gson=new Gson();
                SingBean singBean = gson.fromJson(string, SingBean.class);
                Message message = handler1.obtainMessage();
                message.obj = singBean;
                handler1.sendMessage(message);
            }
        });
    }

    private void jiexi() {
        String url="http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.lry&songid=877578";
        OkHttpClient okHttpClient=new OkHttpClient.Builder().addInterceptor(new HttpInterceptor()).build();
        Request request=new Request.Builder().url(url).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            //成功
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();
                Gson gson=new Gson();
                SongBean songBean = gson.fromJson(string, SongBean.class);
                Message message = handler.obtainMessage();
                message.obj = songBean;
                handler.sendMessage(message);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }
    MediaPlayer mPlayer;

    /**
     * 停止展示歌曲
     */
    public void stopLrcPlay(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 展示歌曲的定时任务
     */
    class LrcTask extends TimerTask{
        @Override
        public void run() {
            //获取歌曲播放的位置
            final long timePassed = mPlayer.getCurrentPosition();
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //滚动歌词
                    mLrcView.seekLrcToTime(timePassed);
                }
            });

        }
    };
}
