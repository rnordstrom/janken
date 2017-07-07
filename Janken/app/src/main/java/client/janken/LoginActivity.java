package client.janken;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.util.HashMap;

import util.janken.HttpHandler;
import util.janken.Keys;

/**
 * This class handles login or registration of a player account.
 * The client enters their (desired) username and password in the
 * given fields, both of which are sent to the server for verification.
 */
public class LoginActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActivity();
    }

    private void setupActivity()
    {
        final EditText usernameField = (EditText) findViewById(R.id.usernameField);
        final EditText passwordField = (EditText) findViewById(R.id.passwordField);
        Button loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();

                System.setProperty(Keys.USERNAME, username);
                new AccountTask().execute(username, password);
            }
        });
    }

    private class AccountTask extends AsyncTask<String, Void, Integer>
    {
        @Override
        protected Integer doInBackground(String... strings)
        {
            HashMap<String, String> params = new HashMap<>();
            HttpHandler handler = new HttpHandler();

            params.put(Keys.ACTION, Keys.ACCOUNT);
            params.put(Keys.USERNAME, strings[0]);
            params.put(Keys.PASSWORD, strings[1]);

            return handler.sendPost(params);
        }

        @Override
        protected void onPostExecute(Integer status)
        {
            if(status >= 200 && status < 300)
            {
                Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                startActivity(intent);
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(getString(R.string.access_denied))
                        .setTitle(getString(R.string.login_failed));
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
}
