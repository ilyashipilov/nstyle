package shipilov.name.nstyle;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import shipilov.name.nstyle.api.LearningStatusInfo;

/**
 * Created by HOME on 17.04.2017.
 */

public class AdminActivity extends AppCompatActivity implements StartLearningFragment.OnStartLearning {

    private LearningScreenFragment learningScreen;
    private StartLearningFragment startLearning;

    private Timer statusTimer;

    private LearningStatusInfo learningStatusInfo;

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
        setContentView(R.layout.activity_admin);

        learningScreen = new LearningScreenFragment();
        startLearning = new StartLearningFragment();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionsOnStorage();
        }

        if (learningStatusInfo == null) {
            NstyleApplication.getAdminApi().status().enqueue(new Callback<LearningStatusInfo>() {
                @Override
                public void onResponse(Call<LearningStatusInfo> call, Response<LearningStatusInfo> response) {
                    learningStatusInfo = response.body();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    if (learningStatusInfo.getStatus() == LearningStatusInfo.Status.WAIT
                            || learningStatusInfo.getStatus() == LearningStatusInfo.Status.COMPLETED
                            || learningStatusInfo.getStatus() == LearningStatusInfo.Status.ERROR) {
                        ft.add(R.id.main_ui_container, startLearning, "startLearning");
                    } else {
                        ft.add(R.id.main_ui_container, learningScreen, "learningScreen");
                        startStatusTask();
                    }
                    ft.commit();
                }

                @Override
                public void onFailure(Call<LearningStatusInfo> call, Throwable t) {
                    showDialog("Initialization error", t.getMessage());
                }
            });
        }


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

    @Override
    public void onStart(InputStream styleImage, InputStream sampleImage, String styleName, int numIterations) {

        RequestBody sampleFile = RequestBody.create(MediaType.parse("multipart/form-data"), readBytes(sampleImage));
        MultipartBody.Part sampleFilePart = MultipartBody.Part.createFormData("sampleImage", "sampleImage", sampleFile);

        RequestBody styleFile = RequestBody.create(MediaType.parse("multipart/form-data"), readBytes(styleImage));
        MultipartBody.Part styleFilePart = MultipartBody.Part.createFormData("styleImage", "styleImage", styleFile);

        Call<LearningStatusInfo> resultCall = NstyleApplication.getAdminApi().startLearning(sampleFilePart, styleFilePart, styleName, numIterations);

        resultCall.enqueue(new Callback<LearningStatusInfo>() {
            @Override
            public void onResponse(Call<LearningStatusInfo> call, Response<LearningStatusInfo> response) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_ui_container, learningScreen, "learningScreen")
                        .commit();

                startStatusTask();
            }

            @Override
            public void onFailure(Call<LearningStatusInfo> call, Throwable t) {
                showDialog("E", "123");
            }
        });

    }

    private void startStatusTask() {
        statusTimer = new Timer();
        StatusSyncTask mMyTimerTask = new StatusSyncTask();
        statusTimer.schedule(mMyTimerTask, 1000, 1000);
    }

    class StatusSyncTask extends TimerTask {

        @Override
        public void run() {
            NstyleApplication.getAdminApi().status().enqueue(new Callback<LearningStatusInfo>() {
                @Override
                public void onResponse(Call<LearningStatusInfo> call, final Response<LearningStatusInfo> response) {
                    if (response.body().getStatus() == LearningStatusInfo.Status.COMPLETED
                            || response.body().getStatus() == LearningStatusInfo.Status.ERROR) {
                        if (response.body().getStatus() == LearningStatusInfo.Status.ERROR) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showDialog("Processing error", response.body().getError());
                                }
                            });
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.main_ui_container, startLearning, "startLearning")
                                        .commit();
                            }
                        });
                        statusTimer.cancel();
                    } else {
                        learningScreen.showStatus(response.body());
                    }
                }

                @Override
                public void onFailure(Call<LearningStatusInfo> call, Throwable t) {
                    showDialog("Receive status error", t.getMessage());
                }
            });

        }
    }

    public byte[] readBytes(InputStream inputStream) {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }
}