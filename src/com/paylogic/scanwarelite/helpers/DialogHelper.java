package com.paylogic.scanwarelite.helpers;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.InputFilter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.ScanActivity;
import com.paylogic.scanwarelite.models.Event;

public class DialogHelper {
	public static final int DIALOG_POSITIVE_BUTTON_LISTENER = 300;
	public static final int DIALOG_NEUTRAL_BUTTON_LISTENER = 301;
	public static final int DIALOG_NEGATIVE_BUTTON_LISTENER = 302;
	public static final int VIEW_ON_CLICK_LISTENER = 303;
	
	// Dialogs used in menus
	public static final int EXIT_DIALOG = 16;
	public static final int MANUAL_INPUT_DIALOG = 17;

	// Dialogs used in SettingsActivity
	public static final int DELETE_DATA_DIALOG = 18;

	// Dialogs used in ScanActivity
	public static final int INVALID_BARCODE_DIALOG = 19;
	public static final int BARCODE_NOT_FOUND_DIALOG = 20;
	public static final int PAYMENT_ERROR_DIALOG = 21;
	public static final int ALREADY_SEEN_DIALOG = 22;
	public static final int INIT_CAMERA_DIALOG = 23;


	public static AlertDialog createAlertDialogById(Context context, int id) {
		return createAlertDialogById(context, id, null, null);
	}

	public static AlertDialog createAlertDialogById(Context context, int id,
			Event event) {
		return createAlertDialogById(context, id, null, event);
	}

	public static AlertDialog createAlertDialogById(Context context, int id,
			SparseArray<OnClickListener> dialogHandlers) {
		return createAlertDialogById(context, id, dialogHandlers, null);
	}

	public static AlertDialog createAlertDialogById(Context context, int id,
			SparseArray<OnClickListener> dialogHandlers, Event event) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		switch (id) {
		case EXIT_DIALOG:
			alertDialogBuilder = createExitDialog(context, dialogHandlers,
					alertDialogBuilder);
			break;
		case INIT_CAMERA_DIALOG:
			alertDialogBuilder = createInitCameraDialog(context,
					alertDialogBuilder);
			break;
		case MANUAL_INPUT_DIALOG:
			alertDialogBuilder = createManualInputDialog(context);
			break;

		case DELETE_DATA_DIALOG:
			alertDialogBuilder = createDeleteDataDialog(context,
					dialogHandlers, alertDialogBuilder);
			break;
		}
		return alertDialogBuilder.create();
	}

	private static AlertDialog.Builder createInitCameraDialog(Context context,
			Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_loading_camera));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_loading_camera));
		return alertDialogBuilder;
	}

	public static AlertDialog createInvalidScanAlertDialog(Context context,
			int id, SparseArray<View.OnClickListener> handlers, String barcode) {
		return createInvalidScanAlertDialog(context, id, handlers, barcode,
				null);
	}

	public static AlertDialog createInvalidScanAlertDialog(Context context,
			int id, SparseArray<View.OnClickListener> viewHandlers,
			String barcode, String seenDate) {

		Resources resources = context.getResources();
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		View scanDialogView = li.inflate(R.layout.barcode_dialog_view, null);

		TextView scanResultTitleView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_title);
		TextView scanResultBarcodeView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_barcode);
		TextView scanResultMessageView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_message);

		scanDialogView.setBackgroundColor(resources
				.getColor(R.color.scanInvalid));
		scanResultTitleView.setText(context
				.getString(R.string.tv_scan_dialog_title_invalid));
		scanResultBarcodeView.setText(String.format(context.getString(
				R.string.tv_scan_dialog_barcode, barcode)));

		scanDialogView.setOnClickListener(viewHandlers
				.get(VIEW_ON_CLICK_LISTENER));

		alertDialogBuilder.setView(scanDialogView);

		switch (id) {
		case INVALID_BARCODE_DIALOG:
			scanResultMessageView.setText(context
					.getString(R.string.tv_scan_dialog_msg_invalid_checksum));
			return alertDialogBuilder.create();

		case BARCODE_NOT_FOUND_DIALOG:
			scanResultMessageView.setText(context
					.getString(R.string.tv_scan_dialog_msg_not_found));
			return alertDialogBuilder.create();

		case PAYMENT_ERROR_DIALOG:
			scanResultMessageView.setText(context
					.getString(R.string.tv_scan_dialog_msg_payment_error));
			return alertDialogBuilder.create();

		case ALREADY_SEEN_DIALOG:
			scanResultMessageView.setText(String.format(context
					.getString(R.string.tv_scan_dialog_msg_already_scanned),
					seenDate));
			return alertDialogBuilder.create();
		default:
			return null;
		}
	}

	public static AlertDialog createDisabledScanAlertDialog(Context context,
			SparseArray<View.OnClickListener> handlers, String barcode,
			String product) {

		Resources resources = context.getResources();
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		View scanDialogView = li.inflate(R.layout.barcode_dialog_view, null);

		TextView scanResultTitleView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_title);
		TextView scanResultBarcodeView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_barcode);
		TextView scanResultMessageView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_message);

		scanDialogView.setBackgroundColor(resources
				.getColor(R.color.scanDisabled));
		scanResultTitleView.setText(context
				.getString(R.string.tv_scan_dialog_title_disabled));
		scanResultBarcodeView.setText(String.format(context.getString(
				R.string.tv_scan_dialog_barcode, barcode)));
		scanResultMessageView
				.setText(String.format(
						context.getString(R.string.tv_scan_dialog_msg_product_disabled),
						product));
		scanDialogView.setOnClickListener(handlers.get(VIEW_ON_CLICK_LISTENER));

		alertDialogBuilder.setView(scanDialogView);
		return alertDialogBuilder.create();
	}

	public static AlertDialog createValidScanAlertDialog(Context context,
			SparseArray<View.OnClickListener> handlers, String barcode,
			String product, String name) {

		Resources resources = context.getResources();
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		View scanDialogView = li.inflate(R.layout.barcode_dialog_view, null);

		TextView scanResultTitleView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_title);
		TextView scanResultBarcodeView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_barcode);
		TextView scanResultMessageView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_message);

		scanDialogView.setBackgroundColor(resources.getColor(R.color.scanOK));
		scanResultTitleView.setText(context
				.getString(R.string.tv_scan_dialog_title_OK));
		scanResultBarcodeView.setText(String.format(context.getString(
				R.string.tv_scan_dialog_barcode, barcode)));
		scanResultMessageView.setText(String.format(
				context.getString(R.string.tv_scan_dialog_msg_OK), name,
				product));
		scanDialogView.setOnClickListener(handlers.get(VIEW_ON_CLICK_LISTENER));

		alertDialogBuilder.setView(scanDialogView);
		return alertDialogBuilder.create();
	}

	private static AlertDialog.Builder createDeleteDataDialog(Context context,
			SparseArray<OnClickListener> dialogHandlers,
			AlertDialog.Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_delete_data));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_delete_data));
		alertDialogBuilder.setPositiveButton(
				context.getString(R.string.dialog_btn_ok),
				dialogHandlers.get(DIALOG_POSITIVE_BUTTON_LISTENER));
		alertDialogBuilder.setNegativeButton(
				context.getString(R.string.dialog_btn_cancel),
				dialogHandlers.get(DIALOG_NEGATIVE_BUTTON_LISTENER));
		return alertDialogBuilder;
	}

	private static AlertDialog.Builder createManualInputDialog(Context context) {
		final ScanActivity scannActivity = (ScanActivity) context;
		scannActivity.setRunning(false);
		
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
		
		alertDialogBuilder.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String barcode = input.getText().toString();

						scannActivity.processBarcode(barcode);
						scannActivity.setRunning(true);
					}
				});

		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						scannActivity.setRunning(true);
					}
				});
		return alertDialogBuilder;
	}

	private static AlertDialog.Builder createExitDialog(Context context,
			SparseArray<OnClickListener> dialogHandlers,
			AlertDialog.Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_confirm_exit));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_confirm_exit));
		alertDialogBuilder.setPositiveButton(
				context.getString(R.string.dialog_btn_exit),
				dialogHandlers.get(DIALOG_POSITIVE_BUTTON_LISTENER));
		alertDialogBuilder.setNegativeButton(
				context.getString(R.string.dialog_btn_cancel),
				dialogHandlers.get(DIALOG_NEGATIVE_BUTTON_LISTENER));
		return alertDialogBuilder;
	}

}
