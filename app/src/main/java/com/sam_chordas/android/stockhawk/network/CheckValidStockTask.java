package com.sam_chordas.android.stockhawk.network;

import android.content.Context;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Nicola on 2016-07-10.
 */
public class CheckValidStockTask {
    final String TAG = FetchChartDataTask.class.getSimpleName();
    private Context mContext;
    private CheckValidStockTaskUpdates callbacks;
    private String mStockSymbol;

    public CheckValidStockTask(Context context, String stockSymbol) {
        this.mContext = context;
        this.callbacks = (CheckValidStockTaskUpdates) mContext;
        this.mStockSymbol = stockSymbol;
    }

    public void execute(){
        callbacks.onTaskStarted();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://finance.yahoo.com/webservice/v1/symbols/"+mStockSymbol+"/quote?format=json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    try {
                        String apiResponse = response.body().string();
                        JSONObject jsonResponse = new JSONObject(apiResponse);
                        if (isValidQuote(jsonResponse)){
                            callbacks.onFound(mStockSymbol);
                        }else{
                            callbacks.onNotFound(mStockSymbol);
                        }
                    } catch (Exception e) {
                        callbacks.onUnknownResponse();
                    }
                } else {
                    callbacks.onBadRequest();
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                callbacks.onServerDown();
            }
        });
    }

    public boolean isValidQuote(JSONObject response){
        try{
            int count = response.getJSONObject("list").getJSONObject("meta").getInt("count");
            if (count > 0){
                return true;
            }
        }catch (Exception e){

        }
        return false;
    }

    public interface CheckValidStockTaskUpdates{
        public void onTaskStarted();
        public void onFound(String symbol);
        public void onNotFound(String symbol);
        public void onBadRequest();
        public void onUnknownResponse();
        public void onServerDown();
    }
}
