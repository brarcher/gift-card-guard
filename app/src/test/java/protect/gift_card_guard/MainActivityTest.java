package protect.gift_card_guard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class MainActivityTest
{
    @Test
    public void initiallyNoGiftCards() throws Exception
    {
        Activity activity = Robolectric.setupActivity(MainActivity.class);
        assertTrue(activity != null);

        TextView helpText = (TextView)activity.findViewById(R.id.helpText);
        assertEquals(View.VISIBLE, helpText.getVisibility());

        ListView list = (ListView)activity.findViewById(R.id.list);
        assertEquals(View.GONE, list.getVisibility());
    }

    @Test
    public void onCreateShouldInflateLayout() throws Exception
    {
        final MainActivity activity = Robolectric.setupActivity(MainActivity.class);

        final Menu menu = shadowOf(activity).getOptionsMenu();
        assertTrue(menu != null);

        // The settings and add button should be present
        assertEquals(menu.size(), 3);

        MenuItem addItem = menu.findItem(R.id.action_add);
        assertNotNull(addItem);
        assertEquals("Add", addItem.getTitle().toString());

        MenuItem settingsItem = menu.findItem(R.id.action_settings);
        assertNotNull(settingsItem);
        assertEquals("Settings", settingsItem.getTitle().toString());

        MenuItem aboutItem = menu.findItem(R.id.action_about);
        assertNotNull(aboutItem);
        assertEquals("About", aboutItem.getTitle().toString());
    }

    @Test
    public void clickAddLaunchesGiftCardViewActivity()
    {
        final MainActivity activity = Robolectric.setupActivity(MainActivity.class);

        shadowOf(activity).clickMenuItem(R.id.action_add);

        Intent intent = shadowOf(activity).peekNextStartedActivityForResult().intent;

        assertEquals(new ComponentName(activity, GiftCardViewActivity.class), intent.getComponent());
        assertNull(intent.getExtras());
    }

    @Test
    public void clickSettingsLaunchesGiftCardViewActivity()
    {
        final MainActivity activity = Robolectric.setupActivity(MainActivity.class);

        shadowOf(activity).clickMenuItem(R.id.action_settings);

        Intent intent = shadowOf(activity).peekNextStartedActivityForResult().intent;

        assertEquals(new ComponentName(activity, SettingsActivity.class), intent.getComponent());
        assertNull(intent.getExtras());
    }

    @Test
    public void addOneGiftCard()
    {
        ActivityController activityController = Robolectric.buildActivity(MainActivity.class).create();

        Activity mainActivity = (Activity)activityController.get();
        activityController.start();
        activityController.resume();

        TextView helpText = (TextView)mainActivity.findViewById(R.id.helpText);
        ListView list = (ListView)mainActivity.findViewById(R.id.list);

        assertEquals(0, list.getCount());

        DBHelper db = new DBHelper(mainActivity);
        db.insertGiftCard("store", "cardId", "value", "receipt");

        assertEquals(View.VISIBLE, helpText.getVisibility());
        assertEquals(View.GONE, list.getVisibility());

        activityController.pause();
        activityController.resume();

        assertEquals(View.GONE, helpText.getVisibility());
        assertEquals(View.VISIBLE, list.getVisibility());

        assertEquals(1, list.getAdapter().getCount());
        Cursor cursor = (Cursor)list.getAdapter().getItem(0);
        assertNotNull(cursor);
    }
}