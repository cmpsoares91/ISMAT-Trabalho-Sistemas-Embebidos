package trabalho.sistemas.embebidos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class DirectionsListActivity extends Activity {

	private String jsonStringOutput;
	private AsyncTask<String, String, String> output;
	private JSONObject json;
	private ArrayList<HashMap<String, String>> stepList = new ArrayList<HashMap<String, String>>();
	static ArrayList<LatLng> route = new ArrayList<LatLng>();// = new LatLng(-33.867, 151.206);
	static ArrayList<String> poly = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_directions_list);
		// Show the Up button in the action bar.
		setupActionBar();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String url = extras.getString("directionsUrl");
			if (url != null) {
				Log.d("testing2ndIntent", url);
				output = new RequestTask().execute(url);
				try {
					jsonStringOutput = output.get();
					Log.d("testing2ndIntent", jsonStringOutput);
					json = new JSONObject(jsonStringOutput);
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							transformeJSONObject();
						}
					});
					t.start();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void transformeJSONObject() {
		try {
			String status = json.get("status").toString();
			if (status.equals("OK")) {
				TextView t1 = (TextView) findViewById(R.id.from);
				TextView t2 = (TextView) findViewById(R.id.to);
				TextView t3 = (TextView) findViewById(R.id.duration);
				TextView t4 = (TextView) findViewById(R.id.distance);

				JSONArray routes = ((JSONArray) json.get("routes"));
				JSONArray legs = (JSONArray) ((JSONObject) routes.get(0))
						.get("legs");
				String start = (String) ((JSONObject) legs.get(0))
						.get("start_address");
				String end = (String) ((JSONObject) legs.get(0))
						.get("end_address");
				String duration = (String) ((JSONObject) ((JSONObject) legs
						.get(0)).get("duration")).get("text");
				String distance = (String) ((JSONObject) ((JSONObject) legs
						.get(0)).get("distance")).get("text");

				t1.setText(start);
				t2.setText(end);
				t3.setText(duration);
				t4.setText(distance);

				
				String latTest = (String)((JSONObject) ((JSONObject) legs.get(0)).get("start_location")).getString("lat");
				Log.d("JSONparsing", latTest);
				String lngTest = (String)((JSONObject) ((JSONObject) legs.get(0)).get("start_location")).getString("lng");
				Log.d("JSONparsing", lngTest);
				
				LatLng startLocation = new LatLng(Double.parseDouble(latTest), Double.parseDouble(lngTest));
				route.add(startLocation);
				
				// Log.d("JSONparsing", "Number of routes: " + routes.length());
				// Log.d("JSONparsing", "Number of legs: " + legs.length());

				// Parsing the step list:
				JSONArray steps = (JSONArray) ((JSONObject) legs.get(0))
						.get("steps");
				for (int i = 0; i < steps.length(); i++) {
					JSONObject c = steps.getJSONObject(i);
					// Storing JSON item in a Variable
					LatLng routePoints = new LatLng(Double.parseDouble((String)(((JSONObject) c.get("start_location")).getString("lat"))), Double.parseDouble((String)(((JSONObject) c.get("start_location")).getString("lng"))));
					route.add(routePoints);
					
					String travelMode = c.getString("travel_mode");
					String polyline = ((JSONObject) c.get("polyline"))
							.getString("points");
					poly.add(polyline);
					duration = ((JSONObject) c.get("duration"))
							.getString("text");
					distance = ((JSONObject) c.get("distance"))
							.getString("text");
					String htmlInstructions = Html.fromHtml(
							c.getString("html_instructions")).toString();

					// Adding value HashMap key => value
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("travel_mode", travelMode);
					map.put("polyline", polyline);
					map.put("duration", duration);
					map.put("distance", distance);
					map.put("html_instructions", htmlInstructions);
					stepList.add(map);
					ListView list = (ListView) findViewById(R.id.listView1);
					Log.d("SmallTest", "After ListView declaration");
					ListAdapter adapter = new SimpleAdapter(
							getApplicationContext(), stepList, R.layout.list_v,
							new String[] { "html_instructions", "travel_mode",
									"duration", "distance" }, new int[] {
									R.id.html_instruction, R.id.travel_mode,
									R.id.duration, R.id.distance });
					list.setAdapter(adapter);
				}
			} else {
				// TODO Add custom error messenger...

				AlertDialog ad = new AlertDialog.Builder(this).create();
				ad.setCancelable(false); // This blocks the 'BACK' button
				ad.setTitle("Oops! Something went wrong!");
				ad.setMessage(status);
				ad.setButton(AlertDialog.BUTTON_NEGATIVE,
						this.getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				ad.show();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.steps_directions:
			// We're here...
			return true;
		case R.id.about:
			// startActivity(aboutIntent);
			return true;
		case R.id.route_map:
			Intent mapIntent = new Intent();
			mapIntent.setClass(getApplicationContext(), MapActivity.class);
			mapIntent.putExtra("route", route);
			mapIntent.putExtra("polylines", poly);
			startActivity(mapIntent);
			return true;
		case R.id.route_options:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
