package edu.kit.tm.pseprak2.alushare.view;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

/**
 * Represent the AudioRecorder of the ChatActivity.
 * Enables recording sound and playing the music before sending.
 *
 * Created by Arthur Anselm on 17.08.15.
 */
public class AudioRecorder {

    private String mFileName = null;
    private String TAG = "AudioRecorder";
    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;
    private ChatController controller;
    private boolean busy;
    //private SeekBar mSeekbar;
    //private Handler mHandler;

    /**
     * The constructor of this class.
     * @param controller    the controller for this audioRecorder object
     */
    public AudioRecorder(ChatController controller){
        if(controller == null){
            throw new NullPointerException();
        }
        this.controller = controller;
        //this.mSeekbar = seekBar;
        //this.mHandler = new Handler();
        this.mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        this.mFileName += "/audiorecord_alushare.m4a";
        this.busy = false;
    }

    /**
     * Invokes the method startRecording() (and sets busy to true) if start is true else
     * stopRecording().
     * @param start boolean to start or stop recording
     */
    public void onRecord(boolean start) {
        if (start) {
            busy = true;
            startRecording();
        } else {
            stopRecording();
        }
    }

    /**
     * Invokes startPlaying() if start is true else stopPlaying().
     * @param start boolean to start or stop playing
     */
    public void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    /**
     * Creates a new mediaPlayer and sets it up. After that starts playing the audoFile stored in
     * the filesPath mFileName
     */
    private void startPlaying() {
        if(mFileName == null){
            throw new NullPointerException("Recorded file is null.");
        }
        setUpMediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    /**
     * Creates a new mediaPlayer and sets the completion listener.
     */
    private void setUpMediaPlayer(){
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e(TAG, " : Playing of Sound completed");
                controller.setPlayButtonVisible(true);
            }
        });

        /*
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                mSeekbar.setMax(mp.getDuration());
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (mp != null && mp.getCurrentPosition() < mp.getDuration()) {
                            mSeekbar.setProgress(mp.getCurrentPosition());
                            Message msg = new Message();
                            int millis = mp.getCurrentPosition();

                            msg.obj = millis / 1000;
                            mHandler.sendMessage(msg);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

            }
        });*/
    }

    /**
     * Stops playing the mediaPlayer with the method release() and releases the references to the
     * mediaPlayer if the mediaPlayer isn't null.
     */
    private void stopPlaying() {
        if(mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        } else {
            Log.e(TAG, "MediaPlayer is null. stopPlaying() can't be executed.");
        }
    }

    /**
     * Creates a new mediaRecorder and set it up.
     * Start recording and saving the audio data into the file stored at fileName.
     */
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(96000);
        mRecorder.setOutputFile(mFileName);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
            return;
        }
            mRecorder.start();
    }

    /**
     * Stops recording, invokes release of the mediaRecorder and releases the references to the
     * mediaRecorder if the mediaRecorder isn't null.
     */
    private void stopRecording() {
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } else {
            Log.e(TAG, "MediaRecorder is null. stopRecording() can't be executed.");
        }
    }

    /**
     *Return the filePath of the recorded audio file
     * @return this.mFileName
     */
    public String getFilePath(){
        return mFileName;
    }

    /**
     * Sets the busyness of this audioRecorder
     * @param busy true if the audioRecorder is active else false
     */
    public void setBusy(boolean busy){
        this.busy = busy;
    }

    /**
     * Checks if this audioRecorder is active oder not. Returns busy.
     * @return  busy
     */
    public boolean isBusy(){
        return busy;
    }

}


