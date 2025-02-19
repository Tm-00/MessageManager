package uk.ac.wlv.cw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;


public class DBHelper extends SQLiteOpenHelper {

    Context context;

    private static final String DATABASE_NAME = "userdata.db";
    private static final String TABLE_NAME = "userdata";


    // column names
    public static final String COL_1 = "ID";
    public static final String COL_2 = "MESSAGE";
    public static final String COL_3 = "IMAGE";


    DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    private SQLiteDatabase mReadableDB;
    private SQLiteDatabase mWriteableDB;

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, MESSAGE TEXT, IMAGE BLOB)");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String message, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, message);
        contentValues.put(COL_3, image);

        long result = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return result != -1; // returns false if insert fails (e.g., user already exists)
    }

   // get stored images and messages
    public Cursor getAllData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }


    public void updateData(String id, String message, byte[] image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        if (message != null){
            cv.put(COL_2, message);
        }

        if (image != null) {
            cv.put(COL_3, image);
        }

        long result = db.update(TABLE_NAME, cv, "id=?", new String[]{id});
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    public int deleteOneRow(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "id=?", new String[]{id});
    }

    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }

    // uses cursor to interate through each row in the database to find matching words to what was searched
    public Cursor search(String searchString) {
        String[] columns = new String[]{COL_2};
        searchString = "%" + searchString + "%";
        String where = COL_2 + " LIKE ?";
        String[]whereArgs = new String[]{searchString};

        Cursor cursor = null;

        try {
            if (mReadableDB == null) {mReadableDB = getReadableDatabase();}
            cursor = mReadableDB.query(TABLE_NAME, columns, where, whereArgs, null, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
        return cursor;
    }
}