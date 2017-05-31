package com.example.dawid.logowanie;

import activity.LoginActivity;
import app.AppConfig;
import helper.SQLiteHandler;
import helper.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dawid.logowanie.Edit.EditProfileActivity;
import com.example.dawid.logowanie.JSONParser;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private TextView txtName;
    private TextView txtEmail;
    private TextView txtUserid;
    private TextView txtSingleid;
    private Button btnLogout;
    private Button btnConnectFB;

    String name;
    String unique_id;
    String email;
    String Punkty;

    boolean isfbidset;
    boolean connectfb;

    private SQLiteHandler db;
    private SessionManager session;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_IUSER = "iuser";
    private static final String TAG_PUNKTY = "Punkty";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


//        requestWindowFeature(Window.FEATURE_NO_TITLE);


        // Pobieranie wszystkich szczegółów użytkownika w tle
        new GetUsersDetails().execute();


        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        txtUserid = (TextView) findViewById(R.id.userid);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnConnectFB = (Button) findViewById(R.id.btnconnectfb);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
            Toast.makeText(this, "Tylko dla zalogowanych użytkowników", Toast.LENGTH_SHORT).show();
        }

        // Pobieranie danych użytkowników z SQLiteHandler
        HashMap<String, String> user = db.getUserDetails();

        name = user.get("name");
        email = user.get("email");
        unique_id = user.get("uid");
        String fb_id = user.get("fb_id");

        View b = findViewById(R.id.btnconnectfb);

        if (fb_id == null || fb_id.isEmpty()) {
            b.setVisibility(View.VISIBLE);
            btnConnectFB.setText("POŁĄCZ KONTO PRZEZ FACEBOOKA");
            isfbidset = false;
        } else {
            b.setVisibility(View.GONE);
        }


        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);
        txtUserid.setText(unique_id);

        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
        if (isfbidset == false) {
            btnConnectFB.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    alertMessage();
                }
            });
        }
    }

    public void alertMessage() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener
                () {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        connectfb = true;
                        logoutUser();
                        Toast.makeText(MainActivity.this, "Kliknij zaloguj się przez facebooka " +
                                "by" + " połączyć konta", Toast.LENGTH_LONG).show();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        // do nothing
                        //    Toast.makeText(MainActivity.this, "No Clicked",
                        //           Toast.LENGTH_LONG).show();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Czy chcesz połączyć konto przez facebooka? (Konto zostanie połączone " +
                "" + "tylko jeśli twój e-mail przy rejestracji jest taki sam jak ten dołączony do" +
                " " + "konta FB").setPositiveButton("TAK", dialogClickListener).setNegativeButton
                ("NIE", dialogClickListener).show();
    }


    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();
        LoginManager.getInstance().logOut();

        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        if (connectfb == true) {
            intent.putExtra("Punkty", connectfb);
        }
        startActivity(intent);
        finish();
    }

    public void onGonextClick(View view) {
        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
        startActivity(intent);
    }

    public void EditProfile(View view) {
        Intent intent = new Intent(MainActivity.this, EditProfileActivity.class);
        intent.putExtra("email", email);
        intent.putExtra("name", name);
        intent.putExtra("Punkty", Punkty);
        intent.putExtra("unique_id", unique_id);


        startActivity(intent);
    }


    class GetUsersDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading user details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting user details in background thread
         */
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    // Check for success tag
                    int success;
                    try {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();

                        params.add(new BasicNameValuePair("email", email));

                        // getting product details by making HTTP request
                        JSONObject json = jsonParser.makeHttpRequest(AppConfig.url_user_details,
                                "GET", params);

                        // check your log for json response
                        Log.d("Single User Details", json.toString());

                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // successfully received product details
                            JSONArray userObj = json

                                    .getJSONArray(TAG_IUSER); // JSON Array

                            // get first product object from JSON Array
                            JSONObject user = userObj.getJSONObject(0);

                            // user with this email found
                            txtSingleid = (TextView) findViewById(R.id.singleid);

                            //Wyświetlenie punktów reputacji użytkownika

                            txtSingleid.setText("Punkty reputacji:  " + user.getString(TAG_PUNKTY));
                            Punkty = txtSingleid.getText().toString();


                        } else {
                            // product with this email not found
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }
    }

}
