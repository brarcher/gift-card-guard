package protect.gift_card_guard;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.res.builder.RobolectricPackageManager;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class GiftCardViewActivityTest
{
    /**
     * Register a handler in the package manager for a image capture intent
     */
    private void registerMediaStoreIntentHandler()
    {
        // Add something that will 'handle' the media capture intent
        RobolectricPackageManager packageManager = shadowOf(RuntimeEnvironment.application.getPackageManager());

        ResolveInfo info = new ResolveInfo();
        info.isDefault = true;

        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.packageName = "does.not.matter";
        info.activityInfo = new ActivityInfo();
        info.activityInfo.applicationInfo = applicationInfo;
        info.activityInfo.name = "DoesNotMatter";

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        packageManager.addResolveInfoForIntent(intent, info);
    }

    /**
     * Save a gift card and check that the database contains the
     * expected values
     *
     * @param activity
     * @param store
     * @param cardId
     * @param value
     * @param expectedReceipt
     */
    private void saveGiftCardWithArguments(final Activity activity,
                                           final String store, final String cardId,
                                           final String value, final String expectedReceipt,
                                           boolean creatingNewCard)
    {
        DBHelper db = new DBHelper(activity);
        if(creatingNewCard)
        {
            assertEquals(0, db.getGiftCardCount());
        }
        else
        {
            assertEquals(1, db.getGiftCardCount());
        }

        final EditText storeField = (EditText) activity.findViewById(R.id.storeName);
        final EditText cardIdField = (EditText) activity.findViewById(R.id.cardId);
        final EditText valueField = (EditText) activity.findViewById(R.id.value);

        final Button saveButton = (Button) activity.findViewById(R.id.saveButton);

        storeField.setText(store);
        cardIdField.setText(cardId);
        valueField.setText(value);

        assertEquals(false, activity.isFinishing());
        saveButton.performClick();
        assertEquals(true, activity.isFinishing());

        assertEquals(1, db.getGiftCardCount());

        GiftCard card = db.getGiftCard(1);
        assertEquals(store, card.store);
        assertEquals(cardId, card.cardId);
        assertEquals(value, card.value);
        assertEquals(expectedReceipt, card.receipt);
    }

    /**
     * Initiate and complete an image capture, returning the
     * location of the resulting file if the capture was
     * a success.
     *
     * @param success
     *      true if the image capture is a success, and a
     *      file is to be created at the requested location,
     *      false otherwise.
     * @param buttonId
     *      id of the button to press to initiate the capture
     * @return The URI pointing to the image file location,
     * regardless if the operation was successful or not.
     */
    private Uri captureImageWithResult(final Activity activity, final int buttonId, final boolean success) throws IOException
    {
        // Start image capture
        final Button captureButton = (Button) activity.findViewById(buttonId);
        captureButton.performClick();

        ShadowActivity.IntentForResult intentForResult = shadowOf(activity).peekNextStartedActivityForResult();
        assertNotNull(intentForResult);

        Intent intent = intentForResult.intent;
        assertNotNull(intent);

        String action = intent.getAction();
        assertNotNull(action);
        assertEquals(MediaStore.ACTION_IMAGE_CAPTURE, action);

        Bundle bundle = intent.getExtras();
        assertNotNull(bundle);

        assertEquals(false, bundle.isEmpty());
        Uri argument = bundle.getParcelable(MediaStore.EXTRA_OUTPUT);
        assertNotNull(argument);
        assertTrue(argument.toString().length() > 0);

        // Respond to image capture, success
        shadowOf(activity).receiveResult(
                intent,
                success ? Activity.RESULT_OK : Activity.RESULT_CANCELED,
                null);

        if(success)
        {
            File imageFile = new File(argument.getPath());
            assertEquals(false, imageFile.exists());
            boolean result = imageFile.createNewFile();
            assertTrue(result);
        }

        return argument;
    }

    private void checkFieldProperties(final Activity activity, final int id, final int visibility,
                                      final String contents)
    {
        final View view = (View) activity.findViewById(id);
        assertNotNull(view);
        assertEquals(visibility, view.getVisibility());
        if(contents != null)
        {
            TextView textView = (TextView)view;
            assertEquals(contents, textView.getText().toString());
        }
    }

    private void checkAllFields(final Activity activity, final String store, final String cardId,
                                final String value, final String comittedReceipt, boolean hasUncommitedReceipt)
    {
        final boolean hasReceipt = (comittedReceipt.length() > 0) || hasUncommitedReceipt;

        final int hasReceiptVisibility = hasReceipt ? View.VISIBLE : View.GONE;
        final int noReceiptVisibility = hasReceipt ? View.GONE : View.VISIBLE;

        checkFieldProperties(activity, R.id.storeName, View.VISIBLE, store);
        checkFieldProperties(activity, R.id.cardId, View.VISIBLE, cardId);
        checkFieldProperties(activity, R.id.value, View.VISIBLE, value);
        checkFieldProperties(activity, R.id.receiptLocation, View.GONE, comittedReceipt);
        checkFieldProperties(activity, R.id.hasReceiptButtonLayout, hasReceiptVisibility, null);
        checkFieldProperties(activity, R.id.noReceiptButtonLayout, noReceiptVisibility, null);
        checkFieldProperties(activity, R.id.captureButton, View.VISIBLE, null);
        checkFieldProperties(activity, R.id.updateButton, View.VISIBLE, null);
        checkFieldProperties(activity, R.id.viewButton, View.VISIBLE, null);
        checkFieldProperties(activity, R.id.saveButton, View.VISIBLE, null);
        checkFieldProperties(activity, R.id.cancelButton, View.VISIBLE, null);
    }

    @Test
    public void startWithoutParametersCheckFieldsAvailable()
    {
        ActivityController activityController = Robolectric.buildActivity(GiftCardViewActivity.class).create();
        activityController.start();
        activityController.visible();
        activityController.resume();

        Activity activity = (Activity)activityController.get();

        checkAllFields(activity, "", "", "", "", false);
    }

    @Test
    public void startWithoutParametersCannotCreateGiftCard()
    {
        ActivityController activityController = Robolectric.buildActivity(GiftCardViewActivity.class).create();
        activityController.start();
        activityController.visible();
        activityController.resume();

        Activity activity = (Activity)activityController.get();
        DBHelper db = new DBHelper(activity);
        assertEquals(0, db.getGiftCardCount());

        final EditText storeField = (EditText) activity.findViewById(R.id.storeName);
        final EditText cardIdField = (EditText) activity.findViewById(R.id.cardId);
        final TextView receiptField = (TextView) activity.findViewById(R.id.receiptLocation);

        final Button saveButton = (Button) activity.findViewById(R.id.saveButton);

        saveButton.performClick();
        assertEquals(0, db.getGiftCardCount());

        storeField.setText("store");
        saveButton.performClick();
        assertEquals(0, db.getGiftCardCount());

        cardIdField.setText("cardId");
        saveButton.performClick();
        assertEquals(0, db.getGiftCardCount());

        receiptField.setText("receipt");
        saveButton.performClick();
        assertEquals(0, db.getGiftCardCount());
    }

    @Test
    public void startWithoutParametersCancel()
    {
        ActivityController activityController = Robolectric.buildActivity(GiftCardViewActivity.class).create();
        activityController.start();
        activityController.visible();
        activityController.resume();

        Activity activity = (Activity)activityController.get();

        final Button cancelButton = (Button) activity.findViewById(R.id.cancelButton);

        assertEquals(false, activity.isFinishing());
        cancelButton.performClick();
        assertEquals(true, activity.isFinishing());
    }

    @Test
    public void startWithoutParametersCreateGiftCardNoReceipt()
    {
        ActivityController activityController = Robolectric.buildActivity(GiftCardViewActivity.class).create();
        activityController.start();
        activityController.visible();
        activityController.resume();

        Activity activity = (Activity)activityController.get();

        saveGiftCardWithArguments(activity, "store", "cardId", "value", "", true);
    }

    @Test
    public void startWithoutParametersCaptureReceiptCreateGiftCard() throws IOException
    {
        ActivityController activityController = Robolectric.buildActivity(GiftCardViewActivity.class).create();
        activityController.start();
        activityController.visible();
        activityController.resume();

        // Add something that will 'handle' the media capture intent
        registerMediaStoreIntentHandler();

        Activity activity = (Activity)activityController.get();

        checkAllFields(activity, "", "", "", "", false);

        // Complete image capture successfully
        Uri imageLocation = captureImageWithResult(activity, R.id.captureButton, true);

        checkAllFields(activity, "", "", "", "", true);

        // Save and check the gift card
        saveGiftCardWithArguments(activity, "store", "cardId", "value", imageLocation.getPath(), true);

        // Ensure that the file still exists
        File imageFile = new File(imageLocation.getPath());
        assertTrue(imageFile.isFile());

        // Delete the file to cleanup
        boolean result = imageFile.delete();
        assertTrue(result);
    }

    @Test
    public void startWithoutParametersCaptureReceiptFailureCreateGiftCard() throws IOException
    {
        ActivityController activityController = Robolectric.buildActivity(GiftCardViewActivity.class).create();
        activityController.start();
        activityController.visible();
        activityController.resume();

        // Add something that will 'handle' the media capture intent
        registerMediaStoreIntentHandler();

        Activity activity = (Activity)activityController.get();

        checkAllFields(activity, "", "", "", "", false);

        // Complete image capture in failure
        Uri imageLocation = captureImageWithResult(activity, R.id.captureButton, false);

        checkAllFields(activity, "", "", "", "", false);

        // Save and check the gift card
        saveGiftCardWithArguments(activity, "store", "cardId", "value", "", true);

        // Check that no file was created
        File imageFile = new File(imageLocation.getPath());
        assertEquals(false, imageFile.exists());
    }

    @Test
    public void startWithoutParametersCaptureReceiptCancel() throws IOException
    {
        ActivityController activityController = Robolectric.buildActivity(GiftCardViewActivity.class).create();
        activityController.start();
        activityController.visible();
        activityController.resume();

        // Add something that will 'handle' the media capture intent
        registerMediaStoreIntentHandler();

        Activity activity = (Activity)activityController.get();

        checkAllFields(activity, "", "", "", "", false);

        // Complete image capture successfully
        Uri imageLocation = captureImageWithResult(activity, R.id.captureButton, true);

        checkAllFields(activity, "", "", "", "", true);

        // Cancel the gift card creation
        final Button cancelButton = (Button) activity.findViewById(R.id.cancelButton);
        assertEquals(false, activity.isFinishing());
        cancelButton.performClick();
        assertEquals(true, activity.isFinishing());
        activityController.destroy();

        // Ensure the image has been deleted
        File imageFile = new File(imageLocation.getPath());
        assertEquals(false, imageFile.exists());
    }

    private ActivityController createActivityWithGiftCard()
    {
        Intent intent = new Intent();
        final Bundle bundle = new Bundle();
        bundle.putInt("id", 1);
        bundle.putBoolean("update", true);
        intent.putExtras(bundle);

        return Robolectric.buildActivity(GiftCardViewActivity.class).withIntent(intent).create();
    }

    @Test
    public void startWithGiftCardNoReceiptCheckDisplay() throws IOException
    {
        ActivityController activityController = createActivityWithGiftCard();
        Activity activity = (Activity)activityController.get();
        DBHelper db = new DBHelper(activity);

        db.insertGiftCard("store", "cardId", "value", "");

        activityController.start();
        activityController.visible();
        activityController.resume();

        checkAllFields(activity, "store", "cardId", "value", "", false);
    }

    @Test
    public void startWithGiftCardWithReceiptCheckDisplay() throws IOException
    {
        ActivityController activityController = createActivityWithGiftCard();
        Activity activity = (Activity)activityController.get();
        DBHelper db = new DBHelper(activity);

        db.insertGiftCard("store", "cardId", "value", "receipt");

        activityController.start();
        activityController.visible();
        activityController.resume();

        checkAllFields(activity, "store", "cardId", "value", "receipt", false);
    }

    @Test
    public void startWithGiftCardWithReceiptUpdateReceipt() throws IOException
    {
        ActivityController activityController = createActivityWithGiftCard();
        Activity activity = (Activity)activityController.get();
        DBHelper db = new DBHelper(activity);

        db.insertGiftCard("store", "cardId", "value", "receipt");

        activityController.start();
        activityController.visible();
        activityController.resume();

        checkAllFields(activity, "store", "cardId", "value", "receipt", false);

        // Add something that will 'handle' the media capture intent
        registerMediaStoreIntentHandler();

        // Complete image capture successfully
        Uri imageLocation = captureImageWithResult(activity, R.id.updateButton, true);

        checkAllFields(activity, "store", "cardId", "value", "receipt", true);

        // Save and check the gift card
        saveGiftCardWithArguments(activity, "store", "cardId", "value", imageLocation.getPath(), false);

        // Ensure that the file still exists
        File imageFile = new File(imageLocation.getPath());
        assertTrue(imageFile.isFile());

        // Delete the file to cleanup
        boolean result = imageFile.delete();
        assertTrue(result);
    }

    @Test
    public void startWithGiftCardWithReceiptUpdateReceiptCancel() throws IOException
    {
        ActivityController activityController = createActivityWithGiftCard();
        Activity activity = (Activity)activityController.get();
        DBHelper db = new DBHelper(activity);

        db.insertGiftCard("store", "cardId", "value", "receipt");

        activityController.start();
        activityController.visible();
        activityController.resume();

        checkAllFields(activity, "store", "cardId", "value", "receipt", false);

        // Add something that will 'handle' the media capture intent
        registerMediaStoreIntentHandler();

        // Complete image capture successfully
        Uri imageLocation = captureImageWithResult(activity, R.id.updateButton, true);

        checkAllFields(activity, "store", "cardId", "value", "receipt", true);

        // Cancel the gift card creation
        final Button cancelButton = (Button) activity.findViewById(R.id.cancelButton);
        assertEquals(false, activity.isFinishing());
        cancelButton.performClick();
        assertEquals(true, activity.isFinishing());
        activityController.destroy();

        // Ensure the image has been deleted
        File imageFile = new File(imageLocation.getPath());
        assertEquals(false, imageFile.exists());
    }
}
