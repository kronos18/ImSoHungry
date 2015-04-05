package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * Created by natou on 04/04/15.
 */
public class RestaurantAdaptateur2 extends CursorAdapter
{
    public RestaurantAdaptateur2(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
        System.out.println("on est dans le constructeur de l'adaptateur");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
