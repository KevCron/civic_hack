package com.kevincronly.civic_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class ClaimActivity extends AppCompatActivity
{
    public static final String PREFS_NAME = "MyPrefsFile";
    private EditText claimTitleET;
    private EditText natureOfBusinessET;
    private EditText licensePlateET;
    private EditText startMileageET;
    private EditText endMileageET;
    private EditText notesET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);

        claimTitleET = (EditText) findViewById(R.id.claim_title_ET);
        natureOfBusinessET = (EditText) findViewById(R.id.nature_of_business_ET);
        licensePlateET = (EditText) findViewById(R.id.license_plate_ET);
        startMileageET = (EditText) findViewById(R.id.start_mileage_ET);
        endMileageET = (EditText) findViewById(R.id.end_mileage_ET);
        notesET = (EditText) findViewById(R.id.notes_ET);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String licensePlateNumber = settings.getString("licensePlateNumber", null);

        if (licensePlateNumber != null)
        {
            licensePlateET.setText(licensePlateNumber);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_claim, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void submit(View view)
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("licensePlateNumber", licensePlateET.getText().toString());
        editor.apply();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("claimTitle", claimTitleET.getText().toString());
        returnIntent.putExtra("natureOfBusiness", natureOfBusinessET.getText().toString());
        returnIntent.putExtra("licensePlateNumber", licensePlateET.getText().toString());
        returnIntent.putExtra("startMileage", startMileageET.getText().toString());
        returnIntent.putExtra("endMileage", endMileageET.getText().toString());
        returnIntent.putExtra("notes", notesET.getText().toString());
        setResult(RESULT_OK,returnIntent);
        finish();
    }
}
