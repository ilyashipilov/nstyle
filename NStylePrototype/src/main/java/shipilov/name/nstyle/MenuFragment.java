package shipilov.name.nstyle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by HOME on 26.04.2017.
 */

public class MenuFragment extends Fragment {
    private Button trainingButton;
    private Button publishingButton;

    public interface Listener {
        void onTraining();
        void onPublishing();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.menu, container, false);

        trainingButton = (Button) rootView.findViewById(R.id.trainingButton);
        publishingButton = (Button) rootView.findViewById(R.id.publishingButton);

        trainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MenuFragment.Listener)getActivity()).onTraining();
            }
        });

        publishingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MenuFragment.Listener)getActivity()).onPublishing();
            }
        });

        return rootView;
    }
}
