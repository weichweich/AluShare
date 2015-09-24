package edu.kit.tm.pseprak2.alushare.model;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import edu.kit.tm.pseprak2.alushare.BuildConfig;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Dominik Köhler
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class ASFileTest {
    private Context mContext = RuntimeEnvironment.application;
    private String conPath = mContext.getFilesDir().getPath() + '/';
    private String expectedFileASName = "Dateiname.jpeg";
    private String expectedPath = conPath + expectedFileASName;
    private long expectedDataID = 1;
    private long expectedASFileID = 1;
    private ASFile expectedFile;

    @Test
    public void testConstructorWithAllParams() {
        expectedFile = new ASFile(expectedASFileID, expectedDataID, expectedPath, expectedFileASName,true);

        assertEquals(expectedPath, expectedFile.getPath());
        assertEquals(expectedFileASName, expectedFile.getASName());
        assertEquals(expectedDataID, expectedFile.getDataId());
        assertEquals(expectedASFileID, expectedFile.getId());
        assertTrue(expectedFile.getReceived());
    }

    @Test
    public void testConstructorWithContextASNameDataID() {
        expectedFile = new ASFile(mContext, expectedFileASName, expectedDataID);
        String[] path = expectedFile.getPath().split("_");

        assertEquals(expectedFileASName, path[1]);
        assertEquals(expectedFileASName, expectedFile.getASName());
        assertEquals(expectedDataID, expectedFile.getDataId());
        assertEquals(-1, expectedFile.getId());
        assertFalse(expectedFile.getReceived());
    }

    @Test
    public void testConstructorWithContextASName() {
        expectedFile = new ASFile(mContext, expectedFileASName, expectedDataID);
        String[] path = expectedFile.getPath().split("_");

        assertEquals(expectedFileASName, path[1]);
        assertEquals(expectedFileASName, expectedFile.getASName());
        assertEquals(expectedDataID, expectedFile.getDataId());
        assertEquals(-1, expectedFile.getId());
        assertFalse(expectedFile.getReceived());
    }
}
