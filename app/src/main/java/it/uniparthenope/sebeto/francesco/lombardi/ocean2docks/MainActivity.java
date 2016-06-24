package it.uniparthenope.sebeto.francesco.lombardi.ocean2docks;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;

public class MainActivity extends Activity {

    static Intent NavigatorIntent;
    static Intent EcdisIntent;
    static Intent RadarIntent;
    static Intent AboutIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMainIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_intro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            ViewAbouts();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initMainIntent(){
       //Create a new activity for Navigator
        NavigatorIntent = new Intent(this, NavigatorActivity.class);
        //Create a new activity for Ecdis
        EcdisIntent = new Intent(this, EcdisActivity.class);
        //Create a new activity for Radar
        RadarIntent = new Intent(this, RadarActivity.class);
        //Create a new activity for Credits
        AboutIntent = new Intent(this, AboutActivity.class);
    }

    public void StartNavigator(View view){
        //Start the Navigator Activity
        startActivity(NavigatorIntent);
    }

    public void StartEcdis(View view){
        //Start the Ecdis Activity
        startActivity(EcdisIntent);
    }

    public void StartRadar(View view){
        //Start the Radar Activity
        startActivity(RadarIntent);
    }

    public void ViewAbouts(){
        //Open the About Activity
        startActivity(AboutIntent);
    }
}
