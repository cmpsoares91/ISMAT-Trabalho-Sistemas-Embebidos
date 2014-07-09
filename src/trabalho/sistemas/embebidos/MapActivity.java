package trabalho.sistemas.embebidos;

import java.util.ArrayList;
import java.util.Iterator;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolygonOptionsCreator;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapActivity extends FragmentActivity {
	static final LatLng HAMBURG = new LatLng(53.558, 9.927);
	static final LatLng KIEL = new LatLng(53.551, 9.993);
	static final LatLng SYDNEY = new LatLng(-33.867, 151.206);
	private GoogleMap map;
	static ArrayList<LatLng> route = new ArrayList<LatLng>();
	static ArrayList<String> polylines = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		// Show the Up button in the action bar.
		// Get a handle to the Map Fragment
		android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
		SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
				.findFragmentById(R.id.map);

		map = mapFragment.getMap();
		map.setMyLocationEnabled(true);

		Bundle extras = getIntent().getExtras();
		try {
			route = (ArrayList<LatLng>) extras.get("route");
			polylines = (ArrayList<String>) extras.get("polylines");
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(route.get(0), 13));
			Log.d("ArrayListTest", route.toString());
			map.addMarker(new MarkerOptions().title("Position A")
					.snippet("This is your departure position.")
					.position(route.get(0)));
			// Polylines 1:
			PolylineOptions p1 = new PolylineOptions();
			p1.geodesic(true);
			p1.color(Color.RED);
			p1.addAll(route);
			// map.addPolyline(p1);

			// Polylines 2:
			PolylineOptions p2 = new PolylineOptions();
			p2.geodesic(true);
			p2.color(Color.BLUE);
			ArrayList<LatLng> temp = new ArrayList<LatLng>();
			for (String encoded : polylines) {
				temp = decodePoly(encoded);
				p2.addAll(temp);
			}
			map.addPolyline(p2);
			map.addMarker(new MarkerOptions().title("Position b")
					.snippet("This is your arrival position.")
					.position(temp.get(temp.size()-1)));

		} catch (Exception e) {
			e.printStackTrace();
		}

		// map.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 13));

		/*
		 * map.addMarker(new MarkerOptions().title("Sydney")
		 * .snippet("The most populous city in Australia.") .position(SYDNEY));
		 */
		setupActionBar();
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
			// Where here
			return true;
		case R.id.route_options:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private ArrayList<LatLng> decodePoly(String encoded) {

		Log.i("Location", "String received: " + encoded);
		ArrayList<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}

		for (int i = 0; i < poly.size(); i++) {
			Log.i("Location", "Point sent: Latitude: " + poly.get(i).latitude
					+ " Longitude: " + poly.get(i).longitude);
		}
		return poly;
	}

}
