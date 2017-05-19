package shipilov.name.nstyle;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by HOME on 18.04.2017.
 */

public class StartLearningFragment extends Fragment {

    private static final int PICK_STYLE_IMAGE = 1;
    private static final int PICK_SAMPLE_IMAGE = 2;

    public interface OnStartLearning {
        void onStart(InputStream styleImage, InputStream sampleImage, String styleName, int numIterations);
    }

    private Button startButton;
    private ImageView styleImage;
    private ImageView sampleImage;
    private NumberPicker iterationsPicker;
    private EditText styleName;

    private Uri styleUri;
    private Uri sampleUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.start_learning_form, container, false);

        iterationsPicker = (NumberPicker) rootView.findViewById(R.id.iterationsPicker);

        initializeIterations(iterationsPicker);

        startButton = (Button) rootView.findViewById(R.id.startButton);
        styleImage = (ImageView) rootView.findViewById(R.id.styleImage);
        sampleImage = (ImageView) rootView.findViewById(R.id.sampleImage);
        styleName = (EditText) rootView.findViewById(R.id.styleName);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ((OnStartLearning)getActivity()).onStart(
                            getActivity().getContentResolver().openInputStream(styleUri),
                            getActivity().getContentResolver().openInputStream(sampleUri),
                            styleName.getText().toString(),
                            getNumIterations(iterationsPicker));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        styleName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                syncStartButton();
            }
        });

        styleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(PICK_STYLE_IMAGE);
            }
        });

        sampleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(PICK_SAMPLE_IMAGE);
            }
        });
        return rootView;
    }

    public static int getNumIterations(NumberPicker iterationsPicker) {
        return iterationsPicker.getValue() < 10 ? iterationsPicker.getValue() * 100 : (iterationsPicker.getValue()-9) * 1000;
    }

    public static void initializeIterations(NumberPicker iterationsPicker) {
        List<String> values = new LinkedList<>();

        for (int i = 100; i<1000; i+=100)
            values.add(String.valueOf(i));

        for (int i = 1000; i<=40000; i+=1000)
            values.add(String.valueOf(i));

        iterationsPicker.setMaxValue(values.size());
        iterationsPicker.setMinValue(1);
        iterationsPicker.setDisplayedValues(values.toArray(new String[0]));
    }

    public void pickImage(int action) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, action);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_STYLE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            styleUri = data.getData();
            styleImage.setImageURI(data.getData());
        }
        if (requestCode == PICK_SAMPLE_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            sampleUri = data.getData();
            sampleImage.setImageURI(data.getData());
        }
        syncStartButton();
    }

    private void syncStartButton() {
        startButton.setEnabled(styleUri != null && sampleUri != null && !styleName.getText().toString().isEmpty());
    }
}
