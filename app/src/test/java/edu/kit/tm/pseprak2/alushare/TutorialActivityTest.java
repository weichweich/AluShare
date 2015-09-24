package edu.kit.tm.pseprak2.alushare;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.ImageButton;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import edu.kit.tm.pseprak2.alushare.view.TutorialActivity;
import edu.kit.tm.pseprak2.alushare.view.fragments.TutorialFragment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by niklas on 08.09.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TutorialActivityTest {
    ViewPager pager;
    Toolbar toolbar;
    TutorialActivity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(TutorialActivity.class).create().pause().resume().get();
        pager = (ViewPager) activity.findViewById(R.id.viewPager);
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar_tutorial);
    }

    @Test
    public void testComponents() {
        assertNotNull(toolbar);
        assertNotNull(pager);
        assertTrue(pager.getAdapter().getCount() > 0);
        TutorialFragment f = TutorialFragment.createInstance(0);
        assertNotNull(f);
        ImageButton bRight = (ImageButton) activity.findViewById(R.id.button_right);
        ImageButton bLeft = (ImageButton) activity.findViewById(R.id.button_left);

        bRight.performClick();
        assertTrue(pager.getCurrentItem() == 1);
        bLeft.performClick();
        assertTrue(pager.getCurrentItem() == 0);
        pager.setCurrentItem(0);
        for(int i = 0; i < pager.getAdapter().getCount(); i++) {
            pager.setCurrentItem(i);
            bRight.performClick();
        }
        assertTrue(activity.isFinishing());
    }

    @Test
    public void testComponents2() {
        pager.removeAllViews();
        assertTrue(pager.getChildCount() == 0);
    }


}
