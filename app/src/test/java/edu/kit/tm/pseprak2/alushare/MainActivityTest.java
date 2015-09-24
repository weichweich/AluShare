package edu.kit.tm.pseprak2.alushare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import net.freehaven.tor.control.examples.Main;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowPreferenceManager;

import edu.kit.tm.pseprak2.alushare.view.MainActivity;
import edu.kit.tm.pseprak2.alushare.view.PersonalInfoActivity;
import edu.kit.tm.pseprak2.alushare.view.Preferences;
import edu.kit.tm.pseprak2.alushare.view.TutorialActivity;
import edu.kit.tm.pseprak2.alushare.view.fragments.ChatTabFragment;
import edu.kit.tm.pseprak2.alushare.view.fragments.ContactTabFragment;
import edu.kit.tm.pseprak2.alushare.view.fragments.FileTabFragment;

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
public class MainActivityTest {
    MainActivity activity;
    ViewPager pager;
    Toolbar toolbar;
    Menu menu;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(MainActivity.class).create().pause().resume().get();
        pager = (ViewPager) activity.findViewById(R.id.viewPager);
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

        menu = new MenuBuilder(activity);
    }

    @Test
    public void testComponents() {
        assertNotNull(toolbar);
        assertEquals(toolbar.getTitle(), activity.getString(R.string.title_activity_main));

        assertNotNull(pager);
        assertTrue(pager.getAdapter().getCount() == 3);

        FragmentPagerAdapter adapter = (FragmentPagerAdapter) pager.getAdapter();
        assertEquals(adapter.getItem(0).getClass().getName(), FileTabFragment.class.getName());
        assertEquals(adapter.getItem(1).getClass().getName(), ChatTabFragment.class.getName());
        assertEquals(adapter.getItem(2).getClass().getName(), ContactTabFragment.class.getName());
    }

    @Test
    public void testOptionsMenu() {
        activity.onCreateOptionsMenu(menu);
        assertTrue(menu.size() == 3);
    }

    @Test
    public void testOptionsItems() {

        RoboMenuItem item = new RoboMenuItem(R.id.action_show_own_info);

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent i  = shadowActivity.getNextStartedActivity();
        assertEquals(TutorialActivity.class.getName(),i.getComponent().getClassName());

        activity.onOptionsItemSelected(item);
        i = shadowActivity.getNextStartedActivity();
        assertEquals(PersonalInfoActivity.class.getName(),i.getComponent().getClassName());

        item = new RoboMenuItem(R.id.action_settings);
        activity.onOptionsItemSelected(item);
        i = shadowActivity.getNextStartedActivity();

        assertEquals(i.getComponent().getClassName(), Preferences.class.getName());

        item = new RoboMenuItem(R.id.action_kill_it_with_fire);
        activity.onOptionsItemSelected(item);
        i = shadowActivity.getNextStartedActivity();

        assertTrue(activity.isFinishing());

        item = new RoboMenuItem(R.id.editTorID);
        assertFalse(activity.onOptionsItemSelected(item));
    }

    @Test
    public void testClickPauseAndReturn() {
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        shadowActivity.pauseAndThenResume();
        SharedPreferences p = ShadowPreferenceManager.getDefaultSharedPreferences(activity);
        assertTrue(p.getInt("POSITION_PAGER", -5) == 1);

        pager.setCurrentItem(0);
        shadowActivity.pauseAndThenResume();

        assertEquals(p.getInt("POSITION_PAGER", -5), 0);
    }
}
