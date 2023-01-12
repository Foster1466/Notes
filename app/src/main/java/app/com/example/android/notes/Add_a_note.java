package app.com.example.android.notes;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import app.com.example.android.notes.data.NoteContract;

import static app.com.example.android.notes.R.string.editor_delete_note_failed;

public class Add_a_note extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_NOTE_LOADER = 0;
     EditText mTitleEditText;
     EditText mDescEditText;

    /**
     * Content URI for the existing note (null if it's a new note)
     */
     Uri mCurrentNoteUri;
    private boolean mNoteHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mNoteHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mNoteHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_note);

        Intent intent = getIntent();
        mCurrentNoteUri = intent.getData();
        if(mCurrentNoteUri == null){
            setTitle(getString(R.string.editor_activity_title_new_note));
        }
        else{
            setTitle(getString(R.string.editor_activity_title_edit_note));
            getLoaderManager().initLoader(EXISTING_NOTE_LOADER, null, this);

        }

        mTitleEditText = (EditText)findViewById(R.id.title_view);
        mDescEditText = (EditText)findViewById(R.id.edit_text_note);

        TextView i = (TextView)findViewById(R.id.save);
        i.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
                finish();
            }
        });
        if (mCurrentNoteUri == null) {
            // This is a new note, so change the app bar to say "Add a Note"
            setTitle(getString(R.string.editor_activity_title_new_note));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a note that hasn't been created yet.)
            invalidateOptionsMenu();
        }

    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new note, hide the "Delete" menu item.
        if (mCurrentNoteUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    public void saveNote(){
        String titleString = mTitleEditText.getText().toString().trim();
        String descString = mDescEditText.getText().toString().trim();

        ContentValues values = new ContentValues();

        values.put(NoteContract.NotesEntry.COLUMN_NOTE_TITLE,titleString);
        values.put(NoteContract.NotesEntry.COLUMN_NOTE_DESC,descString);

        if (mCurrentNoteUri == null) {
            // This is a NEW note, so insert a new note into the provider,
            // returning the content URI for the new note.
            Uri newUri = getContentResolver().insert(NoteContract.NotesEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_note_failed),
                        Toast.LENGTH_SHORT).show();

            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_note_successful),
                        Toast.LENGTH_SHORT).show();

            }
        } else {
            // Otherwise this is an EXISTING note, so update the note with content URI: mCurrentNoteUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentNoteUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentNoteUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_note_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_note_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try{
            getMenuInflater().inflate(R.menu.delete,menu);
        }
        catch (Exception e){

        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteNote();
                return true;
            case android.R.id.home:
                // If the note hasn't changed, continue with navigating up to parent activity
                // which is the {@link NotesActivity}.
                if (!mNoteHasChanged) {
                    NavUtils.navigateUpFromSameTask(Add_a_note.this);

                }
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                NoteContract.NotesEntry._ID,
                NoteContract.NotesEntry.COLUMN_NOTE_TITLE,
                NoteContract.NotesEntry.COLUMN_NOTE_DESC};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentNoteUri,         // Query the content URI for the current note
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of note attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_TITLE);
            int descColumnIndex = cursor.getColumnIndex(NoteContract.NotesEntry.COLUMN_NOTE_DESC);

            // Extract out the value from the Cursor for the given column index
            String title = cursor.getString(titleColumnIndex);
            String desc = cursor.getString(descColumnIndex);

            // Update the views on the screen with the values from the database
            mTitleEditText.setText(title);
            mDescEditText.setText(desc);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTitleEditText.setText("");
        mDescEditText.setText("");
    }
    private void deleteNote() {
// Only perform the delete if this is an existing note.
        if (mCurrentNoteUri != null) {
            // Call the ContentResolver to delete the note at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentnoteUri
            // content URI already identifies the note that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentNoteUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(editor_delete_note_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_note_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}