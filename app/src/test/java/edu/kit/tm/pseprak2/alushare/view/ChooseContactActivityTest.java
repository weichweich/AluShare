package edu.kit.tm.pseprak2.alushare.view;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.R;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)

public class ChooseContactActivityTest {
    ChooseContactActivity activity;


    @Before
    public void setUp(){
        activity = Robolectric.buildActivity(ChooseContactActivity.class).create().start().get();
    }

    @Test
    public void testComponetn(){
        assertNotNull(activity);
    }


}
