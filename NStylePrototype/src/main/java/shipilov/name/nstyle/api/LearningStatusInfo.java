package shipilov.name.nstyle.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by HOME on 17.04.2017.
 */

public class LearningStatusInfo implements Serializable {

    public enum Status {
        WAIT, COMPLETED, ERROR, PROCESSING
    }

    @SerializedName("status")
    @Expose
    private Status status;

    @SerializedName("error")
    @Expose
    private String error;

    @SerializedName("progress")
    @Expose
    private int progress;

    @SerializedName("numIterations")
    @Expose
    private int numIterations;

    @SerializedName("styleName")
    @Expose
    private String styleName;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getNumIterations() {
        return numIterations;
    }

    public void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

}
