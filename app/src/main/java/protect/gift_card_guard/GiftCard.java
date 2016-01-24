package protect.gift_card_guard;

import android.database.Cursor;

public class GiftCard
{
    public final int id;
    public final String store;
    public final String cardId;
    public final String value;
    public final String receipt;

    public GiftCard(final int id, final String store, final String cardId,
                    final String value, final String receipt)
    {
        this.id = id;
        this.store = store;
        this.cardId = cardId;
        this.value = value;
        this.receipt = receipt;
    }

    public static GiftCard toGiftCard(Cursor cursor)
    {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.GiftCardDbIds.ID));
        String store = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.GiftCardDbIds.STORE));
        String cardId = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.GiftCardDbIds.CARD_ID));
        String value = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.GiftCardDbIds.VALUE));
        String receipt = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.GiftCardDbIds.RECEIPT));

        return new GiftCard(id, store, cardId, value, receipt);
    }
}
