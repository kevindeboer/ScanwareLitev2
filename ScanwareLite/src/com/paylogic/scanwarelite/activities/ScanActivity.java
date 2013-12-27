package com.paylogic.scanwarelite.activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paylogic.scanwarelite.PaymentCode;
import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.helpers.DialogHelper;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;
import com.paylogic.scanwarelite.views.CameraPreview;

public class ScanActivity extends Activity {
	static {
		System.loadLibrary("iconv");
	}
	private Camera mCamera;
	private CameraPreview mPreview;
	private TextView startScanningView;
	private AlertDialog.Builder alertDialogBuilder;
	private AlertDialog alertDialog;
	private Dialog scanDialog;
	private SparseArray<View.OnClickListener> viewHandlers = new SparseArray<View.OnClickListener>();
	private SparseArray<DialogInterface.OnClickListener> dialogHandlers = new SparseArray<DialogInterface.OnClickListener>();
	private DialogInterface.OnClickListener positiveButtonListener;
	private DialogInterface.OnClickListener negativeButtonListener;
	private Resources resources;

	private boolean running = false;

	private ImageScanner scanner;
	private Handler autoFocusHandler;

	private boolean hasFlashlight;
	private boolean flashlightEnabled = false;

	private String barcode;
	private MediaPlayer mediaPlayer;
	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (mCamera != null) {
				mCamera.autoFocus(autoFocusCB);
			}
		}
	};

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
	private PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (running) {
				Camera.Parameters parameters = camera.getParameters();
				Size size = parameters.getPreviewSize();

				Image barcodeImage = new Image(size.width, size.height, "Y800");
				barcodeImage.setData(data);

				int result = scanner.scanImage(barcodeImage);
				if (result != 0) {
					running = false;
					mCamera.setPreviewCallback(null);

					SymbolSet syms = scanner.getResults();
					for (Symbol sym : syms) {
						barcode = sym.getData();
						// Im only interested in the first result, but there is
						// no
						// way to index SymbolSet
						break;
					}
					processBarcode(barcode);

				}
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		hasFlashlight = getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FLASH);
		startScanningView = (TextView) findViewById(R.id.textView_start_scanning);

		mCamera = getCameraInstance();
		autoFocusHandler = new Handler();
		resources = getResources();

		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);

		mPreview = new CameraPreview(ScanActivity.this, mCamera, previewCb,
				autoFocusCB);

		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

	}

	protected void onResume() {
		super.onResume();
		mPreview.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!running) {
					Toast.makeText(ScanActivity.this, "Starting to scan",
							Toast.LENGTH_LONG).show();
					running = true;
					startScanningView.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_scan, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem flashlightMenuItem = menu.findItem(R.id.menu_flashlight);
		MenuItem manualInputMenuitem = menu.findItem(R.id.menu_manual_input);

		if (hasFlashlight) {
			if (!flashlightEnabled) {
				flashlightMenuItem.setTitle(resources
						.getString(R.string.menu_item_enable_flashlight));
			} else {
				flashlightMenuItem.setTitle(resources
						.getString(R.string.menu_item_disable_flashlight));
			}
		} else {
			menu.removeItem(R.id.menu_flashlight);
		}

		if (!running) {
			manualInputMenuitem.setVisible(false);
		} else {
			manualInputMenuitem.setVisible(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_manual_input:
			running = false;
			alertDialogBuilder = new AlertDialog.Builder(ScanActivity.this);
			alertDialogBuilder
					.setTitle(getString(R.string.dialog_title_manual_input));
			alertDialogBuilder
					.setMessage(getString(R.string.dialog_msg_manual_input));

			final EditText input = new EditText(this);

			InputFilter[] filterArray = new InputFilter[1];
			filterArray[0] = new InputFilter.LengthFilter(13);
			input.setFilters(filterArray);
			input.setRawInputType(Configuration.KEYBOARD_QWERTY);
			input.setSingleLine(true);
			alertDialogBuilder.setView(input);
			alertDialogBuilder.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							barcode = input.getText().toString();

							processBarcode(barcode);
							running = true;
						}
					});

			alertDialogBuilder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							running = true;
						}
					});
			alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			break;

		case R.id.menu_logout:
			intent = new Intent(ScanActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
			break;

		case R.id.menu_settings:
			intent = new Intent(ScanActivity.this, SettingsActivity.class);
			intent.putExtra("previousActivity", getClass().getName());
			startActivity(intent);
			finish();
			break;

		case R.id.menu_flashlight:

			Parameters p = mCamera.getParameters();
			if (!flashlightEnabled) {
				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				flashlightEnabled = true;
			} else {
				p.setFlashMode(Parameters.FLASH_MODE_OFF);
				flashlightEnabled = false;
			}
			mCamera.setParameters(p);

			break;

		case R.id.menu_exit:
			positiveButtonListener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					finish();
				}
			};
			negativeButtonListener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			};

			dialogHandlers.put(DialogHelper.DIALOG_POSITIVE_BUTTON_LISTENER,
					positiveButtonListener);
			dialogHandlers.put(DialogHelper.DIALOG_NEGATIVE_BUTTON_LISTENER,
					negativeButtonListener);

			alertDialog = DialogHelper.createAlertDialogById(ScanActivity.this,
					DialogHelper.EXIT_DIALOG, dialogHandlers);
			alertDialog.show();
			break;
		}

		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;

		}
	}

	public void processBarcode(String barcode) {
		View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				scanDialog.dismiss();
				running = true;
				mCamera.setPreviewCallback(previewCb);
			}
		};
		viewHandlers.put(DialogHelper.VIEW_ON_CLICK_LISTENER, onClickListener);

		if (barcode.length() == 13) {
			if (!validChecksum(barcode)) {
				scanDialog = DialogHelper.createInvalidScanAlertDialog(
						ScanActivity.this, DialogHelper.INVALID_BARCODE_DIALOG,
						viewHandlers, barcode);
				scanDialog.show();

				mediaPlayer = MediaPlayer.create(ScanActivity.this,
						R.raw.invalid);
				mediaPlayer.start();
				return;

			} else {
				barcode = barcode.substring(0, barcode.length() - 1);
			}
		}

		if (barcode.length() == 12) {
			String query = "";
			Cursor cursor;

			ScanwareLiteOpenHelper scanwareLiteOpenHelper = new ScanwareLiteOpenHelper(
					ScanActivity.this, ScanwareLiteOpenHelper.DATABASE_NAME,
					null, ScanwareLiteOpenHelper.DATABASE_VERSION);

			SQLiteDatabase db = scanwareLiteOpenHelper.getWritableDatabase();

			query = "SELECT " + ScanwareLiteOpenHelper.BARCODES_KEY_CODE + ", "
					+ ScanwareLiteOpenHelper.BARCODES_KEY_NAME + ", "
					+ ScanwareLiteOpenHelper.BARCODES_KEY_PAYSTATUS + ", "
					+ ScanwareLiteOpenHelper.BARCODES_KEY_SEEN + ", "
					+ ScanwareLiteOpenHelper.BARCODES_KEY_SEENDATE + ", "
					+ ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED + ", "
					+ ScanwareLiteOpenHelper.PRODUCTS_KEY_TITLE + " FROM "
					+ ScanwareLiteOpenHelper.BARCODES_TABLE + ", "
					+ ScanwareLiteOpenHelper.PRODUCTS_TABLE
					+ " WHERE barcodes.productID = products.id and code = ?";
			cursor = db.rawQuery(query, new String[] { barcode });

			if (cursor.getCount() == 0) {
				// barcode not found error
				scanDialog = DialogHelper.createInvalidScanAlertDialog(
						ScanActivity.this,
						DialogHelper.BARCODE_NOT_FOUND_DIALOG, viewHandlers,
						barcode);
				scanDialog.show();

				mediaPlayer = MediaPlayer.create(ScanActivity.this,
						R.raw.invalid);
				mediaPlayer.start();
				return;

			} else {
				cursor.moveToFirst();
				int payStatus = cursor
						.getInt(cursor
								.getColumnIndex(ScanwareLiteOpenHelper.BARCODES_KEY_PAYSTATUS));

				// TODO: Make this more specific
				for (PaymentCode payCode : PaymentCode.values()) {
					if (payCode.getCode() == payStatus) {
						// payment error
						scanDialog = DialogHelper.createInvalidScanAlertDialog(
								ScanActivity.this,
								DialogHelper.PAYMENT_ERROR_DIALOG,
								viewHandlers, barcode);
						scanDialog.show();

						mediaPlayer = MediaPlayer.create(ScanActivity.this,
								R.raw.invalid);
						mediaPlayer.start();
						return;
					}
				}

				boolean seen = cursor
						.getInt(cursor
								.getColumnIndex(ScanwareLiteOpenHelper.BARCODES_KEY_SEEN)) > 0;
				if (seen) {
					// already seen error
					String seenDate = cursor
							.getString(cursor
									.getColumnIndex(ScanwareLiteOpenHelper.BARCODES_KEY_SEENDATE));

					scanDialog = DialogHelper.createInvalidScanAlertDialog(
							ScanActivity.this,
							DialogHelper.ALREADY_SEEN_DIALOG, viewHandlers,
							barcode, seenDate);
					scanDialog.show();

					mediaPlayer = MediaPlayer.create(ScanActivity.this,
							R.raw.invalid);
					mediaPlayer.start();
					return;
				}

				boolean allowed = cursor
						.getInt(cursor
								.getColumnIndex(ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED)) > 0;
				String product = cursor
						.getString(cursor
								.getColumnIndex(ScanwareLiteOpenHelper.PRODUCTS_KEY_TITLE));

				if (!allowed) {
					// filtered product error

					scanDialog = DialogHelper.createDisabledScanAlertDialog(
							ScanActivity.this, viewHandlers, barcode, product);
					scanDialog.show();

					mediaPlayer = MediaPlayer.create(ScanActivity.this,
							R.raw.invalid);
					mediaPlayer.start();
					return;
				} else {
					// Ticket OK!
					String name = cursor
							.getString(cursor
									.getColumnIndex(ScanwareLiteOpenHelper.BARCODES_KEY_NAME));

					scanDialog = DialogHelper.createValidScanAlertDialog(
							ScanActivity.this, viewHandlers, barcode, product,
							name);
					scanDialog.show();

					mediaPlayer = MediaPlayer.create(ScanActivity.this,
							R.raw.valid);
					mediaPlayer.start();

					Date now = new Date();

					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String timeStamp = df.format(now);

					ContentValues values = new ContentValues();
					values.put(ScanwareLiteOpenHelper.BARCODES_KEY_SEEN, true);
					values.put(ScanwareLiteOpenHelper.BARCODES_KEY_SEENDATE,
							timeStamp);

					String whereClause = ScanwareLiteOpenHelper.BARCODES_KEY_CODE
							+ "= ?";
					String[] whereArgs = { barcode };

					db.update(ScanwareLiteOpenHelper.BARCODES_TABLE, values,
							whereClause, whereArgs);

					return;
				}
			}

		} else {
			// invalid barcode length
		}
	}

	private boolean validChecksum(String barcode) {
		// If barcode is not 13 characters/digits long
		if (barcode.length() != 13) {
			return false;
		}
		// Sort the numbers by even and odd indexes, without the checksum digit
		ArrayList<Integer> oddIndexNumbers = new ArrayList<Integer>();
		ArrayList<Integer> evenIndexNumbers = new ArrayList<Integer>();
		for (int i = 0; i < 12; i++) {
			if (i % 2 == 0) {
				evenIndexNumbers.add(Character.getNumericValue(barcode
						.charAt(i)));
			} else {
				oddIndexNumbers
						.add(Character.getNumericValue(barcode.charAt(i)));
			}
		}
		// Add all odd numbers, multiply by 3
		int addedOdds = 0;
		for (int i = 0; i < evenIndexNumbers.size(); i++) {
			addedOdds += oddIndexNumbers.get(i);
		}
		int multipliedAddedOdds = addedOdds * 3;
		// Add all even numbers
		int addedEvens = 0;
		for (int i = 0; i < evenIndexNumbers.size(); i++) {
			addedEvens += evenIndexNumbers.get(i);
		}

		// Add the results of previous 2 steps
		int total = addedEvens + multipliedAddedOdds;
		// Subtracts the above result from the next multiple of 10
		int nextTen = (int) (Math.ceil((double) total / 10) * 10);

		int checksum = nextTen - total;

		// Assert if checksum is correct
		if (Character.getNumericValue(barcode.charAt(12)) == checksum) {
			return true;
		} else {
			return false;
		}
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
