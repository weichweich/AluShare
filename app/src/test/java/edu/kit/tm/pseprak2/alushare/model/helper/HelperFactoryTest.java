package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import edu.kit.tm.pseprak2.alushare.BuildConfig;

import static org.junit.Assert.assertNotNull;

/**
 * Created by dominik on 09.09.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class HelperFactoryTest {
    private Context mContext = RuntimeEnvironment.application;

    @Before
    public void setUp() {
        new HelperFactory();
    }

    @Test
    public void testGetChatHelper() {
        assertNotNull(HelperFactory.getChatHelper(mContext));
    }

    @Test
    public void testGetDataHelper() {
        assertNotNull(HelperFactory.getDataHelper(mContext));
    }

    @Test
    public void testGetFileHelper() {
        assertNotNull(HelperFactory.getFileHelper(mContext));
    }

    @Test
    public void testGetDataStateHelper() {
        assertNotNull(HelperFactory.getDataStateHelper(mContext));
    }
}
