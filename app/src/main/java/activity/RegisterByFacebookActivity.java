package activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.dawid.logowanie.Main2Activity;
import com.example.dawid.logowanie.R;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.AppConfig;
import app.AppController;
import helper.SQLiteHandler;
import helper.SessionManager;

/**
 * Created by Janek on 04.05.2017.
 */


public class RegisterByFacebookActivity extends Activity {
    private static final String TAG = RegisterByFacebookActivity.class.getSimpleName();


    String FBid;
    String FEmail;
    String FName;

    private static final String TAG_FBUSERID = "id";
    private static final String TAG_FBUSEREMAIL = "email";
    private static final String TAG_FBUSERNAME = "name";

    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;

    EditText inputNick;
    Button btnRegister;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_by_facebook);

        if (AccessToken.getCurrentAccessToken() == null) {
            goLoginScreen();
        }

        btnRegister = (Button) findViewById(R.id.btnRegister);
        inputNick = (EditText) findViewById(R.id.nick);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        Intent i = getIntent();

        // getting fb_id, FEmail, FName  from intent
        FBid = i.getStringExtra(TAG_FBUSERID);
        FEmail = i.getStringExtra(TAG_FBUSEREMAIL);
        FName = i.getStringExtra(TAG_FBUSERNAME);


        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        session = new SessionManager(getApplicationContext());


// Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String nick = inputNick.getText().toString().trim();

                if (!nick.isEmpty()) {
                    registerUser(nick, FBid, FEmail);


                } else {
                    inputNick.setError("To pole jest wymagane");
                }
            }
        });

    }

    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent
                .FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    /**
     * Function to store user in MySQL database will post params(nick,
     * fb_id, email) to register url
     */
    private void registerUser(final String nick, final String id, final String FEmail) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Rejestrowanie ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTERBYFB,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Register Response: " + response.toString());
                        hideDialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");
                            if (!error) {
                                // User successfully stored in MySQL
                                // Now store the user in sqlite

                                String uid = jObj.getString("uid");
                                JSONObject user = jObj.getJSONObject("user");
                                String nick = user.getString("name");
                                String FEmail = user.getString("email");
                                String fbid = user.getString("fb_id");
                                String created_at = user.getString("created_at");

                                // Inserting row in users table
                                db.addUser(nick, FEmail, uid, fbid, created_at);
                                Toast.makeText(getApplicationContext(), "Użytkownik pomyślnie " +
                                        "zarejestrowany, zdobywasz 1 punkt reputacji za połączenie przez facebook'a!", Toast.LENGTH_LONG).show();
                                //Punkt reputacji jest dodawany w pliku PHP
                                session.setLogin(true);

                                // Launch login activity
                                Intent intent = new Intent(RegisterByFacebookActivity.this, Main2Activity
                                        .class);


                                startActivity(intent);
                                finish();

                            } else {

                                // Error occurred in registration. Get the error
                                // message
                                String errorMsg = jObj.getString("error_msg");
                                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Blad Rejestracji: " + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG)
                        .show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", nick);
                params.put("email", FEmail);
                params.put("fb_id", id);


                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing()) pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing()) pDialog.dismiss();
    }

    public void logout(View view) {
        LoginManager.getInstance().logOut();
        goLoginScreen();
    }
}
