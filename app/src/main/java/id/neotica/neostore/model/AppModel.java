package id.neotica.neostore.model;

/**
 * Created by ryomartin on 21/03/26.
 */

public class AppModel {
    public String packageName;
    public String title;
    public String desccription;

    public AppModel(
            String packageName,
            String title,
            String description
    ) {
        this.packageName = packageName;
        this.title = title;
        this.desccription = description;
    }
}
