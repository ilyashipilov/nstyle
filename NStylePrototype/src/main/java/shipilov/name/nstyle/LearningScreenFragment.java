package shipilov.name.nstyle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.InputStream;

import shipilov.name.nstyle.api.LearningStatusInfo;

/**
 * Created by HOME on 18.04.2017.
 */
public class LearningScreenFragment extends Fragment {

    public interface OnCancelLearning {
        void onCancel();
    }

    private ProgressBar progressBar;
    private TextView progressLabel;
    private Button cancelButton;

    public void showStatus(LearningStatusInfo statusInfo) {
        progressBar.setMax(statusInfo.getNumIterations());
        progressBar.setProgress(statusInfo.getProgress());
        progressLabel.setText("Training \"" + statusInfo.getStyleName() + "\": iteration " + statusInfo.getProgress() + " from " + statusInfo.getNumIterations());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.learning_screen, container, false);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        progressLabel = (TextView) rootView.findViewById(R.id.progress_label);
        cancelButton = (Button) rootView.findViewById(R.id.cancel_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnCancelLearning)getActivity()).onCancel();
            }
        });

        return rootView;
    }

}
