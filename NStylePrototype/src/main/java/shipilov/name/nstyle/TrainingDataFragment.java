package shipilov.name.nstyle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.List;

/**
 * Created by HOME on 25.04.2017.
 */

public class TrainingDataFragment extends Fragment {

    private ViewPager pager;
    private TrainingDataFragment.MyFragmentPagerAdapter pagerAdapter;
    private Switch publicSwitch;
    private Button removeButton;
    private ViewPager.OnPageChangeListener onPageChangeListener;

    public interface Listener {

        // публикация/отключение
        void onPublicChange(String styleId);

        // удаление неудачного стиля
        void onRemove(String styleId);
    }

    public interface DataProvider {
        List<String> getStyles();
        boolean isPublic(String styleId);
    }

    public String getCurrntStyleId() {
        return ((DataProvider)getActivity()).getStyles().get(pager.getCurrentItem());
    }

    public void refreshPageView(int position) {
        pagerAdapter.notifyChangeInPosition(position);
        pagerAdapter.notifyDataSetChanged();
        onPageChangeListener.onPageSelected(pager.getCurrentItem());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.training_data, container, false);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        pagerAdapter = new TrainingDataFragment.MyFragmentPagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);

        removeButton = (Button) rootView.findViewById(R.id.removeStyleButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TrainingDataFragment.Listener)getActivity()).onRemove(getStyles().get(pager.getCurrentItem()));

            }
        });

        publicSwitch = (Switch) rootView.findViewById(R.id.switchPublic);
        publicSwitch.setChecked(((DataProvider)getActivity()).isPublic(getCurrntStyleId()));
        final CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((Listener) getActivity()).onPublicChange(getStyles().get(pager.getCurrentItem()));
            }
        };
        publicSwitch.setOnCheckedChangeListener(switchListener);

        onPageChangeListener = new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                publicSwitch.setOnCheckedChangeListener(null);
                publicSwitch.setChecked(((DataProvider) getActivity()).isPublic(getStyles().get(position)));
                publicSwitch.setOnCheckedChangeListener(switchListener);
            }
        };
        pager.setOnPageChangeListener(onPageChangeListener);


        return rootView;
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        private long baseId = 0;

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TrainingDataItemFragment.newInstance(getStyles().get(position));
        }

        @Override
        public int getCount() {
            return getStyles().size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getStyles().get(position);
        }

        //this is called when notifyDataSetChanged() is called
        @Override
        public int getItemPosition(Object object) {
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
        }


        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        public void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }
    }

    private List<String> getStyles() {
        return ((DataProvider)getActivity()).getStyles();
    }

}
