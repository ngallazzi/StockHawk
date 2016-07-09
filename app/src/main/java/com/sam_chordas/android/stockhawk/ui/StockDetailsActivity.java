package com.sam_chordas.android.stockhawk.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.network.ChartDataUpdates;
import com.sam_chordas.android.stockhawk.network.FetchChartDataTask;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nicola on 2016-06-30.
 */
public class StockDetailsActivity extends AppCompatActivity implements ChartDataUpdates {
    @BindView(R.id.details_container) ScrollView svDetailsContainer;
    @BindView(R.id.stock_symbol) TextView tvStockSymbol;
    @BindView(R.id.stock_price) TextView tvStockPrice;
    @BindView(R.id.stock_variation) TextView tvStockVariation;
    @BindView(R.id.stock_detail_value_1) TextView tvStockDetailValue1;
    @BindView(R.id.stock_detail_value_2) TextView tvStockDetailValue2;
    @BindView(R.id.stock_values_chart) ValueLineChart vlcStockValues;
    @BindView(R.id.dot_progress_bar) DotProgressBar pbChartLoading;
    @BindView(R.id.time_spinner) Spinner sTimeIntervalSelection;

    private static final String TAG = StockDetailsActivity.class.getSimpleName();
    private String mStockSymbol;
    private Context mContext;
    private int mShortAnimationDuration;
    String[] spinnerLabels;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);
        ButterKnife.bind(this);
        mContext = this;
        mStockSymbol = getIntent().getStringExtra(getString(R.string.stock_symbol));
        Uri uriWithSymbol = QuoteProvider.Quotes.withSymbol(mStockSymbol);
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        // Spinner for time interval selection
        spinnerLabels = getResources().getStringArray(R.array.chart_time_interval_labels);
        final String[] spinnerValues = getResources().getStringArray(R.array.chart_time_interval_values);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.chart_time_interval_values, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sTimeIntervalSelection.setContentDescription(getString(R.string.chart_time_interval_label));
        sTimeIntervalSelection.setAdapter(adapter);
        sTimeIntervalSelection.setSelection(0);
        sTimeIntervalSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (Utils.isConnected(mContext)){
                    String period = spinnerValues[position];
                    new FetchChartDataTask(mContext,mStockSymbol,period).execute();
                }else{
                    Snackbar snackbar = Utils.getWarningSnackBar(mContext,svDetailsContainer,getString(R.string.empty_chart));
                    snackbar.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sTimeIntervalSelection.setSelection(0);
            }
        });
        //
        Cursor cursor = getContentResolver().query(uriWithSymbol, null, QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);
        if (cursor.moveToFirst()){
            String stockName = cursor.getString(cursor.getColumnIndex(QuoteColumns.NAME)).toString();
            setTitle(stockName);

            tvStockSymbol.setText(cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)).toString().toUpperCase());
            tvStockSymbol.setContentDescription(getString(R.string.a11y_stock_name,mStockSymbol));

            String stockPrice = (cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            tvStockPrice.setText(stockPrice);
            tvStockPrice.setContentDescription(getString(R.string.a11y_stock_price,stockPrice));

            String change = cursor.getString(cursor.getColumnIndex(QuoteColumns.CHANGE));
            try{
                if (change.contains("+")){
                    tvStockVariation.setTextColor(ContextCompat.getColor(this,R.color.material_green_700));
                }else{
                    tvStockVariation.setTextColor(ContextCompat.getColor(this,R.color.material_red_700));
                }
                String percentChange = (cursor.getString(cursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
                tvStockVariation.setText(change + " ("+ percentChange + ")");
                tvStockVariation.setContentDescription(getString(R.string.a11y_stock_variation,change + " ("+ percentChange + ")"));

                // Day Low
                String daysLow = (cursor.getString(cursor.getColumnIndex(QuoteColumns.DAYS_LOW)));
                tvStockDetailValue1.setText(daysLow);
                tvStockDetailValue1.setContentDescription(getString(R.string.day_low) + " " + daysLow);
                // Day High
                String daysHigh = (cursor.getString(cursor.getColumnIndex(QuoteColumns.DAYS_HIGH)));
                tvStockDetailValue2.setText(daysHigh);
                tvStockDetailValue2.setContentDescription(getString(R.string.day_high) + " " + daysHigh);
                if (Utils.isConnected(this)){
                    String period = sTimeIntervalSelection.getSelectedItem().toString();
                    new FetchChartDataTask(this,mStockSymbol,period).execute();
                }else{
                    Snackbar snackbar = Utils.getWarningSnackBar(mContext,svDetailsContainer,getString(R.string.empty_chart));
                    snackbar.show();
                }
            }catch (Exception e){
                // Unknown stock
                setLayoutForUnknownStock();
            }
        }else{
            // Unknown stock
            setLayoutForUnknownStock();
        }
    }

    private void setLayoutForUnknownStock(){
        setTitle(mStockSymbol);
        tvStockPrice.setText(getString(R.string.not_available));
        tvStockVariation.setText(getString(R.string.not_available));
        tvStockDetailValue1.setText(R.string.not_available);
        tvStockDetailValue2.setText(R.string.not_available);
        Snackbar snackbar = Utils.getWarningSnackBar(mContext,svDetailsContainer,getString(R.string.unknown_stock));
        snackbar.show();
    }

    private void addDataToChart(ArrayList<String> labels, ArrayList<Float> values) {
        ValueLineSeries stockSerie = new ValueLineSeries();
        stockSerie.setColor(ContextCompat.getColor(this,R.color.material_orange_accent_700));

        for (int i=0; i<labels.size(); i++){
            stockSerie.addPoint(new ValueLinePoint(labels.get(i), values.get(i)));
        }
        vlcStockValues.clearChart();
        vlcStockValues.setContentDescription(getString(R.string.stock_chart));
        vlcStockValues.addSeries(stockSerie);
        vlcStockValues.startAnimation();
    }

    @Override
    public void onTaskStarted() {
        pbChartLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuccess(final ArrayList<String> chartLabels, final ArrayList<Float> chartValues) {
        StockDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addDataToChart(chartLabels,chartValues);
                crossFadeProgressBarAndChart();
            }
        });

    }

    @Override
    public void onBadRequest() {
        StockDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbChartLoading.setVisibility(View.GONE);
                Snackbar snackbar = Utils.getWarningSnackBar(mContext, svDetailsContainer, getString(R.string.bad_request));
                snackbar.show();
            }
        });
    }

    @Override
    public void onUnknownResponse() {
        StockDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbChartLoading.setVisibility(View.GONE);
                Snackbar snackbar = Utils.getWarningSnackBar(mContext, svDetailsContainer, getString(R.string.server_error));
                snackbar.show();
            }
        });
    }

    @Override
    public void onServerDown() {
        StockDetailsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbChartLoading.setVisibility(View.GONE);
                Snackbar snackbar = Utils.getWarningSnackBar(mContext,svDetailsContainer,getString(R.string.server_down));
                snackbar.show();
            }
        });
    }

    private void crossFadeProgressBarAndChart() {

        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        vlcStockValues.setAlpha(0f);
        vlcStockValues.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        vlcStockValues.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        pbChartLoading.animate()
                .alpha(0f)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pbChartLoading.setVisibility(View.GONE);
                    }
                });
    }
}
