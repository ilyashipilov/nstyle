package shipilov.name.nstyle;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import shipilov.name.nstyle.api.AdminApi;
import shipilov.name.nstyle.api.ServerApi;

/**
 * Created by HOME on 18.01.2017.
 */

public class NstyleApplication extends Application {

    private static ServerApi serverApi;
    private static AdminApi adminApi;
    private static Retrofit retrofit;
    private static Retrofit adminRetrofit;
    private static Properties properties;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            properties = new Properties();
            properties.load(getBaseContext().getAssets().open("config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void initializeApi(String ip, String adminIp) {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://" + ip + ":8080/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        adminRetrofit = new Retrofit.Builder()
                .baseUrl("http://" + adminIp + ":8080/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        serverApi = retrofit.create(ServerApi.class);
        adminApi = adminRetrofit.create(AdminApi.class);
    }

    public static ServerApi getApi() {
        return serverApi;
    }

    public static AdminApi getAdminApi() {
        return adminApi;
    }

    public static Properties getProperties() {
        return properties;
    }

}