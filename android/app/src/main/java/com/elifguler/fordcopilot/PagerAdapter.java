package com.elifguler.fordcopilot;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                HomeTabFragment tab1 = new HomeTabFragment();
                return tab1;
            case 1:
                GasStationsTabFragment tab2 = new GasStationsTabFragment();
                return tab2;
            case 2:
                EmergencyTabFragment tab3 = new EmergencyTabFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
