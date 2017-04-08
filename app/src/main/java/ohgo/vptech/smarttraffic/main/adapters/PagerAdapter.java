package ohgo.vptech.smarttraffic.main.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import ohgo.vptech.smarttraffic.main.customview.FormSignIn;
import ohgo.vptech.smarttraffic.main.customview.FormSignUp;

/**
 * Created by ypn on 8/23/2016.
 */
public class PagerAdapter extends FragmentStatePagerAdapter {

    int mNumbOfTabs;
    public PagerAdapter(FragmentManager fm, int NumOfTab){
        super(fm);
        this.mNumbOfTabs = NumOfTab;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                FormSignIn home_page = new FormSignIn();
                return home_page;
            case 1:
                FormSignUp message = new FormSignUp();
                return message;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumbOfTabs;
    }
}
