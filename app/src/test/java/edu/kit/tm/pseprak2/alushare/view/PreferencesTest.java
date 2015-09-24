package edu.kit.tm.pseprak2.alushare.view;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.shadows.ShadowPreference;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.R;

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
public class PreferencesTest {
    AppCompatActivity activity;
    PreferenceFragment fragment;
    Toolbar toolbar;
    Menu menu;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Preferences.class).create().resume().get();

        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        fragment = (PreferenceFragment) activity.getFragmentManager().findFragmentByTag("prefFragment");
    }

    @Test
    public void testComponents() {
        assertNotNull(activity);
        assertNotNull(toolbar);
        assertEquals(activity.getString(R.string.title_activity_preferences), toolbar.getTitle());
        assertNotNull(fragment);
    }

    @Test
    public void testOptionItem() {
        RoboMenuItem item = new RoboMenuItem(android.R.id.home);
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        activity.onOptionsItemSelected(item);

        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void testAboutClick() {
        Preference about = fragment.getPreferenceScreen().findPreference("about");
        ShadowPreference shadowPreference = Shadows.shadowOf(about);
        shadowPreference.click();

        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.performClick();
        assertFalse(dialog.isShowing());

    }

    @Test
    public void testStartTutorialTest() {
        Preference tut = fragment.getPreferenceScreen().findPreference("start_tutorial");
        ShadowPreference shadowPreference = Shadows.shadowOf(tut);
        shadowPreference.click();

        ShadowActivity a = Shadows.shadowOf(activity);
        Intent i = a.getNextStartedActivity();
        assertEquals(TutorialActivity.class.getName(),i.getComponent().getClassName());
    }


}
