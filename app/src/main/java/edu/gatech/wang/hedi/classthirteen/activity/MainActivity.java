package edu.gatech.wang.hedi.classthirteen.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.gatech.wang.hedi.classthirteen.Constants;
import edu.gatech.wang.hedi.classthirteen.Helper;
import edu.gatech.wang.hedi.classthirteen.R;
import edu.gatech.wang.hedi.classthirteen.fragment.MapFragment;
import edu.gatech.wang.hedi.classthirteen.fragment.UpdateInfoDialogFragment;


public class MainActivity extends ActionBarActivity implements UpdateInfoDialogFragment.UpdateDialogListener {

    Context context;
    GoogleApiClient googleApiClient;
    MapFragment mapFragment;
    Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    private void initialize() {
        context = this;

        buildGoogleApiClient();
        googleApiClient.connect();
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.map_container, mapFragment);
        fragmentTransaction.commit();
        MapsInitializer.initialize(context);
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_update) {
            UpdateInfoDialogFragment dialogFragment = new UpdateInfoDialogFragment();
            dialogFragment.show(getSupportFragmentManager(), "update");
            return true;
        }
        if (id == R.id.action_refresh) {
            refresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(String name, String org) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.APPNAME, 0).edit();
        editor.putString(Constants.NAME_SAVED,name);
        editor.commit();
        new UpdateInfoTask().execute(name, org);
    }

    private class GetMarkersTask extends AsyncTask<Void, Void, Void> {

        ArrayList<MarkerOptions> markers;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String response = getMarkersJson();
                JSONArray jsonArray = new JSONArray(response);
                if (response != null) {
                    markers = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        Double latitude = jsonObject.getDouble("latitude");
                        Double longitude = jsonObject.getDouble("longitude");
                        LatLng latLng = new LatLng(latitude, longitude);
                        String name = jsonObject.getString("name");
                        name = Helper.convertToUTF8(name);
                        String org = jsonObject.getString("org");
                        org = Helper.convertToUTF8(org);
                        MarkerOptions temp = new MarkerOptions();
                        temp.position(latLng);
                        temp.title(name);
                        temp.snippet(org);
                        markers.add(temp);
                    }
                }
            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mapFragment != null && markers != null) {
                mapFragment.setMarkers(markers);
            }
            SharedPreferences preferences = getSharedPreferences(Constants.APPNAME, 0);
            boolean firstRun = preferences.getBoolean(Constants.FIRST_RUN, true);
            if (firstRun) {
                SharedPreferences.Editor editor= preferences.edit();
                editor.putBoolean(Constants.FIRST_RUN,false);
                editor.commit();
                UpdateInfoDialogFragment dialogFragment = new UpdateInfoDialogFragment();
                dialogFragment.show(getSupportFragmentManager(), "update");
            }
        }

        private String getMarkersJson() {
            InputStream is = null;
            try {
                URL url = new URL(Constants.APPURL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        myLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.e("Location Log", connectionResult.toString());
                        Toast.makeText(MainActivity.this, "Location Service not available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(LocationServices.API)
                .build();
    }

    private void refresh() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetMarkersTask().execute();
        } else {
            Toast.makeText(this, "No Internet Access. Please Check Your Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private class UpdateInfoTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... args) {
            InputStream is = null;
            try {
                URL url = new URL(Constants.UPDATE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("name", args[0]));
                params.add(new BasicNameValuePair("org", args[1]));
                params.add(new BasicNameValuePair("latitude", myLocation.getLatitude() + ""));
                params.add(new BasicNameValuePair("longitude", myLocation.getLongitude() + ""));

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(Helper.getQuery(params));
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
                int response = conn.getResponseCode();
                is = conn.getInputStream();
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                String test = builder.toString();
                return response;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (integer == 200) {
                Toast.makeText(context, "Update Successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Update Failed!", Toast.LENGTH_SHORT).show();
            }
            refresh();
        }
    }
}
