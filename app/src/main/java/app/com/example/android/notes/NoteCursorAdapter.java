package app.com.example.android.notes;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import app.com.example.android.notes.data.NoteContract;

/**
 * {@link NoteCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of note data as its data source. This adapter knows
 * how to create list items for each row of note data in the {@link Cursor}.
 */
public class NoteCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link NoteCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public NoteCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the note data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current note can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView titleTextView = (TextView) view.findViewById(R.id.name);
        TextView descTextView = (TextView) view.findViewById(R.id.summary);

        int titleColumnIndex = cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_TITLE);
        int descColumnIndex = cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_DESC);

        String title = cursor.getString(titleColumnIndex);
        String desc = cursor.getString(descColumnIndex);

        if (TextUtils.isEmpty(desc)) {
            desc = context.getString(R.string.unknown_desc);
        }

        titleTextView.setText(title);
        descTextView.setText(desc);
    }
}