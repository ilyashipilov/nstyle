package shipilov.name.nstyle.api;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;

/**
 * Created by HOME on 17.04.2017.
 */
public interface AdminApi {

    @GET("/admin/learningData")
    Call<Map<String, List<String>>> learningData();

    @Multipart
    @POST("/admin/startLearning")
    Call<LearningStatusInfo> startLearning(@Part MultipartBody.Part sampleImage,
                                           @Part MultipartBody.Part styleImage,
                                           @Query("name") String name,
                                           @Query("numIterations") int hash);

    @GET("/admin/status")
    Call<LearningStatusInfo> status();

    @POST("/admin/removeResult")
    Call<Void> removeResult(@Query("styleId") String styleId);
}
