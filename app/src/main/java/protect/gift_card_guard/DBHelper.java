package protect.gift_card_guard;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "GiftCardGuard.db";
    public static final int DATABASE_VERSION = 1;

    static class GiftCardDbIds
    {
        public static final String TABLE = "cards";
        public static final String ID = "_id";
        public static final String STORE = "store";
        public static final String CARD_ID = "cardid";
        public static final String VALUE = "value";
        public static final String RECEIPT = "receipt";
    }

    public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // create table for gift cards
        db.execSQL("create table " + GiftCardDbIds.TABLE + "(" +
                GiftCardDbIds.ID + " INTEGER primary key autoincrement," +
                GiftCardDbIds.STORE + " TEXT not null," +
                GiftCardDbIds.CARD_ID + " TEXT not null," +
                GiftCardDbIds.VALUE + " TEXT not null," +
                GiftCardDbIds.RECEIPT + " TEXT not null)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Do not support versioning yet
        db.execSQL("DROP TABLE IF EXISTS " + GiftCardDbIds.TABLE);
        onCreate(db);
    }

    public boolean insertGiftCard(final String store, final String cardId, final String value,
                                  final String receipt)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GiftCardDbIds.STORE, store);
        contentValues.put(GiftCardDbIds.CARD_ID, cardId);
        contentValues.put(GiftCardDbIds.VALUE, value);
        contentValues.put(GiftCardDbIds.RECEIPT, receipt);
        final long newId = db.insert(GiftCardDbIds.TABLE, null, contentValues);
        return (newId != -1);
    }


    public boolean updateGiftCard(final int id, final String store, final String cardId,
                                  final String value, final String receipt)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(GiftCardDbIds.STORE, store);
        contentValues.put(GiftCardDbIds.CARD_ID, cardId);
        contentValues.put(GiftCardDbIds.VALUE, value);
        contentValues.put(GiftCardDbIds.RECEIPT, receipt);
        int rowsUpdated = db.update(GiftCardDbIds.TABLE, contentValues,
                GiftCardDbIds.ID + "=?",
                new String[]{Integer.toString(id)});
        return (rowsUpdated == 1);
    }

    public GiftCard getGiftCard(final int id)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data = db.rawQuery("select * from " + GiftCardDbIds.TABLE +
                " where " + GiftCardDbIds.ID + "=?", new String[]{String.format("%d", id)});

        GiftCard card = null;

        if(data.getCount() == 1)
        {
            data.moveToFirst();
            card = GiftCard.toGiftCard(data);
        }

        data.close();

        return card;
    }

    public boolean deleteGiftCard (final int id)
    {
        SQLiteDatabase db = getWritableDatabase();
        int rowsDeleted =  db.delete(GiftCardDbIds.TABLE,
                GiftCardDbIds.ID + " = ? ",
                new String[]{String.format("%d", id)});
        return (rowsDeleted == 1);
    }

    public Cursor getGiftCardCursor()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor res =  db.rawQuery("select * from " + GiftCardDbIds.TABLE +
                " ORDER BY " + GiftCardDbIds.STORE, null);
        return res;
    }

    public int getGiftCardCount()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor data =  db.rawQuery("SELECT Count(*) FROM " + GiftCardDbIds.TABLE, null);

        int numItems = 0;

        if(data.getCount() == 1)
        {
            data.moveToFirst();
            numItems = data.getInt(0);
        }

        data.close();

        return numItems;
    }
}

