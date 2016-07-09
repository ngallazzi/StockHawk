package com.sam_chordas.android.stockhawk.network;

import java.util.ArrayList;

/**
 * Created by Nicola on 2016-07-05.
 */
public interface ChartDataUpdates{
    public void onTaskStarted();
    public void onSuccess(ArrayList<String> chartLabels,ArrayList<Float> chartValues);
    public void onBadRequest();
    public void onUnknownResponse();
    public void onServerDown();
}
