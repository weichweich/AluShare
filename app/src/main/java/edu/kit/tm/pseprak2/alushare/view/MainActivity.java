package edu.kit.tm.pseprak2.alushare.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.network.NetworkingService;
import edu.kit.tm.pseprak2.alushare.view.fragments.ChatTabFragment;
import edu.kit.tm.pseprak2.alushare.view.fragments.ContactTabFragment;
import edu.kit.tm.pseprak2.alushare.view.fragments.FileTabFragment;

/**
 * MainActivity
 */
public class MainActivity extends AppCompatActivity {
    private ViewPager viewpager;

    /**
     * Sets activity_main layout and initializes the toolbar and the viewpager.
     *
     * @param savedInstanceState Saved Parameters
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initViewPager();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(p.getBoolean("firstStart", true)) {
            p.edit().putBoolean("firstStart", false).commit();
            Intent i = new Intent(this,TutorialActivity.class);
            i.putExtra("firstStart", true);
            startActivity(i);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        p.edit().putInt("POSITION_PAGER", viewpager.getCurrentItem()).commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handles clicks on menu items.
     *
     * @param item clicked menu item
     * @return true if item selected, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_own_info:
                startActivity(new Intent(this, PersonalInfoActivity.class));
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, Preferences.class));
                return true;

            case R.id.action_kill_it_with_fire:
                stopService(new Intent(this, NetworkingService.class));
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initializes Toolbar
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.app_name));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
    }

    /**
     * Initializes ViewPager
     */
    private void initViewPager() {
        viewpager = (ViewPager) findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(FileTabFragment.createInstance(), getResources().getString(R.string.filetab));
        pagerAdapter.addFragment(ChatTabFragment.createInstance(), getResources().getString(R.string.chattab));
        pagerAdapter.addFragment(ContactTabFragment.createInstance(), getResources().getString(R.string.contacttab));
        viewpager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewpager);

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(this);
        int position = p.getInt("POSITION_PAGER", 1);
        viewpager.setCurrentItem(position);
    }

    /**
     * PagerAdapter for 3 Tabs (e.g. Fragments)
     */
    private static class PagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Adds a Fragment to the pager with given title.
         *
         * @param fragment Fragment
         * @param title    Fragment title
         */
        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }


        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }


}
