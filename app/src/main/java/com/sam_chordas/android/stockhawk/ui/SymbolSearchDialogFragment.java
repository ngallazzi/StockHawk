package com.sam_chordas.android.stockhawk.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

/**
 * Created by Nicola on 2016-07-07.
 */
public class SymbolSearchDialogFragment extends DialogFragment {
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity()).title(R.string.symbol_search)
                .content(R.string.content_test)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                    @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                        // On FAB click, receive user input. Make sure the stock doesn't already exist
                        // in the DB and proceed accordingly
                        Cursor c = getActivity().getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ? OR " +QuoteColumns.SYMBOL + "= ?",
                                new String[] { input.toString().toUpperCase(), input.toString().toLowerCase() }, null);
                        if (c.getCount() != 0) {
                            Toast toast =
                                    Toast.makeText(getActivity(), getString(R.string.stock_already_saved),
                                            Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                            toast.show();
                            return;
                        } else {
                            // Add the stock to DB
                            Intent intent = new Intent(getActivity(), StockIntentService.class);
                            intent.putExtra("tag", "add");
                            intent.putExtra("symbol", input.toString());
                            getActivity().startService(intent);
                        }
                    }
                }).build();
    }

}
