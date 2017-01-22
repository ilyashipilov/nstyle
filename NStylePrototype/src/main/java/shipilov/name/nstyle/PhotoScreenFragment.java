package shipilov.name.nstyle;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * Фрагмент для выбора фотографии или вызова камеры
 * TODO: только фото пока, реализовать выбор из галереи
 *
 * Created by HOME on 20.01.2017.
 */
public class PhotoScreenFragment extends Fragment {
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private Uri targetUri;

    public interface OnSelectListener {
        void onSelect(File file);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photo_screen, container, false);

        final Button processButton = (Button) rootView.findViewById(R.id.makePhoto);

        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        return rootView;
    }

    private void dispatchTakePictureIntent() {
        targetUri = getActivity().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);

        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), targetUri);

                Bitmap scaledBitmap = resize(bitmap,
                        Integer.valueOf(NstyleApplication.getProperties().getProperty("imageSizeMax")));

                File outputFile = File.createTempFile("nstyle_", ".jpg", this.getActivity().getCacheDir());
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG,
                        Integer.valueOf(NstyleApplication.getProperties().getProperty("imageQuality")),
                        new FileOutputStream(outputFile));

                ((OnSelectListener)getActivity()).onSelect(outputFile);
            } catch (IOException e) {
                Log.i(e.getMessage(), e.getMessage());
            }
        }
    }

    private Bitmap resize(Bitmap source, int maxSize) {
        int outWidth;
        int outHeight;
        int inWidth = source.getWidth();
        int inHeight = source.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        return Bitmap.createScaledBitmap(source, outWidth, outHeight, false);
    }

}
