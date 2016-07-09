package com.sam_chordas.android.stockhawk.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
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
 * Created by Nicola on 2016-07-05.
 */
public class FetchChartDataTask {
    final String TAG = FetchChartDataTask.class.getSimpleName();
    private Context mContext;
    private ChartDataUpdates chartDataTaskCallbacks;
    private String mStockSymbol;
    private String mPeriod;

    public FetchChartDataTask(Context context, String stockSymbol, String period) {
        this.mContext = context;
        this.chartDataTaskCallbacks = (ChartDataUpdates) mContext;
        this.mStockSymbol = stockSymbol;
        this.mPeriod = period;
    }

    public void execute(){
        chartDataTaskCallbacks.onTaskStarted();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://chartapi.finance.yahoo.com/instrument/1.0/" + mStockSymbol + "/chartdata;type=quote;range="+mPeriod+"/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                ArrayList<String> chartLabels;
                ArrayList<Float> chartValues;
                if (response.code() == 200) {
                    try {
                        String apiResponse = response.body().string();
                        if (apiResponse.startsWith("finance_charts_json_callback( ")) {
                            apiResponse = apiResponse.substring(29, apiResponse.length() - 2);
                        }
                        JSONObject jsonResponse = new JSONObject(apiResponse);
                        JSONArray series = jsonResponse.getJSONArray("series");

                        chartLabels = new ArrayList<String>();
                        chartValues = new ArrayList<Float>();

                        for (int i = 0; i < series.length(); i++) {
                            JSONObject item = series.getJSONObject(i);
                            SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMdd");
                            String date = android.text.format.DateFormat.
                                    getMediumDateFormat(mContext).
                                    format(srcFormat.parse(item.getString("Date")));
                            chartLabels.add(date);
                            chartValues.add(Float.parseFloat(item.getString("close")));
                        }
                        chartDataTaskCallbacks.onSuccess(chartLabels,chartValues);
                    } catch (Exception e) {
                        chartDataTaskCallbacks.onUnknownResponse();
                    }
                } else {
                    chartDataTaskCallbacks.onBadRequest();
                }
            }

            @Override
            public void onFailure(Request request, IOException e) {
                chartDataTaskCallbacks.onServerDown();
            }
        });
    }
}
