package com.paylogic.scanwarelite.activities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.paylogic.scanwarelite.APIFacade;
import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.events.DownloadDialog;
import com.paylogic.scanwarelite.dialogs.events.DownloadSpqDialog;
import com.paylogic.scanwarelite.dialogs.events.Error500Dialog;
import com.paylogic.scanwarelite.dialogs.events.InsufficientStorageDialog;
import com.paylogic.scanwarelite.dialogs.events.OnlyReuseDialog;
import com.paylogic.scanwarelite.dialogs.events.OverwriteDialog;
import com.paylogic.scanwarelite.dialogs.events.ReuseOrOverwriteDialog;
import com.paylogic.scanwarelite.helpers.ConnectivityHelper;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;
import com.paylogic.scanwarelite.models.Event;
import com.paylogic.scanwarelite.models.User;
import com.paylogic.scanwarelite.tasks.GetEventsTask;
import com.paylogic.scanwarelite.utils.FileUtils;

public class EventsActivity extends CommonActivity {

	private ListView eventsView;
	private Button retryButton;
	private Button continueButton;

	private EventsAdapter eventsAdapter;
	private Event selectedEvent;

	private GetEventsTask eventsTask;
	private Event existingEvent;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private User user;
	private ArrayList<Event> events;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);

		setConnectivityHelper(new ConnectivityHelper());
		setAPIFacade(new APIFacade());
		settings = getSharedPreferences(PreferenceHelper.PREFS_FILE,
				Context.MODE_PRIVATE);
		events = new ArrayList<Event>();
		user = app.getUser();

		eventsView = (ListView) findViewById(R.id.listView_events);
		retryButton = (Button) findViewById(R.id.button_retry);
		continueButton = (Button) findViewById(R.id.button_event_continue);
		eventsAdapter = new EventsAdapter(EventsActivity.this,
				android.R.layout.simple_list_item_1, events);
		eventsView.setAdapter(eventsAdapter);

		updateEvents(apiFacade);
	}

	protected void onResume() {
		super.onResume();
		eventsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectedEvent = eventsAdapter.getItem(position);
				eventsAdapter.setSelectedIndex(position);

				// Disable continue button for all events but the local event if
				// internet connectivity is lost
				if (!connHelper.isConnected(EventsActivity.this)
						&& !(selectedEvent.getId() == existingEvent.getId())) {
					continueButton.setClickable(false);
				} else {
					continueButton.setClickable(true);
				}
			}
		});

		retryButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updateEvents(apiFacade);
			}
		});

		continueButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (selectedEvent != null) {
					if ((existingEvent != null)
							&& (selectedEvent.getId() == existingEvent.getId())) {
						if (!connHelper.isConnected(EventsActivity.this)) {
							alertDialog = new OnlyReuseDialog(
									EventsActivity.this);
							alertDialog.show();
						} else {
							alertDialog = new ReuseOrOverwriteDialog(
									EventsActivity.this, selectedEvent);

							alertDialog.show();

						}
					} else if (scanwareLiteOpenHelper.databaseExists()) {
						alertDialog = new OverwriteDialog(EventsActivity.this,
								selectedEvent);
						alertDialog.show();
					} else {
						alertDialog = new DownloadDialog(EventsActivity.this,
								selectedEvent);
						alertDialog.show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							R.string.toast_select_event, Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_events, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem toggleEventsMenuItem = menu.findItem(R.id.menu_toggle_events);
		if (settings.getBoolean("showAll", false)) {
			toggleEventsMenuItem
					.setTitle(getString(R.string.menu_item_show_active_events));
		} else {
			toggleEventsMenuItem
					.setTitle(getString(R.string.menu_item_show_all_events));
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent intent = new Intent(EventsActivity.this,
					SettingsActivity.class);
			startActivity(intent);
			break;
		case R.id.menu_toggle_events:
			editor = settings.edit();
			if (settings.getBoolean("showAll", false)) {
				editor.putBoolean(PreferenceHelper.KEY_SHOW_ALL, false);
			} else {
				editor.putBoolean(PreferenceHelper.KEY_SHOW_ALL, true);
			}
			editor.commit();
			updateEvents(apiFacade);
			break;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		// Do not return to login activity
	}

	private void updateEvents(APIFacade apiFacade) {

		eventsTask = new GetEventsTask(EventsActivity.this, apiFacade, user,
				eventsAdapter, scanwareLiteOpenHelper, connHelper,
				settings.getBoolean("showAll", false));
		eventsTask.execute();
		eventsAdapter.setSelectedIndex(-1);
		selectedEvent = null;

	}

	public void setExistingEvent(Event event) {
		existingEvent = event;
	}

	public class EventsAdapter extends ArrayAdapter<Event> {
		private int selectedIndex = -1;
		private Event localEvent;

		public EventsAdapter(Context context, int resource,
				ArrayList<Event> events) {
			super(context, resource, events);
		}

		public void setSelectedIndex(int index) {
			selectedIndex = index;
			notifyDataSetChanged();
		}

		public ArrayList<Event> getData() {
			return events;
		}

		public void setLocalEvent(Event localEvent) {
			localEvent.setName(localEvent.getName() + "*");
			events.add(localEvent);
			this.localEvent = localEvent;
		}

		public void addEvent(Event event) {
			if (event.getId() == localEvent.getId()) {
				localEvent.setDeadline(event.getDeadline());
				localEvent.setEndDate(event.getEndDate());
			} else {
				events.add(event);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			LayoutParams params;
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.view_events_item, null);
			}
			Event event = events.get(position);

			String eventName = event.getName();
			Date eventEndDate = event.getEndDate();
			String dateString = null;
			if (eventEndDate != null) {
				DateFormat df = new SimpleDateFormat("dd-MM-yyyy");

				dateString = df.format(eventEndDate);
			}
			TextView eventNameView = (TextView) v
					.findViewById(R.id.textView_eventName);
			TextView eventDateView = (TextView) v
					.findViewById(R.id.textView_eventDate);

			if (selectedIndex != -1 && position == selectedIndex) {
				v.setBackgroundColor(resources.getColor(R.color.selectedEvent));
			} else {
				v.setBackgroundColor(Color.TRANSPARENT);
			}
			if (event != null) {

				eventNameView.setText(eventName);
				if (dateString != null) {
					eventDateView.setText("(" + dateString + ")");
				}
				params = new LayoutParams(
						(int) (parent.getMeasuredWidth() * 0.7),
						LayoutParams.WRAP_CONTENT);
				eventNameView.setLayoutParams(params);

				params = new LayoutParams(
						(int) (parent.getMeasuredWidth() * 0.3),
						LayoutParams.MATCH_PARENT);
				eventDateView.setLayoutParams(params);

			}
			return v;
		}

	}

	public class DownloadSpqTask extends AsyncTask<Void, Integer, Void> {
		private String url = "https://api.paylogic.nl/API/?command=";
		private String fileName = "event.spq";

		private String username;
		private String password;
		private Event event;

		private boolean error = false;
		private int errorCode = -1;
		private static final int NOT_ENOUGH_DISK_SPACE = -1;

		public DownloadSpqTask(String username, String password, Event event) {
			this.username = username;
			this.password = password;
			this.event = event;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new DownloadSpqDialog(EventsActivity.this);
			progressDialog.show();

			deleteDatabase(ScanwareLiteOpenHelper.DATABASE_NAME);
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			if (!error) {
				Intent intent = new Intent(EventsActivity.this,
						ProductsActivity.class);
				startActivity(intent);
			} else {
				if (errorCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					alertDialog = new Error500Dialog(EventsActivity.this);

					alertDialog.show();
				} else if (errorCode == NOT_ENOUGH_DISK_SPACE) {
					alertDialog = new InsufficientStorageDialog(
							EventsActivity.this);
					alertDialog.show();
				}
			}
		}

		@Override
		protected Void doInBackground(Void... args) {

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
			wl.acquire();

			String command = "sparqCodeList";
			// TODO: remove nocrypt, implement decrypt
			String urlParams = "&username=" + username + "&password="
					+ password + "&mmid=" + event.getId() + "&nocrypt";

			String spqUrl = url + command + urlParams;
			try {

				URL url = new URL(spqUrl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					boolean downloaded = downloadSpqFile(conn);

					if (downloaded) {
						String filePath = getFilesDir().getPath() + "/"
								+ fileName;
						scanwareLiteOpenHelper.importDatabase(filePath);
						deleteFile(fileName);
					}

				} else if (conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					publishProgress(HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// TODO: WHYYYYYYYYYY?!?!
				// http://code.google.com/p/android/issues/detail?id=43212
				wl.release();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			if (values[0] == HttpURLConnection.HTTP_OK) {
				progressDialog
						.setTitle(getString(R.string.dialog_title_downloading_event_data)
								+ event.getId());
				progressDialog.setMessage(String.format(
						getString(R.string.dialog_msg_downloading_event_data),
						values[1] / 1000));

				progressDialog.setIndeterminate(false);
			} else if (values[0] == HttpURLConnection.HTTP_INTERNAL_ERROR) {
				error = true;
				errorCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
			} else if (values[0] == NOT_ENOUGH_DISK_SPACE) {
				error = true;
				errorCode = NOT_ENOUGH_DISK_SPACE;
			} else {
				progressDialog.setProgress(values[0]);
			}
		}

		private boolean downloadSpqFile(HttpURLConnection conn)
				throws IOException {
			int fileLength = conn.getContentLength();
			long availableDiskSpace = FileUtils.availableDiskSpace();

			if (fileLength < availableDiskSpace) {
				publishProgress(HttpURLConnection.HTTP_OK, fileLength);

				InputStream input = conn.getInputStream();
				OutputStream output = openFileOutput(fileName,
						Context.MODE_PRIVATE);

				byte data[] = new byte[4096];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					publishProgress((int) (total * 100 / fileLength));
					output.write(data, 0, count);
				}
				return true;
			} else {
				publishProgress(NOT_ENOUGH_DISK_SPACE);
				return false;
			}
		}

	}
}
