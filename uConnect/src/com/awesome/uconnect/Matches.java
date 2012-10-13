package com.awesome.uconnect;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

public class Matches extends ListActivity {
	
	private ProgressDialog pDialog;
	ArrayList<HashMap<String, String>> usersList;
	
	// JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_USERS = "users";
    private static final String TAG_UID = "UserID";
    private static final String TAG_NAME = "name";
 
    // products JSONArray
    JSONArray users = null;
	
	private Double lat = 0.0;
	private Double lon = 0.0;
	private String name;
	private String fid;
    private static String url_create_location = "http://alvinsee.com/uconnect/update_users.php";
    JSONParser jsonParser = new JSONParser();
	
    Facebook facebook = new Facebook("428162580578188");
    AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
    private SharedPreferences mPrefs;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);
        /*
         * Get existing access_token if any
         */
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
        
        /*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {

            facebook.authorize(this, new String[] {"user_interests",
            		"user_activities"}, new DialogListener() {
                @Override
                public void onComplete(Bundle values) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
                }
    
                @Override
                public void onFacebookError(FacebookError error) {}
    
                @Override
                public void onError(DialogError e) {}
    
                @Override
                public void onCancel() {}
            });
        }
        
        /** back end paramatarization. */
        backend();
        pull();
    }
   
 

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    /** back end paramatarize */
    public void backend() {
        /** getting username and id. */
        try {
			String JsonUser = facebook.request("me");
	 		JSONObject obj = Util.parseJson(JsonUser);
	 		Log.d("Matches", obj.toString());
			fid = obj.getString("id");
	 		Log.d("Matches", fid);
			name = obj.getString("name");
			//JSONArray data = obj.getJSONArray("data");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FacebookError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        /** location stuff */
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        LocationListener locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				/** sending files */
				lat = location.getLatitude();
				lon = location.getLongitude();
				
				String slat = lat.toString();
				String slon = lon.toString();
				ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("fid", fid));
				params.add(new BasicNameValuePair("name", name));
				params.add(new BasicNameValuePair("lat", slat));
				params.add(new BasicNameValuePair("lon", slon));
				JSONObject json = jsonParser.makeHttpRequest(url_create_location, "POST", params);
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
			}
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    
    public void pull() {
    	 // Hashmap for ListView
        usersList = new ArrayList<HashMap<String, String>>();
 
        // Loading products in Background Thread
        new LoadAllUsers().execute();
 
        // Get listview
        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
        	 
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // getting values from selected ListItem
                String UserID = ((TextView) view.findViewById(R.id.UserID)).getText()
                        .toString();
 
                // Starting new intent
                //Intent in = new Intent(getApplicationContext(),
                  //      EditProductActivity.class);
                // sending pid to next activity
                //in.putExtra(TAG_UID, UserID);
 
                // starting new activity and expecting some response back
                //startActivityForResult(in, 100);
            }
        });
    }
    
    
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllUsers extends AsyncTask<String, String, String> {
 
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Matches.this);
            pDialog.setMessage("Loading users. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			// Building Parameters
            List<NameValuePair> params2 = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jsonParser.makeHttpRequest(url_create_location, "GET", params2);
 
            // Check your log cat for JSON reponse
            Log.d("All Users: ", json.toString());
 
            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
 
                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    users = json.getJSONArray(TAG_USERS);
 
                    // looping through All Products
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject c = users.getJSONObject(i);
 
                        // Storing each json item in variable
                        String id = c.getString(TAG_UID);
                        String name = c.getString(TAG_NAME);
 
                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();
 
                        // adding each child node to HashMap key => value
                        map.put(TAG_UID, id);
                        map.put(TAG_NAME, name);
 
                        // adding HashList to ArrayList
                        usersList.add(map);
                    }
                } else {
                    // no products found
                    // Launch Add New product Activity
                    //Intent i = new Intent(getApplicationContext(),
                   //         NewUserActivity.class);
                    // Closing all previous activities
                    //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    //startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
 
            return null;
		}
		
		 protected void onPostExecute(String file_url) {
	            // dismiss the dialog after getting all products
	            pDialog.dismiss();
	            // updating UI from Background Thread
	            runOnUiThread(new Runnable() {
	                public void run() {
	                    /**
	                     * Updating parsed JSON data into ListView
	                     * */
	                    ListAdapter adapter = new SimpleAdapter(
	                            Matches.this, usersList,
	                            R.layout.user, new String[] { TAG_UID,
	                                    TAG_NAME},
	                            new int[] {R.id.UserID, R.id.name});
	                    // updating listview
	                    setListAdapter(adapter);
	                }
	            });
 
		 }	
    }
}
