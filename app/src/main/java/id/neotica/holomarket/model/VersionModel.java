package id.neotica.holomarket.model;

/**
 * Created by ryomartin on 21/03/26.
 */

public class VersionModel {
    public String id;
    public String appId;
    public String versionName;
    public int versionCode;
    public String fileUrl;
    public String changelog;
    public int minSdk;
    public int maxSdk;
    public long createdAt;

    public VersionModel(
            String id,
            String appId,
            String versionName,
            int versionCode,

            String fileUrl,
            String changelog,
            int minSdk,
            int maxSdk,
            long createdAt
    ) {
        this.id = id;
        this.appId = appId;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.fileUrl = fileUrl;
        this.changelog = changelog;
        this.minSdk = minSdk;
        this.maxSdk = maxSdk;
        this.createdAt = createdAt;
    }
}
