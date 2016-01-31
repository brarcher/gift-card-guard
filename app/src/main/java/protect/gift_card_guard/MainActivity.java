package protect.gift_card_guard;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        updateGiftCardList();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        updateGiftCardList();
    }

    private void updateGiftCardList()
    {
        final ListView cardList = (ListView) findViewById(R.id.list);
        final TextView helpText = (TextView) findViewById(R.id.helpText);
        final DBHelper db = new DBHelper(this);

        if(db.getGiftCardCount() > 0)
        {
            cardList.setVisibility(View.VISIBLE);
            helpText.setVisibility(View.GONE);
        }
        else
        {
            cardList.setVisibility(View.GONE);
            helpText.setVisibility(View.VISIBLE);
        }

        Cursor cardCursor = db.getGiftCardCursor();

        final GiftCardCursorAdapter adapter = new GiftCardCursorAdapter(this, cardCursor);
        cardList.setAdapter(adapter);

        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Cursor selected = (Cursor) parent.getItemAtPosition(position);
                GiftCard giftCard = GiftCard.toGiftCard(selected);

                Intent i = new Intent(view.getContext(), GiftCardViewActivity.class);
                final Bundle b = new Bundle();
                b.putInt("id", giftCard.id);
                b.putBoolean("update", true);
                i.putExtras(b);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_add)
        {
            Intent i = new Intent(getApplicationContext(), GiftCardViewActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_settings)
        {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}