package com.awesome.connect;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.facebook.android.*;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.*;
import com.facebook.android.AsyncFacebookRunner;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Matches extends Activity {
	TextView test1;
	TextView test2;
	private Double lat = 0.0;
	private Double lon = 0.0;
	private String name;
	private String fid;
	private JSONArray movies;
    Facebook facebook = new Facebook("428162580578188");
    AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
    private static String url_create_location = "http://alvinsee.com/uconnect/update_users.php";
    private SharedPreferences mPrefs;
    JSONParser jsonParser = new JSONParser();
    AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(facebook);
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matches);
        test1 = (TextView) findViewById(R.id.textView1);
        test2 = (TextView) findViewById(R.id.textView2);
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
        
        try {
			String JsonUser = facebook.request("me");
	 		JSONObject obj = Util.parseJson(JsonUser);
			fid = obj.getString("id");
			name = obj.getString("name");
			movies = obj.getJSONArray("data");
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
        
        test1.setText(fid);
        test2.setText(movies.length());

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
				for (int a = 0; a < movies.length(); a++) {
					try {
						params.add(new BasicNameValuePair("Interest", movies.getString(a)));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				JSONObject json = jsonParser.makeHttpRequest(url_create_location, "POST", params);
				/*
				InputStream is;
				String slat = lat.toString();
				String slon = lon.toString();
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("lat", slat));
				nameValuePairs.add(new BasicNameValuePair("lon", slon));
				try {
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost("external-db.s77737.gridserver.com");
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity entity = response.getEntity();
					is = entity.getContent();
				} catch (Exception e) {
					Log.e("log_tag", "error in http connection" + e.toString());
				}
				*/
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
        
        /*
		String slat = lat.toString();
		String slon = lon.toString();
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("lat", slat));
		params.add(new BasicNameValuePair("lon", slon));
		JSONObject json = jsonParser.makeHttpRequest(url_create_location, "POST", params);
		
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://www.alvinsee.com/uconnect/update_users.php");
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		} catch (Exception e) {
			Log.e("log_tag", "error in http connection" + e.toString());
		}
		*/
    }
   
 

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
}
