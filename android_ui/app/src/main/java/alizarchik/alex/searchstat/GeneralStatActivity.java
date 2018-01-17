package alizarchik.alex.searchstat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import alizarchik.alex.searchstat.Model.GenStatDataItem;
import alizarchik.alex.searchstat.Model.Site;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Olesia on 10.01.2018.
 */

public class GeneralStatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView currentDate;
    private Button buttonSelectSite;
    private ArrayList<String> sites;
    IRestApi restAPI;

    public static final String TAG = "MyLogs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_stat_screen);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();

        buttonSelectSite = findViewById(R.id.button_select_site_GS);
        buttonSelectSite.setOnClickListener((v) -> onClick());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graphics, menu);
        return true;
    }

    private void init() {
        List<GenStatDataItem> myDataset = getDataSet();
        sites = new ArrayList<>();
        mRecyclerView = findViewById(R.id.rvGenStat);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new GSRecyclerAdapter(myDataset);
        mRecyclerView.setAdapter(mAdapter);
        // рисую линию вокруг элемента списка
        DividerItemDecoration divider = new
                DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this,
                R.drawable.line_divider));
        mRecyclerView.addItemDecoration(divider);
        currentDate = findViewById(R.id.tvCurrentDate);
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("E, MMM d, ''yy", Locale.ENGLISH);
        currentDate.setText("Current date: " + formatForDateNow.format(dateNow));
    }

    public List<GenStatDataItem> getDataSet() {
        GenStatDataItem item1 = new GenStatDataItem("Name#1", 231);
        GenStatDataItem item2 = new GenStatDataItem("Name#2", 102);
        GenStatDataItem item3 = new GenStatDataItem("Name#3", 259);
        List<GenStatDataItem> dataSet = new ArrayList<>();
        dataSet.add(item1);
        dataSet.add(item2);
        dataSet.add(item3);

        return dataSet;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.graphics:
                Intent intent = new Intent(this, PieChartActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick() {
        Retrofit retrofit;
        try {
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://195.110.59.16:8081/restapi-v2/?")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            restAPI = retrofit.create(IRestApi.class);
        } catch (Exception io) {
            Log.d(TAG, "no retrofit: " + io.getMessage());
            return;
        }
        // подготовили вызов на сервер
        TokenStorage tokenStorage = TokenStorage.getInstance();
        Log.d(TAG, "token from storage: " + tokenStorage.loadToken(this));
        Call<List<Site>> call = restAPI.getSites(tokenStorage.loadToken(this));
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkinfo = connectivityManager.getActiveNetworkInfo();
        if (networkinfo != null && networkinfo.isConnected()) {
            // запускаем
            try {
                loadSites(call);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "no retrofit: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Подключите интернет");
            Toast.makeText(this, "Подключите интернет", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSites(Call<List<Site>> call) throws IOException {
        call.enqueue(new Callback<List<Site>>() {
            @Override
            public void onResponse(Call<List<Site>> call, Response<List<Site>> response) {
                if (response.isSuccessful()) {
                    if (response != null && sites.isEmpty()) {
                        Site site;
                        for (int i = 0; i < response.body().size(); i++) {
                            site = response.body().get(i);
                            Log.d(TAG, "response.body() sites:" + site.getSiteName());
                            sites.add(site.getSiteName());
                        }
                    }
                } else {
                    Log.d(TAG, "onResponse error: " + response.code());
                }
                final CharSequence[] sitesArray = sites.toArray(new String[sites.size()]);

                AlertDialog.Builder builder = new AlertDialog.Builder(GeneralStatActivity.this);
                builder.setTitle("Select a site");
                builder.setItems(sitesArray, (dialogInterface, i) -> {
                    // Do something with the selection
                });
                builder.show();
            }

            @Override
            public void onFailure(Call<List<Site>> call, Throwable t) {
                Log.d(TAG, "onFailure " + t.getMessage());
            }
        });
    }
}