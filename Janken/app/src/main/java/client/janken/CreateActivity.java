package client.janken;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;

import util.janken.ConnectionHandler;
import util.janken.HttpHandler;
import util.janken.Keys;
import util.janken.ServerConnection;

/**
 * This activity class handles the creation of a game
 * instance on the server. The name of the instance is
 * provided by the client. The client will be immediately
 * connected to the game instance upon completion of the
 * class' tasks.
 */
public class CreateActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        setupActivity();
    }

    private void setupActivity()
    {
        final EditText gameTitleField = (EditText) findViewById(R.id.gameTitleField);
        Button createButton = (Button) findViewById(R.id.createButton);

        createButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new CreateGameTask().execute(gameTitleField.getText().toString());
            }
        });
    }

    private class CreateGameTask extends AbstractJoinTask
    {
        @Override
        protected Integer doInBackground(String... strings)
        {
            HashMap<String, String> params = new HashMap<>();
            HttpHandler handler = new HttpHandler();

            params.put(Keys.ACTION, Keys.CREATE_INSTANCE);
            params.put(Keys.INSTANCE_NAME, strings[0]);
            int status = handler.sendPost(params);

            if(status >= 200 && status < 300)
                super.doInBackground(strings);

            return status;
        }

        @Override
        protected void onPostExecute(Integer status)
        {
            if(status >= 200 && status < 300)
            {
                Intent intent = new Intent(CreateActivity.this, PlayActivity.class);
                startActivity(intent);
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateActivity.this);
                builder.setMessage(getString(R.string.game_exists))
                        .setTitle(getString(R.string.game_creation_failed));
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
}
