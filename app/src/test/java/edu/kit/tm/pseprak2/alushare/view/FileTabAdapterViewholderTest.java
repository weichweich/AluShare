package edu.kit.tm.pseprak2.alushare.view;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;
import org.robolectric.shadows.ShadowViewGroup;
import org.robolectric.util.FragmentTestUtil;
import static org.robolectric.Shadows.shadowOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.AluShare;
import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.helper.ASFileHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.FileTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.ItemClickListener;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.FileTabRecyclerItemViewHolder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by niklas on 26.08.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class FileTabAdapterViewholderTest {
    TestFragment fragment;
    FileTabRecyclerAdapter adapter;
    Context context = RuntimeEnvironment.application;
    ASFileHelper helper;
    List<ASFile> files;
    LinearLayoutManager manager;
    RecyclerView recyclerView;

    @Before
    public void setUp() throws Exception {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        helper = HelperFactory.getFileHelper(context);
        files = helper.getFiles();

        fragment = TestFragment.createInstance(TestFragment.TYPE_FILE_TAB);
        SupportFragmentTestUtil.startVisibleFragment(fragment);

        adapter = (FileTabRecyclerAdapter) fragment.getAdapter();
        manager = fragment.getLayoutManager();
        recyclerView = fragment.getRecyclerView();


    }


    //------ TAB ADAPTER TESTS ------
    @Test
    public void testGetList1() {
        List<ASFile> files = helper.getFiles();
        adapter.updateDataSet(files);

        assertTrue(files.size() == adapter.getList().size());
    }

    @Test
    public void testGetList2() {
        List<ASFile> l = adapter.getList();
        assertTrue(l == null);
    }

    @Test
    public void testGetItemCount1() {
        adapter.updateDataSet(files);

        assertTrue(adapter.getItemCount() == files.size());
    }

    @Test
    public void testGetItemCount2() {
        assertTrue(adapter.getItemCount() == 0);
    }

    @Test(expected = NullPointerException.class)
    public void testItemRemove1() {
        adapter.removeItem(0);
    }

    @Test
    public void testItemRemove2() {
        adapter.updateDataSet(files);
        ASFile file = adapter.removeItem(0);
        assertNotNull(file);

        assertTrue(files.size() == adapter.getItemCount());
    }

    @Test
    public void testUpdateDataSet() {
        adapter.updateDataSet(files);
        assertTrue(files.size() == adapter.getItemCount());

        adapter.updateDataSet(null);
        assertTrue(adapter.getItemCount() == 0);
    }

    @Test
    public void testUpdateData() {
        ASFile file = files.get(0);
        adapter.updateDataSet(files);
        file.setASName("neuerName");
       // adapter.updateData(file);
        assertTrue(adapter.getList().get(0).getASName() == "neuerName");
    }

    @Test(expected = NullPointerException.class)
    public void testAddItem1() {
        ASFile file = new ASFile(context, "einname");
        adapter.addItem(adapter.getItemCount(), file);
    }

    @Test
    public void testAddItem() {
        adapter.updateDataSet(new ArrayList<ASFile>());

        ASFile file = new ASFile(context, "einname");
        adapter.addItem(adapter.getItemCount(), file);

        assertTrue(adapter.getItemCount() == 1);


        adapter.updateDataSet(files);
        adapter.addItem(adapter.getItemCount(), file);

        assertTrue(adapter.getItemCount() == files.size());
    }

    @Test(expected = NullPointerException.class)
    public void testMove1() {
        adapter.moveItem(0, 100);
    }

    @Test
    public void testMove2() {
        adapter.updateDataSet(files);
        ASFile file1 = files.get(0);
        ASFile file2 = files.get(2);
        adapter.moveItem(0, 2);
        assertTrue(files.get(2).getId() == file1.getId());
        assertTrue(files.get(1).getId() == file2.getId());
    }

    @Test (expected = NullPointerException.class)
    public void testAnimateTo1() {
        adapter.animateTo(new ArrayList<ASFile>());
    }

    @Test
    public void testAnimateTo2() {
        adapter.updateDataSet(files);
        files.remove(1);
        ASFile file1 = new ASFile(context,"ersteFile");
        ASFile file2 = new ASFile(context,"zweiteFile");
        files.add(file1);
        files.add(file2);
        adapter.animateTo(files);
    }

    @Test
    public void testAnimateTo3() {
        ASFile file1 = new ASFile(context,"ersteFile");
        ASFile file2 = new ASFile(context,"zweiteFile");
        adapter.updateDataSet(new ArrayList<ASFile>());

        ArrayList<ASFile> list = new ArrayList<>();
        list.add(file1);
        list.add(file2);
        adapter.animateTo(list);
        assertTrue(list.size() == adapter.getItemCount());
        adapter.updateDataSet(files);
        adapter.animateTo(files);
        assertTrue(files.size() == adapter.getItemCount());
    }

    @Test
    public void testAnimateTo4() {
        ASFile file1 = new ASFile(context,"ersteFile");
        ASFile file2 = new ASFile(context,"zweiteFile");
        adapter.updateDataSet(new ArrayList<ASFile>());

        ArrayList<ASFile> list = new ArrayList<>();
        list.add(file1);
        list.add(file2);
        adapter.animateTo(list);

        Collections.swap(list, 0, 1);
        adapter.animateTo(list);
    }

    @Test
    public void testSetListener() {
        adapter.setItemListener(new ItemClickListener() {
            @Override
            public void onItemClick(View view, long id) {

            }
        });
    }
    //------ TAB ADAPTER TEST ------

    @Test
    public void testRecyclerView1() {
        adapter.updateDataSet(files);

        RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(recyclerView, 0);
        assertNotNull(holder);

        for(int i = 0; i < files.size(); i++) {
            adapter.onBindViewHolder(holder, i);
            ASFile file = files.get(i);

            TextView view = (TextView) holder.itemView.findViewById(R.id.file_path);
            assertTrue(view.getText().equals(file.getASName()));

            view = (TextView) holder.itemView.findViewById(R.id.file_from);
            assertTrue(view.getText().equals(file.getPath()));

            ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.image);
            assertNotNull(imageView.getDrawable());
        }
    }

    @Test (expected = NullPointerException.class)
    public void testRecyclerViewClick() {
        adapter.updateDataSet(files);

        RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(recyclerView, 0);
        assertNotNull(holder);

        adapter.onBindViewHolder(holder, 0);

        holder.itemView.performClick();
        holder.itemView.performLongClick();
        //NullPointer liegt an Robolectric, viewholder wird lange gedrückt! Context Menü wird versucht zu erstellen

        assert(adapter.getId() == files.get(0).getId());
        assertTrue(fragment.wasClicked());
        assertTrue(fragment.getClickID() == files.get(0).getId());
    }

    @After
    public void tearDown() {
        TestHelper.resetHelperFactory();
    }
}
