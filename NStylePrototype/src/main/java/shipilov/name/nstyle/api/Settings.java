package shipilov.name.nstyle.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Информация с сервера обработки для инициализации.
 * В настоящий момент это список доступных фильтров.
 * @see Filter
 *
 * Created by HOME on 18.01.2017.
 */
public class Settings implements Serializable {

    /**
     * Информация о фильтре включает идентификатор, наименование для пользователя и base64-кодированную иконку
     */
    public static class Filter implements Serializable {
        @SerializedName("id")
        @Expose
        private String id;

        @SerializedName("name")
        @Expose
        private String name;

        @SerializedName("icon")
        @Expose
        private String icon;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @SerializedName("filters")
    @Expose
    private List<Filter> filters;

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

}
