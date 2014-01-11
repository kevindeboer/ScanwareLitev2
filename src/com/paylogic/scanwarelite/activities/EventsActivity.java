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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
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

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.events.DownloadDialog;
import com.paylogic.scanwarelite.dialogs.events.DownloadSpqDialog;
import com.paylogic.scanwarelite.dialogs.events.Error500Dialog;
import com.paylogic.scanwarelite.dialogs.events.GetEventsDialog;
import com.paylogic.scanwarelite.dialogs.events.InsufficientStorageDialog;
import com.paylogic.scanwarelite.dialogs.events.NoResourcesDialog;
import com.paylogic.scanwarelite.dialogs.events.OnlyReuseDialog;
import com.paylogic.scanwarelite.dialogs.events.OverwriteDialog;
import com.paylogic.scanwarelite.dialogs.events.ReuseOrOverwriteDialog;
import com.paylogic.scanwarelite.helpers.ConnectivityHelper;
import com.paylogic.scanwarelite.helpers.PreferenceHelper;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;
import com.paylogic.scanwarelite.models.Event;
import com.paylogic.scanwarelite.utils.FileUtils;

public class EventsActivity extends CommonActivity {

	private ListView eventsView;
	private Button retryButton;
	private Button continueButton;

	private ArrayList<Event> events;
	private EventsAdapter events_adapter;
	private Event selectedEvent;

	private GetEventsTask eventsTask;

	private String username;
	private String password;
	private int userId;
	private Event existingEvent;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_events);
		settings = getSharedPreferences(PreferenceHelper.PREFS_FILE,
				Context.MODE_PRIVATE);

		events = new ArrayList<Event>();

		username = app.getUsername();
		password = app.getPassword();
		userId = app.getUserId();

		eventsView = (ListView) findViewById(R.id.listView_events);
		retryButton = (Button) findViewById(R.id.button_retry);
		continueButton = (Button) findViewById(R.id.button_event_continue);

		events_adapter = new EventsAdapter(EventsActivity.this,
				android.R.layout.simple_list_item_1, events);
		eventsView.setAdapter(events_adapter);

		updateEvents();
	}

	protected void onResume() {
		super.onResume();
		eventsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selectedEvent = events.get(position);
				events_adapter.setSelectedIndex(position);

				// Disable continue button for all events but the local event if
				// internet connectivity is lost
				if (!ConnectivityHelper.isConnected(EventsActivity.this)
						&& !(selectedEvent.getId() == existingEvent.getId())) {
					continueButton.setClickable(false);
				} else {
					continueButton.setClickable(true);
				}
			}
		});

		retryButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				updateEvents();
			}
		});

		continueButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (selectedEvent != null) {
					if ((existingEvent != null)
							&& (selectedEvent.getId() == existingEvent.getId())) {
						if (!ConnectivityHelper
								.isConnected(EventsActivity.this)) {
							alertDialog = new OnlyReuseDialog(
									EventsActivity.this);
							alertDialog.show();
						} else {
							alertDialog = new ReuseOrOverwriteDialog(
									EventsActivity.this, selectedEvent);

							alertDialog.show();

						}
					} else if (databaseExists()) {
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
			updateEvents();
			break;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		// Do not return to login activity
	}

	private void updateEvents() {

		eventsTask = new GetEventsTask();
		eventsTask.execute();
		events_adapter.setSelectedIndex(-1);
		selectedEvent = null;

	}

	private boolean databaseExists() {
		return getDatabasePath(ScanwareLiteOpenHelper.DATABASE_NAME).exists();
	}

	private class EventsAdapter extends ArrayAdapter<Event> {
		private int selectedIndex = -1;

		public EventsAdapter(Context context, int resource,
				ArrayList<Event> events) {
			super(context, resource, events);
		}

		public void setSelectedIndex(int index) {
			selectedIndex = index;
			notifyDataSetChanged();
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

			DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
			Date eventEndDate = event.getEndDate();
			String dateString = df.format(eventEndDate);

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
				eventDateView.setText("(" + dateString + ")");

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

	private class GetEventsTask extends AsyncTask<Void, Event, Void> {
		private HttpURLConnection conn;
		private String url = "https://api.paylogic.nl/API/?command=";
		boolean databaseExists;
		boolean isConnected;
		boolean noResources;

		@Override
		protected void onPreExecute() {
			progressDialog = new GetEventsDialog(EventsActivity.this);
			progressDialog.show();

			events.clear();
			events_adapter.notifyDataSetChanged();

			databaseExists = databaseExists();
			isConnected = ConnectivityHelper.isConnected(EventsActivity.this);
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();

			if (noResources) {
				alertDialog = new NoResourcesDialog(EventsActivity.this);
				alertDialog.show();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (databaseExists || isConnected) {

				// if isConnected
				// else if databaseExists
				// else noResources = true;

				// Get events from API
				if (isConnected) {
					getOnlineEvents();
				}

				// Get event from database
				if (databaseExists) {
					try {
						getLocalEvent();
					} catch (SQLiteException e) {

					}
				}

			} else {
				noResources = true;
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Event... event) {
			super.onProgressUpdate(event);
			for (Event e : events) {
				if (e.getId() == event[0].getId()) {
					e.setLocalEvent(true);

					// Move local event to top
					Event localEvent = events.get(events.indexOf(e));
					events.remove(e);
					events.add(0, localEvent);
					events_adapter.notifyDataSetChanged();

					return;
				}
			}
			events.add(event[0]);
			events_adapter.notifyDataSetChanged();
		}

		private void getOnlineEvents() {
			String command = "sparqMMList";
			String urlParams = "&username=" + username + "&password="
					+ password;

			String eventsUrl = url + command + urlParams;

			try {
				URL url = new URL(eventsUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(conn.getInputStream());

				Element root = doc.getDocumentElement();
				NodeList modules = root.getElementsByTagName("module");

				for (int i = 0; i < modules.getLength(); i++) {
					Node node = modules.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;

						int id = Integer.parseInt(element.getAttribute("id"));

						String title = element.getTextContent();
						String endDate = element.getAttribute("enddate");
						String deadline = element.getAttribute("deadline");

						Event event = new Event(id, title, endDate, deadline);

						Date now = new Date();

						if (event.getEndDate().after(now)
								|| settings.getBoolean("showAll", false)) {
							publishProgress(event);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}

		private void getLocalEvent() {
			db = scanwareLiteOpenHelper.getReadableDatabase();
			String[] columns = new String[] { ScanwareLiteOpenHelper.USER_KEY_ID };
			Cursor cursor = db.query(ScanwareLiteOpenHelper.USER_TABLE,
					columns, null, null, null, null, null, "1");

			int dbUserId = 0;
			if (cursor != null && cursor.moveToFirst()) {
				dbUserId = cursor.getInt(cursor
						.getColumnIndex(ScanwareLiteOpenHelper.USER_KEY_ID));
			}

			if (dbUserId == userId) {
				columns = new String[] { ScanwareLiteOpenHelper.EVENT_KEY_ID,
						ScanwareLiteOpenHelper.EVENT_KEY_TITLE };

				cursor = db.query(ScanwareLiteOpenHelper.EVENTS_TABLE, columns,
						null, null, null, null, null, "1");
				if (cursor != null && cursor.moveToFirst()) {
					int eventId = cursor
							.getInt(cursor
									.getColumnIndex(ScanwareLiteOpenHelper.EVENT_KEY_ID));
					String eventTitle = cursor
							.getString(cursor
									.getColumnIndex(ScanwareLiteOpenHelper.EVENT_KEY_TITLE));
					existingEvent = new Event(eventId, eventTitle, null, null);
					publishProgress(existingEvent);
				}
			}
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
