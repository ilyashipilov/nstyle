package shipilov.name.nstyle;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.net.URL;

/**
 * Created by HOME on 25.04.2017.
 */

public class TrainingDataItemFragment extends Fragment {
    static final String ARGUMENT_ID = "arg_id";

    String id;

    static TrainingDataItemFragment newInstance(String styleId) {
        TrainingDataItemFragment pageFragment = new TrainingDataItemFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_ID, styleId);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (id == null) {
            id = getArguments().getString(ARGUMENT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.training_data_item, null);

        final ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        final ImageView resultView = (ImageView) view.findViewById(R.id.result);
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        iconView.setImageBitmap(BitmapFactory.decodeStream(new URL(serverAddress(id) + "/styleImage.jpg").openStream()));
                        resultView.setImageBitmap(BitmapFactory.decodeStream(new URL(serverAddress(id) + "/result.jpg").openStream()));
                    } catch (IOException e) {
                        //
                    }
                }
            });
            thread.start();
            thread.join();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return view;
    }

    private String serverAddress(String styleId) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return "http://" + sharedPref.getString("adminIp", null) + ":8080/images/" + styleId;
    }
}