package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.IndexBDRestaurant;

/**
 * {@link RestaurantAdaptateur} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link android.widget.ListView}.
 */
public class RestaurantAdaptateur extends CursorAdapter {


    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {

        public final ImageView iconView;

        public final TextView descriptionView;


        public ViewHolder(View view)
        {
//            j'affecte tout les elements de ma vue
            System.out.println("On est dans le constructeur ");
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);

//            on recupere là ou on place le restaurant dans le fichier xml
            descriptionView = (TextView) view.findViewById(R.id.list_item_restaurant_textview);
            System.out.println("On est sortie du constructeur ");
        }
    }

    public RestaurantAdaptateur(Context context, Cursor cursor, int flags)
    {
        super(context, cursor, flags);
        System.out.println("on est dans le constructeur de l'adaptateur");
        System.out.println("Le curseur est-il null ? : " +cursor);
        System.out.println("");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        System.out.println("on est dans new view");
        int layoutId = R.layout.list_item_forecast;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /**
     * La fonction represente la vue que l'on va lier
     * @param view
     * @param context
     * @param cursor
     */
    public void bindView(View view, Context context, Cursor cursor) {
        System.out.println("######################");
        System.out.println("ON EST DANS BINDvIEW");
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        System.out.println("apres le get tag");

//        String imageUrl = cursor.getString(RestaurantFragment.COL_RESTAURANT_IMG_LIST);
//        System.out.println("L'url de l'image en bdd est : " +imageUrl);
//        /*
//        On telecharge les images illustrants le restaurant
//         */
//        DownloadImageTask imageTask;
//        if (imageUrl != null) {
//
//            imageTask = new DownloadImageTask(viewHolder.iconView);
//            //on applique l'image a l'icone view
//            imageTask.execute(imageUrl);
//        }

        //on recupere l'image dans la bdd et on l'applique a iconView
        byte[] image = cursor.getBlob(IndexBDRestaurant.INDEX_IMAGE_LISTE);
        Bitmap imageBitmap = null;
        if (image != null) {
            imageBitmap = Utility.getImage(image);

        }
        viewHolder.iconView.setImageBitmap(imageBitmap);

//        switch (viewType) {
//            case VIEW_TYPE_TODAY: {
//                // Get weather icon
//                viewHolder.iconView.setImageResource(Utility.getImageRestaurant(
//                        cursor.getInt(RestaurantFragment.COL_WEATHER_CONDITION_ID)));
//                break;
//            }
//            case VIEW_TYPE_FUTURE_DAY: {
//                // Get weather icon
//                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(
//                        cursor.getInt(RestaurantFragment.COL_WEATHER_CONDITION_ID)));
//                break;
//            }
//        }

        // Read date from cursor
//        long dateInMillis = cursor.getLong(RestaurantFragment.COL_WEATHER_DATE);
//        Find TextView and set formatted date on it
//        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));

        //On lit le nom des restaurants depuis notre curseur de base de donnee
        String nom = cursor.getString(RestaurantFragment.COL_RESTAURANT_NAME);
        System.out.println("le nom est le suivant :\n" +nom);
        System.out.println("viewholder : "+viewHolder);
        // Find TextView and set weather forecast on it
        viewHolder.descriptionView.setText(nom);

        // For accessibility, add a content nom to the icon field
//        viewHolder.iconView.setContentDescription(nom);

        // Read user preference for metric or imperial temperature units
//        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
//        double high = cursor.getDouble(RestaurantFragment.COL_WEATHER_MAX_TEMP);
//        viewHolder.highTempView.setText(Utility.formatTemperature(context, high));

        // Read low temperature from cursor
//        double low = cursor.getDouble(RestaurantFragment.COL_WEATHER_MIN_TEMP);
//        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low));
    }




}