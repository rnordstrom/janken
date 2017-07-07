package client.janken;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import util.janken.ConnectionHandler;
import util.janken.HttpHandler;
import util.janken.Keys;
import util.janken.ServerConnection;

/**
 * This activity class handles the retrieval and presentation
 * of data that is necessary to view a list of available game
 * instances and join a given instance by choice. A connection
 * to the instance will be made once the class has finished
 * its intended tasks.
 */
public class JoinActivity extends ListActivity implements AdapterView.OnItemClickListener
{
    private List<String> instances;
    private String instanceName;
    private Button joinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        setupActivity();
    }

    private void setupActivity()
    {
        joinButton = (Button) findViewById(R.id.joinButton);
        joinButton.setEnabled(false);
        new GetInstancesTask().execute();

        joinButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new JoinTask().execute(instanceName);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
    {
        instanceName = instances.get(position);
        joinButton.setEnabled(true);
    }

    private class GetInstancesTask extends AsyncTask<Void, Void, List<String>>
    {
        @Override
        protected List<String> doInBackground(Void... voids)
        {
            HttpHandler handler = new HttpHandler();
            HashMap<String, String> params = new HashMap<>();

            params.put(Keys.ACTION, Keys.GET_INSTANCES);

            instances = (List<String>) handler.sendGet(params);

            return instances;
        }

        @Override
        protected void onPostExecute(List<String> instances)
        {
            if(instances.isEmpty())
                joinButton.setVisibility(View.GONE);

            try
            {
                ListView instanceList;
                instanceList = getListView();
                instanceList.setAdapter(new ArrayAdapter<>(JoinActivity.this, R.layout.link_item, instances));
                instanceList.setOnItemClickListener(JoinActivity.this);
            }
            catch (NullPointerException npe)
            {
                joinButton.setVisibility(View.GONE);
                npe.printStackTrace(System.err);
            }
        }
    }

    private class JoinTask extends AbstractJoinTask
    {
        @Override
        protected Integer doInBackground(String... strings)
        {
            super.doInBackground(strings);

            return null;
        }

        @Override
        protected void onPostExecute(Integer status)
        {
            Intent intent = new Intent(JoinActivity.this, PlayActivity.class);
            startActivity(intent);
        }
    }
}
