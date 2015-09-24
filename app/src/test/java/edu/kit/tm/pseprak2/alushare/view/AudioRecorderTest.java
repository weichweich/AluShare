package edu.kit.tm.pseprak2.alushare.view;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockNetProtocol;

import static org.junit.Assert.assertTrue;

/**
 *
 * Created by arthur anselm on 29.08.15.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AudioRecorderTest {

    private AudioRecorder audioRecorder;
    private Context context = RuntimeEnvironment.application;
    private ChatActivity chatActivity;
    private ChatController chatController;
    //private MediaRecorder mRecorder = null;
    //private MediaPlayer mPlayer = null;



    @Before
    public void setUp(){
        String chatId = new MockNetProtocol(context).createNewNetworkChatID();
        DummyDataSet.generateChat(chatId, context);

        //Starting ChatActivity with chatId used above
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(context.getString(R.string.CHAT_ID), chatId);
        this.chatActivity = Robolectric.buildActivity(ChatActivity.class)
                .withIntent(chatIntent).create().get();
        chatController = new ChatController(chatActivity);
        audioRecorder = new AudioRecorder(chatController);
    }

    @Test(expected=NullPointerException.class)
    public void testCreateAudioRecorder(){
        new AudioRecorder(null);
    }

    @Test
    public void testOnRecord() throws NoSuchFieldException, IllegalAccessException {
        audioRecorder.onRecord(true);
        Field field = AudioRecorder.class.getDeclaredField("mRecorder");
        field.setAccessible(true);
        MediaRecorder mediaRecorder = (MediaRecorder) field.get(audioRecorder);
        assertTrue(mediaRecorder != null);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioRecorder.onRecord(false);
        mediaRecorder = (MediaRecorder) field.get(audioRecorder);
        assertTrue(mediaRecorder == null);
    }

    @Test
    public void testOnRecordStop() throws NoSuchFieldException, IllegalAccessException {
        audioRecorder.onRecord(false);
        Field field = AudioRecorder.class.getDeclaredField("mRecorder");
        field.setAccessible(true);
        MediaRecorder mediaRecorder = (MediaRecorder) field.get(audioRecorder);
        assertTrue(mediaRecorder == null);

    }


    public void testOnPlay(){
        audioRecorder.onPlay(true);

        audioRecorder.onPlay(false);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testOnPlayStartWithoutRecording(){
        audioRecorder.onPlay(true);
    }

    @Test
    public void tesOnPlayStopWithoutRecording() throws NoSuchFieldException, IllegalAccessException {
        audioRecorder.onPlay(false);
        Field field = AudioRecorder.class.getDeclaredField("mRecorder");
        field.setAccessible(true);
        MediaRecorder mediaRecorder = (MediaRecorder) field.get(audioRecorder);
        assertTrue(mediaRecorder == null);
    }

    @After
    public void tearDown() {
        /*if(this.mPlayer != null) {
            this.mPlayer.release();
            this.mPlayer = null;
        }
        if(this.mRecorder != null) {
            this.mRecorder.stop();
            this.mRecorder.release();
            this.mRecorder = null;
        }*/
        if(this.audioRecorder != null) {
            this.audioRecorder = null;
        }
        this.chatController = null;
        this.chatActivity = null;
        TestHelper.resetHelperFactory();
    }
}

