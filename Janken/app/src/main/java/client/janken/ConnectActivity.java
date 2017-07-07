package client.janken;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import util.janken.Keys;

/**
 * This activity class verifies the connectivity between the client
 * and the server. No connections to the server are made.
 * The host address of the server is provided by the client.
 */
public class ConnectActivity extends AppCompatActivity
{
    private EditText ipField;
    private ProgressBar progress;
    private boolean ipIsReachable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        setupActivity();
    }

    private void setupActivity()
    {
        ipField = (EditText) findViewById(R.id.ipField);
        Button connectButton = (Button) findViewById(R.id.connectButton);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setVisibility(View.GONE);

        connectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ConnectivityManager connectivityManager = (ConnectivityManager)
                        getSystemService(view.getContext().CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected())
                {
                    progress.setVisibility(View.VISIBLE);
                    new CheckConnectionTask().execute(ipField.getText().toString());
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ConnectActivity.this);
                    builder.setMessage(getString(R.string.no_internet))
                            .setTitle(getString(R.string.no_connection));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private class CheckConnectionTask extends AsyncTask<String, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(String... ips)
        {
            try
            {
                InetAddress ip = InetAddress.getByName(ips[0]);

                if(ip.toString().contains("10.0.2.2"))
                    ip = InetAddress.getLocalHost();

                if(ip.isReachable(1000))
                    ipIsReachable = true;
            }
            catch (UnknownHostException uhe)
            {
                uhe.printStackTrace(System.err);
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace(System.err);
            }

            return ipIsReachable;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean)
        {
            progress.setVisibility(View.GONE);

            if(ipIsReachable)
            {
                Intent intent = new Intent(ConnectActivity.this, LoginActivity.class);
                String ip = ipField.getText().toString();
                String url = "http://" + ip + ":8080/janken_server_web/LobbyServlet";
                System.setProperty(Keys.IP, ip);
                System.setProperty(Keys.URL, url);
                startActivity(intent);
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ConnectActivity.this);
                builder.setMessage(getString(R.string.unable_to_connect))
                        .setTitle(getString(R.string.connection_failed));
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }
}
