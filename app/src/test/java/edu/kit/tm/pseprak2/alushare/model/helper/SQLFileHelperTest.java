package edu.kit.tm.pseprak2.alushare.model.helper;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.model.ASFile;

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
public class SQLFileHelperTest {
    private Context mContext = RuntimeEnvironment.application;

    private SQLFileHelper fileHelper;

    private ASFile expectedFile;
    private String updatedFileASName;
    private String expectedFileASName;
    private long expectedDataID;
    private long expectedASFileID;
    private long updatedDataID;
    private String conPath;

    @Before
    public void setUp() throws Exception{
        fileHelper = new SQLFileHelper(mContext);
        assertNotNull(fileHelper);

        conPath = mContext.getFilesDir().getPath() + '/';

        expectedFileASName = "Urlaubsfoto.png";
        expectedDataID = 0;
        expectedFile = new ASFile(mContext, expectedFileASName, expectedDataID);
        fileHelper.insert(expectedFile);
        expectedASFileID = expectedFile.getId();

        updatedFileASName = "NeuerName.png";
        updatedDataID = 1;
    }

    @Test
    public void testInsertFileThatsNotAlreadyInDB() {
        assertASFile(expectedFile, expectedFileASName, expectedDataID);
    }

    public void assertASFile(ASFile file, String expectedFileASName, long expectedDataID) {
        assertEquals(expectedFileASName, file.getASName());
        assertEquals(conPath + file.getName(), file.getPath());
        assertEquals(expectedDataID, file.getDataId());
        assertFalse(file.getId() == -1);
    }

    @Test
    public void testInsertFileThatsAlreadyInDBAnGetsUpdated() {
        expectedFile.setASName(updatedFileASName);
        expectedFile.setDataId(updatedDataID);
        fileHelper.insert(expectedFile);

        ASFile tmp = fileHelper.getFileByID(expectedFile.getId());
        assertASFile(tmp, updatedFileASName, updatedDataID);
    }

    @Test
    public void testUpdateFileThatsNotAlreadyInDB() {
        ASFile tmp = new ASFile(mContext, updatedFileASName, updatedDataID);
        fileHelper.update(tmp);

        assertASFile(tmp, updatedFileASName, updatedDataID);
    }

    @Test
    public void testUpdateFileThatsAlreadyInDB() {
        expectedFile.setASName(updatedFileASName);
        expectedFile.setDataId(updatedDataID);
        fileHelper.update(expectedFile);

        ASFile tmp = fileHelper.getFileByID(expectedFile.getId());
        assertASFile(tmp, updatedFileASName, updatedDataID);
    }

    @Test
    public void testDelete() {
        fileHelper.delete(expectedFile);
        assertNull(fileHelper.getFileByID(expectedFile.getId()));
    }

    @Test
    public void testGetFiles() {
        for (int i = 1; i < 30; i++) {
            String tmpASName = i + expectedFileASName;
            long tmpDataID = i;

            fileHelper.insert(new ASFile(mContext, tmpASName, tmpDataID));
        }
        assertEquals(30, fileHelper.getFiles().size());
    }

    @Test
    public void testGetFilesWithCorrectLimitAndOffset() {
        for (int i = 1; i < 30; i++) {
            String tmpASName = i + expectedFileASName;
            long tmpDataID = i;

            fileHelper.insert(new ASFile(mContext, tmpASName, tmpDataID));
        }
        List<ASFile> asFileList = new ArrayList<>();
        asFileList.addAll(fileHelper.getFiles(10, asFileList.size()));
        assertEquals(10, asFileList.size());
        asFileList.addAll(fileHelper.getFiles(10, asFileList.size()));
        assertEquals(20, asFileList.size());
        asFileList.addAll(fileHelper.getFiles(10, asFileList.size()));
        assertEquals(30, asFileList.size());
    }

    @Test
    public void testGetFilesWithFalseLimitAndOffset() {
        for (int i = 1; i < 30; i++) {
            String tmpASName = i + expectedFileASName;
            long tmpDataID = i;

            fileHelper.insert(new ASFile(mContext, tmpASName, tmpDataID));
        }
        assertEquals(30, fileHelper.getFiles(0, -1).size());
    }

    @Test
    public void testGetReceivedFiles() {
        List<ASFile> fileList = fileHelper.getReceivedFiles();

        for (ASFile file : fileList) {
            assertTrue(file.getReceived());
        }
    }

    @Test
    public void testGetReceivedFilesByName() {
        List<ASFile> fileList = fileHelper.getReceivedFilesByName(expectedFileASName);

        for (ASFile file : fileList) {
            assertTrue(file.getReceived());
            assertEquals(expectedFileASName, file.getASName());
        }
    }

    @Test
    public void testGetSendFiles() {
        List<ASFile> fileList = fileHelper.getSendFiles();

        for (ASFile file : fileList) {
            assertFalse(file.getReceived());
        }
    }

    @Test
    public void testGetSendFilesByName() {
        List<ASFile> fileList = fileHelper.getSendFilesByName(expectedFileASName);

        for (ASFile file : fileList) {
            assertFalse(file.getReceived());
            assertEquals(expectedFileASName, file.getASName());
        }
    }

    @Test
    public void testGetFilesByName() {
        ASFile tmp = new ASFile(mContext, "Partyfoto.png");
        tmp.setDataId(1);
        fileHelper.insert(tmp);
        assertEquals(2, fileHelper.getFilesByName("foto.png").size());
        assertEquals(1, fileHelper.getFilesByName("Urlaubs").size());
        assertEquals(expectedFile, fileHelper.getFilesByName("Urlaubs").get(0));
    }

    @Test
    public void testGetFileByDataID() {
        assertEquals(expectedFile, fileHelper.getFileByDataID(expectedDataID));
        assertNull(fileHelper.getFileByDataID(-1));
    }

    @Test
    public void testGetFileByID() {
        assertEquals(expectedFile, fileHelper.getFileByID(expectedASFileID));
        assertNull(fileHelper.getFileByID(-1));
    }

    @After
    public void tearDown() throws Exception {

    }
}
