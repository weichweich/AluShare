package edu.kit.tm.pseprak2.alushare.view;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import edu.kit.tm.pseprak2.alushare.BuildConfig;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CreateContactActivityTest {
    CreateContactActivity activity;

    @Before
    public  void setUp(){
        activity = Robolectric.buildActivity(CreateContactActivity.class).create().start().get();
    }

    @Test
    public void testComponent(){
        assertNotNull(activity);
    }

    @Test
    public void buildDialog(){
        activity.buildDialog();
    }

    @Test(expected = NullPointerException.class)
    public void addTest(){
        activity.add(activity.getCurrentFocus());
    }

}
