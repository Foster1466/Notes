package app.com.example.android.notes.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Foster on 8/23/2017.
 */

public final class NoteContract {
    public static final String CONTENT_AUTHORITY = "com.example.android.notes";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_NOTES = "notes";

    public static final class NotesEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOTES);
        public final static String TABLE_NAME = "Notes";
        public static final String _ID = BaseColumns._ID;
        public final static String COLUMN_NOTE_TITLE = "title";
        public final static String COLUMN_NOTE_DESC = "description";
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single Note.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTES;

    }
}
