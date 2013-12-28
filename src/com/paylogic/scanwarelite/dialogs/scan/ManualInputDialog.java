package com.paylogic.scanwarelite.dialogs.scan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.text.InputFilter;
import android.widget.EditText;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.ScanActivity;

public class ManualInputDialog extends AlertDialog.Builder {
	public ManualInputDialog(final Context context) {
		super(context);
		
		final ScanActivity scanActivity = (ScanActivity) context;
		scanActivity.setRunning(false);
		
		AlertDialog.Builder alertDialogBuilder;
		alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_manual_input));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_manual_input));

		final EditText input = new EditText(context);

		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(13);
		input.setFilters(filterArray);
		input.setRawInputType(Configuration.KEYBOARD_QWERTY);
		input.setSingleLine(true);
		alertDialogBuilder.setView(input);
		
		alertDialogBuilder.setPositiveButton(context.getString(R.string.dialog_btn_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String barcode = input.getText().toString();

						scanActivity.processBarcode(barcode);
						scanActivity.setRunning(true);
					}
				});

		alertDialogBuilder.setNegativeButton(context.getString(R.string.dialog_btn_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						scanActivity.setRunning(true);
					}
				});
	}
}
