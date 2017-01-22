package shipilov.name.nstyle.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Результат обработки. Если обработка завершилась успешно возвращаем идентификатор результата,
 * который используется для запроса изображения и выставляется флаг success = true.
 * Если в результате обработки произошла ошибка, флаг success = false, а в поле error передается сообщение.
 *
 * Реализация сервера гарантирует возможность доступа к результату по идентификатору в течении
 * некоторого определенного периода времени с момены выполнения обработки.
 *
 * Created by HOME on 21.01.2017.
 */
public class ProcessingResult implements Serializable {

    @SerializedName("resultId")
    @Expose
    private String resultId;

    @SerializedName("success")
    @Expose
    private boolean success;

    @SerializedName("error")
    @Expose
    private String error;

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
