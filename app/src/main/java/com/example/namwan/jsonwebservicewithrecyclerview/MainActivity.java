package com.example.namwan.jsonwebservicewithrecyclerview;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "RecyclerViewJSON";
    private List<JsonData> feedList;
    private RecyclerView mRecyclerView;
    private RecyclerviewAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progress_bar);

        String url = "http://www.lovedesigner.net/api/get_posts/?count=20";
        new GetDataBinding().execute(url);
    }

    private class GetDataBinding extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... strings) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200){
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null){
                        response.append(line);
                    }
                    parseResult(response.toString());

                    result = 1; // successful
                }else {
                    result = 0; // Failed to fetch data!
                }
            }catch (Exception e){
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressBar.setVisibility(View.GONE);

            if (result == 1){
                adapter = new RecyclerviewAdapter(feedList, MainActivity.this);
                mRecyclerView.setAdapter(adapter);

                adapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(JsonData item) {
                        Toast.makeText(MainActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                Toast.makeText(MainActivity.this, "Failed to fetch JSON data!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseResult(String s) {
        try{
            JSONObject response = new JSONObject(s);
            JSONArray posts = response.getJSONArray("posts");
            feedList = new ArrayList<>();
            for (int i=0; i<posts.length(); i++){
                JSONObject post = posts.optJSONObject(i);
                JsonData item = new JsonData();
                item.setTitle(post.optString("title"));
                item.setThumbnail(post.optString("thumbnail"));
                feedList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
