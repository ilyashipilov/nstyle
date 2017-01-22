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

import java.io.Serializable;
import java.util.List;

import shipilov.name.nstyle.api.Settings;

/**
 * Фрагмент с выбором стиля
 *
 * Created by HOME on 19.01.2017.
 */
public class StyleSelectFragment extends Fragment {
    public interface OnSelectListener {
        void onSelect(String filterId);
    }

    static final String ARGUMENT_FILTERS = "arg_filters";

    ViewPager pager;
    PagerAdapter pagerAdapter;
    List<Settings.Filter> filters;

    public static StyleSelectFragment newInstance(List<Settings.Filter> filters) {
        final Bundle arguments = new Bundle();
        arguments.putSerializable(ARGUMENT_FILTERS, (Serializable) filters);
        final StyleSelectFragment styleSelect = new StyleSelectFragment();
        styleSelect.setArguments(arguments);
        return styleSelect;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filters = (List<Settings.Filter>)getArguments().getSerializable(ARGUMENT_FILTERS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.style_select, container, false);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager());
        pager.setAdapter(pagerAdapter);
        final Button processButton = (Button) rootView.findViewById(R.id.processButton);
        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnSelectListener)getActivity()).onSelect(filters.get(pager.getCurrentItem()).getId());
            }
        });
        return rootView;
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return StyleSelectItemFragment.newInstance(filters.get(position));
        }

        @Override
        public int getCount() {
            return filters.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return filters.get(position).getName();
        }

    }

}