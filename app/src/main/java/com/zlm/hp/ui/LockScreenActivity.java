package com.zlm.hp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.SongSingerDB;
import com.zlm.hp.lyrics.utils.LyricsUtil;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.model.SongSingerInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.utils.AniUtil;
import com.zlm.hp.utils.ImageUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import base.utils.ColorUtil;
import base.widget.SingerImageView;
import base.widget.SwipeBackLayout;
import base.widget.lock.LockButtonRelativeLayout;
import base.widget.lock.LockPalyOrPauseButtonRelativeLayout;
import base.widget.lrc.ManyLineLyricsViewV2;

/**
 * @Description: 锁屏界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018/1/19 16:07
 * @Throws:
 */
public class LockScreenActivity extends BaseActivity {

    private HPApplication mHPApplication;

    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 滑动提示图标
     */
    private ImageView lockImageView;
    private AnimationDrawable aniLoading;

    /**
     * 时间
     */
    private TextView timeTextView;
    /**
     * 日期
     */
    private TextView dateTextView;
    /**
     * 星期几
     */
    private TextView dayTextView;

    /**
     * 歌名
     */
    private TextView songNameTextView;
    /**
     * 歌手
     */
    private TextView songerTextView;
    //暂停、播放图标
    private ImageView playImageView;
    private ImageView pauseImageView;

    /**
     * 上一首按钮
     */
    private LockButtonRelativeLayout prewButton;
    /**
     * 下一首按钮
     */
    private LockButtonRelativeLayout nextButton;

    /**
     * 播放或者暂停按钮
     */
    private LockPalyOrPauseButtonRelativeLayout playOrPauseButton;

    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    /**
     * 广播监听
     */
    private AudioBroadcastReceiver.AudioReceiverListener mAudioReceiverListener = new AudioBroadcastReceiver.AudioReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };

    /**
     * 分钟广播
     */
    private Handler mTimeHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            setDate();
        }
    };

    /**
     * 分钟变化广播
     */
    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                mTimeHandler.sendEmptyMessage(0);
            }
        }
    };

    /**
     * 歌手写真
     */
    private SingerImageView mSingerImageView;

    /**
     * 多行歌词视图
     */
    private ManyLineLyricsViewV2 mManyLineLyricsView;

    //、、、、、、、、、、、、、、、、、、、、、、、、、翻译和音译歌词、、、、、、、、、、、、、、、、、、、、、、、、、、、
    //翻译歌词
    private ImageView mHideTranslateImg;
    private ImageView mShowTranslateImg;
    //音译歌词
    private ImageView mHideTransliterationImg;
    private ImageView mShowTransliterationImg;

    //翻译歌词/音译歌词
    private ImageView mShowTTToTranslateImg;
    private ImageView mShowTTToTransliterationImg;
    private ImageView mHideTTImg;

    private final int HASTRANSLATELRC = 0;
    private final int HASTRANSLITERATIONLRC = 1;
    private final int HASTRANSLATEANDTRANSLITERATIONLRC = 2;
    private final int NOEXTRALRC = 3;

    /**
     * 屏幕宽度
     */
    private int mScreensWidth;

    private Handler mExtraLrcTypeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NOEXTRALRC:

                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);


                    break;
                case HASTRANSLATEANDTRANSLITERATIONLRC:


                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);
                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);

                    if (mManyLineLyricsView.isManyLineLrc()) {
                        //翻译歌词/音译歌词
                        mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                        mShowTTToTransliterationImg.setVisibility(View.VISIBLE);
                        mHideTTImg.setVisibility(View.INVISIBLE);
                    } else {
                        //翻译歌词/音译歌词
                        mShowTTToTranslateImg.setVisibility(View.VISIBLE);
                        mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                        mHideTTImg.setVisibility(View.INVISIBLE);
                    }

                    break;
                case HASTRANSLITERATIONLRC:

                    //翻译歌词
                    mHideTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTranslateImg.setVisibility(View.INVISIBLE);

                    if (mManyLineLyricsView.isManyLineLrc()) {
                        //音译歌词
                        mHideTransliterationImg.setVisibility(View.VISIBLE);
                        mShowTransliterationImg.setVisibility(View.INVISIBLE);
                    } else {
                        //音译歌词
                        mHideTransliterationImg.setVisibility(View.INVISIBLE);
                        mShowTransliterationImg.setVisibility(View.VISIBLE);
                    }


                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);

                    break;
                case HASTRANSLATELRC:

                    if (mManyLineLyricsView.isManyLineLrc()) {
                        //翻译歌词
                        mHideTranslateImg.setVisibility(View.VISIBLE);
                        mShowTranslateImg.setVisibility(View.INVISIBLE);
                    } else {
                        //翻译歌词
                        mHideTranslateImg.setVisibility(View.INVISIBLE);
                        mShowTranslateImg.setVisibility(View.VISIBLE);
                    }


                    //音译歌词
                    mHideTransliterationImg.setVisibility(View.INVISIBLE);
                    mShowTransliterationImg.setVisibility(View.INVISIBLE);

                    //翻译歌词/音译歌词
                    mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                    mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                    mHideTTImg.setVisibility(View.INVISIBLE);


                    break;

            }

        }
    };

    //、、、、、、、、、、、、、、、、、、、、、、、、、翻译和音译歌词、、、、、、、、、、、、、、、、、、、、、、、、、、、


    @Override
    protected int setContentViewId() {
        return R.layout.activity_lock_screen;
    }

    @Override
    protected void preLoad() {
        super.preLoad();
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        //
        mHPApplication = HPApplication.getInstance();
        //
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setShadowEnable(false);
        mSwipeBackLayout.setmSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {
            @Override
            public void finishView() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        //提示右滑动图标
        lockImageView = (ImageView) findViewById(R.id.tip_image);
        aniLoading = (AnimationDrawable) lockImageView.getBackground();

        //时间
        timeTextView = (TextView) findViewById(R.id.time);
        dateTextView = (TextView) findViewById(R.id.date);
        dayTextView = (TextView) findViewById(R.id.day);

        //歌手与歌名
        songNameTextView = (TextView) findViewById(R.id.songName);
        songerTextView = (TextView) findViewById(R.id.songer);


        playImageView = (ImageView) findViewById(R.id.play);
        pauseImageView = (ImageView) findViewById(R.id.pause);
        //播放按钮、上一首，下一首
        prewButton = (LockButtonRelativeLayout) findViewById(R.id.prev_button);
        prewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //
                Intent preIntent = new Intent(AudioBroadcastReceiver.ACTION_PREMUSIC);
                preIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(preIntent);

            }
        });

        nextButton = (LockButtonRelativeLayout) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //
                Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_NEXTMUSIC);
                nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(nextIntent);

            }
        });

        playOrPauseButton = (LockPalyOrPauseButtonRelativeLayout) findViewById(R.id.play_pause_button);
        playOrPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int playStatus = mHPApplication.getPlayStatus();
                if (playStatus == AudioPlayerManager.PLAYING) {

                    Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(resumeIntent);

                } else {
                    if (playStatus == AudioPlayerManager.PAUSE) {

                        AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                        if (audioInfo != null) {

                            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                            Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_RESUMEMUSIC);
                            resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                            resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(resumeIntent);

                        }

                    } else {
                        if (mHPApplication.getCurAudioMessage() != null) {
                            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                            AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                            if (audioInfo != null) {
                                audioMessage.setAudioInfo(audioInfo);
                                Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PLAYMUSIC);
                                resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                                resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                                sendBroadcast(resumeIntent);
                            }
                        }
                    }
                }
            }
        });


        //歌手写真
        mSingerImageView = findViewById(R.id.singerimg);
        mSingerImageView.setVisibility(View.INVISIBLE);

        //多行歌词

        //
        mManyLineLyricsView = findViewById(R.id.lock_manyLineLyricsView);
        //不能触摸和点击事件
        mManyLineLyricsView.setTouchAble(false);
        //翻译歌词
        mHideTranslateImg = findViewById(R.id.hideTranslateImg);
        mHideTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTranslateImg.setVisibility(View.INVISIBLE);
                mShowTranslateImg.setVisibility(View.VISIBLE);
                if (mHPApplication.getCurAudioMessage() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.NOSHOWEXTRALRC, (int) mHPApplication.getCurAudioMessage().getPlayProgress());
                } else {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.NOSHOWEXTRALRC, 0);
                }

                mHPApplication.setManyLineLrc(mManyLineLyricsView.isManyLineLrc());

            }
        });
        mShowTranslateImg = findViewById(R.id.showTranslateImg);
        mShowTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTranslateImg.setVisibility(View.VISIBLE);
                mShowTranslateImg.setVisibility(View.INVISIBLE);

                if (mHPApplication.getCurAudioMessage() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLATELRC, (int) mHPApplication.getCurAudioMessage().getPlayProgress());
                } else {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLATELRC, 0);
                }

                mHPApplication.setManyLineLrc(mManyLineLyricsView.isManyLineLrc());

            }
        });
        //音译歌词
        mHideTransliterationImg = findViewById(R.id.hideTransliterationImg);
        mHideTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTransliterationImg.setVisibility(View.INVISIBLE);
                mShowTransliterationImg.setVisibility(View.VISIBLE);

                if (mHPApplication.getCurAudioMessage() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.NOSHOWEXTRALRC, (int) mHPApplication.getCurAudioMessage().getPlayProgress());
                } else {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.NOSHOWEXTRALRC, 0);
                }
                mHPApplication.setManyLineLrc(mManyLineLyricsView.isManyLineLrc());

            }
        });
        mShowTransliterationImg = findViewById(R.id.showTransliterationImg);
        mShowTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHideTransliterationImg.setVisibility(View.VISIBLE);
                mShowTransliterationImg.setVisibility(View.INVISIBLE);

                if (mHPApplication.getCurAudioMessage() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLITERATIONLRC, (int) mHPApplication.getCurAudioMessage().getPlayProgress());

                } else {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLITERATIONLRC, 0);
                }
                mHPApplication.setManyLineLrc(mManyLineLyricsView.isManyLineLrc());
            }
        });

        //翻译歌词/音译歌词
        mShowTTToTranslateImg = findViewById(R.id.showTTToTranslateImg);
        mShowTTToTranslateImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.VISIBLE);
                mHideTTImg.setVisibility(View.INVISIBLE);

                if (mHPApplication.getCurAudioMessage() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLATELRC, (int) mHPApplication.getCurAudioMessage().getPlayProgress());
                } else {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLATELRC, 0);
                }
                mHPApplication.setManyLineLrc(mManyLineLyricsView.isManyLineLrc());
            }
        });
        mShowTTToTransliterationImg = findViewById(R.id.showTTToTransliterationImg);
        mShowTTToTransliterationImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.INVISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                mHideTTImg.setVisibility(View.VISIBLE);

                if (mHPApplication.getCurAudioMessage() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLITERATIONLRC, (int) mHPApplication.getCurAudioMessage().getPlayProgress());
                } else {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.SHOWTRANSLITERATIONLRC, 0);
                }
                mHPApplication.setManyLineLrc(mManyLineLyricsView.isManyLineLrc());
            }
        });
        mHideTTImg = findViewById(R.id.hideTTImg);
        mHideTTImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mShowTTToTranslateImg.setVisibility(View.VISIBLE);
                mShowTTToTransliterationImg.setVisibility(View.INVISIBLE);
                mHideTTImg.setVisibility(View.INVISIBLE);

                if (mHPApplication.getCurAudioMessage() != null) {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.NOSHOWEXTRALRC, (int) mHPApplication.getCurAudioMessage().getPlayProgress());
                } else {
                    mManyLineLyricsView.setExtraLrcStatus(ManyLineLyricsViewV2.NOSHOWEXTRALRC, 0);
                }
                mHPApplication.setManyLineLrc(mManyLineLyricsView.isManyLineLrc());
            }
        });


        //设置额外歌词回调事件
        mManyLineLyricsView.setExtraLyricsListener(new ManyLineLyricsViewV2.ExtraLyricsListener() {

            @Override
            public void hasTranslateLrcCallback() {
                mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLATELRC);
            }

            @Override
            public void hasTransliterationLrcCallback() {
                mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLITERATIONLRC);
            }

            @Override
            public void hasTranslateAndTransliterationLrcCallback() {
                mExtraLrcTypeHandler.sendEmptyMessage(HASTRANSLATEANDTRANSLITERATIONLRC);
            }

            @Override
            public void noExtraLrcCallback() {
                mExtraLrcTypeHandler.sendEmptyMessage(NOEXTRALRC);
            }
        });
        //设置字体大小和歌词颜色
        mManyLineLyricsView.setLrcFontSize(mHPApplication.getLrcFontSize());
        int lrcColor = ColorUtil.parserColor(mHPApplication.getLrcColorStr()[mHPApplication.getLrcColorIndex()]);
        mManyLineLyricsView.setLrcColor(lrcColor);
        mManyLineLyricsView.setManyLineLrc(mHPApplication.isManyLineLrc(), 0);


        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreensWidth = display.getWidth();

        //注册广播
        registerReceiver();

    }

    private void registerReceiver() {


        //注册接收音频播放广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(getApplicationContext());
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());

        //注册分钟变化广播
        IntentFilter mTimeFilter = new IntentFilter();
        mTimeFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, mTimeFilter);
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {

        AniUtil.startAnimation(aniLoading);
        setDate();

        //加载音频数据
        AudioInfo curAudioInfo = mHPApplication.getCurAudioInfo();
        if (curAudioInfo != null) {
            Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
            doAudioReceive(getApplicationContext(), initIntent);
        } else {
            Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
            doAudioReceive(getApplicationContext(), nullIntent);
        }
    }

    /**
     * 设置日期
     */
    private void setDate() {

        String str = "";
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

        Calendar lastDate = Calendar.getInstance();
        str = sdfDate.format(lastDate.getTime());
        dateTextView.setText(str);
        str = sdfTime.format(lastDate.getTime());
        timeTextView.setText(str);

        String mWay = String.valueOf(lastDate.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "日";
        } else if ("2".equals(mWay)) {
            mWay = "一";
        } else if ("3".equals(mWay)) {
            mWay = "二";
        } else if ("4".equals(mWay)) {
            mWay = "三";
        } else if ("5".equals(mWay)) {
            mWay = "四";
        } else if ("6".equals(mWay)) {
            mWay = "五";
        } else if ("7".equals(mWay)) {
            mWay = "六";
        }
        dayTextView.setText("星期" + mWay);

    }


    /**
     * 处理音频广播事件
     *
     * @param context
     * @param intent
     */
    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)) {
            //空数据

            songNameTextView.setText(R.string.def_songName);
            songerTextView.setText(R.string.def_artist);

            playImageView.setVisibility(View.VISIBLE);
            pauseImageView.setVisibility(View.INVISIBLE);

            playOrPauseButton.setPlayingProgress(0);
            playOrPauseButton.setMaxProgress(0);
            playOrPauseButton.invalidate();


            //歌手写真
            mSingerImageView.setVisibility(View.INVISIBLE);
            mSingerImageView.setSongSingerInfos(getApplicationContext(), null);

            //
            mManyLineLyricsView.setLyricsUtil(null, 0, 0);


        } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {

            //初始化
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            AudioInfo audioInfo = mHPApplication.getCurAudioInfo();


            songNameTextView.setText(audioInfo.getSongName());
            songerTextView.setText(audioInfo.getSingerName());

            if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING) {
                playImageView.setVisibility(View.INVISIBLE);
                pauseImageView.setVisibility(View.VISIBLE);
            } else {
                playImageView.setVisibility(View.VISIBLE);
                pauseImageView.setVisibility(View.INVISIBLE);
            }

            playOrPauseButton.setMaxProgress((int) audioInfo
                    .getDuration());
            playOrPauseButton.setPlayingProgress((int) audioMessage.getPlayProgress());
            playOrPauseButton.invalidate();


            //
            mSingerImageView.setVisibility(View.INVISIBLE);
            mSingerImageView.setSongSingerInfos(getApplicationContext(), null);
            //加载歌手写真
            ImageUtil.loadSingerImg(getApplicationContext(), audioInfo.getHash(), audioInfo.getSingerName());


            //加载歌词
            String keyWords = "";
            if (audioInfo.getSingerName().equals("未知")) {
                keyWords = audioInfo.getSongName();
            } else {
                keyWords = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
            }
            LyricsManager.getLyricsManager(getApplicationContext()).loadLyricsUtil(keyWords, keyWords, audioInfo.getDuration() + "", audioInfo.getHash());

            //
            mManyLineLyricsView.setLyricsUtil(null, 0, 0);


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)) {

            //播放

            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);

            if (pauseImageView.getVisibility() != View.VISIBLE) {
                pauseImageView.setVisibility(View.VISIBLE);
            }
            if (playImageView.getVisibility() != View.INVISIBLE) {
                playImageView.setVisibility(View.INVISIBLE);
            }
            playOrPauseButton.setPlayingProgress((int) audioMessage.getPlayProgress());
            playOrPauseButton.invalidate();


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PAUSEMUSIC)) {
            //暂停完成
            pauseImageView.setVisibility(View.INVISIBLE);
            playImageView.setVisibility(View.VISIBLE);

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_RESUMEMUSIC)) {
            //唤醒完成
            pauseImageView.setVisibility(View.VISIBLE);
            playImageView.setVisibility(View.INVISIBLE);


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYINGMUSIC)) {
            //播放中
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();//(AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            if (audioMessage != null) {

                playOrPauseButton.setPlayingProgress((int) audioMessage.getPlayProgress());
                playOrPauseButton.invalidate();

                AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                if (audioInfo != null) {
                    //更新歌词
                    if (mManyLineLyricsView.getLyricsUtil() != null && mManyLineLyricsView.getLyricsUtil().getHash().equals(audioInfo.getHash())) {
                        mManyLineLyricsView.updateView((int) audioMessage.getPlayProgress());
                    }
                }

            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_LRCLOADED)) {

            //歌词加载完成
            AudioMessage curAudioMessage = mHPApplication.getCurAudioMessage();
            AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
            String hash = audioMessage.getHash();
            if (hash.equals(mHPApplication.getCurAudioInfo().getHash())) {
                //
                LyricsUtil lyricsUtil = LyricsManager.getLyricsManager(getApplicationContext()).getLyricsUtil(hash);
                if (lyricsUtil != null) {
                    lyricsUtil.setHash(hash);
                    mManyLineLyricsView.setLyricsUtil(lyricsUtil, mScreensWidth / 3 * 2, (int) curAudioMessage.getPlayProgress());
                    mManyLineLyricsView.updateView((int) curAudioMessage.getPlayProgress());
                }
            }


        } else if (action.equals(AudioBroadcastReceiver.ACTION_RELOADSINGERIMG)) {

            //重新加载歌手写真
            if (mHPApplication.getCurAudioInfo() != null) {
                String hash = intent.getStringExtra("hash");
                if (mHPApplication.getCurAudioInfo().getHash().equals(hash)) {
                    String singerName = intent.getStringExtra("singerName");
                    mSingerImageView.setVisibility(View.INVISIBLE);
                    mSingerImageView.setSongSingerInfos(getApplicationContext(), null);
                    //加载歌手写真
                    ImageUtil.loadSingerImg(getApplicationContext(), hash, singerName);

                }
            }


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SINGERIMGLOADED)) {
            //歌手写真加载完成
            if (mHPApplication.getCurAudioInfo() != null) {
                String hash = intent.getStringExtra("hash");
                if (mHPApplication.getCurAudioInfo().getHash().equals(hash)) {
                    mSingerImageView.setVisibility(View.VISIBLE);

                    String singerName = intent.getStringExtra("singerName");
                    String[] singerNameArray = null;
                    if (singerName.contains("、")) {

                        String regex = "\\s*、\\s*";
                        singerNameArray = singerName.split(regex);


                    } else {
                        singerNameArray = new String[1];
                        singerNameArray[0] = singerName;
                    }


                    //设置数据
                    List<SongSingerInfo> list = SongSingerDB.getSongSingerDB(context).getAllSingerImg(singerNameArray, false);
                    mSingerImageView.setSongSingerInfos(getApplicationContext(), list);
                }
            }
        }
    }


    @Override
    protected boolean isAddStatusBar() {
        setStatusColor(Color.TRANSPARENT);
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return R.id.status_parent_view;
    }

    @Override
    public void finish() {
        AniUtil.stopAnimation(aniLoading);

        //注销广播
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext());
        //注销分钟变化广播
        unregisterReceiver(mTimeReceiver);
        //
        super.finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) { // 屏蔽按键
        if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
