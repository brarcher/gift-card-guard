package protect.gift_card_guard;

import android.app.Activity;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class DatabaseTest
{
    private DBHelper db;

    @Before
    public void setUp()
    {
        Activity activity = Robolectric.setupActivity(MainActivity.class);
        db = new DBHelper(activity);
    }

    @Test
    public void addRemoveOneGiftCard()
    {
        assertEquals(0, db.getGiftCardCount());
        boolean result = db.insertGiftCard("store", "cardId", "value", "receipt");
        assertTrue(result);
        assertEquals(1, db.getGiftCardCount());

        GiftCard giftCard = db.getGiftCard(1);
        assertNotNull(giftCard);
        assertEquals("store", giftCard.store);
        assertEquals("cardId", giftCard.cardId);
        assertEquals("value", giftCard.value);
        assertEquals("receipt", giftCard.receipt);

        result = db.deleteGiftCard(1);
        assertTrue(result);
        assertEquals(0, db.getGiftCardCount());
        assertNull(db.getGiftCard(1));
    }

    @Test
    public void updateGiftCard()
    {
        boolean result = db.insertGiftCard("store", "cardId", "value", "receipt");
        assertTrue(result);
        assertEquals(1, db.getGiftCardCount());

        result = db.updateGiftCard(1, "store1", "cardId1", "value1", "receipt1");
        assertTrue(result);
        assertEquals(1, db.getGiftCardCount());

        GiftCard giftCard = db.getGiftCard(1);
        assertNotNull(giftCard);
        assertEquals("store1", giftCard.store);
        assertEquals("cardId1", giftCard.cardId);
        assertEquals("value1", giftCard.value);
        assertEquals("receipt1", giftCard.receipt);
    }

    @Test
    public void updateMissingGiftCard()
    {
        assertEquals(0, db.getGiftCardCount());

        boolean result = db.updateGiftCard(1, "store1", "cardId1", "value1", "receipt1");
        assertEquals(false, result);
        assertEquals(0, db.getGiftCardCount());
    }

    @Test
    public void emptyGiftCardValues()
    {
        boolean result = db.insertGiftCard("", "", "", "");
        assertTrue(result);
        assertEquals(1, db.getGiftCardCount());

        GiftCard giftCard = db.getGiftCard(1);
        assertNotNull(giftCard);
        assertEquals("", giftCard.store);
        assertEquals("", giftCard.cardId);
        assertEquals("", giftCard.value);
        assertEquals("", giftCard.receipt);
    }

    @Test
    public void giftCardsViaCursor()
    {
        final int CARDS_TO_ADD = 10;

        // Add the gift cards in reverse order, to ensure
        // that they are sorted
        for(int index = CARDS_TO_ADD-1; index >= 0; index--)
        {
            boolean result = db.insertGiftCard("store" + index, "cardId" + index, "value" + index, "receipt" + index);
            assertTrue(result);
        }

        assertEquals(CARDS_TO_ADD, db.getGiftCardCount());

        Cursor cursor = db.getGiftCardCursor();
        assertNotNull(cursor);

        assertEquals(CARDS_TO_ADD, cursor.getCount());

        cursor.moveToFirst();

        for(int index = 0; index < CARDS_TO_ADD; index++)
        {
            assertEquals("store"+index, cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.GiftCardDbIds.STORE)));
            assertEquals("cardId"+index, cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.GiftCardDbIds.CARD_ID)));
            assertEquals("value"+index, cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.GiftCardDbIds.VALUE)));
            assertEquals("receipt"+index, cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.GiftCardDbIds.RECEIPT)));

            cursor.moveToNext();
        }

        assertTrue(cursor.isAfterLast());
        cursor.close();
    }
}
