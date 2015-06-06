package com.kevincronly.civic_app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
{
    private ArrayList<String> users;
    private EditText userET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        setTitle("trackr");

        /*
        ArrayList<String> users = new ArrayList<String>();
        users.add("robertlee");
        users.add("pg13");
        users.add("c_morgan");
        users.add("sallyclark");
        users.add("dtuff");
        users.add("deronw");*/

        EditText userET = (EditText) findViewById(R.id.email_ET);

        final Button btn = (Button) findViewById(R.id.login_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                login();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();



        return super.onOptionsItemSelected(item);
    }

    public void login()
    {
        //if (users.contains(userE))
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}
