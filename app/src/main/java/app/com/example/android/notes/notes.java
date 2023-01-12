package app.com.example.android.notes;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import app.com.example.android.notes.data.NoteContract;

public class notes extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int Note_Loader = 0;
    NoteCursorAdapter mCursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        // Find the ListView which will be populated with the note data
        ListView noteListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        noteListView.setEmptyView(emptyView);
        mCursorAdapter = new NoteCursorAdapter(this, null);
        noteListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(notes.this, Add_a_note.class);

                // Form the content URI that represents the specific Note that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link NoteEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.notes/notes/2"
                // if the Note with ID 2 was clicked on.
                Uri currentNoteUri = ContentUris.withAppendedId(NoteContract.NotesEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentNoteUri);

                // Launch the {@link EditorActivity} to display the data for the current note.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(Note_Loader, null, this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try{
            getMenuInflater().inflate(R.menu.notes,menu);
        }
        catch (Exception e){

        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(notes.this,Add_a_note.class);
        startActivity(i);
        return true;
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                NoteContract.NotesEntry._ID,
                NoteContract.NotesEntry.COLUMN_NOTE_TITLE,
                NoteContract.NotesEntry.COLUMN_NOTE_DESC };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                NoteContract.NotesEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link NoteCursorAdapter} with this new cursor containing updated note data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
