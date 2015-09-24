package edu.kit.tm.pseprak2.alushare.model;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ProfileTest {
    ContactHelper helper;
    String torID = "OwnTorID";
    Context context = RuntimeEnvironment.application.getApplicationContext();

    @Before
    public void setUp(){
        helper = HelperFactory.getContacHelper(context);
        helper.setOwnNID(torID);
    }


    @Test
    public void getNetworkAddressTest(){
        assertEquals(Profile.getNetworkadress(context), torID);
    }

    @Test
    public void getNameTest(){
        assertEquals(Profile.getOwnName(context), context.getString(R.string.profile_not_found));
    }

    @After
    public void tearDown(){
        this.helper = null;
        TestHelper.resetHelperFactory();
    }
}
