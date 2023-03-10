package app.com.example.android.notes.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.security.Provider;

import static android.R.attr.id;
import static android.R.attr.name;
import static android.icu.lang.UCharacter.JoiningGroup.PE;
import static android.icu.text.Normalizer.NO;
import static app.com.example.android.notes.data.NoteContract.NotesEntry.CONTENT_LIST_TYPE;

/**
 * {@link ContentProvider} for Notess app.
 */
public class NotesProvider extends ContentProvider {
    /** URI matcher code for the content URI for the notes table */
    public static final int NOTES = 100;

    /** URI matcher code for the content URI for a single note in the notes table */
    public static final int NOTE_ID = 101;

    /** URI matcher object to match a context URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.notes/notes" will map to the
        // integer code {@link #NOTES}. This URI is used to provide access to MULTIPLE rows
        // of the notes table.
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES, NOTES);

        // The content URI of the form "content://com.example.android.notes/notes/#" will map to the
        // integer code {@link #noteS_ID}. This URI is used to provide access to ONE single row
        // of the notes table.

        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.notes/notes/3" matches, but
        // "content://com.example.android.notes/notes" (without a number at the end) doesn't match.
        sUriMatcher.addURI(NoteContract.CONTENT_AUTHORITY, NoteContract.PATH_NOTES + "/#", NOTE_ID);
    }

    /** Tag for the log messages */
    public static final String LOG_TAG = NotesProvider.class.getSimpleName();

    private NoteDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new NoteDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                // For the NOTES code, query the notes table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the notes table.
                cursor = database.query(NoteContract.NotesEntry.TABLE_NAME, projection, selection, selectionArgs,null,null,sortOrder);
                break;
            case NOTE_ID:
                // For the Note_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.notes/notes/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = NoteContract.NotesEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the notes table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(NoteContract.NotesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return insertNote(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a note into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertNote(Uri uri, ContentValues values) {
        String title = values.getAsString(NoteContract.NotesEntry.COLUMN_NOTE_TITLE);
        if (title == null) {
            throw new IllegalArgumentException("Note requires a title");
        }
        String desc = values.getAsString(NoteContract.NotesEntry.COLUMN_NOTE_DESC);
        if (desc == null) {
            throw new IllegalArgumentException("Note requires a description");
        }
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(NoteContract.NotesEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri,null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }
    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return updateNote(uri, contentValues, selection, selectionArgs);
            case NOTE_ID:
                // For the Note_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = NoteContract.NotesEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateNote(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update notes in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more Notes).
     * Return the number of rows that were successfully updated.
     */
    private int updateNote(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(NoteContract.NotesEntry.COLUMN_NOTE_TITLE)) {
            String title = values.getAsString(NoteContract.NotesEntry.COLUMN_NOTE_TITLE);
            if (title == null) {
                throw new IllegalArgumentException("Note requires a title");
            }
        }

        if (values.containsKey(NoteContract.NotesEntry.COLUMN_NOTE_DESC)) {
            String desc = values.getAsString(NoteContract.NotesEntry.COLUMN_NOTE_DESC);
            if (desc == null) {
                throw new IllegalArgumentException("Note requires a desc");
            }
        }
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        int rowsUpdated = database.update(NoteContract.NotesEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;

    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                // Delete all rows that match the selection and selection args

                rowsDeleted = database.delete(NoteContract.NotesEntry.TABLE_NAME, selection, selectionArgs);

                break;
            case NOTE_ID:
                // Delete a single row given by the ID in the URI
                selection = NoteContract.NotesEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(NoteContract.NotesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }
    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                return NoteContract.NotesEntry.CONTENT_LIST_TYPE;
            case NOTE_ID:
                return NoteContract.NotesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}