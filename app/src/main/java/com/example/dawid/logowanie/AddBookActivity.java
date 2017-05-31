package com.example.dawid.logowanie;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import com.example.dawid.logowanie.JSONParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import activity.LoginActivity;
import app.AppConfig;
import helper.SQLiteHandler;
import helper.SessionManager;

import com.example.dawid.logowanie.List.BookListActivity;


/**
 * Created by Dawid on 25.04.2017.
 */

public class AddBookActivity extends AppCompatActivity {

    private ProgressDialog pDialog;

    private EditText author_name;
    private EditText author_sname;
    private EditText book_title;
    private EditText book_category;

    private SQLiteHandler db;
    private SessionManager session;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    JSONParser jsonParser = new JSONParser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        author_name = (EditText) findViewById(R.id.author_name);
        author_sname = (EditText) findViewById(R.id.author_sname);
        book_title = (EditText) findViewById(R.id.book_title);
        book_category = (EditText) findViewById(R.id.book_category);

        author_name.setFilters(new InputFilter[]{getNameEditTextFilter()});
        author_sname.setFilters(new InputFilter[]{getNameEditTextFilter()});
        book_category.setFilters(new InputFilter[]{getNameEditTextFilter()});


        // SqLiteHandler
        db = new SQLiteHandler(getApplicationContext());

        // SessionManager
        session = new SessionManager(getApplicationContext());

        // sprawdzenie czy użytkownik jest zalogowany
        if (!session.isLoggedIn()) {
            logoutUser();
            Toast.makeText(this, "Tylko dla zalogowanych użytkowników", Toast.LENGTH_SHORT).show();
        }

        // Pobieranie danych użytkowników z SQLiteHandler
        HashMap<String, String> user = db.getUserDetails();

        final String name = user.get("name");
        final String email = user.get("email");


        // Stworzenie przycisku
        Button btnBookAdd = (Button) findViewById(R.id.btnBookAdd);


        // Działanie przycisku
        btnBookAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // Stowrzenie nowej książki w tle

                String authorname = author_name.getText().toString();
                String authorsname = author_sname.getText().toString();
                String title = book_title.getText().toString();
                String category = book_category.getText().toString();

                new CreateNewBook().execute(authorname, authorsname, title, category, email, name);

            }
        });


    }

    // Zabezpiecznie wprowadzanego tekstu
    public static InputFilter getNameEditTextFilter() {
        return new InputFilter() {

            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int
                    dstart, int dend) {

                boolean keepOriginal = true;
                StringBuilder sb = new StringBuilder(end - start);
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (isCharAllowed(c)) sb.append(c);
                    else keepOriginal = false;
                }
                if (keepOriginal) return null;
                else {
                    if (source instanceof Spanned) {
                        SpannableString sp = new SpannableString(sb);
                        TextUtils.copySpansFrom((Spanned) source, start, sb.length(), null, sp, 0);
                        return sp;
                    } else {
                        return sb;
                    }
                }
            }

            private boolean isCharAllowed(char c) {
                Pattern ps = Pattern.compile("^[a-zA-ZąćęłńóśżźĄĆĘŁŃÓŚŻŹ ]+$");
                Matcher ms = ps.matcher(String.valueOf(c));
                return ms.matches();
            }
        };
    }

    private void logoutUser() {
        session.setLogin(false);
        db.deleteUsers();
        // Launching the login activity
        Intent intent = new Intent(AddBookActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    // Tworzenie nowego wpisu do bazy danych
    class CreateNewBook extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddBookActivity.this);
            pDialog.setMessage("Dodawanie książki");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        }

        // Tworzenie nowej ksiażki
        protected String doInBackground(String... args) {

            String authorname = args[0];
            String authorsname = args[1];
            String title = args[2];
            String category = args[3];
            String email = args[4];
            String name = args[5];

            // Parametry książki
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("authorname", authorname));
            params.add(new BasicNameValuePair("authorsname", authorsname));
            params.add(new BasicNameValuePair("title", title));
            params.add(new BasicNameValuePair("category", category));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("name", name));


            // pobieranie JSON Object
            JSONObject json = jsonParser.makeHttpRequest(AppConfig.url_create_product, "POST",
                    params);

            Log.d("Create Response", json.toString());

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    //książka dodana poprawnie
                    Intent i = new Intent(getApplicationContext(), BookListActivity.class);
                    startActivity(i);

                    // zamykanie tego ekranu
                    finish();
                } else {
                    // nie udało się dodać książki
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
        }

    }

}