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
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.scan.AlreadySeenDialog;
import com.paylogic.scanwarelite.dialogs.scan.BarcodeNotFoundDialog;
import com.paylogic.scanwarelite.dialogs.scan.DisabledProductDialog;
import com.paylogic.scanwarelite.dialogs.scan.InitCameraDialog;
import com.paylogic.scanwarelite.dialogs.scan.InvalidBarcodeDialog;
import com.paylogic.scanwarelite.dialogs.scan.InvalidBarcodeLengthDialog;
import com.paylogic.scanwarelite.dialogs.scan.ManualInputDialog;
import com.paylogic.scanwarelite.dialogs.scan.PaymentErrorDialog;
import com.paylogic.scanwarelite.dialogs.scan.ValidProductDialog;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;
import com.paylogic.scanwarelite.views.CameraPreview;

public class ScanActivity extends CommonActivity {
	static {
		System.loadLibrary("iconv");
	}

	private TextView startScanningView;
	private CameraPreview mPreview;
	private Camera mCamera;
	private ImageScanner scanner;
	private InitCameraTask initCameraTask;
	private MediaPlayer mediaPlayer;
	private boolean hasFlashlight;
	private boolean flashlightEnabled;
	private String barcode;
	private boolean scanning = false;
	private Handler autoFocusHandler;
	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (mCamera != null) {
				mCamera.autoFocus(autoFocusCB);
			}
		}
	};

	private AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	private PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (scanning) {
				Camera.Parameters parameters = camera.getParameters();
				Size size = parameters.getPreviewSize();

				Image barcodeImage = new Image(size.width, size.height, "Y800");
				barcodeImage.setData(data);

				int result = scanner.scanImage(barcodeImage);
				if (result != 0) {
					stopScanning();

					SymbolSet syms = scanner.getResults();
					for (Symbol sym : syms) {
						barcode = sym.getData();
						// Im only interested in the first result, but there is
						// no way to index SymbolSet
						break;
					}
					processBarcode(barcode);

				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		hasFlashlight = getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FLASH);

	}

	@Override
	protected void onResume() {
		super.onResume();
		initCameraTask = new InitCameraTask();
		initCameraTask.execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
		startScanningView.setVisibility(View.GONE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_scan, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem flashlightMenuItem = menu.findItem(R.id.menu_flashlight);
		MenuItem manualInputMenuitem = menu.findItem(R.id.menu_manual_input);
		MenuItem pauseScanningMenuItem = menu
				.findItem(R.id.menu_pause_scanning);

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

		if (!scanning) {
			pauseScanningMenuItem.setVisible(false);
			manualInputMenuitem.setVisible(false);

		} else {
			pauseScanningMenuItem.setVisible(true);
			manualInputMenuitem.setVisible(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_manual_input:
			alertDialog = new ManualInputDialog(ScanActivity.this);
			alertDialog.show();
			break;

		case R.id.menu_settings:
			flashlightEnabled = false;
			intent = new Intent(ScanActivity.this,
					SettingsActivity.class);
			startActivity(intent);
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

		case R.id.menu_pause_scanning:
			stopScanning();
			startScanningView.setVisibility(View.VISIBLE);
			break;

		case R.id.menu_view_statistics:
			intent = new Intent(ScanActivity.this,
					StatisticsActivity.class);
			startActivity(intent);
		}

		return true;
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
			mPreview.getHolder().removeCallback(mPreview);
			mCamera.release();
			mCamera = null;
		}
	}

	public void dismissDialog() {
		alertDialog.dismiss();
	}

	public void startScanning() {
		scanning = true;
		// mCamera.setPreviewCallback(previewCb);
		invalidateOptionsMenu();
	}

	public void stopScanning() {
		scanning = false;
		// mCamera.setPreviewCallback(null);
		// mPreview.getHolder().removeCallback(mPreview);
		invalidateOptionsMenu();
	}

	public void processBarcode(String barcode) {
		if (barcode.length() == 13) {
			if (!validChecksum(barcode)) {
				alertDialog = new InvalidBarcodeDialog(ScanActivity.this,
						barcode);
				alertDialog.show();

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
				alertDialog = new BarcodeNotFoundDialog(ScanActivity.this,
						barcode);
				alertDialog.show();

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
				if (payStatus != 102) {
					alertDialog = new PaymentErrorDialog(ScanActivity.this,
							barcode);
					alertDialog.show();

					mediaPlayer = MediaPlayer.create(ScanActivity.this,
							R.raw.invalid);
					mediaPlayer.start();
					return;
				}

				boolean seen = cursor
						.getInt(cursor
								.getColumnIndex(ScanwareLiteOpenHelper.BARCODES_KEY_SEEN)) > 0;
				if (seen) {
					// already seen error
					String seenDate = cursor
							.getString(cursor
									.getColumnIndex(ScanwareLiteOpenHelper.BARCODES_KEY_SEENDATE));

					alertDialog = new AlreadySeenDialog(ScanActivity.this,
							barcode, seenDate);
					alertDialog.show();

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

					alertDialog = new DisabledProductDialog(ScanActivity.this,
							barcode, product);
					alertDialog.show();

					mediaPlayer = MediaPlayer.create(ScanActivity.this,
							R.raw.invalid);
					mediaPlayer.start();
					return;
				} else {
					// Ticket OK!
					String name = cursor
							.getString(cursor
									.getColumnIndex(ScanwareLiteOpenHelper.BARCODES_KEY_NAME));

					alertDialog = new ValidProductDialog(ScanActivity.this,
							barcode, product, name);
					alertDialog.show();

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
			alertDialog = new InvalidBarcodeLengthDialog(ScanActivity.this);
			alertDialog.show();
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

	private class InitCameraTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressDialog = new InitCameraDialog(ScanActivity.this);
			progressDialog.show();
			setContentView(R.layout.activity_scan);
			startScanningView = (TextView) findViewById(R.id.textView_start_scanning);
		}

		@Override
		protected void onPostExecute(Void result) {

			startScanningView = (TextView) findViewById(R.id.textView_start_scanning);

			autoFocusHandler = new Handler();

			progressDialog.dismiss();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mCamera = getCameraInstance();

			scanner = new ImageScanner();
			scanner.setConfig(0, Config.X_DENSITY, 3);
			scanner.setConfig(0, Config.Y_DENSITY, 3);
			runOnUiThread(new Runnable() {
				public void run() {
					FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
					mPreview = new CameraPreview(ScanActivity.this, mCamera,
							previewCb, autoFocusCB);

					preview.addView(mPreview);
					mCamera.startPreview();
					if (!scanning) {
						startScanningView.setVisibility(View.VISIBLE);
					}
					mPreview.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (!scanning) {
								Toast.makeText(ScanActivity.this,
										"Starting to scan", Toast.LENGTH_LONG)
										.show();
								startScanning();
								startScanningView.setVisibility(View.GONE);
							}
						}
					});

				}
			});

			return null;
		}

	}
}
