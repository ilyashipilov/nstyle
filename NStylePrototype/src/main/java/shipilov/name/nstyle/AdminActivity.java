package shipilov.name.nstyle;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.soundcloud.android.crop.Crop;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import shipilov.name.nstyle.api.LearningStatusInfo;
import shipilov.name.nstyle.api.ProcessingResult;
import shipilov.name.nstyle.api.Settings;

/**
 * Created by HOME on 17.04.2017.
 */

public class AdminActivity extends AppCompatActivity implements StartLearningFragment.OnStartLearning, ServerIpFragment.OnConnectListener, TrainingDataFragment.DataProvider, TrainingDataFragment.Listener, MenuFragment.Listener {

    private LearningScreenFragment learningScreen;
    private StartLearningFragment startLearning;
    private ServerIpFragment serverIpFragment;
    private TrainingDataFragment trainingDataFragment;
    private MenuFragment menuFragment;

    private Timer statusTimer;

    private LearningStatusInfo learningStatusInfo;
    private List<String> styles;
    private List<String> publicStyles;

    private Mat mat;

    private File outputFile;
    private static String gpuServerAdminAddress;

    private void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle(title);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mat=new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_admin);

        learningScreen = new LearningScreenFragment();
        startLearning = new StartLearningFragment();
        serverIpFragment = new ServerIpFragment();
        trainingDataFragment = new TrainingDataFragment();
        menuFragment = new MenuFragment();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissionsOnStorage();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (learningStatusInfo == null) {
            ft.add(R.id.main_ui_container, serverIpFragment, "serverIpFragment");
        } else {
            ft.replace(R.id.main_ui_container, menuFragment, "menuFragment").addToBackStack(null);
        }
        ft.commit();
    }

    private void go() {
        NstyleApplication.getAdminApi().status().enqueue(new Callback<LearningStatusInfo>() {
            @Override
            public void onResponse(Call<LearningStatusInfo> call, Response<LearningStatusInfo> response) {
                learningStatusInfo = response.body();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                if (learningStatusInfo.getStatus() == LearningStatusInfo.Status.WAIT
                        || learningStatusInfo.getStatus() == LearningStatusInfo.Status.COMPLETED
                        || learningStatusInfo.getStatus() == LearningStatusInfo.Status.ERROR) {
                    ft.replace(R.id.main_ui_container, startLearning, "startLearning").addToBackStack(null);;
                } else {
                    ft.replace(R.id.main_ui_container, learningScreen, "learningScreen").addToBackStack(null);;
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


    @Override
    public void onTraining() {
        go();
    }

    @Override
    public void onPublishing() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        refreshStyles();
        ft.replace(R.id.main_ui_container, trainingDataFragment, "trainingDataFragment").addToBackStack(null);
        ft.commit();
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
                showDialog("Init error", t.getMessage());
            }
        });

    }

    private void startStatusTask() {
        statusTimer = new Timer();
        StatusSyncTask mMyTimerTask = new StatusSyncTask();
        statusTimer.schedule(mMyTimerTask, 1000, 1000);
    }

    @Override
    public void onConnect(String ip, String adminIp) {
        NstyleApplication.initializeApi(ip, adminIp);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.main_ui_container, menuFragment, "menuFragment").addToBackStack(null);
        ft.commit();
    }

    //запрос данных от админки и с процессингового сервера
    private void refreshStyles() {
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, List<String>> learningData = NstyleApplication.getAdminApi().learningData().execute().body();
                    styles = new ArrayList<>(learningData.keySet());
                    Settings settings = NstyleApplication.getApi().getSettings(new HashMap<String, String>()).execute().body();
                    publicStyles = new ArrayList<>(settings.getFilters().size());
                    for (Settings.Filter filter: settings.getFilters()) {
                        publicStyles.add(filter.getId());
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        worker.start();
        try {
            worker.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getStyles() {
        return styles;
    }

    @Override
    public boolean isPublic(String styleId) {
        return publicStyles.contains(styleId);
    }

    @Override
    public void onPublicChange(final String styleId) {

        if (publicStyles.contains(styleId)) {
            NstyleApplication.getApi().removeStyle(styleId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    refreshStyles();
                    showDialog("Unpublished success", "Style \"" + styleId + "\" successful unpublished");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    showDialog("Unpublishing error", t.getMessage());
                }
            });
        } else {
            new LoadImageTask().execute(styleImageAddress(styleId), styleId);
        }
    }

    @Override
    public void onRemove(final String styleId) {

        if (publicStyles.contains(styleId)) {
            NstyleApplication.getApi().removeStyle(styleId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    removeTraining(styleId);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    showDialog("Unpublishing error", t.getMessage());
                }
            });
        } else {
            removeTraining(styleId);
        }

    }

    private void removeTraining(String styleId) {
        final int indexOf = styles.indexOf(styleId);
        NstyleApplication.getAdminApi().removeResult(styleId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                refreshStyles();
                trainingDataFragment.refreshPageView(indexOf);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showDialog("Remove error", t.getMessage());
            }
        });
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

    public class LoadImageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                return MediaStore.Images.Media.insertImage(getContentResolver(),
                        BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent()),
                        "NStyle_" + new Date().getTime(), "description");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(String filePath) {

            try {
                outputFile = File.createTempFile("cropped", "jpg", AdminActivity.this.getCacheDir());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Crop.of(Uri.parse(filePath), Uri.fromFile(outputFile)).asSquare().start(AdminActivity.this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Crop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {


            Bitmap photo = null;
            try {
                photo = BitmapFactory.decodeStream(new FileInputStream(outputFile));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);

            Mat src = Imgcodecs.imdecode(new MatOfByte(stream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
            Mat dst = new Mat();
            Size size = new Size(125, 125); //client icons size
            Imgproc.resize(src, dst, size, 0, 0, Imgproc.INTER_CUBIC);

            try {
                File tmpFile = File.createTempFile("cropped_resized", ".png", AdminActivity.this.getCacheDir());

                Imgcodecs.imwrite(tmpFile.getAbsolutePath(), dst);

                final String styleId = trainingDataFragment.getCurrntStyleId();
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), tmpFile);
                MultipartBody.Part body = MultipartBody.Part.createFormData("styleIcon", tmpFile.getName(), requestFile);

                NstyleApplication.getApi().placeStyle(body, styleId, styleId, networkUrlAddress(styleId)).enqueue(new Callback<ProcessingResult>() {
                    @Override
                    public void onResponse(Call<ProcessingResult> call, Response<ProcessingResult> response) {
                        if (response.body().isSuccess()) {
                            showDialog("Success", "Style \"" + styleId + "\" published");
                            refreshStyles();
                        }
                        else
                            showDialog("Error", "Publish \"" + styleId + "\" error: " + response.body().getError());
                    }

                    @Override
                    public void onFailure(Call<ProcessingResult> call, Throwable t) {
                        showDialog("Error", "Publish \"" + styleId + "\" error: " + t.getMessage());
                    }
                });



            } catch (IOException e) {
                throw new RuntimeException(e);
            }


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

    private String styleImageAddress(String styleId) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return "http://" + sharedPref.getString("adminIp", null) + ":8080/images/" + styleId + "/styleImage.jpg";
    }

    private String networkUrlAddress(String styleId) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        return "http://" + sharedPref.getString("adminIp", null) + ":8080/styleT7place/" + styleId + ".t7";
    }

    public static Bitmap getResizedBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public static String getGpuServerAddress() {
        return getAddress("http://wisesharksoftware.com/servergpu.html");
    }

    public static String getGpuServerAdminAddress() {
        return getAddress("http://wisesharksoftware.com/servergpuadmin.html");
    }

    @Nullable
    private static String getAddress(final String spec) {
        final StringBuffer result = new StringBuffer();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader in = null;
                try {
                    URL url = new URL(spec);
                    in = new BufferedReader( new InputStreamReader( url.openStream()));
                    result.append(in.readLine());
                    in.close();
                } catch (Exception ex) {
                } finally {
                    if (in != null)
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                }
            }
        });

        try {
            thread.start();
            thread.join();
        } catch (InterruptedException e) {
        }

        return result.toString();
    }


}