package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;
import com.sam_chordas.android.stockhawk.ui.StockDetailsActivity;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{
  private static Context mContext;
  private static Typeface robotoLight;
  private boolean isPercent;
  public QuoteCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    mContext = context;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    robotoLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Light.ttf");
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_quote, parent, false);
    ViewHolder vh = new ViewHolder(itemView);
    return vh;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){
    final String symbol = cursor.getString(cursor.getColumnIndex("symbol"));
    boolean hasDetails;
    viewHolder.symbol.setText(symbol);
    viewHolder.symbol.setContentDescription(mContext.getString(R.string.a11y_stock_name,symbol));
    String price = cursor.getString(cursor.getColumnIndex("bid_price"));
    if (price.isEmpty()){
      hasDetails = false;
      viewHolder.bidPrice.setText(mContext.getString(R.string.not_available));
      viewHolder.bidPrice.setContentDescription(mContext.getString(R.string.a11y_stock_price,mContext.getString(R.string.not_available_extended)));
    }else{
      viewHolder.bidPrice.setText(price);
      viewHolder.bidPrice.setContentDescription(mContext.getString(R.string.a11y_stock_price,price));
      hasDetails = true;
    }

    int sdk = Build.VERSION.SDK_INT;
    if (cursor.getInt(cursor.getColumnIndex("is_up")) == 1){
      if (sdk < Build.VERSION_CODES.JELLY_BEAN){
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }else {
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_green));
      }
    } else{
      if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
        viewHolder.change.setBackgroundDrawable(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      } else{
        viewHolder.change.setBackground(
            mContext.getResources().getDrawable(R.drawable.percent_change_pill_red));
      }
    }
    if (Utils.showPercent){
      String percentageChange = cursor.getString(cursor.getColumnIndex("percent_change"));
      if (percentageChange.isEmpty()){
        viewHolder.change.setText(mContext.getString(R.string.not_available));
        viewHolder.change.setContentDescription(mContext.getString(R.string.a11y_stock_variation,mContext.getString(R.string.not_available_extended)));
      }else{
        viewHolder.change.setText(percentageChange);
        viewHolder.change.setContentDescription(mContext.getString(R.string.a11y_stock_variation,percentageChange));
      }
    } else{
      String change = cursor.getString(cursor.getColumnIndex("change"));
      if (change.isEmpty()){
        viewHolder.change.setText(mContext.getString(R.string.not_available));
        viewHolder.change.setContentDescription(mContext.getString(R.string.a11y_stock_variation,mContext.getString(R.string.not_available_extended)));
      }else{
        viewHolder.change.setText(change);
        viewHolder.change.setContentDescription(mContext.getString(R.string.a11y_stock_variation,change));
      }
    }
    // Valid stock
    if (hasDetails){
      viewHolder.stockContainer.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(mContext, StockDetailsActivity.class);
          intent.putExtra(mContext.getString(R.string.stock_symbol),symbol);
          mContext.startActivity(intent);
        }
      });
    // Unknown stock
    }else{
      viewHolder.stockContainer.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Toast.makeText(mContext,mContext.getString(R.string.unknown_stock),Toast.LENGTH_LONG).show();
        }
      });
    }
  }

  @Override public void onItemDismiss(int position) {
    Cursor c = getCursor();
    c.moveToPosition(position);
    String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
    mContext.getContentResolver().delete(QuoteProvider.Quotes.withSymbol(symbol), null, null);
    notifyItemRemoved(position);
  }

  @Override public int getItemCount() {
    return super.getItemCount();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder
      implements ItemTouchHelperViewHolder, View.OnClickListener{
    public final LinearLayout stockContainer;
    public final TextView symbol;
    public final TextView bidPrice;
    public final TextView change;
    public ViewHolder(View itemView){
      super(itemView);
      stockContainer = (LinearLayout) itemView.findViewById(R.id.stock_container);
      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      symbol.setTypeface(robotoLight);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      change = (TextView) itemView.findViewById(R.id.change);
    }

    @Override
    public void onItemSelected(){
      itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear(){
      itemView.setBackgroundColor(0);
    }

    @Override
    public void onClick(View v) {

    }
  }
}
