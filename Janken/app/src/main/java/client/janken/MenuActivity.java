package client.janken;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * This activity class is transitory, and allows the client
 * to choose an action to be taken by the application before
 * being redirected to a suitable view and handler activity.
 */
public class MenuActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setupActivity();
    }

    private void setupActivity()
    {
        Button createOptionButton = (Button) findViewById(R.id.createOptionButton);
        Button joinOptionButton = (Button) findViewById(R.id.joinOptionButton);
        Button statsOptionButton = (Button) findViewById(R.id.statsOptionButton);

        createOptionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent createIntent = new Intent(MenuActivity.this, CreateActivity.class);
                startActivity(createIntent);
            }
        });

        joinOptionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent joinIntent = new Intent(MenuActivity.this, JoinActivity.class);
                startActivity(joinIntent);
            }
        });

        statsOptionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent statsIntent = new Intent(MenuActivity.this, StatsActivity.class);
                startActivity(statsIntent);
            }
        });
    }
}
