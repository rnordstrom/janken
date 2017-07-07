package client.janken;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import util.janken.HttpHandler;
import util.janken.Keys;

/**
 * This activity class manages the retrieval and
 * presentation of player stats, as fetched from
 * the server.
 */
public class StatsActivity extends ListActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        new GetStatsTask().execute();
    }

    private class GetStatsTask extends AsyncTask<Void, Void, List<String>>
    {
        @Override
        protected List<String> doInBackground(Void... voids)
        {
            try
            {
                HttpHandler handler = new HttpHandler();
                HashMap<String, String> params = new HashMap<>();

                params.put(Keys.ACTION, Keys.GET_STATS);
                HashMap<String, Integer> response = (HashMap<String, Integer>) handler.sendGet(params);

                List<String> stats = new ArrayList<>();
                String stat;

                for (String player : response.keySet())
                {
                    stat = player + ": " + response.get(player);
                    stats.add(stat);
                }

                return stats;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<String> stats)
        {
            try
            {
                ListView statList;
                statList = getListView();

                statList.setAdapter(new ArrayAdapter<>(StatsActivity.this, R.layout.link_item, stats));
            }
            catch (NullPointerException npe)
            {
                npe.printStackTrace(System.err);
            }
        }
    }
}
