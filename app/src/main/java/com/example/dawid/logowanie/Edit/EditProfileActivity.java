package com.example.dawid.logowanie.Edit;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dawid.logowanie.JSONParser;
import com.example.dawid.logowanie.MainActivity;
import com.example.dawid.logowanie.R;
import com.facebook.FacebookSdk;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.AppConfig;

/**
 * Created by Janek on 15.05.2017.
 */

public class EditProfileActivity extends AppCompatActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    String name;
    String email;
    String points;
    String username;
    String uid;
    String useremail;
    String userphone;
    String userpointstext;
    String message;

    int ipoints;

    boolean check = false;
    boolean isemptyuserphone;

    private EditText txtName;
    private EditText txtEmail;
    private TextView txtPunkty;
    private EditText txtPhone;

    Button btnSaveProfile;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_UNIID = "unique_id";
    private static final String TAG_NAME = "name";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_POINTS = "Punkty";
    private static final String TAG_PHONE = "telefon";
    private static final String TAG_IUSER = "iuser";

    SmsReceiver receiver;
    IntentFilter filter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtEmail = (EditText) findViewById(R.id.user_email);
        txtName = (EditText) findViewById(R.id.user_name);
        txtPunkty = (TextView) findViewById(R.id.user_points);
        txtPhone = (EditText) findViewById(R.id.user_number);
        btnSaveProfile = (Button) findViewById(R.id.btnSaveProfile);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            name = bundle.getString("name");
            email = bundle.getString("email");
            points = bundle.getString("Punkty");
            uid = bundle.getString("unique_id");

        }
        txtEmail.setText(email);
        new GetUserDetails().execute();


        // Getting complete user details in background thread

        // save button click event
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update product
                username = txtName.getText().toString();
                userpointstext = txtPunkty.getText().toString();
                useremail = txtEmail.getText().toString();
                userphone = txtPhone.getText().toString();
                ipoints = Integer.parseInt(userpointstext);

                // String number = "1234567890";
                message = "Verification message.";
                if (!userphone.isEmpty()) {


                    SmsManager sm = SmsManager.getDefault();
                    sm.sendTextMessage(userphone, null, message, null, null);
                    receiver = new SmsReceiver();
                    filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                    registerReceiver(receiver, filter);


                }


                if (isValidPhone(userphone)) {
                    //Oczekiwanie 5 sekund na odpowiedz z klasy SmsReceiver jesli stan zmiennej
                    // boolean nie zmieni sie na true wyswietli ze podano nie poprawny numer
                    // (BETA VERSION)
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (check == false) {
                                Toast.makeText(getApplicationContext(), "Niepoprawny numer!",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }, 5400);


                } else {
                    txtPhone.setError("Niepoprawny numer");

                }
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    public static boolean isValidPhone(String phone) {
        String expression = "^([0-9\\+]|\\(\\d{1,3}\\))[0-9\\-\\. ]{8,8}$";
        CharSequence inputString = phone;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputString);
        if (matcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    //BETA VERSION
    class SmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SmsMessage msg;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                msg = msgs[0];
            } else {
                Object pdus[] = (Object[]) intent.getExtras().get("pdus");
                msg = SmsMessage.createFromPdu((byte[]) pdus[0]);
            }

            String number = msg.getOriginatingAddress();
            String message2 = msg.getMessageBody();
            check = true;
            if (PhoneNumberUtils.compare(number, userphone)) {
                //Sprawdzenie czy wczesniej przypisana wiadomosc jest taka ktora przyszla z powrotem
                if (message.equals(message2)) {


                    if (isemptyuserphone == true) {
                        ipoints = ipoints + 1;
                        userpointstext = Integer.toString(ipoints);
                        Toast.makeText(getApplicationContext(), "Zdobywasz punkt za dodanie " +
                                "numeru telefonu!", Toast.LENGTH_LONG).show();
                        new SaveUserDetails().execute(username, userpointstext, useremail,
                                userphone);

                    } else {
                        new SaveUserDetails().execute(username, userpointstext, useremail,
                                userphone);
                    }


                }

            }


        }
    }

    class GetUserDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditProfileActivity.this);
            pDialog.setMessage("Loading product details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting product details in background thread
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

                        JSONObject json = jsonParser.makeHttpRequest(AppConfig.url_user_details,
                                "GET", params);

                        // check your log for json response
                        Log.d("Single Product Details", json.toString());

                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // successfully received product details
                            JSONArray productObj = json

                                    .getJSONArray(TAG_IUSER); // JSON Array

                            // get first product object from JSON Array
                            JSONObject product = productObj.getJSONObject(0);


                            // display user data in EditText

                            txtName.setText(product.getString(TAG_NAME));
                            txtPunkty.setText(product.getString(TAG_POINTS));
                            txtPhone.setText(product.getString(TAG_PHONE));
                            String userphone = txtPhone.getText().toString();

                            if (userphone.isEmpty()) {
                                isemptyuserphone = true;
                            }


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
            pDialog = new ProgressDialog(EditProfileActivity.this);
            pDialog.setMessage("Saving product ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        /**
         * Saving product
         */
        protected String doInBackground(String... args) {

            String username = args[0];
            String userpoints = args[1];
            String useremail = args[2];
            String userphone = args[3];


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_UNIID, uid));
            params.add(new BasicNameValuePair(TAG_NAME, username));
            params.add(new BasicNameValuePair(TAG_POINTS, userpoints));
            params.add(new BasicNameValuePair(TAG_PHONE, userphone));
            params.add(new BasicNameValuePair(TAG_EMAIL, useremail));


            // sending modified data through http request
            JSONObject json = jsonParser.makeHttpRequest(AppConfig.URL_update_user, "POST", params);

            // check json success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully updated
                    Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                    intent.putExtra("userphone", userpointstext);
                    startActivity(intent);
                    finish();

                } else {
                    // failed to update user
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
            // dismiss the dialog once user updated
            pDialog.dismiss();
        }
    }


}



