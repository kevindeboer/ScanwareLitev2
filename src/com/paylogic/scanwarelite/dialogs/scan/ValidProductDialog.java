package com.paylogic.scanwarelite.dialogs.scan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.ScanActivity;

public class ValidProductDialog extends AlertDialog.Builder {
	private Resources resources;
	private LayoutInflater li;
	private TextView scanResultTitleView;
	private TextView scanResultBarcodeView;
	private TextView scanResultMessageView;
	private ScanActivity scanActivity;

	public ValidProductDialog(final Context context, String barcode,
			String product, String name) {
		super(context);
		scanActivity = (ScanActivity) context;

		resources = context.getResources();

		li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View scanDialogView = li.inflate(R.layout.barcode_dialog_view, null);
		scanDialogView.setBackgroundColor(resources.getColor(R.color.scanOK));
		
		scanResultTitleView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_title);
		scanResultBarcodeView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_barcode);
		scanResultMessageView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_message);

		scanResultTitleView.setText(context
				.getString(R.string.tv_scan_dialog_title_OK));
		scanResultBarcodeView.setText(String.format(context.getString(
				R.string.tv_scan_dialog_barcode, barcode)));
		scanResultMessageView.setText(String.format(
				context.getString(R.string.tv_scan_dialog_msg_OK), name,
				product));

		scanDialogView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scanActivity.dismissDialog();
				scanActivity.startScanning();
			}
		});

		setView(scanDialogView);
	}
}
