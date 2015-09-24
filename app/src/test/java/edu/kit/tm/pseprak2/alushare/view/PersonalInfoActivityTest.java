package edu.kit.tm.pseprak2.alushare.view;

import android.content.Context;
import android.support.v7.widget.Toolbar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PersonalInfoActivityTest {
    PersonalInfoActivity activity;
    Toolbar toolbar;
    ContactHelper helper;
    Context context = RuntimeEnvironment.application;;


    @Before
    public  void setUp(){
        HelperFactory.getContacHelper(context).setOwnNID("Tor-Id");
        activity = Robolectric.buildActivity(PersonalInfoActivity.class).create().start().get();
        toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

    }

    @Test
    public void testComponents(){
        assertNotNull(activity);
        assertNotNull(toolbar);
    }

    @Test
    public void testOptionItem(){
        RoboMenuItem item = new RoboMenuItem(R.id.action_add);
        RoboMenuItem item2 = new RoboMenuItem(R.id.action_settings);
        RoboMenuItem item3 = new RoboMenuItem(android.R.id.home);

        assertTrue(activity.onOptionsItemSelected(item));
        assertTrue(activity.onOptionsItemSelected(item2));
        assertTrue(activity.onOptionsItemSelected(item3));


    }

    @Test
    public void stopTest(){
        Robolectric.buildActivity(PersonalInfoActivity.class).create().start().stop().destroy();
    }

    @After
    public void teardown(){
        TestHelper.resetHelperFactory();
    }
}
