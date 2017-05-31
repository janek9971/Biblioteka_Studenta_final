package com.example.dawid.logowanie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;

/**
 * Created by Janek on 18.05.2017.
 */

public class BorrowBookActivity extends AppCompatActivity {

    private static final String TAG_EMAILBOOK = "emailbook";

    String telefon;
    String fb_id;

    private ProgressDialog pDialog;

    // JSONparser class
    JSONParser jsonParser = new JSONParser();

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_IUSER = "iuser";
    private static final String TAG_FBID = "fb_id";
    private static final String TAG_PHONE = "telefon";

    String emailbooks;
    boolean issetfbid;
    boolean issetphone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Pobieranie szczegółów ksiązęk z intent
        Intent i = getIntent();

        // pobienie id ksiżki (pid) z intentu
        emailbooks = i.getStringExtra(TAG_EMAILBOOK);

        //Pobranie danych o użytkowniku który dodał tę książkę
        new GetUserDetails().execute();

    }

    // kontakt przez facebooka
    public void viafb(View view) {
        //issetfbid 168 linia sprawdzenie czy ma przypisany fb_id
        if (issetfbid == true) {


            String facebookUrl = "https://www.facebook.com/" + fb_id;
            try {
                int versionCode = getPackageManager().getPackageInfo("com.facebook.katana", 0)
                        .versionCode;
                if (versionCode >= 3002850) {
                    Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } else {
                    Uri uri = Uri.parse("fb://page/<id_here>");
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
            } catch (PackageManager.NameNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
            }
        } else {
            Toast.makeText(this, "Użytkownik nie połączył konta przez facebooka", Toast
                    .LENGTH_SHORT).show();

        }
    }

    // pożyczanie książki przez email
    public void viamail(View view) {
        String mail = "mailto: " + emailbooks;
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse(mail));
        startActivity(Intent.createChooser(emailIntent, "Send feedback"));
    }

    // pożyczanie książki przez telefon
    public void viaphone(View view) {
        //issetfbid 172 linia sprawdzenie czy ma przypisany numer telefonu
        if (issetphone == true) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + telefon));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "Użytkownik nie udostępnił numeru telefonu", Toast.LENGTH_SHORT)
                    .show();

        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    class GetUserDetails extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(BorrowBookActivity.this);
            pDialog.setMessage("Loading product details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        protected String doInBackground(String... params) {

            runOnUiThread(new Runnable() {
                public void run() {
                    int success;
                    try {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();

                        params.add(new BasicNameValuePair("email", emailbooks));

                        JSONObject json = jsonParser.makeHttpRequest(AppConfig.url_user_details,
                                "GET", params);


                        Log.d("Single Product Details", json.toString());

                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            JSONArray productObj = json.getJSONArray(TAG_IUSER);
                            JSONObject product = productObj.getJSONObject(0);

                            telefon = product.getString("telefon");
                            fb_id = product.getString("fb_id");
                            //Sprawdzenie czy użytkownik który dodał książkę połączył konto przez
                            // facebook'a
                            if (fb_id != null && !fb_id.isEmpty()) {
                                issetfbid = true;
                            }
                            //Sprawdzenie czy użytkownik który dodał książkę, dodał numer telefonu
                            if (telefon != null && !telefon.isEmpty()) {
                                issetphone = true;
                            }


                        } else {
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
        }
    }
}