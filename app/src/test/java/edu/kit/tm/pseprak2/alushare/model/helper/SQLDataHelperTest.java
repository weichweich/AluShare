package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;

import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_CHAT_NETWORK_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_DATA_TEXT;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_SENDER_ID;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.KEY_TIMESTAMP;
import static edu.kit.tm.pseprak2.alushare.model.helper.SQLDatabaseHelper.TABLE_DATA;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Dominik Köhler
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class SQLDataHelperTest {
    private Context mContext = RuntimeEnvironment.application;
    private DataHelper dataHelper;
    private ChatHelper chatHelper;
    private ContactHelper contactHelper;
    private Data newData;
    private Chat chat;
    private List<Data> dataList;

    @Before
    public void setUp() throws Exception {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        dataHelper = HelperFactory.getDataHelper(mContext);
        chatHelper = HelperFactory.getChatHelper(mContext);
        contactHelper = HelperFactory.getContacHelper(mContext);

        chat = chatHelper.getChats().get(0);
        dataList = DummyDataSet.generateDataList(mContext, 2, chat.getReceivers());
        newData = dataList.get(0);
        newData.setNetworkChatID(chat.getNetworkChatID());
    }

    @Test
    public void testInsertDataNotAlreadyInDB() {
        dataHelper.insert(newData);
        assertFalse(newData.getId() == -1);
    }

    @Test
    public void testInsertDataAlreadyInDB() {
        String newText = "Text hat sich geändert!";

        Data alreadyInDB = dataHelper.getDataByID(1);
        alreadyInDB.setText(newText);
        dataHelper.insert(alreadyInDB);
        Data tmp = dataHelper.getDataByID(alreadyInDB.getId());

        assertEquals(newText, tmp.getText());
    }

    @Test
    public void testUpdateDataNotAlreadyInDB() {
        dataHelper.update(newData);
        assertFalse(newData.getId() == -1);
    }

    @Test
    public void testUpdateDataAlreadyInDB() {
        String newText = "Text hat sich geändert!";

        Data alreadyInDB = dataHelper.getDataByID(1);
        alreadyInDB.setText(newText);
        dataHelper.update(alreadyInDB);
        Data tmp = dataHelper.getDataByID(alreadyInDB.getId());

        assertEquals(newText, tmp.getText());
    }

    @Test
    public void testDelete() {
        Data alreadyInDB = dataHelper.getDataByID(1);
        dataHelper.delete(alreadyInDB);
        Data tmp = dataHelper.getDataByID(alreadyInDB.getId());
        assertEquals(null, tmp);
    }

    @Test
    public void testExistDataInDB() {
        Data alreadyInDB = chat.getDataObjects().get(0);
        assertTrue(dataHelper.exist(alreadyInDB));
    }

    @Test
    public void testExistDataNotInDB() {
        newData.setId(666);
        assertFalse(dataHelper.exist(newData));
    }

    @Test
    public void testDeleteByNetworkChatID() {
        dataHelper.deleteByNetworkChatID(chat.getNetworkChatID());
        assertEquals(0, dataHelper.getDataObjectsByNetworkChatID(chat.getNetworkChatID()).size());
    }

    @Test
    public void tesGetDataObjects() {
        assertEquals(30, dataHelper.getDataObjects().size());
    }

    @Test
    public void testGetDataObjectsByNetworkChatID() {
        assertEquals(10, dataHelper.getDataObjectsByNetworkChatID(chat.getNetworkChatID()).size());
    }

    @Test
    public void testGetDataObjectsByDataState() {
        List<Data> dataList = dataHelper.getDataObjectsByDataState(DataState.Type.NOT_SENT);
        for (Data tmp : dataList) {
            Iterator iter = tmp.getState().entrySet().iterator();
            while (iter.hasNext()) {
                assertEquals(DataState.Type.NOT_SENT, iter.next());
            }
        }
    }

    @Test
    public void testGetDataByID() {
        for (int i = 1; i < 31; i++) {
            assertFalse(null == dataHelper.getDataByID(i));
        }
    }

    @Test
    public void testGetDataObjectsByDataStateAndContact() {
        Contact contact = contactHelper.getContacts().get(0);
        List<Data> dataList = dataHelper.getDataObjectsByDataStateAndContact(DataState.Type.NOT_SENT, contact);
        for (Data tmp : dataList) {
            assertTrue(tmp.getReceivers().contains(contact));
            Iterator iter = tmp.getState().entrySet().iterator();
            while (iter.hasNext()) {
                assertEquals(DataState.Type.NOT_SENT, iter.next());
            }
        }
    }

    @Test (expected = RuntimeException.class)
    public void testcursorToDataShouldThrowException() {
        Data tmp = dataList.get(1);
        tmp.setText("");
        tmp.setFile(null);
        dataHelper.insert(tmp);
        dataHelper.getDataByID(tmp.getId());
    }

    @After
    public void tearDown() {
        TestHelper.resetHelperFactory();
    }
}
