package app.com.example.android.notes.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import app.com.example.android.notes.data.NoteContract.NotesEntry;

/**
 * Created by Foster on 8/23/2017.
 */

public class NoteDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "text.db";
    private static final int DATABASE_VERSION = 1;

    public NoteDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the notes table
        String SQL_CREATE_NOTES_TABLE =  "CREATE TABLE " + NotesEntry.TABLE_NAME + " ("
                + NotesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NotesEntry.COLUMN_NOTE_TITLE + " TEXT NOT NULL, "
                + NotesEntry.COLUMN_NOTE_DESC + " TEXT NOT NULL); ";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_NOTES_TABLE );
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
