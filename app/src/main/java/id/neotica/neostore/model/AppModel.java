package id.neotica.neostore.model;

/**
 * Created by ryomartin on 21/03/26.
 */

public class AppModel {
    public String packageName;
    public String title;
    public String description;
    public String iconUrl;

    public AppModel(
            String packageName,
            String title,
            String description,
            String iconUrl
    ) {
        this.packageName = packageName;
        this.title = title;
        this.description = description;
        this.iconUrl = iconUrl;
    }
}
