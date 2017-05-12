package com.ryanlentz.inventory;


import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ryanlentz.inventory.data.ItemContract.ItemEntry;

/**
 * Allows the user to add and edit items in the inventory
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /** ID for the loader */
    private static final int EXISTING_ITEM_LOADER = 0;

    /** Content URI for an existing item */
    private Uri mCurrentItemUri;

    /** EditText field to enter the item's name */
    EditText mNameEditText;

    /** EditText field to enter the item's quantity */
    EditText mQuantityEditText;

    /** EditText field to enter the item's price */
    EditText mPriceEditText;

    /** EditText field to enter the item's description */
    EditText mDescriptionEditText;

    /** Boolean flag that keeps track of whether the item has been edited (true) or not (false) */
    private boolean mItemHasChanged = false;

    /**
     * Listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Checks if the intent that launched this activity has an attached URI
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // Finds Buttons
        Button saveButton = (Button) findViewById(R.id.save_button);
        Button deleteButton = (Button) findViewById(R.id.delete_button);
        Button increaseQuantityButton = (Button) findViewById(R.id.add_button);
        Button decreaseQuantityButton = (Button) findViewById(R.id.minus_button);

        // Finds EditTexts for item information
        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_edit_text);
        mPriceEditText = (EditText) findViewById(R.id.price_edit_text);
        mDescriptionEditText = (EditText) findViewById(R.id.description_edit_text);

        // If there is NOT a URI attached to the intent, we are creating a new item
        if (mCurrentItemUri == null) {
            // Makes the title bar say "Add an Item"
            setTitle(getString(R.string.add_item));

            // Sets the quantity to zero
            mQuantityEditText.setText("0");

            // Removes delete button since no item exists to delete yet
            deleteButton.setVisibility(View.GONE);

        } else {
            // Makes the title bar say "Edit Item", since the is an existing URI and item
            setTitle(getString(R.string.edit_item));

            // Sets on click listeners on the delete button
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog();
                }
            });

            // Initializes a loader to read the item data from the database and display it in the
            // editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Sets on click listener on the decrease button
        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gets the current quantity value
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString());

                // Checks that the quantity is greater than one so that it cannot drop below zero
                if (quantity >= 1) {

                    // Decreases the quantity by one
                    quantity--;

                    // Sets the EditText to reflect the updated value
                    mQuantityEditText.setText(Integer.toString(quantity));
                }
            }
        });

        // Sets on click listener on the increase button
        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gets the current quantity value
                int quantity = Integer.parseInt(mQuantityEditText.getText().toString());

                // Increases the quantity by one
                quantity++;

                // Sets the EditText to reflect the updated value
                mQuantityEditText.setText(Integer.toString(quantity));
            }
        });

        // Sets on click listener on the save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });

        // Sets up OnTouchListeners on all input fields so that we can determine if they have
        // been modified
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        decreaseQuantityButton.setOnTouchListener(mTouchListener);
        increaseQuantityButton.setOnTouchListener(mTouchListener);
    }

    /**
     * Gets user input from editor and saves pet in the database
     */
    private void saveItem() {
        // Reads input fields and uses trim to eliminate white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();

        // Checks if this is a new or existing item and if the fields in the editor are blank
        if (mCurrentItemUri == null && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(quantityString)
                && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(descriptionString)) {
            // Returns early without saving since no info was entered
            return;
        }

        // Checks if the name has a value and, if not, alerts user one is required
        if (TextUtils.isEmpty(nameString)) {
            // Lets user know that a name is required
            showNameRequiredDialog();
            return;
        }

        // Default value for quantity
        int quantity = 0;

        // Checks if the quantity has a value and, if so, converts it to an integer
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        // Default value for price
        double price = 0;

        // Checks if the price has a value and, if so, converts it to a double
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        // Create a ContentValues object where the keys are column names and values are item
        // attributes
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, price);
        values.put(ItemEntry.COLUMN_ITEM_DESCRIPTION, descriptionString);

        // Determines if this is a new or existing item by checking mCurrentItemUri
        if (mCurrentItemUri == null) {
            // Inserts a new item into the provider and saves the new content URI
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            // Shows a Toast indicating whether or not the insertion was successful
            if (newUri == null) {
                // If newUri is null, there was an error with the insertion
                Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
            } else {
                // The insertion was successful
                Toast.makeText(this, R.string.save_successful, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Updates the existing item and saves the number of rows affected
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Shows a Toast indicating whether or not the update was successful
            displayToast(rowsAffected, R.string.update_failed, R.string.update_successful);
        }

        // Returns to CatalogActivity
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Responds to a click on the back arrow in the app bar
        if (item.getItemId() == android.R.id.home) {
            // Checks if the item has changed
            if (!mItemHasChanged) {
                // Item hasn't changed so navigates to parent activity
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;
            } else {
                // Item has changed so displays dialogue to confirm leaving without saving changes
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigates to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Shows a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks if there have been any unsaved changes when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Creates a click listener to confirm that changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Shows a dialog that warns the user of unsaved changes that will be lost
     * if they continue leaving the editor
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Creates an AlertDialog.Builder and sets the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Creates and shows the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompts the user to confirm that they want to delete this item
     */
    private void showDeleteConfirmationDialog() {
        // Creates an AlertDialog.Builder, sets the message and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Creates and shows the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Let's the user know that a name is a required attribute
     */
    private void showNameRequiredDialog() {
        // Creates an AlertDialog.Builder, sets the message and a click listeners
        // for the positive button on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.name_required);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "OK" button, so dismiss the dialogue and continue editing item
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Creates and shows the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Deletes the current item in the editor from the database
     */
    private void deleteItem() {
        // Deletes this item from the database and save the rows deleted
        int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

        // Shows a Toast indicating whether or not the deletion was successful
        displayToast(rowsDeleted, R.string.deletion_failed, R.string.deletion_successful);

        // Closes EditorActivity
        finish();
    }

    /**
     * Displays a Toast indicating success or failure of the attempted action
     * @param rows              The number of rows affected by the action
     * @param successMessage
     * @param failMessage
     */
    public void displayToast(int rows, int failMessage, int successMessage) {
        // Shows a Toast indicating whether or not the update was successful
        if (rows == 0) {
            // If no rows were affected, the update was unsuccessful
            Toast.makeText(this, failMessage, Toast.LENGTH_SHORT).show();
        } else {
            // The update was successful
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Creates a projection with all of the columns from the inventory table
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_DESCRIPTION};

        // Executes the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Current context
                mCurrentItemUri,        // URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Checks if the cursor is null or empty
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Moves to the first row of the cursor and reads data
        if (data.moveToFirst()) {
            // Finds the columns of the data we want
            int nameColumn = data.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int quantityColumn = data.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int priceColumn = data.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int descriptionColumn = data.getColumnIndex(ItemEntry.COLUMN_ITEM_DESCRIPTION);

            // Extracts the value from the cursor for the given column
            String name = data.getString(nameColumn);
            int quantity = data.getInt(quantityColumn);
            double price = data.getDouble(priceColumn);
            String description = data.getString(descriptionColumn);

            // Updates the views with the extracted values
            mNameEditText.setText(name);
            mQuantityEditText.setText(String.valueOf(quantity));
            mPriceEditText.setText(String.format("%.2f", price));
            mDescriptionEditText.setText(description);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Clears data from the input fields
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mDescriptionEditText.setText("");
    }
}
