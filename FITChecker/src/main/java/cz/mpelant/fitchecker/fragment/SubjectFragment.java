package cz.mpelant.fitchecker.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.viewpagerindicator.TabPageIndicator;

import cz.mpelant.fitchecker.R;
import cz.mpelant.fitchecker.model.Subject;

/**
 * Fragment with viewpager with edux and exams fragments
 * Created by David Bilik[david.bilik@ackee.cz] on 15. 5. 2014.
 */
public class SubjectFragment extends BaseFragment {
    protected static final String TAG = SubjectFragment.class.getName();

    public static SubjectFragment newInstance(Subject subject) {
        SubjectFragment f = new SubjectFragment();
        f.setArguments(DisplaySubjectFragment.generateArgs(subject));
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager vp = (ViewPager) view.findViewById(R.id.viewPager);
        PagerSlidingTabStrip indicator = (PagerSlidingTabStrip) view.findViewById(R.id.indicator);
        vp.setAdapter(new VPAdapter(getChildFragmentManager()));
        indicator.setViewPager(vp);
        indicator.setTextColorResource(R.color.apptheme_color);

    }

    class VPAdapter extends FragmentPagerAdapter {
        private static final int COUNT = 2;

        public VPAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    return ExamListFragment.newInstance((Subject) getArguments().getParcelable(DisplaySubjectFragment.SUBEJCT));
                case 0:
                    return DisplaySubjectFragment.newInstance((Subject) getArguments().getParcelable(DisplaySubjectFragment.SUBEJCT));
            }
            return null;
        }

        @Override
        public int getCount() {
            return COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.edux);
                case 1:
                    return getString(R.string.exams);
            }
            return "";
        }
    }
}
