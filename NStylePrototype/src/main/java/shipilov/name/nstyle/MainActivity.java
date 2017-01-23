package shipilov.name.nstyle;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import shipilov.name.nstyle.api.ProcessingResult;
import shipilov.name.nstyle.api.Settings;


public class MainActivity extends AppCompatActivity implements StyleSelectFragment.OnSelectListener, PhotoScreenFragment.OnSelectListener {

    private Settings settings;
    private PhotoScreenFragment photoScreen;
    private StyleSelectFragment styleSelect;

    private File fileToProcessing;
    private HashCalculator hashCalculator = new HashCalculator.FirstBytesAndSaltImpl();

    private void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionsOnStorage();
        }

        photoScreen = new PhotoScreenFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.main_ui_container, photoScreen, "photoScreen");
        ft.commit();

        //load settings
        if (settings == null) {
            lockScreen();
            NstyleApplication.getApi().getSettings(new HashMap<String, String>()).enqueue(new Callback<Settings>() {
                @Override
                public void onResponse(Call<Settings> call, Response<Settings> response) {
                    unlockScreen();
                    settings = response.body();
                    styleSelect = StyleSelectFragment.newInstance(settings.getFilters());
                }

                @Override
                public void onFailure(Call<Settings> call, Throwable t) {
                    unlockScreen();
                    showDialog("Initialization error", t.getMessage());
                }
            });
        }

    }

    private void process(File file, String styleId) throws Exception {
        //на время отправки/обработки/загрузки результата залочим экран
        lockScreen();

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        String hash;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            hash = hashCalculator.calculate(inputStream);
        } finally {
            if (inputStream != null)
                inputStream.close();
        }

        Call<ProcessingResult> resultCall = NstyleApplication.getApi()
                .process(body, styleId, hash);

        resultCall.enqueue(new Callback<ProcessingResult>() {
            @Override
            public void onResponse(Call<ProcessingResult> call, Response<ProcessingResult> response) {
                if (!response.body().isSuccess()) {
                    unlockScreen();
                    showDialog("Processing error", response.body().getError());
                    return;
                }
                new LoadImageTask().execute(NstyleApplication.getProperties().getProperty("serverUrl") + "result?resultId=" + response.body().getResultId());
            }

            @Override
            public void onFailure(Call<ProcessingResult> call, Throwable t) {
                unlockScreen();
                showDialog("FAIL", "Fail on send data");
            }
        });
    }

    // после фото
    @Override
    public void onSelect(File file) {
        fileToProcessing = file;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_ui_container, styleSelect, "styleSelect")
                .addToBackStack( null ).commit();
    }

    // после выбора стиля
    @Override
    public void onSelect(String filterId) {
        try {
            process(fileToProcessing, filterId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class LoadImageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                return MediaStore.Images.Media.insertImage(getContentResolver(),
                        BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent()),
                        "NStyle_" + new Date().getTime(), "description");
            } catch (IOException e) {
                unlockScreen();
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String filePath) {
            unlockScreen();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(filePath));
            startActivity(intent);
        }
    }

    private void lockScreen() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void unlockScreen() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissionsOnStorage() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
    }
}
