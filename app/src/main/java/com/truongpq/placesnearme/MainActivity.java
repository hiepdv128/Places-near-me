package com.truongpq.placesnearme;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.maps.GoogleMap;
import com.truongpq.placesnearme.adapters.FragmentAdapter;
import com.truongpq.placesnearme.fragments.FavoriteFragment;
import com.truongpq.placesnearme.fragments.MapFragment;
import com.truongpq.placesnearme.interfaces.Comuticator;
import com.truongpq.placesnearme.models.Place;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private boolean doubleBackToExitPressedOnce = false;

    private ViewPager viewPager;
    private FragmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new FragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setViewPager(viewPager);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 1) {
            viewPager.setCurrentItem(0);
        }

        if (viewPager.getCurrentItem() == 0) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }
}
