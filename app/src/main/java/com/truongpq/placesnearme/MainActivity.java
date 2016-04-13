package com.truongpq.placesnearme;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.maps.GoogleMap;
import com.truongpq.placesnearme.adapters.FragmentAdapter;
import com.truongpq.placesnearme.fragments.MapFragment;
import com.truongpq.placesnearme.interfaces.Comuticator;
import com.truongpq.placesnearme.models.Place;

import java.util.List;

public class MainActivity extends AppCompatActivity {

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
}
