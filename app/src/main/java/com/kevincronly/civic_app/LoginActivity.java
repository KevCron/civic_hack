package com.kevincronly.civic_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by kevin.cronly on 6/9/15.
 */
public class LoginActivity extends AppCompatActivity
{
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String TAG = "LoginActivity";
    private EditText userNameET;
    private EditText passwordET;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        userNameET = (EditText)findViewById(R.id.username_ET);
        passwordET = (EditText)findViewById(R.id.password_ET);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String userName = settings.getString("Username", null);
        String password = settings.getString("Password", null);

        if (userName != null && password != null)
        {
            userNameET.setText(userName);
            passwordET.setText(password);
        }
    }

    public void login(View view)
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Username", userNameET.getText().toString());
        editor.putString("Password", passwordET.getText().toString());
        editor.apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userName", userNameET.getText().toString());
        //startActivity(intent);
        Log.i(TAG, "login");

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
        {
            Log.i(TAG, "Network connected, retrieving data");
            //new AuthTask().execute("http://23e25190.ngrok.com/user/login");
            startActivity(intent);
        }
        else
        {
            Snackbar.make(findViewById(R.id.login_layout), "Network Error", Snackbar.LENGTH_LONG).show();
            Log.i(TAG, "No network");
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String callURL(String myurl) throws IOException
    {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;
        String response = "";

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Log.i(TAG, "connection opened");
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            List<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
            params.add(new AbstractMap.SimpleEntry<String, String>("user_name", userNameET.getText().toString()));
            params.add(new AbstractMap.SimpleEntry<String, String>("password", passwordET.getText().toString()));

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(params));
            writer.flush();
            writer.close();
            os.close();

            // Starts the query
            conn.connect();
            Log.i(TAG, "connected");
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response = "FAILED";
                Snackbar.make(findViewById(R.id.login_layout), "Connection Error", Snackbar.LENGTH_LONG).show();
            }

            // Convert the InputStream into a string
            return response;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    private String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException
    {
        Reader reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    private String getQuery(List<AbstractMap.SimpleEntry<String, String>> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (AbstractMap.SimpleEntry<String, String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private class AuthTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... url)
        {
            // params comes from the execute() call: params[0] is the url.
            try
            {
                return callURL(url[0]);
            }
            catch (IOException e)
            {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result)
        {
            Log.i(TAG, "responseString is " + result);
            if (result.equals("Login Successful"))
                onSuccess();
            else
                onFailed();
        }
    }

    private void onSuccess()
    {
        Log.i(TAG, "onSuccess");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("userName", userNameET.getText().toString());
        startActivity(intent);
    }

    private void onFailed()
    {
        Log.i(TAG, "onFailed");
        Snackbar.make(findViewById(R.id.login_layout), "Username or password incorrect", Snackbar.LENGTH_LONG).show();
    }
}