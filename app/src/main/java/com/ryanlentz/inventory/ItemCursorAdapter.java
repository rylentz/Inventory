package com.ryanlentz.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ryanlentz.inventory.data.ItemContract.ItemEntry;

import static android.R.attr.id;
import static com.ryanlentz.inventory.R.id.price;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */
public class ItemCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link ItemCursorAdapter}
     * @param context   The context
     * @param cursor    The cursor from which to get the data
     */
    public ItemCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
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
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Finds the views associated with the data to be displayed in the list item layout
        TextView itemNameTextView = (TextView) view.findViewById(R.id.name);
        TextView itemPriceTextView = (TextView) view.findViewById(price);
        TextView itemQuantityTextView = (TextView) view.findViewById(R.id.quantity);
        Button sellItemButton = (Button) view.findViewById(R.id.sell_button);

        // Extracts the string data to be displayed
        String itemName = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_NAME));
        Double itemPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_PRICE));
        final String itemQuantity = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_QUANTITY));
        final String itemId = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry._ID));

        // Initializes each view with the appropriate string
        itemNameTextView.setText(itemName);
        itemPriceTextView.setText(String.format("$%.2f", itemPrice));
        itemQuantityTextView.setText(itemQuantity);

        // Set on click listener on the sellItemButton
        sellItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sellItem(itemId, itemQuantity);
            }
        });
    }

    /**
     * Decreases the quantity by one
     */
    private void sellItem(String id, String quantityString) {
        // Gets quantity as an integer
        int quantity = Integer.parseInt(quantityString);

        // Checks if quantity is at least one since it cannot go below zero
        if (quantity >= 1) {
            // An item has sold so decrease quantity by one
            quantity--;
            // Creates a ContentValues object where the keys are column names and values are item
            // attributes
            ContentValues values = new ContentValues();
            values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);

            // Creates the URI for the current item
            Uri uri = Uri.withAppendedPath(ItemEntry.CONTENT_URI, id);

            // Updates the database for the current item with the new quantity
            mContext.getContentResolver().update(uri, values, null, null);
        }
    }
}
