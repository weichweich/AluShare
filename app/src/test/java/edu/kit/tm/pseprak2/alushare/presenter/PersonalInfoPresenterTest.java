package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;
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

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.PersonalInfoActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PersonalInfoPresenterTest {
    Context context = RuntimeEnvironment.application;
    PersonalInfoPresenter presenter;
    PersonalInfoActivity activity;
    ContactHelper helper;
    String networkaddress = "OwnTorId";


    @Before
    public void setUp() {
        presenter = new PersonalInfoPresenter();
        activity = Robolectric.buildActivity(PersonalInfoActivity.class).create().get();
        helper = HelperFactory.getContacHelper(activity.getApplicationContext());
        helper.setOwnNID(networkaddress);
        presenter.onTakeView(activity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void onTakeViewTest() {
        presenter.onTakeView(null);
    }

    @Test
    public void setQrCodeTest() {
        presenter.setQRCode();
        assertNotNull(((ImageView) activity.findViewById(R.id.QRCode)).getDrawable());
    }

    @Test
    public void getOwnNameTest() {
        assertEquals(presenter.getName(), context.getString(R.string.profile_not_found));
    }

    @Test
    public void setTorId() {
        presenter.setTorId();
        String address = ((TextView) activity.findViewById(R.id.textViewTor)).getText().toString();
        assertEquals(networkaddress, address);
    }

    @After
    public void teardown() {
        helper = null;
        activity = null;
        presenter = null;
        TestHelper.resetHelperFactory();
    }

}
