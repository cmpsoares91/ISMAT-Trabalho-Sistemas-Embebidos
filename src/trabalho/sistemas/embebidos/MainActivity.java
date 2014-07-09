package trabalho.sistemas.embebidos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

public class MainActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	// Necessary constants for GooglePlayServicesUtil verifications:
	private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	/*
	 * Global variable to hold the LocationClient used to retrieve our
	 * localization:
	 * 
	 * LocationClient:
	 * http://developer.android.com/reference/com/google/android/
	 * gms/location/LocationClient.html
	 */
	private LocationClient mLocationClient;

	/*
	 * Global variable to hold the current location:
	 * 
	 * Location:
	 * http://developer.android.com/reference/android/location/Address.html
	 */
	private Location myCurrentLocation;

	/*
	 * Some global helper/temporary variables:
	 */
	private String tempString;
	private TextView tv;
	private Time now = new Time();

	/*
	 * Holding Variables:
	 */
	private List<String> destinationStringList;
	private List<Address> destinationList;
	private int destSpinnerOption, travelModeSpinnerOption, metricSystemOption;
	private Boolean tollCheckBox = false, highWayCheckBox = false,
			departureCheckbox = false, arrivalCheckbox = false;
	private Intent listIntent, mapIntent, aboutIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("FileLocation", "MainActivity: onCreate()");
		setContentView(R.layout.activity_main);

		/*
		 * Create a new location client, using the enclosing class to handle
		 * Callback:
		 */
		mLocationClient = new LocationClient(this, this, this);

		/*
		 * Setting Event Handlers:
		 */
		setCheckBoxHandler(R.id.timePicker1, R.id.datePicker1, R.id.checkBox3);
		setCheckBoxHandler(R.id.timePicker2, R.id.datePicker2, R.id.checkBox4);
		setAvoidCheckBoxHandler(R.id.checkBox1, R.id.checkBox2);
		setMainButtonHandlers(R.id.button1, R.id.button2);
		setAddressChecker(R.id.editText1, R.id.spinner2);
		setBasicSpinnerHandler(R.id.spinner1, R.id.spinner3);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.steps_directions:
			if (listIntent != null) {
				submitAllOptions();
			}
			return true;
		case R.id.about:
			// startActivity(aboutIntent);
			return true;
		case R.id.route_map:
			// startActivity(mapIntent);
			return true;
		case R.id.route_options:
			// Is here...
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// Log.d("FileLocation", "MainActivity: onCreateOptionsMenu()");

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/*
	 * Called when the Activity becomes visible.
	 */
	@Override
	protected void onStart() {
		Log.d("FileLocation", "MainActivity: onStart()");
		super.onStart();

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				updateLocation();
			}
		});
		t.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("FileLocation", "MainActivity: onResume()");
	}

	@Override
	protected void onPause() {
		Log.d("FileLocation", "MainActivity: onPause()");
		if (mLocationClient.isConnected()) {
			mLocationClient.disconnect();
		}
		super.onPause();
	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		Log.d("FileLocation", "MainActivity: onStop()");
		// Disconnecting the client invalidates it.
		if (mLocationClient.isConnected()) {
			mLocationClient.disconnect();
		}
		super.onStop();
	}
	
	/*
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {		
		//Save destination string:
		EditText destination = (EditText) findViewById(R.id.editText1);
		String destinationString = destination.getText().toString();
		savedInstanceState.putString("destinationString", destinationString);
		
		//Save Spinner Integers:
		savedInstanceState.putInt("destSpinnerOption", destSpinnerOption);
		savedInstanceState.putInt("travelModeSpinnerOption", travelModeSpinnerOption);
		savedInstanceState.putInt("metricSystemOption", metricSystemOption);
		
		//Save checkbox states:
		savedInstanceState.putBoolean("tollCheckBox", tollCheckBox);
		savedInstanceState.putBoolean("highWayCheckBox", highWayCheckBox);
		savedInstanceState.putBoolean("departureCheckbox", departureCheckbox);
		savedInstanceState.putBoolean("arrivalCheckbox", departureCheckbox);
		super.onSaveInstanceState(savedInstanceState);
	}
	*/
	
	/*
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		//Restore destination string:
		EditText destination = (EditText) findViewById(R.id.editText1);
		String destinationString = savedInstanceState.getString("destinationString");
		destination.setText(destinationString);
		
		//Restore Spinner Integers:
		destSpinnerOption = savedInstanceState.getInt("destSpinnerOption");
		Spinner sp1 = (Spinner) findViewById(R.id.spinner2);
		sp1.setSelection(destSpinnerOption);
		
		travelModeSpinnerOption = savedInstanceState.getInt("travelModeSpinnerOption");
		Spinner sp2 = (Spinner) findViewById(R.id.spinner1);
		sp2.setSelection(travelModeSpinnerOption);
		
		metricSystemOption = savedInstanceState.getInt("metricSystemOption");
		Spinner sp3 = (Spinner) findViewById(R.id.spinner3);
		sp3.setSelection(metricSystemOption);
		
		//Restore checkbox states:
		savedInstanceState.putBoolean("tollCheckBox", tollCheckBox);
		savedInstanceState.putBoolean("highWayCheckBox", highWayCheckBox);
		savedInstanceState.putBoolean("departureCheckbox", departureCheckbox);
		savedInstanceState.putBoolean("arrivalCheckbox", departureCheckbox);
		
		
		super.onRestoreInstanceState(savedInstanceState);
	}
	*/

	/**
	 * Methods to verify if GooglePlayServicesUtil is working, if not
	 * checkPlayServices() returns 'false', if it's ready to go it will return
	 * 'true'. Code was adapted from the following source: Author: Alex Lockwood
	 * Link:
	 * http://www.androiddesignpatterns.com/2013/01/google-play-services-setup
	 * .html
	 */
	private boolean checkPlayServices() {
		Log.d("FileLocation", "MainActivity: checkPlayServices()");
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (status != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
				showErrorDialog(status);
			} else {
				Toast.makeText(this, "This device is not supported.",
						Toast.LENGTH_LONG).show();
				finish();
			}
			return false;
		}
		return true;
	}

	void showErrorDialog(int code) {
		Log.d("FileLocation", "MainActivity: showErrorDialog()");
		GooglePlayServicesUtil.getErrorDialog(code, this,
				REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("FileLocation", "MainActivity: onActivityResult()");
		switch (requestCode) {
		case REQUEST_CODE_RECOVER_PLAY_SERVICES:
			if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Google Play Services must be installed.",
						Toast.LENGTH_SHORT).show();
				finish();
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Methods for GooglePlayServicesUtil verification end here.
	 */

	/**
	 * Methods for localization requests with the Google Services API:
	 * 
	 * Necessary implementations for GooglePlayServicesClient:
	 * GooglePlayServicesClient.ConnectionCallbacks,
	 * GooglePlayServicesClient.OnConnectionFailedListener
	 * 
	 * Link:
	 * http://developer.android.com/training/location/retrieve-current.html
	 */

	/*
	 * Called by Location Services if the attempt to Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d("FileLocation", "MainActivity: onConnectionFailed()");
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			showErrorDialog(connectionResult.getErrorCode());
		}

	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle arg0) {
		Log.d("FileLocation", "MainActivity: onConnected()");
		// Display the connection status
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		getMyCurrentLocation();
		updateLocationValues(myCurrentLocation);
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		Log.d("FileLocation", "MainActivity: onDisconnected()");
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Methods for localization requests with the Google Services API end here.
	 */

	private void updateLocation() {
		// Connect the client.
		if (checkPlayServices()) {
			// Then we're good to go!
			Log.d("GooglePlayServicesUtilTesting", "It's alive!");
			if (!mLocationClient.isConnected()) {
				mLocationClient.connect();
			}
		} else {
			Log.d("GooglePlayServicesUtilTesting", "It's broken!");
		}
		/*
		 * Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
		 * Log.d("GooglePlayServicesUtilTesting",
		 * Integer.toString(spinner1.getSelectedItemPosition()));
		 * spinner1.setSelection(2); Log.d("GooglePlayServicesUtilTesting",
		 * Integer.toString(spinner1.getSelectedItemPosition()));
		 */
	}

	/**
	 * Method to store in variable myCurrentLocation the Location pointer of my
	 * GPS coordinates
	 */
	private void getMyCurrentLocation() {
		// Retrieves last accurate Location
		Log.d("FileLocation", "MainActivity: getMyCurrentLocation()");
		myCurrentLocation = mLocationClient.getLastLocation();

		// Turn information into temporary string for logging:
		tempString = "Your current GPS coordenates are: \nLatitude: "
				+ myCurrentLocation.getLatitude() + "\nLongitude: "
				+ myCurrentLocation.getLongitude() + "\nAltitude: "
				+ myCurrentLocation.getAltitude() + " meters.\nAcurracy: "
				+ myCurrentLocation.getAccuracy() + " meters.\nProvider: "
				+ myCurrentLocation.getProvider();
		Log.d("GooglePlayServicesUtilTesting", tempString);
	}

	private void updateLocationValues(Location l) {
		// Latitude value:
		tv = (TextView) findViewById(R.id.distance);
		tempString = l.getLatitude() + "º";
		tv.setText(tempString);

		// Longitude value:
		tv = (TextView) findViewById(R.id.textView5);
		tempString = l.getLongitude() + "º";
		tv.setText(tempString);

		// Address value:
		Geocoder g = getGeocoder();
		List<Address> address;
		try {
			address = g.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
			/*
			 * for (Address address2 : address) {
			 * Log.d("GooglePlayServicesUtilTesting", address2.toString()); }
			 */// For debugging
				// Log.d("GooglePlayServicesUtilTesting",
				// address.get(0).toString());
			tv = (TextView) findViewById(R.id.textView7);
			tempString = address.get(0).getAddressLine(0);
			tv.setText(tempString);
			tv = (TextView) findViewById(R.id.textView8);
			tempString = address.get(0).getAddressLine(1);
			tv.setText(tempString);
			tv = (TextView) findViewById(R.id.textView9);
			tempString = address.get(0).getAddressLine(2);
			tv.setText(tempString);
		} catch (IOException e) {
			tempString = "Address Unavailable.";
			tv.setText(tempString);
			e.printStackTrace();
		}
	}

	private Geocoder getGeocoder() {
		try {
			Locale current = getResources().getConfiguration().locale;
			Geocoder g = new Geocoder(this, current);
			return g;
		} catch (Exception e) {
			Geocoder g = new Geocoder(this);
			return g;
		}
	}

	private void setAvoidCheckBoxHandler(int checkbox1, int checkbox2) {
		CheckBox cbToll = (CheckBox) findViewById(checkbox1);
		CheckBox cbHW = (CheckBox) findViewById(checkbox2);

		cbToll.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				updateCheckBoxVariables();
			}
		});

		cbHW.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				updateCheckBoxVariables();
			}
		});
	}

	private void setBasicSpinnerHandler(int travelMode, int metric) {
		final Spinner sp1 = (Spinner) findViewById(travelMode);
		final Spinner sp2 = (Spinner) findViewById(metric);

		sp1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				travelModeSpinnerOption = sp1.getSelectedItemPosition();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}
		});

		sp2.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				travelModeSpinnerOption = sp2.getSelectedItemPosition();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}
		});
	}

	private void setAddressChecker(int textEditor, int optionSpinner) {
		final EditText te = (EditText) findViewById(textEditor);
		final Spinner sp = (Spinner) findViewById(optionSpinner);

		te.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// Do nothing...
				// Log.d("setAdressChecker", "In beforeTextChanged()");

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Do nothing...
				// Log.d("setAdressChecker", "In onTextChanged()");

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// Log.d("setAdressChecker", "In afterTextChanged()");
				Geocoder g = getGeocoder();
				String addressString = te.getText().toString();

				if (addressString != null && !addressString.equals("")) {
					try {
						destinationList = g.getFromLocationName(addressString,
								5);
						destinationStringList = new ArrayList<String>();

						for (Address address : destinationList) {
							tempString = "";

							// String formatting:
							for (int i = 0; i < 3; i++) {
								String addressLine = address.getAddressLine(i);
								if (addressLine != null) {
									if (i == 0) {
										tempString = addressLine;
									} else {
										tempString = String.format("%s, %s",
												tempString, addressLine);
									}
								}
							}

							destinationStringList.add(tempString);
						}
						ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
								getApplicationContext(),
								android.R.layout.simple_spinner_item,
								destinationStringList);
						dataAdapter
								.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						sp.setAdapter(dataAdapter);
						sp.setVisibility(Spinner.VISIBLE);
					} catch (IOException e) {
						tempString = "Address Unavailable.";
						Toast.makeText(getApplicationContext(), tempString,
								Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
				} else {
					sp.setVisibility(Spinner.GONE);
					// Log.d("setAdressChecker", "In onTextChanged():else");
				}
			}

		});

		sp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				travelModeSpinnerOption = sp.getSelectedItemPosition();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}
		});
	}

	/*
	 * Setting Buttons Handlers:
	 */
	private void setMainButtonHandlers(int submitID, int clearID) {
		Button submit = (Button) findViewById(submitID);
		Button clear = (Button) findViewById(clearID);

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submitAllOptions();

			}
		});

		clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				resetAllOptions();
			}
		});

	}

	protected void submitAllOptions() {
		listIntent = new Intent();
		listIntent.setClass(this, DirectionsListActivity.class);
		if (verifyOptions()) {
			tempString = createLink();
			listIntent.putExtra("directionsUrl", tempString);

			startActivity(listIntent);
		}
	}

	private String createLink() {
		String temp = "http://maps.googleapis.com/maps/api/directions/json?";
		Boolean sensor = false;
		Boolean alternatives = false;

		temp = String
				.format("%s%s",
						temp,
						"origin="
								+ myCurrentLocation.getLatitude()
								+ ","
								+ myCurrentLocation.getLongitude()
								+ "&destination="
								+ destinationList.get(destSpinnerOption)
										.getLatitude()
								+ ","
								+ destinationList.get(destSpinnerOption)
										.getLongitude());

		temp = String.format("%s%s", temp, "&sensor=" + sensor.toString());
		temp = String.format("%s%s", temp,
				"&alternatives=" + alternatives.toString());
		if (tollCheckBox) {
			temp = String.format("%s%s", temp, "&avoid=tolls");
		}
		if (highWayCheckBox) {
			temp = String.format("%s%s", temp, "&avoid=highways");
		}
		if (metricSystemOption == 0) {
			temp = String.format("%s%s", temp, "&units=metric");
		} else {
			temp = String.format("%s%s", temp, "&units=imperial");
		}
		Log.d("SCTest", Integer.toString(travelModeSpinnerOption));
		switch (travelModeSpinnerOption) {
		case 1: {
			temp = String.format("%s%s", temp, "&mode=walking");
			break;
		}
		case 2: {
			temp = String.format("%s%s", temp, "&mode=bicycling");
			break;
		}
		case 3: {
			temp = String.format("%s%s", temp, "&mode=transit");
			break;
		}
		default: {
			temp = String.format("%s%s", temp, "&mode=driving");
		}
		}
		if (departureCheckbox) {
			TimePicker tp1 = (TimePicker) findViewById(R.id.timePicker1);
			DatePicker dp1 = (DatePicker) findViewById(R.id.datePicker1);

			Time tt = new Time();
			tt.set(0, tp1.getCurrentMinute(), tp1.getCurrentHour(),
					dp1.getDayOfMonth(), dp1.getMonth(), dp1.getYear());
			Log.d("DatesTest", tt.toString());
			temp = String.format("%s%s", temp,
					"&departure_time=" + (tt.toMillis(false) / 1000));

		}
		if (arrivalCheckbox) {
			TimePicker tp2 = (TimePicker) findViewById(R.id.timePicker2);
			DatePicker dp2 = (DatePicker) findViewById(R.id.datePicker2);

			Time tt = new Time();
			tt.set(0, tp2.getCurrentMinute(), tp2.getCurrentHour(),
					dp2.getDayOfMonth(), dp2.getMonth(), dp2.getYear());
			Log.d("DatesTest", tt.toString());
			temp = String.format("%s%s", temp,
					"&arrival_time=" + (tt.toMillis(false) / 1000));
		}
		Log.d("UrlTest", temp);

		return temp;
	}

	private boolean verifyOptions() {
		if (myCurrentLocation != null) {
			if (destinationList.get(destSpinnerOption) != null) {
				if (travelModeSpinnerOption == 3) {
					if (departureCheckbox || arrivalCheckbox) {
						return true;
					} else {
						Toast.makeText(
								getApplicationContext(),
								"Please select at least a departure or arrival date if you want to use Transit mode.",
								Toast.LENGTH_LONG).show();
						return false;
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}

	protected void resetAllOptions() {
		// Reset Text:
		destinationStringList = null;
		destinationList = null;
		EditText editText = (EditText) findViewById(R.id.editText1);
		editText.setText(null);
		Spinner destinationSpinner = (Spinner) findViewById(R.id.spinner2);
		destinationSpinner.setSelection(0);
		destSpinnerOption = 0;
		// tollCheckBox = highWayCheckBox = departureCheckbox = arrivalCheckbox
		// = false;

		// Reset checkboxes:
		CheckBox cbToll = (CheckBox) findViewById(R.id.checkBox1);
		CheckBox cbHW = (CheckBox) findViewById(R.id.checkBox2);
		CheckBox cbDep = (CheckBox) findViewById(R.id.checkBox3);
		CheckBox cbArr = (CheckBox) findViewById(R.id.checkBox4);

		cbToll.setChecked(false);
		cbHW.setChecked(false);
		cbDep.setChecked(false);
		cbArr.setChecked(false);

		// Reset Basic Spinners:
		Spinner travelMode = (Spinner) findViewById(R.id.spinner1);
		Spinner metric = (Spinner) findViewById(R.id.spinner3);

		travelMode.setSelection(0);
		metric.setSelection(0);

		TimePicker tp1 = (TimePicker) findViewById(R.id.timePicker1);
		TimePicker tp2 = (TimePicker) findViewById(R.id.timePicker2);
		DatePicker dp1 = (DatePicker) findViewById(R.id.datePicker1);
		DatePicker dp2 = (DatePicker) findViewById(R.id.datePicker2);

		now.setToNow();
		tp1.setCurrentHour(now.hour);
		tp1.setCurrentMinute(now.minute);
		tp2.setCurrentHour(now.hour);
		tp2.setCurrentMinute(now.minute);

		dp1.updateDate(now.year, now.month, now.monthDay);
		dp2.updateDate(now.year, now.month, now.monthDay);

	}

	/*
	 * Setting TimePicker to country convention and setting CheckBox Event
	 * Handler:
	 */
	private void setCheckBoxHandler(int timepicker, int datepicker, int checkbox) {
		final TimePicker tp = (TimePicker) findViewById(timepicker);
		final DatePicker dp = (DatePicker) findViewById(datepicker);
		CheckBox cb = (CheckBox) findViewById(checkbox);

		tp.setIs24HourView(DateFormat.is24HourFormat(getApplicationContext()));

		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (buttonView.isChecked()) {
					tp.setVisibility(TimePicker.VISIBLE);
					dp.setVisibility(DatePicker.VISIBLE);
				} else {
					tp.setVisibility(TimePicker.GONE);
					dp.setVisibility(DatePicker.GONE);
				}
				updateCheckBoxVariables();
			}
		});
	}

	protected void updateCheckBoxVariables() {
		CheckBox cbToll = (CheckBox) findViewById(R.id.checkBox1);
		CheckBox cbHW = (CheckBox) findViewById(R.id.checkBox2);
		CheckBox cbDep = (CheckBox) findViewById(R.id.checkBox3);
		CheckBox cbArr = (CheckBox) findViewById(R.id.checkBox4);

		tollCheckBox = cbToll.isChecked();
		highWayCheckBox = cbHW.isChecked();
		departureCheckbox = cbDep.isChecked();
		arrivalCheckbox = cbArr.isChecked();
	}
}