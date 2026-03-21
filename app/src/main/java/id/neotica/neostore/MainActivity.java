package id.neotica.neostore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

    // Pagination State
    private int currentPage = 1;
    private int totalPages = 1;
    private Button btnLoadMore;
    private View footerView;

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

        footerView = getLayoutInflater().inflate(R.layout.footer_load_more, null);
        btnLoadMore = (Button) footerView.findViewById(R.id.btn_load_more);
        listView.addFooterView(footerView);

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

        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPage < totalPages) {
                    currentPage++;
                    fetchApps(currentPage);
                }
            }
        });

        fetchApps(currentPage);
    }

    private void fetchApps(final int pageToLoad) {
        String targetUrl = BuildConfig.BASE_URL + "/apps/feed?page="+pageToLoad;

        new ApiTask(this, "GET", targetUrl, null, "Loading Neostore...", new ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {

                    JSONObject root = new JSONObject(response);

                    currentPage = root.optInt("page", 1);
                    totalPages = root.optInt("total_pages", 1);

                    JSONArray dataArray = root.optJSONArray("data");

                    if (pageToLoad == 1) adapter.clear();

                    if (dataArray != null) {
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject appObj = dataArray.getJSONObject(i);

                            String packageName = appObj.optString("package_name", "");
                            String title = appObj.optString("title", "");
                            String desc = appObj.optString("description", "");

                            adapter.add(new AppModel(packageName, title, desc));
                        }
                    }

                    if (currentPage >= totalPages) {
                        btnLoadMore.setVisibility(View.GONE);
                    } else {
                        btnLoadMore.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error parsing server data: " + e.toString(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                if (pageToLoad > 1) {
                    currentPage--;
                }
            }
        }).execute();
    }
}
