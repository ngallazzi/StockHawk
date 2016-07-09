package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          batchOperations.add(buildBatchOperation(jsonObject));
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
        //Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncatePrice(String bidPrice){
    try{
      bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    }catch (Exception e){
      bidPrice = "";
    }
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    try{
      double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
      change = String.format("%.2f", round);
      StringBuffer changeBuffer = new StringBuffer(change);
      changeBuffer.insert(0, weight);
      changeBuffer.append(ampersand);
      change = changeBuffer.toString();
    }catch (Exception e){
      change = "";
    }
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncatePrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }
      else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }
      /* Additional infos */
      builder.withValue(QuoteColumns.NAME,jsonObject.getString("Name"));
      builder.withValue(QuoteColumns.DAYS_LOW,truncatePrice(jsonObject.getString("DaysLow")));
      builder.withValue(QuoteColumns.DAYS_HIGH,truncatePrice(jsonObject.getString("DaysHigh")));
      builder.withValue(QuoteColumns.YEAR_LOW,truncatePrice(jsonObject.getString("YearLow")));
      builder.withValue(QuoteColumns.YEAR_HIGH,truncatePrice(jsonObject.getString("YearHigh")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE_FROM_YEAR_LOW,jsonObject.getString("PercentChangeFromYearLow"));
      builder.withValue(QuoteColumns.PERCENT_CHANGE_FROM_YEAR_HIGH,jsonObject.getString("PercebtChangeFromYearHigh"));
    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

  public static boolean isConnected (Context context){
    boolean isConnected;
    ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    isConnected = activeNetwork != null &&
            activeNetwork.isConnectedOrConnecting();
    return isConnected;
  }

  public static Snackbar getWarningSnackBar(Context context, View attachedView, String message){
      Snackbar snackBar = Snackbar.make(attachedView,message, Snackbar.LENGTH_LONG);
      snackBar.getView().setBackgroundColor(ContextCompat.getColor(context,R.color.material_orange_accent_700));
      View view = snackBar.getView();
      TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
      tv.setTextColor(Color.WHITE);
      return snackBar;
  }
}
