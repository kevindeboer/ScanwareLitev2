package com.paylogic.scanwarelite.dialogs.scan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.ScanActivity;

public class ScanAlertDialog extends AlertDialog {
	private ScanActivity scanActivity;
	private LayoutInflater li;

	private TextView scanResultTitleView;
	private TextView scanResultBarcodeView;
	private TextView scanResultMessageView;
	private View scanDialogView;

	private Resources resources;
	
	public ScanAlertDialog(Context context, String barcode, int colorId,
			String title, String message) {
		super(context);
		scanActivity = (ScanActivity) context;

		resources = context.getResources();

		li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		scanDialogView = li.inflate(R.layout.barcode_dialog_view, null);

		scanResultTitleView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_title);
		scanResultBarcodeView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_barcode);
		scanResultMessageView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_message);

		scanDialogView.setBackgroundColor(resources.getColor(colorId));
		
		scanResultBarcodeView.setText(String.format(context.getString(
				R.string.tv_scan_dialog_barcode, barcode)));

		scanResultTitleView.setText(title);

		scanResultMessageView.setText(message);

		scanDialogView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scanActivity.dismissDialog();
				scanActivity.startScanning();
			}
		});

		setView(scanDialogView);

//		setOnCancelListener(new OnCancelListener() {
//
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				dialog.dismiss();
//				scanActivity.startScanning();
//
//			}
//		});
	}
}
