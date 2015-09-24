package edu.kit.tm.pseprak2.alushare.model;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by dominik on 08.09.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class DataStateTest {
    private Context mContext = RuntimeEnvironment.application;
    private DataState expectedDataState;
    private Contact expectedContact;
    private DataState.Type expectedType;
    private int expectedProgress;

    @Before
    public void setUp() {
        expectedType = DataState.Type.NOT_SENT;
        expectedContact = new Contact("asdefiuwefmg");
        expectedProgress = 0;

        expectedDataState = new DataState(expectedContact, expectedType, expectedProgress);
    }

    @Test
    public void testCloningConstructor() {
        expectedDataState = new DataState(expectedDataState);
        assertEquals(expectedType, expectedDataState.getDataStateType());
        assertEquals(-1, expectedDataState.getProgress());
        assertEquals(expectedContact, expectedDataState.getReceiver());
    }

    @Test
    public void testConstructorWithContactAndTypeAndProgress() {
        assertEquals(expectedType, expectedDataState.getDataStateType());
        assertEquals(-1, expectedDataState.getProgress());
        assertEquals(expectedContact, expectedDataState.getReceiver());
    }

    @Test
    public void testConstructorWithContactAndTypeAndProgressB() {
        expectedType = DataState.Type.RECEIVING;
        expectedDataState = new DataState(expectedContact, expectedType, expectedProgress);
        assertEquals(expectedType, expectedDataState.getDataStateType());
        assertEquals(expectedProgress, expectedDataState.getProgress());
        assertEquals(expectedContact, expectedDataState.getReceiver());
    }

    @Test
    public void testConstructorWithContact() {
        expectedDataState = new DataState(expectedContact);

        assertEquals(DataState.Type.UNKNOWN, expectedDataState.getDataStateType());
        assertEquals(-1, expectedDataState.getProgress());
        assertEquals(expectedContact, expectedDataState.getReceiver());

    }

    @Test
    public void testConstructorWithContactAndType() {
        expectedDataState = new DataState(expectedContact, expectedType);

        assertEquals(expectedType, expectedDataState.getDataStateType());
        assertEquals(-1, expectedDataState.getProgress());
        assertEquals(expectedContact, expectedDataState.getReceiver());
    }

    @Test
    public void testReceived() {
        assertFalse(expectedDataState.received());
        expectedDataState = new DataState(expectedContact, DataState.Type.RECEIVED_READ);
        assertTrue(expectedDataState.received());
        expectedDataState = new DataState(expectedContact, DataState.Type.RECEIVED_UNREAD);
        assertTrue(expectedDataState.received());
        expectedDataState = new DataState(expectedContact, DataState.Type.RECEIVING);
        assertTrue(expectedDataState.received());
    }

    @Test
    public void testResetSendingState() {
        expectedDataState.resetSendingState();
        assertEquals(DataState.Type.NOT_SENT, expectedDataState.getDataStateType());
    }

    @Test
    public void testSetProgress() {
        expectedDataState.setProgress(100);
        assertEquals(-1, expectedDataState.getProgress());

        expectedDataState = new DataState(expectedContact, DataState.Type.SENDING);
        expectedDataState.setProgress(100);
        assertEquals(100, expectedDataState.getProgress());
    }

    @Test
    public void testSendingStarted() {
        expectedDataState.sendingStarted();
        assertEquals(DataState.Type.SENDING, expectedDataState.getDataStateType());
        assertEquals(0, expectedDataState.getProgress());

        expectedDataState = new DataState(expectedContact, DataState.Type.SENDING_SUCCESS);
        expectedDataState.sendingStarted();
    }

    @Test
    public void testSendingFinished() {
        expectedDataState.sendingFinished();

        expectedDataState = new DataState(expectedContact, DataState.Type.SENDING);
        expectedDataState.sendingFinished();
        assertEquals(DataState.Type.SENDING_SUCCESS, expectedDataState.getDataStateType());
    }

    @Test
    public void testSendingFailed() {
        expectedDataState.sendingFailed();

        expectedDataState = new DataState(expectedContact, DataState.Type.SENDING);
        expectedDataState.sendingFailed();
        assertEquals(DataState.Type.SENDING_FAILED, expectedDataState.getDataStateType());
    }

    @Test
    public void testWasSendSuccessful() {
        assertFalse(expectedDataState.wasSendSuccessful());
        expectedDataState = new DataState(expectedContact, DataState.Type.SENDING_SUCCESS);
        assertTrue(expectedDataState.wasSendSuccessful());
    }

    @Test
    public void testWasRead() {
        assertFalse(expectedDataState.wasRead());
        expectedDataState = new DataState(expectedContact, DataState.Type.RECEIVED_READ);
        assertTrue(expectedDataState.wasRead());
    }

    @Test
    public void testWasFailedToSend() {
        assertFalse(expectedDataState.wasFailedToSend());
        expectedDataState = new DataState(expectedContact, DataState.Type.SENDING_FAILED);
        assertTrue(expectedDataState.wasFailedToSend());
    }

    @Test
    public void testSetWasRead() {
        assertFalse(expectedDataState.setWasRead());

        expectedDataState = new DataState(expectedContact, DataState.Type.RECEIVED_UNREAD);
        assertTrue(expectedDataState.setWasRead());
        assertEquals(DataState.Type.RECEIVED_READ, expectedDataState.getDataStateType());

        expectedDataState = new DataState(expectedContact, DataState.Type.RECEIVING);
        assertFalse(expectedDataState.setWasRead());
    }

    @Test
    public void testCreateStates() {
        List<Contact> contactList = new ArrayList<>();
        contactList.add(new Contact(1, "asd1", "asd1"));
        contactList.add(new Contact(2, "asd2", "asd2"));
        contactList.add(new Contact(3, "asd2", "asd3"));

        HashMap<Long, DataState> hashMap = DataState.createStates(contactList, DataState.Type.NOT_SENT);

        for (Contact con : contactList) {
            assertEquals(DataState.Type.NOT_SENT, hashMap.get(con.getId()).getDataStateType());
        }
    }
}