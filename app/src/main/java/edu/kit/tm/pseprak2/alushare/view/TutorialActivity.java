package edu.kit.tm.pseprak2.alushare.view;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.view.fragments.TutorialFragment;

public class TutorialActivity extends AppCompatActivity implements View.OnClickListener {
    ViewPager pager;
    PagerAdapter pagerAdapter;
    ImageButton buttonLeft;
    ImageButton buttonRight;
    ProgressBar progressBar;
    private boolean firstStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        initToolbar();
        pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setOffscreenPageLimit(1);
        buttonLeft = (ImageButton) findViewById(R.id.button_left);
        buttonLeft.setOnClickListener(this);
        buttonRight = (ImageButton) findViewById(R.id.button_right);
        buttonRight.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.tutorial_progress);
        progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(this, R.color.accentColor), PorterDuff.Mode.SRC_IN);
        progressBar.setMax(100);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(TutorialFragment.createInstance(R.drawable.tut1));
        pagerAdapter.addFragment(TutorialFragment.createInstance(R.drawable.tut2));
        pagerAdapter.addFragment(TutorialFragment.createInstance(R.drawable.tut3));
        pagerAdapter.addFragment(TutorialFragment.createInstance(R.drawable.tut4));
        pagerAdapter.addFragment(TutorialFragment.createInstance(R.drawable.tut5));
        pagerAdapter.addFragment(TutorialFragment.createInstance(R.drawable.tut6));
        pagerAdapter.addFragment(TutorialFragment.createInstance(R.drawable.tut7));
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                updateProgressbar();
                if (pager.getCurrentItem() == pager.getAdapter().getCount() - 1) {
                    buttonRight.setImageResource(R.drawable.ic_done_white_24dp);

                } else {
                    buttonRight.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24dp);
                }
            }
        });
        try {
            firstStart = getIntent().getExtras().getBoolean("firstStart", false);
        } catch (NullPointerException e) {
            firstStart = false;
        }

        updateProgressbar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }

    @Override
    public void onClick(View v) {
        int curItem = pager.getCurrentItem();
        int itemCount = pager.getAdapter().getCount();
        int newPos = 0;
        if (v == buttonLeft) {
            if (curItem > 0) {
                newPos = curItem - 1;
                pager.setCurrentItem(newPos);
            }
        } else if (v == buttonRight) {
            if (curItem < (itemCount - 1)) {
                newPos = curItem + 1;
                pager.setCurrentItem(newPos);
            } else if (curItem == (itemCount - 1)) {
                if (firstStart) {
                    startActivity(new Intent(this, PersonalInfoActivity.class));
                }
                this.finish();
            }
        }
        pager.destroyDrawingCache();

    }

    // Updated Progressbar auf akutellen Status
    private void updateProgressbar() {
        int curItem = pager.getCurrentItem() + 1;
        int itemCount = pager.getAdapter().getCount();
        double prog = ((double) curItem / (double) itemCount) * 100;
        progressBar.setProgress((int) prog);
    }

    // Initialisiert Toolbar
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_tutorial);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * PagerAdapter for Images
     */
    private static class PagerAdapter extends FragmentStatePagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Adds a Fragment to the pager with given title.
         *
         * @param fragment Fragment
         */
        public void addFragment(Fragment fragment) {
            fragmentList.add(fragment);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

    }
}
