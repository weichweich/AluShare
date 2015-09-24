package edu.kit.tm.pseprak2.alushare.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.RobolectricInternals;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowScaleGestureDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ASFileHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by niklas on 02.09.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class IntentGeneratorTest {
    Context context = RuntimeEnvironment.application.getApplicationContext();
    String path;
    String resourcePath;
    ASFileHelper fileHelper;
    ContactHelper contactHelper;
    @Before
    public void setUp() {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");

        fileHelper = HelperFactory.getFileHelper(context);
        contactHelper = HelperFactory.getContacHelper(context);
        ShadowLog.stream = System.out;
        path = TestHelper.getFilesPath();
        String dbPath = TestHelper.getDatabasePath();
        resourcePath = TestHelper.getTestResourcePath();
    }

    @Test (expected = NullPointerException.class) // FileProvider kann nicht aufgerufen werden
    public void test1() {
        ASFile file = fileHelper.getFiles().get(0);
        File testImage = new File(resourcePath + "raw/einBild.jpg");
        try {
            TestHelper.copyFileUsingStream(testImage, file);
            System.out.println("Succes!");
        } catch (Exception e) {

        }
        Intent i = IntentGenerator.getIntentByFileId(file.getId(), context);
        assertNotNull(i);
    }

    @Test
    public void test2() {
        Contact c = contactHelper.getContacts().get(0);
        Intent i = IntentGenerator.getIntentByContactId(c.getId(), context);
        assertNotNull(i);
    }

    @After
    public void tearDown() {
        TestHelper.resetHelperFactory();
    }


}
