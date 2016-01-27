package protect.gift_card_guard;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

public class GiftCardViewActivity extends AppCompatActivity
{
    private static final String TAG = "GiftCardGuard";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private String capturedUncommittedReceipt = null;

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

        final Button captureButton = (Button) findViewById(R.id.captureButton);
        final Button updateButton = (Button) findViewById(R.id.updateButton);
        final Button viewButton = (Button) findViewById(R.id.viewButton);
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

            // Display the 'has receipt' buttons if the database records
            // the receipt or one was just captured
            if(giftCard.receipt.isEmpty() && capturedUncommittedReceipt == null)
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
            if(capturedUncommittedReceipt == null)
            {
                noReceiptButtonLayout.setVisibility(View.VISIBLE);
            }
            else
            {
                hasReceiptButtonLayout.setVisibility(View.VISIBLE);
            }

            setTitle(R.string.addGiftCardTitle);
        }

        View.OnClickListener captureCallback = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(capturedUncommittedReceipt != null)
                {
                    Log.i(TAG, "Deleting unsaved image: " + capturedUncommittedReceipt);
                    File unneededReceipt = new File(capturedUncommittedReceipt);
                    if(unneededReceipt.delete() == false)
                    {
                        Log.e(TAG, "Unable to delete unnecessary file: " + capturedUncommittedReceipt);
                    }
                    capturedUncommittedReceipt = null;
                }

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                {
                    File imageLocation = getNewImageLocation();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageLocation));
                    capturedUncommittedReceipt = imageLocation.getAbsolutePath();
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.pictureCaptureError, Toast.LENGTH_LONG).show();
                }
            }
        };

        captureButton.setOnClickListener(captureCallback);
        updateButton.setOnClickListener(captureCallback);

        viewButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(v.getContext(), ReceiptViewActivity.class);
                final Bundle b = new Bundle();

                final TextView receiptField = (TextView) findViewById(R.id.receiptLocation);

                String receipt = receiptField.getText().toString();
                if(capturedUncommittedReceipt != null)
                {
                    receipt = capturedUncommittedReceipt;
                }

                b.putString("receipt", receipt);
                i.putExtras(b);
                startActivity(i);
            }
        });

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

                if(capturedUncommittedReceipt != null)
                {
                    // Delete the old receipt, it is no longer needed
                    File oldReceipt = new File(receipt);
                    if(oldReceipt.delete() == false)
                    {
                        Log.e(TAG, "Unable to delete old receipt file: " + capturedUncommittedReceipt);
                    }

                    // Remember the new receipt to save
                    receipt = capturedUncommittedReceipt;
                    capturedUncommittedReceipt = null;
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
    protected void onDestroy()
    {
        if(capturedUncommittedReceipt != null)
        {
            // The receipt was captured but never used
            Log.i(TAG, "Deleting unsaved image: " + capturedUncommittedReceipt);
            File unneededReceipt = new File(capturedUncommittedReceipt);
            if(unneededReceipt.delete() == false)
            {
                Log.e(TAG, "Unable to delete unnecessary file: " + capturedUncommittedReceipt);
            }
            capturedUncommittedReceipt = null;
        }

        super.onDestroy();
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
                Log.e(TAG, "Deleting gift card: " + giftCardId);

                DBHelper db = new DBHelper(this);
                GiftCard giftCard = db.getGiftCard(giftCardId);
                if(giftCard.receipt.isEmpty() == false)
                {
                    File receiptFile = new File(giftCard.receipt);
                    if(receiptFile.delete() == false)
                    {
                        Log.e(TAG, "Unable to delete receipt image: " + receiptFile.getAbsolutePath());
                    }
                }

                db.deleteGiftCard(giftCardId);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File getNewImageLocation()
    {
        File imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if(imageDir.exists() == false)
        {
            if(imageDir.mkdirs() == false)
            {
                Log.e(TAG, "Failed to create receipts image directory");
                Toast.makeText(this, R.string.pictureCaptureError, Toast.LENGTH_LONG).show();
                return null;
            }
        }

        UUID imageFilename = UUID.randomUUID();
        File receiptFile = new File(imageDir, imageFilename.toString() + ".png");

        return receiptFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == REQUEST_IMAGE_CAPTURE)
        {
            if (resultCode == RESULT_OK)
            {
                Log.i(TAG, "Image file saved: " + capturedUncommittedReceipt);
            }
            else
            {
                Log.e(TAG, "Failed to create receipt image: " + resultCode);
                // No iamge was actually created, simply forget the patch
                capturedUncommittedReceipt = null;
            }

            onResume();
        }
    }
}
