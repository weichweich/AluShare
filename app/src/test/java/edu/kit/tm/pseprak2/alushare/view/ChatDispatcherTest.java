package edu.kit.tm.pseprak2.alushare.view;

import android.content.Context;
import android.content.Intent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockNetProtocol;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChatRecyclerAdapter;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by root on 30.08.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ChatDispatcherTest {

    private Context context = RuntimeEnvironment.application.getApplicationContext();
    private ChatActivity chatActivity;
    private ChatDispatcher chatDispatcher;

    @Before
    public void setUp(){
        String chatId = new MockNetProtocol(context).createNewNetworkChatID();
        DummyDataSet.generateChat(chatId, context);

        //Starting ChatActivity with chatId used above
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(context.getString(R.string.CHAT_ID), chatId);
        this.chatActivity = Robolectric.buildActivity(ChatActivity.class)
                .withIntent(chatIntent).create().start().get();
        chatDispatcher = new ChatDispatcher(chatActivity);
    }

    @Test
    public void testDispatchIsLocked() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, NoSuchFieldException {
        //for(int i = 1;)
        int request = ChatDispatcher.REQUEST_SELECT_FILE;
        Field field = ChatDispatcher.class.getDeclaredField("dispatchLocked");
        field.setAccessible(true);
        boolean[] locked = (boolean[]) field.get(chatDispatcher);
        assertFalse(locked[request]);
        Method method = ChatDispatcher.class.getDeclaredMethod("dispatchIsLocked", int.class);
        method.setAccessible(true);
        boolean lockd = (boolean) method.invoke(chatDispatcher, request);
        assertFalse(lockd);
        lockd = (boolean) method.invoke(chatDispatcher, request);
        assertTrue(lockd);
        locked = (boolean[]) field.get(chatDispatcher);
        assertTrue(locked[request]);
    }

    @Test
    public void testSelectPicVidIntent(){
        chatDispatcher.selectPicVidIntent();
        ShadowActivity shadowActivity = Shadows.shadowOf(chatActivity);
        ShadowActivity.IntentForResult intent = shadowActivity.getNextStartedActivityForResult();
        assertTrue( intent.getClass().getName().equals(ChatActivity.class.getName()));
    }

    @Test
    public void testSelectFileIntent(){
        chatDispatcher.selectFileIntent();
        ShadowActivity shadowActivity = Shadows.shadowOf(chatActivity);
        ShadowActivity.IntentForResult intent = shadowActivity.getNextStartedActivityForResult();
        assertTrue(intent.getClass().getName().equals(ChatActivity.class.getName()));
    }
    @Test
    public void testTakePictureIntent(){
        chatDispatcher.takePictureIntent();
        ShadowActivity shadowActivity = Shadows.shadowOf(chatActivity);
        ShadowActivity.IntentForResult intent = shadowActivity.getNextStartedActivityForResult();
        assertTrue( intent.getClass().getName().equals(ChatActivity.class.getName()));
    }
    @Test
    public void testTakeVideoIntent(){
        chatDispatcher.takeVideoIntent();
        ShadowActivity shadowActivity = Shadows.shadowOf(chatActivity);
        ShadowActivity.IntentForResult intent = shadowActivity.getNextStartedActivityForResult();
        assertTrue( intent.getClass().getName().equals(ChatActivity.class.getName()));
    }

    @After
    public void tearDown(){
        chatDispatcher = null;
        chatActivity = null;
        TestHelper.resetHelperFactory();

    }
}
