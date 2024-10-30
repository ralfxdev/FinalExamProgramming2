package gt.edu.umg.finalexamprogramming2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PhotoDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "photos.db";
    private static final int DATABASE_VERSION = 1;

    public PhotoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE photos ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "image_uri TEXT, "
                + "latitude REAL, "
                + "longitude REAL, "
                + "name TEXT, "
                + "date TEXT, "
                + "hora TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS photos");
        onCreate(db);
    }

    public boolean deletePhotoById(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("photos", "id = ?", new String[]{id});
        db.close();
        return rowsDeleted > 0;
    }

}
