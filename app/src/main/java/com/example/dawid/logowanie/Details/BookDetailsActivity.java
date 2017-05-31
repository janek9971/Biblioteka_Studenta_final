package com.example.dawid.logowanie.Details;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dawid.logowanie.BorrowBookActivity;
import com.example.dawid.logowanie.Edit.EditBookActivity;
import com.example.dawid.logowanie.Gmail.SendMailActivity;
import com.example.dawid.logowanie.JSONParser;
import com.example.dawid.logowanie.List.BookListActivity;
import com.example.dawid.logowanie.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.AppConfig;
import helper.SQLiteHandler;
import helper.SessionManager;

/**
 * Created by Janek on 03.05.2017.
 */

public class BookDetailsActivity extends AppCompatActivity {
    TextView txtAuthorname;
    TextView txtAuthorsname;
    TextView txtTitle;
    TextView txtCategory;
    TextView txtAddedby;
    TextView txtPid;
    //Powinno byc bookid ale ze wzgledu na duzo zmian w MySQL, plikach PHP oraz we wszysztkich aktywnosiach pozostaje pid
    String pid;
    String emailbooks;
    String emailcheck;
    String title;
    String status;
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();


    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BOOK = "product";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "authorname";
    private static final String TAG_SNAME = "authorsname";
    private static final String TAG_TITLE = "title";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_EMAILBOOK = "emailbook";
    private static final String TAG_EMAILUSER = "emailuser";
    private static final String TAG_UNAME = "name";
    private static final String TAG_STATUS = "status";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent i = getIntent();

        // getting  pid (bookid) from intent
        pid = i.getStringExtra(TAG_PID);
        //Pobrany z poprzedniej aktywnosci email uzytkownika ktory zostal przypisany do ksiazki podczas jej dodawania
        emailbooks = i.getStringExtra(TAG_EMAILBOOK);
        title = i.getStringExtra(TAG_TITLE);
        //Pobrany email uzytkownika w poprzedniej aktywnosci z SQLite
        emailcheck = i.getStringExtra(TAG_EMAILUSER);
        String uname = i.getStringExtra(TAG_UNAME);
        status = i.getStringExtra(TAG_STATUS);

        txtPid = (TextView) findViewById(R.id.pid);
        txtPid.setText(pid);
        txtAddedby = (TextView) findViewById(R.id.email);
        txtAddedby.setText("DODANE PRZEZ " + uname);


        // Getting complete book details in background thread
        new GetBookDetails().execute();


    }

    public void EditBook(View view) {
        //Sprawdzanie czy email uzytkownika przypisany do ksiazki jest taki sam jak email obecnie zalogowanego uzytkownika
        if (emailbooks.equals(emailcheck)) {
            Intent intent = new Intent(getBaseContext(), EditBookActivity.class);
            // sending pid to next activity
            intent.putExtra(TAG_PID, pid);

            // starting new activity and expecting some response back
            startActivity(intent);
        }  else {
            Toast.makeText(this, "Możesz edytować tylko dodane przez Siebie książki", Toast
                    .LENGTH_SHORT).show();

        }


    }

    public void BorrowBook(View view) {

        if (status.equals("na_stanie")) {
            Intent intent = new Intent(getBaseContext(), BorrowBookActivity.class);
            // sending pid to next activity
            intent.putExtra(TAG_EMAILBOOK, emailbooks);

            startActivity(intent);
        } else {
            Toast.makeText(this, "Ksiązką jest już wypożyczona", Toast.LENGTH_SHORT).show();

        }
    }
    //Zgłoszenie "złej" książki
    public void report(View view) {

        Intent i = new Intent(getApplicationContext(), SendMailActivity.class);
        i.putExtra(TAG_PID, pid);
        i.putExtra(TAG_TITLE, title);
        i.putExtra(TAG_EMAIL, emailcheck);

        startActivity(i);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    class GetBookDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(BookDetailsActivity.this);
            pDialog.setMessage("Loading books details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting books details in background thread
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
                        params.add(new BasicNameValuePair("pid", pid));

                        // getting books details by making HTTP request
                        JSONObject json = jsonParser.makeHttpRequest(AppConfig
                                .url_book_details, "GET", params);

                        // check your log for json response
                        Log.d("Single Book Details", json.toString());

                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // successfully received product details
                            JSONArray bookObj = json

                                    .getJSONArray(TAG_BOOK); // JSON Array

                            // get first product object from JSON Array
                            JSONObject book = bookObj.getJSONObject(0);

                            // books with this pid found

                            txtAuthorname = (TextView) findViewById(R.id.author_name);
                            txtAuthorsname = (TextView) findViewById(R.id.author_sname);
                            txtTitle = (TextView) findViewById(R.id.book_title);
                            txtCategory = (TextView) findViewById(R.id.book_category);


                            // display books data in EditText

                            txtAuthorname.setText("Imię autora:  " + book.getString(TAG_NAME));
                            txtAuthorsname.setText("Nazwisko autora:  " + book.getString
                                    (TAG_SNAME));
                            txtTitle.setText("Tytuł:  " + book.getString(TAG_TITLE));
                            txtCategory.setText("Kategoria:  " + book.getString(TAG_CATEGORY));

                        } else {
                            // books with pid not found
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
