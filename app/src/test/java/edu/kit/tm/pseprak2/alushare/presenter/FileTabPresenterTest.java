package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.helper.ASFileHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.SQLChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.SQLDataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.SQLFileHelper;
import edu.kit.tm.pseprak2.alushare.view.adapter.FileTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.fragments.FileTabFragment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by niklas on 26.08.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class FileTabPresenterTest {
    Context context = RuntimeEnvironment.application;
    FileTabPresenter presenter;
    FileTabRecyclerAdapter adapter;
    FileTabFragment fragment;
    ASFileHelper fileHelper;
    ChatHelper chatHelper;
    DataHelper dataHelper;

    @Before
    public void setUp() throws Exception {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        ShadowLog.stream = System.out;
        fileHelper = new SQLFileHelper(context);
        chatHelper = new SQLChatHelper(context);
        dataHelper = new SQLDataHelper(context);
        fragment = FileTabFragment.createInstance();
        SupportFragmentTestUtil.startFragment(fragment);
        adapter = new FileTabRecyclerAdapter(fragment);
        presenter = new FileTabPresenter(fragment, adapter);


    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePresenter() {
        presenter = new FileTabPresenter(null, adapter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePresenter2() {
        presenter = new FileTabPresenter(fragment, null);
    }

    @Test
    public void testSearch() {
        presenter.showFileList("RANDOMQUERRY");
        assertTrue(adapter.getItemCount() == 0);
        presenter.showFileList("");
        assertTrue(adapter.getItemCount() == fileHelper.getFiles().size());
    }

    @Test
    public void testSetAll() {
        presenter.showFiles(0);
        List<ASFile> list = adapter.getList();
        assertTrue(list.size() == fileHelper.getFiles().size());
    }

    @Test
    public void testRemoveFile() {
        List<ASFile> fileList = fileHelper.getFiles();
        int sizeBefore = fileList.size();

        ASFile file = fileHelper.getFiles().get(0);
        presenter.removeFile(file.getId());
        int sizeAfter = fileHelper.getFiles().size();

        assertTrue(sizeAfter == (sizeBefore - 1));
    }

    @Test
    public void testSetSend() {
        presenter.showFiles(1);
        List<ASFile> list = adapter.getList();
        for (ASFile file : list) {
            assertFalse(file.getReceived());
        }
    }

    @Test
    public void testSetReceived() {
        presenter.showFiles(2);
        List<ASFile> list = adapter.getList();
        for (ASFile file : list) {
            assertTrue(file.getReceived());
        }
    }


    @After
    public void tearDown() {
        if (fragment != null) {
            FragmentManager m = fragment.getActivity().getSupportFragmentManager();
            m.beginTransaction().remove(fragment).commit();
        }
        this.chatHelper = null;
        this.fileHelper = null;
        this.presenter = null;
        this.adapter = null;
        this.fragment = null;
        TestHelper.resetHelperFactory();
    }
}
