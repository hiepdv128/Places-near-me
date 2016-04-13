package com.truongpq.placesnearme.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.astuetz.PagerSlidingTabStrip.IconTabProvider;
import com.truongpq.placesnearme.R;
import com.truongpq.placesnearme.fragments.MapFragment;
import com.truongpq.placesnearme.fragments.FavoriteFragment;

public class FragmentAdapter extends FragmentPagerAdapter implements IconTabProvider {
    final int PAGE_COUNT = 2;

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return MapFragment.newInstance();
            case 1: return FavoriteFragment.newInstance();
            default: return MapFragment.newInstance();
        }

    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    @Override
    public int getPageIconResId(int position) {
        switch (position) {
            case 0: return R.drawable.ic_map;
            case 1: return R.drawable.ic_favorite;
        }
        return R.drawable.ic_map;
    }
}
