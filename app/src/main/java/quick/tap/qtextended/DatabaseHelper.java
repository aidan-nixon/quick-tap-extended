/**
 * As the MainActivity is destroyed as soon as it is opened, this DatabaseHelper
 * class is used to record the state of the toggle as this cannot be stored
 * through the lifecycle of the MainActivity.
 */

package quick.tap.qtextended;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "qtextended";
    private static final int DB_VERSION = 1;

    /**
     * Create DB.
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Create a single column for the DB (to record toggle).
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE RECORD ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "TOGGLE REAL);"
        );
        ContentValues recordValues = new ContentValues();
        recordValues.put("TOGGLE", 0);
        db.insert("RECORD", null, recordValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    /**
     * Implement toggle functionality by overwriting a single cell in the DB.
     * @param db
     * @return toggle value
     */
    public static int toggle(SQLiteDatabase db) {
        Cursor cursor = db.query("RECORD", new String[]{"_id", "TOGGLE"}, "_id = 1", null, null, null, "_id ASC");  // Create query with reference to parameterised ID.
        cursor.moveToFirst();

        int toggle = cursor.getInt(1);
        toggle = 1 - toggle;

        ContentValues recordValues = new ContentValues();
        recordValues.put("TOGGLE", toggle);
        db.update("RECORD", recordValues, "_id = ?", new String[] {"1"});

        if(cursor != null) cursor.close();
        if(db != null) db.close();
        return toggle;
    }
}
