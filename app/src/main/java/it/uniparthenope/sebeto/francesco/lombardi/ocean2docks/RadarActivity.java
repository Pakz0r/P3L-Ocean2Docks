package it.uniparthenope.sebeto.francesco.lombardi.ocean2docks;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Button;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.content.Context;
import android.location.Criteria;
import android.view.View;

public class RadarActivity extends Activity implements SensorEventListener, LocationListener {

    private ImageView mPointer;
    private Button textAzimut;
    private Button textLat;
    private Button textLong;
    private Button textSpeed;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private LocationManager locationManager;

    private int currSpeedSys = 0; // 0 - m/s; 1 - km/h; 2 - nodi;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        provider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provider, 2000, 1, this);

        mPointer = (ImageView) findViewById(R.id.img_compass);
        textAzimut = (Button) findViewById(R.id.buttonAzimut);
        textLat = (Button) findViewById(R.id.buttonLatitude);
        textLong = (Button) findViewById(R.id.buttonLongitude);
        textSpeed = (Button) findViewById(R.id.buttonSpeed);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
            mLastAccelerometerSet = false;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            RotateCompass();
            mLastAccelerometerSet = false;
        }
    }

    public void RotateCompass(){
        SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
        SensorManager.getOrientation(mR, mOrientation);
        float azimuthInRadians = mOrientation[0];
        float azimuthInDegress = (float)((Math.toDegrees(azimuthInRadians)+360)%360);
        if(Math.abs( azimuthInDegress + mCurrentDegree ) > 1.0) {
            RotateAnimation ra = new RotateAnimation(
                    mCurrentDegree,
                    -azimuthInDegress,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            ra.setDuration(210);
            ra.setFillAfter(true);
            mPointer.startAnimation(ra);
            mCurrentDegree = -azimuthInDegress;
            buttonTextUpdate(textAzimut, "CurrentAzimut: " + Float.toString(Math.abs(mCurrentDegree)) + "°");
        }
    }

    public void buttonTextUpdate(Button button, String text){
        button.setText(text);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onLocationChanged(Location location) {
        buttonTextUpdate(textLat, "Latitude: " + location.getLatitude());
        buttonTextUpdate(textLong, "Longitude: " + location.getLongitude());
        switch (currSpeedSys){
            default: {
                buttonTextUpdate(textSpeed, "CurrentSpeed: "+ location.getSpeed() + " m/s");
                break;
            }
            case 1: {
                buttonTextUpdate(textSpeed, "CurrentSpeed: "+ (location.getSpeed()*3.6) + " km/h");
                break;
            }
            case 2: {
                buttonTextUpdate(textSpeed, "CurrentSpeed: "+ (location.getSpeed()*1.943845) + " nm/h");
                break;
            }
        }
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

    public void ChangeSpeedSys(View view) {
        switch (currSpeedSys){
            case 0: {
                currSpeedSys = 1;
                break;
            }
            case 1: {
                currSpeedSys = 2;
                break;
            }
            default: {
                currSpeedSys = 0;
                break;
            }
        }
        Location myLocation = locationManager.getLastKnownLocation(provider);
        if(myLocation != null) onLocationChanged(myLocation);
    }

}