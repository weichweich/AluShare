package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import edu.kit.tm.pseprak2.alushare.BuildConfig;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Dominik Köhler
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class SQLDatabaseHelperTest {
    private Context mContext = RuntimeEnvironment.application;
    private SQLDatabaseHelper helper;
    private ChatHelper chatHelper;

    @Before
    public void setUp() throws Exception {
        helper = new SQLDatabaseHelper(mContext);
        chatHelper = HelperFactory.getChatHelper(mContext);
    }

    @Test
    public void testCreateDB() {
        SQLiteDatabase db = helper.getWritableDatabase();
        assertTrue(db.isOpen());
        db.close();
    }

    @Test
    public void testOnUpgrade() {
        SQLiteDatabase db = helper.getWritableDatabase();
        helper.onUpgrade(db, 1, 1);
        assertTrue(chatHelper.getChats().size() == 0);
    }
}
