package protect.gift_card_guard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
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

        // Only the + is in the menu
        assertEquals(menu.size(), 1);

        assertEquals("Add", menu.findItem(R.id.action_add).getTitle().toString());
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
}