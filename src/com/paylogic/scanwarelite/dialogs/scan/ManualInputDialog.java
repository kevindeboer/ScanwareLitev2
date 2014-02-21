package com.paylogic.scanwarelite.dialogs.scan;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.widget.EditText;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.ScanActivity;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class ManualInputDialog extends CommonAlertDialog {
	public ManualInputDialog(final Context context) {
		super(context);

		final ScanActivity scanActivity = (ScanActivity) context;
		scanActivity.stopScanning();

		setTitle(context.getString(R.string.dialog_title_manual_input));
		setMessage(context.getString(R.string.dialog_msg_manual_input));

		final EditText input = new EditText(context);

		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(13);
		input.setFilters(filterArray);
		input.setRawInputType(Configuration.KEYBOARD_QWERTY);
		input.setSingleLine(true);
		setView(input);

		setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_btn_ok),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						String barcodeString = input.getText().toString();

						scanActivity.processBarcode(barcodeString);
					}
				});

		setButton(BUTTON_NEGATIVE,
				context.getString(R.string.dialog_btn_cancel),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						scanActivity.startScanning();
					}
				});

		setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if ((keyCode == KeyEvent.KEYCODE_BACK)) {
					dialog.cancel();
					scanActivity.startScanning();
					return true;
				}
				return false;
			}
		});

	}
}
