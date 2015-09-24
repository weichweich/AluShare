package edu.kit.tm.pseprak2.alushare.model;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import edu.kit.tm.pseprak2.alushare.BuildConfig;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.format;

/**
 * Created by dominik on 17.08.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class DataTest {
    private Context mContext = RuntimeEnvironment.application;

    private String expectedText;
    private long expectedDataID;
    private String expectedNetworkChatID;
    private Data expectedData;
    private ASFile expectedASFile;
    private DataState expectedState;
    private Timestamp expectedTimestamp;
    private Contact expectedSender;
    private Contact expectedContact;
    private List<Contact> expectedReceiver = new ArrayList<>();
    private HashMap<Long, DataState> expectedStateMap = new HashMap<>();
    private Data tmp;

    @Before
    public void setUp() throws Exception {
        expectedDataID = 0;
        expectedNetworkChatID = "i4327uhgni7ndxf";
        expectedText = "Hallo, wie geht es dir?";
        expectedTimestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
        expectedSender = new Contact("test.onion");
        expectedContact = new Contact("test2.onion");
        expectedReceiver.add(expectedContact);
        expectedASFile = new ASFile(mContext, "Dateiname.jpeg", expectedDataID);
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.NOT_SENT);

        expectedData = new Data(expectedDataID, expectedNetworkChatID, expectedSender, expectedReceiver, expectedStateMap,expectedTimestamp, expectedText, expectedASFile);
    }

    @Test
    public void testConstructorWithIdAndNetworkChatIdAndSenderAndReceiverAndDataStateAndTimestampAndTextAndASFile() {
        assertEquals(expectedDataID, expectedData.getId());
        assertEquals(expectedNetworkChatID, expectedData.getNetworkChatID());
        assertEquals(expectedSender, expectedData.getSender());
        assertEquals(expectedReceiver, expectedData.getReceivers());
        assertEquals(expectedStateMap, expectedData.getState());
        assertEquals(expectedTimestamp, expectedData.getTimestamp());
        assertEquals(expectedText, expectedData.getText());
        assertEquals(expectedASFile, expectedData.getFile());
    }

    @Test
    public void testConstructorWithIdAndNetworkChatIdAndSenderAndReceiverAndDataStateAndTimestampAndText() {
        expectedData = new Data(expectedDataID, expectedNetworkChatID, expectedSender, expectedReceiver, expectedStateMap,expectedTimestamp, expectedText);

        assertEquals(expectedDataID, expectedData.getId());
        assertEquals(expectedNetworkChatID, expectedData.getNetworkChatID());
        assertEquals(expectedSender, expectedData.getSender());
        assertEquals(expectedReceiver, expectedData.getReceivers());
        assertEquals(expectedStateMap, expectedData.getState());
        assertEquals(expectedTimestamp, expectedData.getTimestamp());
        assertEquals(expectedText, expectedData.getText());
        assertEquals(null, expectedData.getFile());
    }

    @Test
    public void testConstructorWithIdAndNetworkChatIdAndSenderAndReceiverAndDataStateAndTimestampAndASFile() {
        expectedData = new Data(expectedDataID, expectedNetworkChatID, expectedSender, expectedReceiver, expectedStateMap, expectedTimestamp, expectedASFile);

        assertEquals(expectedDataID, expectedData.getId());
        assertEquals(expectedNetworkChatID, expectedData.getNetworkChatID());
        assertEquals(expectedSender, expectedData.getSender());
        assertEquals(expectedReceiver, expectedData.getReceivers());
        assertEquals(expectedStateMap, expectedData.getState());
        assertEquals(expectedTimestamp, expectedData.getTimestamp());
        assertEquals("", expectedData.getText());
        assertEquals(expectedASFile, expectedData.getFile());
    }

    @Test
    public void testConstructorWithSenderAndReceiverAndDataStateAndTextAndASFile() {
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedText, expectedASFile);

        assertEquals(-1, expectedData.getId());
        assertEquals("", expectedData.getNetworkChatID());
        assertEquals(expectedSender, expectedData.getSender());
        assertEquals(expectedReceiver, expectedData.getReceivers());
        assertEquals(expectedStateMap, expectedData.getState());
        assertEquals(null, expectedData.getTimestamp());
        assertEquals(expectedText, expectedData.getText());
        assertEquals(expectedASFile, expectedData.getFile());
    }

    @Test
    public void testConstructorWithSenderAndReceiverAndDataStateAndText() {
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedText);

        assertEquals(-1, expectedData.getId());
        assertEquals("", expectedData.getNetworkChatID());
        assertEquals(expectedSender, expectedData.getSender());
        assertEquals(expectedReceiver, expectedData.getReceivers());
        assertEquals(expectedStateMap, expectedData.getState());
        assertEquals(null, expectedData.getTimestamp());
        assertEquals(expectedText, expectedData.getText());
        assertEquals(null, expectedData.getFile());
    }

    @Test
    public void testConstructorWithSenderAndReceiverAndDataStateAndASFile() {
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);

        assertEquals(-1, expectedData.getId());
        assertEquals("", expectedData.getNetworkChatID());
        assertEquals(expectedSender, expectedData.getSender());
        assertEquals(expectedReceiver, expectedData.getReceivers());
        assertEquals(expectedStateMap, expectedData.getState());
        assertEquals(null, expectedData.getTimestamp());
        assertEquals("", expectedData.getText());
        assertEquals(expectedASFile, expectedData.getFile());
    }

    @Test
    public void testCloneConstructor() {
        Data clone = new Data(expectedData);

        assertEquals(expectedDataID, clone.getId());
        assertEquals(expectedNetworkChatID, clone.getNetworkChatID());
        assertEquals(expectedSender, clone.getSender());
        assertEquals(expectedReceiver, clone.getReceivers());
        assertEquals(expectedStateMap, clone.getState());
        assertEquals(expectedTimestamp, clone.getTimestamp());
        assertEquals(expectedText, clone.getText());
        assertEquals(expectedASFile, clone.getFile());
    }

    @Test
    public void testGetState() {
        assertEquals(expectedStateMap.get(expectedContact.getId()), expectedData.getState(expectedContact));
    }

    @Test
    public void testGetStateShouldReturnNull() {
        Contact notInStateMap = new Contact(999, "asdsaderfe", "asdfaef4re");
        assertEquals(null, expectedData.getState(notInStateMap));
    }

    @Test
    public void testSetReceiverShouldRemoveSelf() {
        expectedReceiver.add(expectedSender);
        expectedData.setReceivers(expectedReceiver);
        assertFalse(expectedData.getReceivers().contains(expectedSender));
    }

    @Test
    public void testGetTimestampString() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        assertEquals(format.format(expectedTimestamp), expectedData.getTimestampString(mContext));
    }

    @Test
    public void testGetTimestampStringShouldBeYesterday() {
        expectedData.setTimestamp(new Timestamp(Calendar.getInstance().getTimeInMillis() - 86400000));
        assertEquals("Gestern", expectedData.getTimestampString(mContext));
    }

    @Test
    public void testGetTimestampStringShouldBeTheDate() {
        Timestamp oldDate = new Timestamp(Calendar.getInstance().getTimeInMillis() - 86400000 * 2);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");
        expectedData.setTimestamp(oldDate);
        assertEquals(format.format(oldDate), expectedData.getTimestampString(mContext));
    }

    @Test
    public void testResend() {
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.SENDING_FAILED);
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);
        assertTrue(expectedData.resend());
        assertEquals(DataState.Type.NOT_SENT, expectedStateMap.get(expectedContact.getId()).getDataStateType());
    }

    @Test
    public void stopSending() {
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.SENDING);
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);
        expectedData.stopSending();
        assertEquals(DataState.Type.SENDING_FAILED, expectedStateMap.get(expectedContact.getId()).getDataStateType());
    }

    @Test
    public void needsResendTrue(){
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.SENDING_FAILED);
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);
        assertTrue(expectedData.needsResend());
    }

    @Test
    public void needsResendFalse(){
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.SENDING);
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);
        assertFalse(expectedData.needsResend());
    }

    @Test
    public void testSendingCompletedTrue(){
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.SENDING_SUCCESS);
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);
        assertTrue(expectedData.sendingCompleted());
    }

    @Test
    public void testSendingCompletedFalse(){
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.NOT_SENT);
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);
        assertFalse(expectedData.sendingCompleted());
    }

    @Test
    public void testSendingStopped(){
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.SENDING);
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);
        expectedData.sendingStopped();
        assertEquals(DataState.Type.SENDING_FAILED, expectedStateMap.get(expectedContact.getId()).getDataStateType());
    }

    @Test
    public void testWasNotSendTrue(){
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.NOT_SENT);
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);
        assertTrue(expectedData.wasNotSend());
    }

    @Test
    public void testWasNotSendFalse(){
        expectedStateMap = DataState.createStates(expectedReceiver, DataState.Type.SENDING);
        expectedData = new Data(expectedSender, expectedReceiver, expectedStateMap, expectedASFile);
        assertFalse(expectedData.wasNotSend());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testSetReceiverStateMapShouldThrowUp() {
        expectedStateMap = new HashMap<Long, DataState>();
        Contact notInStateMap = new Contact(999, "asdsaderfe", "asdfaef4re");
        expectedReceiver.add(notInStateMap);
        expectedData.setReceivers(expectedReceiver);
        expectedData.setReceiverStateMap(expectedStateMap);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testSetReceiverStateMapShouldThrowUpAgain() {
        expectedStateMap = new HashMap<Long, DataState>();
        Contact conA = new Contact(999, "asdsaderfe", "asdfaef4re");
        Contact conB = new Contact(666, "asdsaderfe", "asdfaef4re");
        expectedReceiver.add(conA);
        expectedReceiver.add(conB);
        expectedData.setReceivers(expectedReceiver);

        expectedStateMap.put(expectedContact.getId(), new DataState(expectedContact, DataState.Type.RECEIVED_READ));
        expectedStateMap.put(conA.getId(), new DataState(conA, DataState.Type.UNKNOWN));
        expectedStateMap.put(conB.getId(), new DataState(conB, DataState.Type.SENDING));
        expectedData.setReceiverStateMap(expectedStateMap);
    }

    @Test
    public void testEquals() {
        int i = 0;
        tmp = new Data(expectedData);

        assertTrue(expectedData.equals(expectedData));
        assertTrue(expectedData.equals(tmp));
        assertFalse(expectedData.equals(null));
        assertFalse(expectedData.equals(i));

        tmp.setText("othertext");
        assertEqualsReturnsFalse();

        tmp.setTimestamp(null);
        assertEqualsReturnsFalse();

        tmp.setTimestamp(new Timestamp(0));
        assertEqualsReturnsFalse();

        tmp.setFile(null);
        assertEqualsReturnsFalse();

        tmp.setFile(new ASFile(mContext, "magic"));
        assertEqualsReturnsFalse();

        tmp.setSender(null);
        assertEqualsReturnsFalse();

        tmp.setSender(new Contact("Wreeeeeeeeehhhhhhhhh"));
        assertEqualsReturnsFalse();

        tmp.setReceivers(new ArrayList<Contact>());
        assertEqualsReturnsFalse();
    }

    private void assertEqualsReturnsFalse() {
        assertFalse(expectedData.equals(tmp));
        tmp = new Data(expectedData);
    }
}
