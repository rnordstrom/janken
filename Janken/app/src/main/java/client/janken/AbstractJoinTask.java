package client.janken;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import util.janken.ConnectionHandler;
import util.janken.HttpHandler;
import util.janken.Keys;
import util.janken.ServerConnection;

/**
 * This abstract class represents the basic task of joining a game instance.
 */
public abstract class AbstractJoinTask extends AsyncTask<String, Void, Integer>
{
    @Override
    protected Integer doInBackground(String... strings)
    {
        HttpHandler handler = new HttpHandler();
        HashMap<String, String> params = new HashMap<>();

        params.put(Keys.ACTION, Keys.JOIN_INSTANCE);
        params.put(Keys.INSTANCE_NAME, strings[0]);

        String host = System.getProperty(Keys.IP);
        ArrayList<Integer> ports = (ArrayList<Integer>) handler.sendGet(params);

        ServerConnection serverConnection = new ServerConnection(host, ports.get(0));
        ConnectionHandler.setConnection(serverConnection);

        ServerConnection utilityConnection = new ServerConnection(host, ports.get(1));
        ConnectionHandler.setUtilityConnection(utilityConnection);

        if(serverConnection.isConnected())
            serverConnection.sendMessage(System.getProperty(Keys.USERNAME));

        return 0;
    }
}
