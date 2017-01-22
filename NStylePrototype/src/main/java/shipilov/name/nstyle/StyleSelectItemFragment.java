package shipilov.name.nstyle;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import shipilov.name.nstyle.api.Settings;

/**
 * Элемент списка стилей
 *
 * Created by HOME on 19.01.2017.
 */

public class StyleSelectItemFragment extends Fragment {
    static final String ARGUMENT_ICON = "arg_icon";
    static final String ARGUMENT_ID = "arg_id";

    byte[] icon;
    String id;

    static StyleSelectItemFragment newInstance(Settings.Filter filter) {
        StyleSelectItemFragment pageFragment = new StyleSelectItemFragment();
        Bundle arguments = new Bundle();
        arguments.putByteArray(ARGUMENT_ICON, Base64.decode(filter.getIcon(), Base64.DEFAULT));
        arguments.putString(ARGUMENT_ID, filter.getId());
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (id == null && icon == null) {
            id = getArguments().getString(ARGUMENT_ID);
            icon = getArguments().getByteArray(ARGUMENT_ICON);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.style_select_item, null);

        final ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        iconView.setImageBitmap(BitmapFactory.decodeByteArray(icon, 0, icon.length));

        return view;
    }
}
