package id.neotica.neostore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.neotica.neostore.model.AppModel;
import id.neotica.neostore.network.ApiCallback;
import id.neotica.neostore.network.ApiTask;
import id.neotica.neostore.ui.AppAdapter;
import id.neotica.neostore.ui.detail.AppDetailActivity;
import id.neotica.neostore.utils.CrashCatcher;

public class MainActivity extends Activity {

    private ListView listView;
    private AppAdapter adapter;
    private List<AppModel> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashCatcher.init(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        CrashCatcher.showCrashLogIfAny(this);

        TextView tvTitle = (TextView) findViewById(R.id.tv_title);

        tvTitle.setText("Welcome User!");

        listView = (ListView) findViewById(R.id.lv_main);
        appList = new ArrayList<>();
        adapter = new AppAdapter(this, appList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppModel clickedApp = adapter.getItem(position);
                if (clickedApp != null) {
                    Intent intent = new Intent(MainActivity.this, AppDetailActivity.class);
                    intent.putExtra("PACKAGE_NAME", clickedApp.packageName);
                    startActivity(intent);
                }
            }
        });

        fetchApps();
    }

    private void fetchApps() {
        String targetUrl = BuildConfig.BASE_URL + "/apps/feed";

        new ApiTask(this, "GET", targetUrl, null, "Loading Neostore...", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    adapter.clear();

                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject appObj = jsonArray.getJSONObject(i);

                        String packageName = appObj.optString("package_name", "");
                        String title = appObj.optString("title", "");
                        String desc = appObj.optString("description", "");

                        adapter.add(new AppModel(packageName, title, desc));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error parsing server data: " + e.toString(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }).execute();
    }
}
