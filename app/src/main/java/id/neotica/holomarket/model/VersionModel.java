package id.neotica.holomarket.model;

/**
 * Created by ryomartin on 21/03/26.
 */

public class VersionModel {
    public String versionName;
    public int versionCode;
    public String fileUrl;
    public String changelog;

    public VersionModel(String versionName, int versionCode, String fileUrl, String changelog) {
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.fileUrl = fileUrl;
        this.changelog = changelog;
    }
}
