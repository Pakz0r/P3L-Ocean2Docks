package it.uniparthenope.sebeto.francesco.lombardi.ocean2docks;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.os.AsyncTask;
import android.widget.Button;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;



public class EcdisActivity extends Activity implements LocationListener {

    // provvede alle informazioni di sicurezza e segnala la presenza di un pericolo in prossimità
    // di un pericolo di navigazione
    // + L'ECHOSCANDAGLIO: Che si preoccupa di fornire le informazioni riguardanti
    // la profondità dell'acqua.


    private LocationManager locationManager;
    private String provider;

    private Button weatherBG;
    private Button windBG;
    private Button ecoBG;
    private Button CurrPos;
    private Button NearDock;

    private static String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";

    Bitmap mBitmap2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecdis);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provider, 2000, 1, this);

        weatherBG = (Button) findViewById(R.id.buttonWeather);
        windBG = (Button) findViewById(R.id.buttonWind);
        ecoBG = (Button) findViewById(R.id.buttonES);
        CurrPos = (Button) findViewById(R.id.buttonLocation);
        NearDock = (Button) findViewById(R.id.buttonDocks);

        InputStream is = this.getResources().openRawResource(R.drawable.openseamap);
        mBitmap2 = BitmapFactory.decodeStream(is);

    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        new ReadJSONFeedTask().
                execute(BASE_URL + "?lat=" + latitude + "&lon=" + longitude + "&APPID=9298949d3e04246848be4bce503d37b6");

        int waterDepth = new MapUtils().getMapPixelColor(mBitmap2, latitude, longitude);
        ecoBG.setText("Water Depth: "+ waterDepth);

    }
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }
    @Override
    public void onProviderEnabled(String s) {
    }
    @Override
    public void onProviderDisabled(String s) {
    }

    private class ReadJSONFeedTask extends AsyncTask <String, Void, String> {

        protected String doInBackground(String... urls) {
            return readJSONFeed(urls[0]);
        }

        public String readJSONFeed(String url) {
            HttpURLConnection con = null ;
            InputStream is = null;
            try {
                con = (HttpURLConnection) ( new URL(url)).openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);
                con.setDoOutput(true);
                con.connect();

                StringBuilder buffer = new StringBuilder();
                is = con.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while (  (line = br.readLine()) != null )
                    buffer.append(line + "\r\n");
                is.close();
                con.disconnect();
                return buffer.toString();
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
            finally {
                try { is.close(); } catch(Throwable t) {}
                try { con.disconnect(); } catch(Throwable t) {}
            }
            return null;
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jObj = new JSONObject(result);
                JSONObject sysObj = getObject("sys", jObj);

                JSONArray jArr = jObj.getJSONArray("weather");
                JSONObject JSONWeather = jArr.getJSONObject(0);

                String currWeather = getString("main", JSONWeather);

                int drawableID = 0;
                if(currWeather.equals("Rain"))
                    drawableID = R.drawable.weather_rain;
                else if(currWeather.equals("Clouds"))
                    drawableID = R.drawable.weather_cloud;
                else if(currWeather.equals("Snow"))
                    drawableID = R.drawable.weather_snow;
                else if(currWeather.equals("Sun"))
                    drawableID = R.drawable.weather_sun;

                weatherBG.setCompoundDrawablesWithIntrinsicBounds(0,0,0,drawableID);

                JSONObject wObj = getObject("wind", jObj);

                windBG.setText("Wind Speed: "+ getFloat("speed", wObj) + " mps"+
                        "\n Wind Direction:" + getFloat("deg", wObj) + "°");

                CurrPos.setText("Current Location: (" + getString("country", sysObj) + ") " + getString("name", jObj));

            } catch (Exception e) { }
        }

        private JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
            JSONObject subObj = jObj.getJSONObject(tagName);
            return subObj;
        }

        private String getString(String tagName, JSONObject jObj) throws JSONException {
            return jObj.getString(tagName);
        }

        private float  getFloat(String tagName, JSONObject jObj) throws JSONException {
            return (float) jObj.getDouble(tagName);
        }

        private int  getInt(String tagName, JSONObject jObj) throws JSONException {
            return jObj.getInt(tagName);
        }
    }

    private class MapUtils{

        double min_lat = -2.4159074493956316E7;
        double max_lat = 2.4159074493956316E7;
        double min_lng = -2.415907449395633E7;
        double max_lng = 2.41590744939563E7;

        private double clipValue(double value, double maxValue, double minValue){
            return Math.min(Math.max(value, maxValue), minValue);
        }

        private double getMapDimensionsByZoomLevel(int zoomLevel){
            return 400 << zoomLevel;
        }

        private int getMapPixelColor(Bitmap mBitmap2, double lat,double lng){
            int level = 0;
            double mapSize = getMapDimensionsByZoomLevel(level);

            lat = clipValue(lat, min_lat, max_lat);
            lng = clipValue(lng, min_lng, max_lng);

            double x = (lng + 180) / 360;
            double sinLat = Math.sin(lat * Math.PI / 180);
            double y = 0.5 - Math.log((1 + sinLat) / (1 - sinLat)) / (4 * Math.PI);

            int pixelX = (int) (clipValue(x * mapSize + 0.5, 0, mapSize - 1));
            int pixelY = (int) (clipValue(y * mapSize + 0.5, 0, mapSize - 1));
            return mBitmap2.getPixel(pixelX, pixelY);
        }
    }
}