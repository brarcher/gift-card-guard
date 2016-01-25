package protect.gift_card_guard;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GiftCardViewActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.giftcard_view_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        final Bundle b = getIntent().getExtras();
        final int giftCardId = b != null ? b.getInt("id") : 0;
        final boolean updateGiftCard = b != null && b.getBoolean("update", false);

        final EditText storeField = (EditText) findViewById(R.id.storeName);
        final EditText cardIdField = (EditText) findViewById(R.id.cardId);
        final EditText valueField = (EditText) findViewById(R.id.value);
        final TextView receiptField = (TextView) findViewById(R.id.receiptLocation);
        final LinearLayout hasReceiptButtonLayout = (LinearLayout) findViewById(R.id.hasReceiptButtonLayout);
        final LinearLayout noReceiptButtonLayout = (LinearLayout) findViewById(R.id.noReceiptButtonLayout);

        final Button saveButton = (Button) findViewById(R.id.saveButton);
        final Button cancelButton = (Button) findViewById(R.id.cancelButton);

        final DBHelper db = new DBHelper(this);

        noReceiptButtonLayout.setVisibility(View.GONE);
        hasReceiptButtonLayout.setVisibility(View.GONE);

        if(updateGiftCard)
        {
            final GiftCard giftCard = db.getGiftCard(giftCardId);

            storeField.setText(giftCard.store);
            cardIdField.setText(giftCard.cardId);
            valueField.setText(giftCard.value);
            receiptField.setText(giftCard.receipt);

            storeField.setEnabled(false);
            cardIdField.setEnabled(false);

            if(giftCard.receipt.isEmpty())
            {
                noReceiptButtonLayout.setVisibility(View.VISIBLE);
            }
            else
            {
                hasReceiptButtonLayout.setVisibility(View.VISIBLE);
            }

            setTitle(R.string.editGiftCardTitle);
        }
        else
        {
            noReceiptButtonLayout.setVisibility(View.VISIBLE);

            setTitle(R.string.addGiftCardTitle);
        }

        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                String store = storeField.getText().toString();
                String cardId = cardIdField.getText().toString();
                String value = valueField.getText().toString();
                String receipt = receiptField.getText().toString();

                if(store.isEmpty())
                {
                    Snackbar.make(v, R.string.noStoreError, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if(value.isEmpty())
                {
                    Snackbar.make(v, R.string.noValueError, Snackbar.LENGTH_LONG).show();
                    return;
                }

                if(updateGiftCard)
                {
                    db.updateGiftCard(giftCardId, store, cardId, value, receipt);
                }
                else
                {
                    db.insertGiftCard(store, cardId, value, receipt);
                }

                finish();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        final Bundle b = getIntent().getExtras();
        final boolean updateGiftCard = b != null && b.getBoolean("update", false);

        // Only display a menu if we are not adding a new gift card
        if(updateGiftCard)
        {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.delete_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        final Bundle b = getIntent().getExtras();
        final int giftCardId = b != null ? b.getInt("id") : 0;

        switch(id)
        {
            case R.id.action_delete:
                DBHelper db = new DBHelper(this);
                db.deleteGiftCard(giftCardId);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
