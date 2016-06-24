package it.uniparthenope.sebeto.francesco.lombardi.ocean2docks;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;

public class IntroActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    }

    public void SetMenuActivity(View view) {
        //Start the Main Activity of the application
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
        this.finish();
    }

}
