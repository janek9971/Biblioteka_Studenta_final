package com.example.dawid.logowanie.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dawid.logowanie.AddBookActivity;
import com.example.dawid.logowanie.Details.BookDetailsActivity;
import com.example.dawid.logowanie.Edit.EditBookActivity;
import com.example.dawid.logowanie.JSONParser;
import com.example.dawid.logowanie.R;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import activity.LoginActivity;
import app.AppConfig;
import helper.SQLiteHandler;
import helper.SessionManager;


/**
 * Created by Dawid on 25.04.2017.
 */

public class BookListActivity extends ListActivity {

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_BOOKS = "products";
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

    // Tworzenie obiektu JSON Parser

    JSONParser jParser = new JSONParser();
    ArrayList<HashMap<String, String>> booksList;

    //JSONArray
    JSONArray books = null;

    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.all_books);

        // SqLiteHandler
        db = new SQLiteHandler(getApplicationContext());
        // SessionManager
        session = new SessionManager(getApplicationContext());


        Bundle bundle = getIntent().getExtras();

        // Pobieranie danych użytkownika z SQLiteHandler
        HashMap<String, String> user = db.getUserDetails();
        final String emailcheck = user.get("email");

        // Hashmap for ListView
        booksList = new ArrayList<HashMap<String, String>>();

        // ładowanie książek w tle
        new LoadAllBooks().execute();

        // Pobranie ListView
        ListView lv = getListView();


        // Po wybraniu jednej ksiażki odpalanie ekranu edycji książki
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // pobrani wartości z wybranej książki
                String pid = ((TextView) view.findViewById(R.id.pid)).getText().toString();
                String emailbooks = ((TextView) view.findViewById(R.id.email)).getText().toString();
                String uname = ((TextView) view.findViewById(R.id.uname)).getText().toString();
                String title = ((TextView) view.findViewById(R.id.book_title)).getText().toString();
                String status = ((TextView) view.findViewById(R.id.isborrow)).getText().toString();


                // rozpoczęcie nowej aktywności
                Intent in = new Intent(getApplicationContext(), BookDetailsActivity.class);
                // wysyłanie pid do następniej aktywności
                in.putExtra(TAG_PID, pid);
                in.putExtra(TAG_EMAILBOOK, emailbooks);
                in.putExtra(TAG_TITLE, title);
                in.putExtra(TAG_EMAILUSER, emailcheck);
                in.putExtra(TAG_UNAME, uname);
                in.putExtra(TAG_STATUS, status);

                // rozpoczęcie nowej aktywności i oczekiwanie na jakąś odpowiedź
                startActivityForResult(in, 100);

            }
        });

    }

    // Odpowiedź z EditBookActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Jeśli resultCode otrzymano 100 oznacz to, że użytkownik edytował książke
        if (resultCode == 100) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    // AsyncTask działający w tle w celu załadowania wszystkich książek poprzez HTTP Request
    class LoadAllBooks extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(BookListActivity.this);
            pDialog.setMessage("ładowanie ksiażek, proszę czekać...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Pobieranie wszystkich książek z URL
        protected String doInBackground(String... args) {
            // Parametry książek
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            JSONObject json = jParser.makeHttpRequest(AppConfig.url_all_books, "GET", params);

            Log.d("All Books: ", json.toString());

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // książki znalezione
                    // Pobranie tablicy książek
                    books = json.getJSONArray(TAG_BOOKS);

                    // Pętla przez wszystkie książki
                    for (int i = 0; i < books.length(); i++) {
                        JSONObject c = books.getJSONObject(i);

                        // Przeechowanie każdego obiektu JSON w zmiennych
                        String id = c.getString(TAG_PID);
                        String author_name = c.getString(TAG_NAME);
                        String author_sname = c.getString(TAG_SNAME);
                        String title = c.getString(TAG_TITLE);
                        String category = c.getString(TAG_CATEGORY);
                        String email = c.getString(TAG_EMAIL);
                        String name = c.getString(TAG_UNAME);
                        String status = c.getString(TAG_STATUS);

                        // tworzenie nowej HashMapy
                        HashMap<String, String> map = new HashMap<String, String>();

                        // dodawanie każdego Node do HashMap
                        map.put(TAG_PID, id);
                        map.put(TAG_NAME, author_name);
                        map.put(TAG_SNAME, author_sname);
                        map.put(TAG_TITLE, title);
                        map.put(TAG_CATEGORY, category);
                        map.put(TAG_EMAIL, email);
                        map.put(TAG_UNAME, name);
                        map.put(TAG_STATUS, status);
                        // dodawanie HashList do ArrayList
                        booksList.add(map);
                    }
                } else {
                    // nie znalezniono książek
                    // Odpal aktywność dodawania książek
                    Intent i = new Intent(getApplicationContext(), AddBookActivity.class);
                    // Zamykanie poprzednich aktywności
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            // aktualizuj UI z Background Thread
            runOnUiThread(new Runnable() {
                public void run() {

                    // Aktualizuj sparsowane JSON data w ListView
                    ListAdapter adapter = new SimpleAdapter(BookListActivity.this, booksList, R
                            .layout.list_book, new String[]{TAG_PID, TAG_NAME, TAG_SNAME,
                            TAG_TITLE, TAG_CATEGORY, TAG_EMAIL, TAG_UNAME, TAG_STATUS}, new
                            int[]{R.id.pid, R.id.author_name, R.id.author_sname, R.id.book_title,
                            R.id.book_category, R.id.email, R.id.uname, R.id.isborrow});
                    // aktualizuj listview
                    setListAdapter(adapter);
                }
            });

        }

    }

}
