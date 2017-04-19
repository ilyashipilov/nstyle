package shipilov.name.nstyle;

import android.app.Application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
    private Retrofit retrofit;
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

        retrofit = new Retrofit.Builder()
                .baseUrl(getProperties().getProperty("serverUrl"))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        serverApi = retrofit.create(ServerApi.class);
        adminApi = retrofit.create(AdminApi.class);
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