package edu.kit.tm.pseprak2.alushare.view;

/**
 * Created by niklas on 31.08.15.
 */

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowPreferenceManager;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import java.io.File;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ASFileHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.FileTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.ChatTabRecyclerItemViewHolder;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.FileTabRecyclerItemViewHolder;
import edu.kit.tm.pseprak2.alushare.view.fragments.ChatTabFragment;
import edu.kit.tm.pseprak2.alushare.view.fragments.FileTabFragment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class FileTabFragmentTest {
    FileTabFragment fragment;
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    FileTabRecyclerAdapter adapter;
    Context context = RuntimeEnvironment.application;
    MenuInflater inflater;
    Menu menu;
    ASFileHelper helper;
    List<ASFile> files;
    SharedPreferences preferences;
    AppCompatActivity activity;
    @Before
    public void setUp() {
        DummyDataSet.copyDataSet("ASDB_Tabs.db");
        helper = HelperFactory.getFileHelper(context);
        files = helper.getFiles();

        activity = Robolectric.buildActivity(TestActivity.class).create().start().resume().get();
        fragment = FileTabFragment.createInstance();
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

        recyclerView = (RecyclerView) fragment.getView().findViewById(R.id.recyclerView);
        adapter = (FileTabRecyclerAdapter) recyclerView.getAdapter();
        manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        menu = new MenuBuilder(activity);
        inflater = new MenuInflater(activity);

        preferences = ShadowPreferenceManager.getDefaultSharedPreferences(context);
    }

    @Test
    public void testComponents() {
        assertNotNull(recyclerView);
        assertFalse(fragment.onQueryTextSubmit("asd"));
    }

    /**
     * Robolectric ist im Moment im bezug auf ActionMenuItem's verbugt, in einer neueren Version wird die NullPointerException nicht benötigt
     */
    @Test (expected = NullPointerException.class)
    public void testOptionsMenu1() {
        int selected = -1;
        fragment.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.filter_all);
        fragment.onOptionsItemSelected(item);
        assertTrue(files.size() == adapter.getItemCount());
        selected = preferences.getInt("fileTabShow", -1);
        assertEquals(selected, 0);
    }
    @Test (expected = NullPointerException.class)
    public void testOptionsMenu2() {
        int selected = -1;
        fragment.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.filter_send);
        fragment.onOptionsItemSelected(item);
        files = helper.getSendFiles();
        assertTrue(files.size() == adapter.getItemCount());
        selected = preferences.getInt("fileTabShow", -1);
        assertTrue(selected == 1);

    }
    @Test (expected = NullPointerException.class)
    public void testOptionsMenu3() {
        int selected = -1;
        fragment.onCreateOptionsMenu(menu, inflater);
        MenuItem item= menu.findItem(R.id.filter_received);
        fragment.onOptionsItemSelected(item);
        files = helper.getReceivedFiles();
        assertTrue(files.size() == adapter.getItemCount());
        selected = preferences.getInt("fileTabShow", -1);
        assertTrue(selected == 2);
    }

    @Test
    public void testContextMenu()  {
        adapter.updateDataSet(files);
        long fileToDelete = files.get(0).getId();
        FileTabRecyclerItemViewHolder viewHolder = (FileTabRecyclerItemViewHolder) adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);
        try {
            viewHolder.itemView.performLongClick(); // Danke Robolectric. NullPointer weil irgendwas mit Menu buggy..
        } catch (NullPointerException e) {
            //Nichts tun.
        }
        RoboMenuItem item = new RoboMenuItem(R.id.context_tab_file_delete);
        item.setGroupId(R.id.context_tab_file_group);

        fragment.onContextItemSelected(item); // Wirft NullPointerException weil Robolectric das Fragment nicht richtig mocked this.getView ist Null..
        assertNull(helper.getFileByID(fileToDelete));
    }

    @Test
    public void testContextMenu2()  {
        adapter.updateDataSet(files);

        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);
        try {
            viewHolder.itemView.performLongClick(); // Danke Robolectric. NullPointer weil irgendwas mit Menu buggy..
        } catch (NullPointerException e) {
            //Nichts tun.
        }
        RoboMenuItem item = new RoboMenuItem(R.id.context_tab_file_rename);
        item.setGroupId(R.id.context_tab_file_group);

        fragment.onContextItemSelected(item);

        android.support.v7.app.AlertDialog dialog = (android.support.v7.app.AlertDialog) ShadowAlertDialog.getLatestDialog();

        Button b = dialog.getButton(Dialog.BUTTON_POSITIVE);
        EditText editText = (EditText) dialog.findViewById(R.id.alter_dialog_edit);
        editText.setText("neuerName");
        b.performClick();

        files = helper.getFiles();
        String newName = files.get(0).getASName();
        assertEquals("neuerName", newName);

        fragment.onContextItemSelected(item);
        dialog = (android.support.v7.app.AlertDialog) ShadowAlertDialog.getLatestDialog();
        b = dialog.getButton(Dialog.BUTTON_NEGATIVE);
        b.performClick();
        assertFalse(dialog.isShowing());

    }



    @Test (expected = NullPointerException.class) // FileProvider kann kein Bild finden.
    public void testRecyclerClick() {
        adapter.updateDataSet(files);
        RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(recyclerView, 0);
        adapter.onBindViewHolder(viewHolder, 0);

        viewHolder.itemView.performClick();
    }

    @Test
    public void testSearch() {
        adapter.updateDataSet(files);
        ASFile c = files.get(0);
        fragment.onQueryTextChange(c.getASName());
        int amount = helper.getFilesByName(c.getASName()).size();
        assertTrue(adapter.getItemCount() == amount);
    }
    @After
    public void tearDown() {
        TestHelper.resetHelperFactory();
    }
}
