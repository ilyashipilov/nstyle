package shipilov.name.nstyle.api;

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
 * Интерфейс сервера обработки изображений.
 *
 * Последовательность работы:
 *  - запрос настроек, включающих доступные варианты обработки
 *   @see Settings
 * - отправка запроса на обработку с синхронным ответом
 * - запрос результата обработки
 *
 * Created by HOME on 18.01.2017.
 */
public interface ServerApi {

    /**
     * Запрос параметров инициализации - доступных фильтров обработки
     * @see Settings
     * @param options любые параметры, например, требуемый размер иконок фильтров, или оплаченое приложение,
     *                к примеру, может иметь больше доступных фильтров, и т.п.
     * @return настройки инициализации
     */
    @GET("/settings")
    Call<Settings> getSettings(@QueryMap Map<String, String> options);

    /**
     * Выполнение обработки в синхронном режиме.
     * Реализация на сервере обеспечивает возможность доступа к результату обработки сразу после ответа.
     *
     * @see #getSettings(Map)
     * @see #result(String)
     *
     * @param file файл с изображением
     * @param styleId идентификатор фильтра
     * @param hash хэш, расчитанный по секретному алгоритму
     *
     * @return результат выполнения
     */
    @Multipart
    @POST("/process")
    Call<ProcessingResult> process(@Part MultipartBody.Part file,
                                   @Query("styleId") String styleId,
                                   @Query("hash") String hash);

    /**
     * Результат обработки - изображение
     * @see #process(MultipartBody.Part, String, String)
     *
     * @param resultId идентификатор результата
     * @return изображение
     */
    @GET("/result")
    @Streaming
    Call<ResponseBody> result(@Query("resultId") String resultId);

    /**
     * Установка нового стиля. Только для администратора.
     * TODO: секьюрность
     *
     * @param styleId
     * @param styleName
     * @param styleIcon иконка стиля
     * @param networkFileUrl url файла с обученной сетью
     * @return
     */
    @POST("/placeStyle")
    Call<ResponseBody> placeStyle(
            @Query("styleId") String styleId,
            @Query("styleName") String styleName,
            @Part("styleIcon") MultipartBody.Part styleIcon,
            @Query("networkFileUrl") String networkFileUrl);

    /**
     * Удаление стиля. Только для администратора.
     * TODO: секьюрность
     *
     * @param styleId
     * @return
     */
    @POST("/removeStyle")
    Call<ResponseBody> removeStyle(
            @Query("styleId") String styleId);

}
