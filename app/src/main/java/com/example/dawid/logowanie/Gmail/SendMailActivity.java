package com.example.dawid.logowanie.Gmail;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.dawid.logowanie.R;

public class SendMailActivity extends Activity {

	private static final String TAG_PID = "pid";
	private static final String TAG_TITLE = "title";
	private static final String TAG_EMAIL = "email";
	String pid;
	String title;
	String emailcheck;
	TextView Subject;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_mail);
		final Button send = (Button) this.findViewById(R.id.button1);


		Intent i = getIntent();

		// Pobieranie szczegółów książek i użytkownika
		pid = i.getStringExtra(TAG_PID);
		title = i.getStringExtra(TAG_TITLE);
		emailcheck = i.getStringExtra(TAG_EMAIL);
		Subject = (TextView) findViewById(R.id.editText4);
		Subject.setText("Zgłoszone przez: " + emailcheck + "    ID: " + pid + "      Tytuł:   " + "\"" + title + "\"");

		send.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				Log.i("SendMailActivity", "Send Button Clicked.");

				String fromEmail = "bibliotekastudenta@gmail.com";
				String fromPassword = "zaq1@WSX";
				String toEmails = "wladcabibliotetkistudenta@gmail.com";
				List<String> toEmailList = Arrays.asList(toEmails
						.split("\\s*,\\s*"));
				Log.i("SendMailActivity", "To List: " + toEmailList);
				String emailSubject = ((TextView) findViewById(R.id.editText4))
						.getText().toString();

				String emailBody = ((TextView) findViewById(R.id.editText5))
						.getText().toString();

				new SendMailTask(SendMailActivity.this).execute(fromEmail,
						fromPassword, toEmailList, emailSubject, emailBody);


			}
		});
	}
}
