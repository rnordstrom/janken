package client.janken;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import util.janken.ConnectionHandler;
import util.janken.Keys;
import util.janken.ServerConnection;

/**
 * This activity class manages a game session in progress.
 * It contains methods that prepare and update the view
 * in response to client actions and server states,
 * in addition to internally managing network connections
 * and communications with the server. The class makes use
 * of scheduled multi-threading and parallel execution of
 * asynchronous tasks.
 */
public class PlayActivity extends AppCompatActivity
{
    private List<String> players;
    private Button readyButton;
    private Button waitButton;
    private Button rockButton;
    private Button paperButton;
    private Button scissorsButton;
    private TextView winnerText;
    private TextView winnerName;
    private ProgressBar gameProgress;
    private ServerConnection serverConnection;
    private ServerConnection utilityConnection;
    private TextView player1;
    private TextView player2;
    private TextView player3;
    private TextView player4;
    private final ScheduledExecutorService heartBeatScheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService updateScheduler = Executors.newScheduledThreadPool(1);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setupActivity();
        new WaitForConnectionTask().execute();
    }

    private void setupActivity()
    {
        players = new ArrayList<>();

        serverConnection = ConnectionHandler.getConnection();
        utilityConnection = ConnectionHandler.getUtilityConnection();

        heartBeatScheduler.scheduleAtFixedRate(new HeartBeatTask(), 1, 1, TimeUnit.SECONDS);
        updateScheduler.scheduleAtFixedRate(new GetPlayerListTask(), 1, 1, TimeUnit.SECONDS);

        readyButton = (Button) findViewById(R.id.readyButton);
        waitButton = (Button) findViewById(R.id.waitButton);
        rockButton = (Button) findViewById(R.id.rockButton);
        paperButton = (Button) findViewById(R.id.paperButton);
        scissorsButton = (Button) findViewById(R.id.scissorsButton);
        winnerText = (TextView) findViewById(R.id.winnerText);
        winnerName = (TextView) findViewById(R.id.winnerName);
        gameProgress = (ProgressBar) findViewById(R.id.gameProgress);

        readyButton.setEnabled(false);
        waitButton.setEnabled(false);
        rockButton.setEnabled(false);
        paperButton.setEnabled(false);
        scissorsButton.setEnabled(false);
        winnerText.setVisibility(View.GONE);

        player1 = (TextView) findViewById(R.id.player1);
        player2 = (TextView) findViewById(R.id.player2);
        player3 = (TextView) findViewById(R.id.player3);
        player4 = (TextView) findViewById(R.id.player4);

        readyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new ReadyTask().execute(Keys.READY_MESSAGE);
                winnerText.setVisibility(View.GONE);
                winnerName.setText("");
                gameProgress.setVisibility(View.VISIBLE);
            }
        });

        waitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new ReadyTask().execute(Keys.WAIT_MESSAGE);
                winnerText.setVisibility(View.GONE);
                winnerName.setText("");
                gameProgress.setVisibility(View.VISIBLE);
            }
        });

        rockButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                gameProgress.setVisibility(View.VISIBLE);
                setPlayButtons(false);
                new ChoiceTask().execute(Keys.ROCK);
            }
        });

        paperButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                gameProgress.setVisibility(View.VISIBLE);
                setPlayButtons(false);
                new ChoiceTask().execute(Keys.PAPER);
            }
        });

        scissorsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                gameProgress.setVisibility(View.VISIBLE);
                setPlayButtons(false);
                new ChoiceTask().execute(Keys.SCISSORS);
            }
        });
    }

    private void setPlayButtons(boolean state)
    {
        rockButton.setEnabled(state);
        paperButton.setEnabled(state);
        scissorsButton.setEnabled(state);
    }

    private void setReadyButtons(boolean state)
    {
        readyButton.setEnabled(state);
        waitButton.setEnabled(state);
    }

    private void updatePlayerList(List<String> players)
    {
        for (int i = 0; i < Keys.MAX_PLAYERS; i++)
        {
            switch(i)
            {
                case 0 : player1.setText("");
                    break;
                case 1 : player2.setText("");
                    break;
                case 2 : player3.setText("");
                    break;
                case 3 : player4.setText("");
                    break;
            }
        }

        for (int i = 0; i < players.size(); i++)
        {
            switch(i)
            {
                case 0 : player1.setText(players.get(i));
                    break;
                case 1 : player2.setText(players.get(i));
                    break;
                case 2 : player3.setText(players.get(i));
                    break;
                case 3 : player4.setText(players.get(i));
                    break;
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        System.out.println("Destroying game session...");
        heartBeatScheduler.shutdown();
        updateScheduler.shutdown();
        ConnectionHandler.getConnection().close();
        ConnectionHandler.getUtilityConnection().close();
        ConnectionHandler.setConnection(null);
        ConnectionHandler.setUtilityConnection(null);
        System.out.println("Game session destroyed!");
    }

    private class WaitForConnectionTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            updatePlayerList(players);
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            serverConnection.waitForMessage();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            if(players.size() < 4)
                setReadyButtons(true);
            else
                setPlayButtons(true);

            gameProgress.setVisibility(View.GONE);
        }
    }

    private class WaitForGameReadyTask extends AsyncTask<Void, Void, String>
    {
        @Override
        protected String doInBackground(Void... voids)
        {
            String input = serverConnection.waitForMessage();
            String status = "";

            if(input.equals(Keys.WAIT_MESSAGE) || input.equals(Keys.PLAYING_MESSAGE))
                status = input;

            System.out.println(status + " received!");

            return status;
        }

        @Override
        protected void onPostExecute(String status)
        {
            updatePlayerList(players);

            if(status.equals(Keys.WAIT_MESSAGE))
                new WaitForConnectionTask().execute();
            else if(status.equals(Keys.PLAYING_MESSAGE))
            {
                setPlayButtons(true);
                gameProgress.setVisibility(View.GONE);
            }
        }
    }

    private class ReadyTask extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... strings)
        {
            serverConnection.sendMessage(strings[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            setReadyButtons(false);
            new WaitForGameReadyTask().execute();
        }
    }

    private class ChoiceTask extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... strings)
        {
            serverConnection.sendMessage(strings[0]);

            return serverConnection.waitForMessage();
        }

        @Override
        protected void onPostExecute(String result)
        {
            if(!result.equals(Keys.WAIT_MESSAGE))
            {
                setReadyButtons(true);

                winnerText.setVisibility(View.VISIBLE);
                winnerName.setText(result);
                gameProgress.setVisibility(View.GONE);
            }
            else
                new WaitForConnectionTask().execute();
        }
    }

    private class HeartBeatTask implements Runnable
    {
        @Override
        public void run()
        {
            utilityConnection.sendMessage(Keys.HEARTBEAT);
        }
    }

    private class GetPlayerListTask implements Runnable
    {
        @Override
        public void run()
        {
            List<String> players = utilityConnection.waitForPlayerlist();

            new UpdatePlayerListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, players);
        }
    }

    private class UpdatePlayerListTask extends AsyncTask<List<String>, Void, List<String>>
    {
        @Override
        protected List<String> doInBackground(List<String>... lists)
        {
            players = lists[0];

            return players;
        }

        @Override
        protected void onPostExecute(List<String> players)
        {
            updatePlayerList(players);
        }
    }
}
