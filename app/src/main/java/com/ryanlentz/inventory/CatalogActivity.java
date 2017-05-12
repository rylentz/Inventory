package com.ryanlentz.inventory;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ryanlentz.inventory.data.ItemContract.ItemEntry;


/**
 * Displays a list of items that have been added to the app
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    /** Id for the loader */
    private static final int ITEM_LOADER = 0;
    /** Adapter for the ListView */
    ItemCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        //Sets up FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Finds the ListView to display items
        ListView itemListView = (ListView) findViewById(R.id.list);

        // Finds the ListView to display when there are no items
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        // Sets up an Adapter to create a list item for each row of item date in the Cursor
        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        // Sets up the item click listener
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Creates a new intent to launch the EditorActivity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Forms the content URI of the specific item that was clicked by appending the id
                // of the item to the end of the URI
                Uri currentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);

                // Sets the URI on the data field of this intent
                intent.setData(currentItemUri);

                // Launches the intent to the EditorActivity
                startActivity(intent);
            }
        });

        // Starts the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Defines a projection for the table columns that we want
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY};

        // Returns a loader that will execute the ContentProvider's query method on a background
        // thread
        return new CursorLoader(this,   // Parent activity context
                ItemEntry.CONTENT_URI,  // Content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Updates Adapter with this new cursor containing item data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Calls callback when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
