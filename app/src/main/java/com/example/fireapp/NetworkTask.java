package com.example.fireapp;


import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkTask extends AsyncTask<Void, Void, String> {

    private String tempUrl;
    private NetworkTaskListener listener;

    public NetworkTask(String url, NetworkTaskListener listener) {
        tempUrl = url;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... params) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(tempUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
//            Log.d("DEBUG LOG", "Response code: " + response.code());
//            Log.d("DEBUG LOG", "Response code: " + response.body().string());
            return response.body().string(); // Return the response body string
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            listener.onNetworkTaskComplete(result);
//            Log.d("DEBUG LOG", "onPostExecute result: " + result);
        }
    }
}