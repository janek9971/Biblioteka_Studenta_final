package com.example.dawid.logowanie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.dawid.logowanie.Edit.EditBookActivity;
import com.example.dawid.logowanie.List.BookListActivity;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import activity.LoginActivity;
import activity.RegisterActivity;
import app.AppConfig;
import app.AppController;
import helper.SQLiteHandler;
import helper.SessionManager;

public class Main2Activity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    String fb_id;
    String email;
    //String firstime = "first";
    String uid;
    String FBidfirst;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    private SQLiteHandler db;
    private SessionManager session;
    private static final String TAG_FBUSEREMAIL = "email";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_UNIID = "unique_id";
    private static final String TAG_FBUSERID = "id";
    private static final String TAG_UPDATEFBID = "fb_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Intent i = getIntent();

        // getting id  from intent
        String FEmail = i.getStringExtra(TAG_FBUSEREMAIL);
        FBidfirst = i.getStringExtra(TAG_FBUSERID);


        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());


        HashMap<String, String> user = db.getUserDetails();
// getting email, unique_id and fb_id from
        email = user.get("email");
        uid = user.get("uid");
        fb_id = user.get("fb_id");
//Sprawdzenie czy email w SQLite jest taki sam jak Email pobrany pobrany w poprzedniej aktywności
// przez Facebook SDK oraz czy fb_id jest puste
        if (email.equals(FEmail) && fb_id.isEmpty()) {
            //SaveUserDetails wykonuje się tylko raz podczas łączenia istniejacego konta z
            // facebookiem
            new SaveUserDetails().execute(FBidfirst);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string
                .navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent = new Intent(this, AboutProgramActivity.class);
        startActivity(intent);

        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.book_add:


                intent = new Intent(this, AddBookActivity.class);
                startActivity(intent);


                break;

            case R.id.book_list:
                intent = new Intent(this, BookListActivity.class);
                startActivity(intent);
                break;
            case R.id.settings:


                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.logout:
                logoutUser();
                intent = new Intent(Main2Activity.this, LoginActivity.class);
                if (fb_id != null) {
                    intent.putExtra("fb_id", fb_id);
                } else {
                    intent.putExtra("fb_id", FBidfirst);

                }
                intent.putExtra("fbemail", email);
                startActivity(intent);

                finish();
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();
        LoginManager.getInstance().logOut();

    }

    /**
     * Background Async Task to  Save product Details
     */
    class SaveUserDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Main2Activity.this);

        }

        /**
         * Saving product
         */
        protected String doInBackground(String... args) {

            String FBid = args[0];


            // getting updated data from EditTexts


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_UNIID, uid));
            params.add(new BasicNameValuePair(TAG_UPDATEFBID, FBid));


            // sending modified data through http request
            JSONObject json = jsonParser.makeHttpRequest(AppConfig.URL_update_user_null, "POST",
                    params);

            // check json success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully updated

                    checkLoginFB(email, FBidfirst);

                } else {
                    // failed to update product
                }
            } catch (JSONException e) {
                e.printStackTrace();


            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product updated
            pDialog.dismiss();
        }
    }


    /**
     * function to verify login details in mysql db
     */
    private void checkLoginFB(final String email, final String FBidfirst) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        //    pDialog.setMessage("Logowanie ...");
//        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig
                .URL_update_userfb, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                //          hideDialog();


                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);
                        db.deleteUsers();

                        // Now store the user in SQLite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String name = user.getString("name");
                        String email = user.getString("email");
                        String fbid = user.getString("fb_id");
                        String created_at = user.getString("created_at");

                        // Inserting row in users table
                        db.addUser(name, email, uid, fbid, created_at);


                        Toast.makeText(getApplicationContext(), "Zdobywasz punkt za połączenie "
                                + "przez Facebooka!", Toast.LENGTH_LONG).show();
                        //Punkt reputacji jest dodawany w pliku PHP


                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Blad logowania: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG)
                        .show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("fb_id", FBidfirst);


                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void hideDialog() {
        if (pDialog.isShowing()) pDialog.dismiss();
    }

}
