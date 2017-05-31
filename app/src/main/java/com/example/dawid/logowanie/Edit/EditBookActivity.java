package com.example.dawid.logowanie.Edit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.Volley;
import com.example.dawid.logowanie.JSONParser;
import com.example.dawid.logowanie.List.BookListActivity;
import com.example.dawid.logowanie.R;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.AppConfig;


/**
 * Created by Janek on 02.05.2017.
 */

public class EditBookActivity extends AppCompatActivity {

    private EditText txtAuthorname;
    private EditText txtAuthorsname;
    private EditText txtTitle;
    private EditText txtCategory;
    private EditText txtEmail;
    private TextView txtStatus;
    Button btnSave;
    CheckBox simpleCheckBox;
    Button btnDelete;
    String status;
    String pid;
    String Status;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();


    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BOOK = "product";
    private static final String TAG_PID = "pid";
    private static final String TAG_NAME = "authorname";
    private static final String TAG_SNAME = "authorsname";
    private static final String TAG_TITLE = "title";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_STATUS = "status";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_book);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // save button
        btnSave = (Button) findViewById(R.id.btnSave);
        btnDelete = (Button) findViewById(R.id.btnDelete);

        simpleCheckBox = (CheckBox) findViewById(R.id.checkBox);

        // getting book details from intent
        Intent i = getIntent();
// pid =getIntent().getStringExtra(TAG_PID);
        // getting book id (pid) from intent
        pid = i.getStringExtra(TAG_PID);

        // Getting complete book details in background thread
        new GetBookDetails().execute();


        // save button click event
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // starting background task to update book
                String authorname = txtAuthorname.getText().toString();
                String authorsname = txtAuthorsname.getText().toString();
                String title = txtTitle.getText().toString();
                String category = txtCategory.getText().toString();
                String email = txtEmail.getText().toString();

                // simpleCheckBox = (CheckBox) findViewById(R.id.checkBox);

                Boolean checkBoxState = simpleCheckBox.isChecked();


                if (checkBoxState == true) {
                    status = "wypozyczona";
                } else {
                    status = "na_stanie";
                }

                if (!authorname.isEmpty() && !authorsname.isEmpty() && !title.isEmpty() &&
                        !category.isEmpty() && isValidEdit(authorname) && isValidEdit
                        (authorsname) && isValidEdit(category)) {

                    new SaveBookDetails().execute(authorname, authorsname, title, category,
                            email, status);
                } else {
                    Toast.makeText(getApplicationContext(), "Wypełnij poprawnie wszystkie pola!",
                            Toast.LENGTH_LONG).show();

                }
            }
        });

        // Delete button click event
        btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // deleting book in background thread
                String authorname = txtAuthorname.getText().toString();
                String authorsname = txtAuthorsname.getText().toString();
                String title = txtTitle.getText().toString();
                String category = txtCategory.getText().toString();
                String email = txtEmail.getText().toString();
                new DeleteBook().execute(authorname, authorsname, title, category, email, status);
            }
        });


        simpleCheckBox.setOnClickListener(new View.OnClickListener() {
            //simpleCheckBox = (CheckBox) findViewById(R.id.checkBox);
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {

                    simpleCheckBox.setText("WYPOZYCZONA");

                } else {
                    simpleCheckBox.setText("Na Stanie");

                }
            }

        });

    }

    /**
     * EditText Book Category, Authorname, Author Surname validation
     */

    public static boolean isValidEdit(final String text) {

        Pattern pattern;
        Matcher matcher;
        final String TEXT_PATTERN = ("^[a-zA-ZąćęłńóśżźĄĆĘŁŃÓŚŻŹ ]+$");
        pattern = Pattern.compile(TEXT_PATTERN);
        matcher = pattern.matcher(text);

        return matcher.matches();

    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Background Async Task to Get complete book details
     */
    class GetBookDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditBookActivity.this);
            pDialog.setMessage("Loading book details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting book details in background thread
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

                        // getting book details by making HTTP request
                        // Note that book details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(AppConfig.url_book_details,
                                "GET", params);

                        // check your log for json response
                        Log.d("Single Book Details", json.toString());

                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // successfully received book details
                            JSONArray bookObj = json.getJSONArray(TAG_BOOK); // JSON Array

                            // get first book object from JSON Array
                            JSONObject book = bookObj.getJSONObject(0);

                            // book with this pid found
                            // Edit Text
                            txtAuthorname = (EditText) findViewById(R.id.author_name);
                            txtAuthorsname = (EditText) findViewById(R.id.author_sname);
                            txtTitle = (EditText) findViewById(R.id.book_title);
                            txtCategory = (EditText) findViewById(R.id.book_category);
                            txtEmail = (EditText) findViewById(R.id.email);
                            txtStatus = (TextView) findViewById(R.id.status);

                            // display book data in EditText
                            txtAuthorname.setText(book.getString(TAG_NAME));
                            txtAuthorsname.setText(book.getString(TAG_SNAME));
                            txtTitle.setText(book.getString(TAG_TITLE));
                            txtCategory.setText(book.getString(TAG_CATEGORY));
                            txtEmail.setText(book.getString(TAG_EMAIL));
                            txtStatus.setText(book.getString(TAG_STATUS));

                            //Pobieranie statusu ksiazki z MySQL
                            Status = txtStatus.getText().toString();

                            if (Status.equals("wypozyczona")) {
                                simpleCheckBox.setChecked(true);
                                simpleCheckBox.setText("WYPOZYCZONA");
                            } else {
                                simpleCheckBox.setChecked(false);
                                simpleCheckBox.setText("Na Stanie");
                            }


                        } else {
                            // book with pid not found
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
     * Background Async Task to  Save Book Details
     */
    class SaveBookDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditBookActivity.this);
            pDialog.setMessage("Saving book ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        /**
         * Saving Book
         */
        protected String doInBackground(String... args) {

            String authorname = args[0];
            String authorsname = args[1];
            String title = args[2];
            String category = args[3];
            String email = args[4];
            String status = args[5];

            // getting updated data from EditTexts


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair(TAG_PID, pid));
            params.add(new BasicNameValuePair(TAG_NAME, authorname));
            params.add(new BasicNameValuePair(TAG_SNAME, authorsname));
            params.add(new BasicNameValuePair(TAG_TITLE, title));
            params.add(new BasicNameValuePair(TAG_CATEGORY, category));
            params.add(new BasicNameValuePair(TAG_EMAIL, email));
            params.add(new BasicNameValuePair(TAG_STATUS, status));


            // sending modified data through http request
            JSONObject json = jsonParser.makeHttpRequest(AppConfig.url_update_book, "POST", params);

            // check json success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully updated
                    Intent intent = new Intent(EditBookActivity.this, BookListActivity.class);
                    startActivity(intent);
                    finish();

                } else {
                    // failed to update book
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
            // dismiss the dialog once book uupdated
            pDialog.dismiss();
        }
    }

    /*****************************************************************
     * Background Async Task to Delete Book
     * */
    class DeleteBook extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EditBookActivity.this);
            pDialog.setMessage("Deleting Book...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Deleting Book
         */
        protected String doInBackground(String... args) {

            // Check for success tag
            int success;
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("pid", pid));

                // getting book details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(AppConfig.url_delete_product,
                        "POST", params);

                // check your log for json response
                Log.d("Delete Product", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // book successfully deleted

                    Intent intent = new Intent(EditBookActivity.this, BookListActivity.class);
                    startActivity(intent);
                    finish();
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
            // dismiss the dialog once book deleted
            pDialog.dismiss();

        }

    }
}