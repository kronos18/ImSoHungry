package com.example.android.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.RestaurantFragment;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.RestaurantContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.Vector;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter
{



    public final String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;




    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
            RestaurantContract.RestaurantEntry.COLUMN_RESTAURANT_ID,
//            RestaurantContract.RestaurantEntry.COLUMN_MAX_TEMP,
//            RestaurantContract.RestaurantEntry.COLUMN_MIN_TEMP,
//            RestaurantContract.RestaurantEntry.COLUMN_SHORT_DESC
    };


    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Methode qui va nous permettre d'utiliser l'api sitra
     * @param account
     * @param extras
     * @param authority
     * @param provider
     * @param syncResult
     */
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        String locationQuery = Utility.getPreferredLocation(getContext());
        System.out.println("La location de la requete est : "+locationQuery);
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        try {
            // Construct the URL for the sitra query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://api.sitra-tourisme.com/api/v002/recherche/list-objets-touristiques?query={"projetId":"1143","apiKey":"m4VH2Zee","criteresQuery":"type:RESTAURATION","order":"DISTANCE","center":{"type":"Point","coordinates":%5B5.768291999999974,45.193761%5D},"radius":5000}

            final String COORDONNE_PARAM = "5.768291999999974,45.193761";
            final String RAYON_PARAM = "5000";
            final String BASE_URL = "http://api.sitra-tourisme.com/api/v002/recherche/list-objets-touristiques?query={\"projetId\":\"1143\",\"apiKey\":\"m4VH2Zee\",\"criteresQuery\":\"type:RESTAURATION\",\"order\":\"DISTANCE\",\"center\":{\"type\":\"Point\",\"coordinates\":[5.768291999999974,45.193761]},\"radius\":5000}";

//            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
//                    .appendQueryParameter(COORDONNE_PARAM, locationQuery)
//                    .appendQueryParameter(RAYON_PARAM, format)
//                    .appendQueryParameter(UNITS_PARAM, units)
//                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                    .build();

//            URL url = new URL(builtUri.toString());

            URL url = new URL(BASE_URL);
            System.out.println("L'url est :\n"+url);
            System.out.println("On effectue la connection");
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            System.out.println("On est connecte");
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            forecastJsonStr = buffer.toString();
            /*appele de la fonction qui recupere les mise a jour */
            System.out.println("Avant appelle fonction !");
            getRestaurantDataFromJson(forecastJsonStr, locationQuery);
            System.out.println("Apres appelle fonction !");

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getRestaurantDataFromJson(String forecastJsonStr,
                                           String locationSetting)
            throws JSONException {

        System.out.println("On est dans la fonction");

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
//        final String OWM_CITY = "city";
//        final String OWM_CITY_NAME = "name";
//        final String OWM_COORD = "coord";
//
//        // Location coordinate
//        final String OWM_LATITUDE = "lat";
//        final String OWM_LONGITUDE = "lon";
//
//        // Weather information.  Each day's forecast info is an element of the "list" array.
//        final String RESTAURANT_LIST = "list";
//
//        final String OWM_PRESSURE = "pressure";
//        final String OWM_HUMIDITY = "humidity";
//        final String OWM_WINDSPEED = "speed";
//        final String OWM_WIND_DIRECTION = "deg";
//
//        // All temperatures are children of the "temp" object.
//        final String OWM_TEMPERATURE = "temp";
//        final String OWM_MAX = "max";
//        final String OWM_MIN = "min";
//
//        final String OWM_WEATHER = "weather";
//        final String OWM_DESCRIPTION = "main";
        final String RESTAURANT_LIST = "objetsTouristiques";
        final String ILLUSTRATION_LIST = "illustrations";
        final String COMMUNICATION_LIST = "moyensCommunication";
        final String TRADUCTION_FICHIER_LIST = "traductionFichiers";
        final String RESTAURANT_ID = "id";
        final String LABEL_NOM = "nom";
        final String TYPE = "type";
        final String COORDONNEES = "coordonnees";
        final String LABEL_LIBELLE = "libelleFr";
        final String LABEL_FR = "fr";
        final String LABEL_ADRESSE = "adresse1";
        final String LABEL_CODEPOSTAL = "codePostal";
        final String LABEL_COMMUNE = "commune";
        final String LABEL_URL_LIST_IMG = "urlListe";
        final String LABEL_URL_FICHE_IMG = "urlFiche";
        final String LABEL_URL_DIAPORAMA_IMG = "urlDiaporama";
        final String LABEL_URL__IMG = "url";
        final String INFORMATION = "informations";
        final String PRESENTATION = "presentation";
        final String DESCRIPTIF = "descriptifCourt";
        final String LOCALISATION = "localisation";
        final String GEOJSON = "geoJson";
        final String COORDONNEE_LIST = "coordinates";

        final String ADRESSE = "adresse";
        final String GEOLOCALISATION = "geolocalisation";

        try {
            JSONObject objetTouristiqueJson = new JSONObject(forecastJsonStr);
            JSONArray restaurantArray = objetTouristiqueJson.getJSONArray(RESTAURANT_LIST);

//            JSONObject cityJson = objetTouristiqueJson.getJSONObject(OWM_CITY);
//            String cityName = cityJson.getString(OWM_CITY_NAME);
//
//            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
//            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
//            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);
//
//            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            // Insert the new weather information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(restaurantArray.length());


            int idRestaurant;

//            pour tout les restaurants on recupere le nom, l'adresse , le descriptif et le telephone du resto'
            for(int i = 0; i < restaurantArray.length(); i++)
            {
                // These are the values that will be collected.
                String nomRestaurant;
                String numeroTelephone;
                String siteWeb;
                idRestaurant=i;
                String description;
                String urlListImage = null;
                String urlFicheImage = null;
                String urlDiaporamaImage;
                String urlImage;
                Location localisation = null;

                // Get the JSON object representing the restaurant
                JSONObject tabResto = restaurantArray.getJSONObject(i);
                JSONObject nomRestoTmp;

                //On recupere les JSONobjet "nom" et on recupere le contenu
                nomRestoTmp = tabResto.getJSONObject("nom");
                nomRestaurant = nomRestoTmp.getString(LABEL_LIBELLE);

                //On recupere les JSONobjet "nom" et on recupere le contenu
                JSONObject infoTmp = tabResto.getJSONObject(INFORMATION);

//                on recupere la liste des moyens de communication
                JSONArray moyenCommunicationArray = infoTmp.getJSONArray(COMMUNICATION_LIST);


                boolean illustrationExiste = !tabResto.isNull(ILLUSTRATION_LIST);

//                on recupere la liste des illustration
                System.out.println("illustration existe  : " + illustrationExiste);
                if (illustrationExiste)
                {
                    JSONArray illustrationsArray = tabResto.getJSONArray(ILLUSTRATION_LIST);
                    urlListImage = getUrlListImage(TRADUCTION_FICHIER_LIST, LABEL_URL_LIST_IMG, illustrationsArray);
                    urlFicheImage = getUrlListImage(TRADUCTION_FICHIER_LIST, LABEL_URL_LIST_IMG, illustrationsArray);
                    urlDiaporamaImage = getUrlListImage(TRADUCTION_FICHIER_LIST, LABEL_URL_LIST_IMG, illustrationsArray);
                    urlImage = getUrlListImage(TRADUCTION_FICHIER_LIST, LABEL_URL_LIST_IMG, illustrationsArray);


                    System.out.println("L'url de l'image est la suivante : " + urlImage);
                    System.out.println("L'url liste de l'image est la suivante : " + urlListImage);
                    System.out.println("L'url fiche de l'image est la suivante : " + urlFicheImage);
                    System.out.println("L'url diaporama de l'image est la suivante : " + urlDiaporamaImage);
                }



                System.out.println("Le nom du restaurant est : "+nomRestaurant);
                System.out.println("Les moyens de communnication sont : ");

                //On cree la liste des moyens de communication du restaurant
                LinkedList<MoyenCommunication> moyenCommunicationListe = new LinkedList<MoyenCommunication>();

                //on recupere les donnees concernants les moyens de communications du restaurant
                for (int j = 0; j < moyenCommunicationArray.length(); j++)
                {
                    //on recupere les moyens de communication
                    JSONObject moyenCommunicationJSON = moyenCommunicationArray.getJSONObject(j);
                    JSONObject typeJSON = moyenCommunicationJSON.getJSONObject(TYPE);
                    JSONObject coordonneesJSON = moyenCommunicationJSON.getJSONObject(COORDONNEES);

                    //On recupere le type de moyen de communication
                    String typeMoyenCommunication = typeJSON.getString(LABEL_LIBELLE);

                    //On recupere les coordonnees du moyen de communication
                    String coordonneesMoyenCommunication = coordonneesJSON.getString(LABEL_FR);

                    MoyenCommunication monMoyenDeCommunication = new MoyenCommunication(typeMoyenCommunication, coordonneesMoyenCommunication);

                    //on ajoute le moyen de commu a la liste
                    moyenCommunicationListe.add(monMoyenDeCommunication);

                    System.out.println(typeMoyenCommunication+" :  " + coordonneesMoyenCommunication+"\n");

                }


                //On recupere la description du restaurant
                String descriptifRestaurant = null;
                JSONObject presentationJsonObject = tabResto.getJSONObject(PRESENTATION);
                int taille = presentationJsonObject.length();
                boolean existeDescription = taille > 0;

                if (existeDescription)
                {
                    //on recupere le decriptif du restaurant
                    descriptifRestaurant = presentationJsonObject.getJSONObject(DESCRIPTIF).getString(LABEL_LIBELLE);

                    System.out.println(descriptifRestaurant);
                }

                    /****** A CONTINUER POUR LES COORDONNEE DU RESTO*/
                /*Recuperation de la localisation gps du restaurant*/
                localisation = new Location("LocalisationRestaurant");

                /*Recuperation de l'adresse, de la ville et du code postal du restaurant*/
                JSONObject champsAdressejsonObject = tabResto.getJSONObject(LOCALISATION).getJSONObject(ADRESSE);

                String adresseRestaurant = champsAdressejsonObject.getString(LABEL_ADRESSE);
                String codePostalRestaurant = champsAdressejsonObject.getString(LABEL_CODEPOSTAL);
                String villeRestaurant = champsAdressejsonObject.getJSONObject(LABEL_COMMUNE).getString(LABEL_NOM);
                System.out.println("adresse : "+adresseRestaurant+", codeP : "+codePostalRestaurant+", ville : "+villeRestaurant);


                System.out.println("------------------------------");
//
                /*On cree notre content value*/
                ContentValues restaurantValues = new ContentValues();

                /*On se charge de recuperer les images et de les mettre en base de donnée*/
                byte[] imageListeByte = null;
                byte[] imageFicheByte = null;
                imageListeByte = getImagesBytesFromUrl(urlListImage, imageListeByte);
                imageFicheByte = getImagesBytesFromUrl(urlFicheImage, imageFicheByte);

                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_RESTAURANT_ID, idRestaurant);
                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_NAME, nomRestaurant);
                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_ADRESSE, adresseRestaurant);
                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_VILLE, villeRestaurant);
                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_CODEPOSTAL, codePostalRestaurant);
                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_DESCRIPTION, descriptifRestaurant);

                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_IMG_LIST,imageListeByte );
                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_IMAGE_FICHE,imageFicheByte );
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_DESCRIPTION, descriptifRestaurant);
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_DATE, dateTime);
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_HUMIDITY, humidity);
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_PRESSURE, pressure);
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_WIND_SPEED, windSpeed);
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_DEGREES, windDirection);
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_MAX_TEMP, high);
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_MIN_TEMP, low);
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_SHORT_DESC, description);
//                restaurantValues.put(RestaurantContract.RestaurantEntry.COLUMN_RESTAURANT_ID, weatherId);

                cVVector.add(restaurantValues);
            }

//          on insert l'ensemble des resultats dans la base de donnees SQL

            int inserted = 0;

            // add to database
            Uri dataBaseRestaurantUri = RestaurantContract.RestaurantEntry.CONTENT_URI;
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(dataBaseRestaurantUri, cvArray);

                // delete old data so we don't build up an endless history
//                getContext().getContentResolver().delete(RestaurantContract.RestaurantEntry.CONTENT_URI,
//                        RestaurantContract.RestaurantEntry.COLUMN_DATE + " <= ?",
//                        new String[] {Long.toString(dayTime.setJulianDay(julianStartDay-1))});

//                notifyWeather();
            }

            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");

            System.out.println("On verifie maintenant notre base de donnée");
            System.out.println("--------------------------------------------");
            System.out.println("Affichage :\n");


            System.out.println("le path est  :" + getContext().getDatabasePath("restaurant"));

            System.out.println("On cree notre curseur");
            Cursor restaurantCursor =
                    getContext().getContentResolver().query(
                    dataBaseRestaurantUri, RestaurantFragment.RESTAURANT_COLUMNS,
                    null ,
                    null,
                    null);
            System.out.println("apres initialisation du curseur");
            if (restaurantCursor == null) {
                System.out.println("le curseur est null");
            }
            else {
                System.out.println("nb de ligne : " + restaurantCursor.getCount());
            }
            restaurantCursor.close();
//            System.out.println("nom");
//            System.out.println("-----------");
////            on affiche toutes les valeurs inserer
//            while (restaurantCursor.moveToNext()) {
//                // Faire quelque chose
//                System.out.println(restaurantCursor.getString(IndexBDRestaurant.INDEX_NOM));
//            }
//            //si on a reussi a avoir le 1er element
////            if (restaurantCursor.moveToFirst())
////            {
////                System.out.println(restaurantCursor.getString(IndexBDRestaurant.INDEX_RESTAURANT_ID));
////            }
////            else {
////                System.out.println("Le curseur est null");
////            }
//////            }
////            restaurantCursor.close();


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        System.out.println("On sort de la fonction");
    }

    private byte[] getImagesBytesFromUrl(String urlListImage, byte[] imageByte) {
        if (urlListImage != null) {
            Bitmap bitmap = Utility.loadBitmap(urlListImage);
            imageByte = Utility.getBytes(bitmap);
            System.out.println("Image null : " + imageByte);
        }
        else
            return null;
        return imageByte;
    }

    private String getUrlListImage(String TRADUCTION_FICHIER_LIST, String LABEL_URL_LIST_IMG, JSONArray illustrationsArray) throws JSONException {
        String urlListImage;
        JSONArray traductionFichierArray;
        traductionFichierArray = illustrationsArray.getJSONObject(0).getJSONArray(TRADUCTION_FICHIER_LIST);
        JSONObject traductionJsonObject = traductionFichierArray.getJSONObject(0);
        urlListImage = traductionJsonObject.getString(LABEL_URL_LIST_IMG);
        return urlListImage;
    }

    private void notifyWeather() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));

        if ( displayNotifications )
        {

//            String lastNotificationKey = context.getString(R.string.pref_last_notification);
//            long lastSync = prefs.getLong(lastNotificationKey, 0);
//
//            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
//                // Last sync was more than 1 day ago, let's send a notification with the weather.
//                String locationQuery = Utility.getPreferredLocation(context);
//
//                Uri weatherUri = RestaurantContract.RestaurantEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());
//
//                // we'll query our contentProvider, as always
//                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);
//
//                if (cursor.moveToFirst()) {
//                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
//                    double high = cursor.getDouble(INDEX_MAX_TEMP);
//                    double low = cursor.getDouble(INDEX_MIN_TEMP);
//                    String desc = cursor.getString(INDEX_SHORT_DESC);
//
//                    int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
//                    Resources resources = context.getResources();
//                    Bitmap largeIcon = BitmapFactory.decodeResource(resources,
//                            Utility.getImageRestaurant(weatherId));
//                    String title = context.getString(R.string.app_name);
//
//                    // Define the text of the forecast.
//                    String contentText = String.format(context.getString(R.string.format_notification),
//                            desc,
//                            Utility.formatTemperature(context, high),
//                            Utility.formatTemperature(context, low));
//
//                    // NotificationCompatBuilder is a very convenient way to build backward-compatible
//                    // notifications.  Just throw in some data.
//                    NotificationCompat.Builder mBuilder =
//                            new NotificationCompat.Builder(getContext())
//                                    .setColor(resources.getColor(R.color.sunshine_light_blue))
//                                    .setSmallIcon(iconId)
//                                    .setLargeIcon(largeIcon)
//                                    .setContentTitle(title)
//                                    .setContentText(contentText);
//
//                    // Make something interesting happen when the user clicks on the notification.
//                    // In this case, opening the app is sufficient.
//                    Intent resultIntent = new Intent(context, MainActivity.class);
//
//                    // The stack builder object will contain an artificial back stack for the
//                    // started Activity.
//                    // This ensures that navigating backward from the Activity leads out of
//                    // your application to the Home screen.
//                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//                    stackBuilder.addNextIntent(resultIntent);
//                    PendingIntent resultPendingIntent =
//                            stackBuilder.getPendingIntent(
//                                    0,
//                                    PendingIntent.FLAG_UPDATE_CURRENT
//                            );
//                    mBuilder.setContentIntent(resultPendingIntent);
//
//                    NotificationManager mNotificationManager =
//                            (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
//                    // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
//                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());
//
//                    //refreshing last sync
//                    SharedPreferences.Editor editor = prefs.edit();
//                    editor.putLong(lastNotificationKey, System.currentTimeMillis());
//                    editor.commit();
//                }
//                cursor.close();
//            }
        }
    }

//    /**
//     * Helper method to handle insertion of a new location in the weather database.
//     *
//     * @param locationSetting The location string used to request updates from the server.
//     * @param cityName A human-readable city name, e.g "Mountain View"
//     * @param lat the latitude of the city
//     * @param lon the longitude of the city
//     * @return the row ID of the added location.
//     */
//    long addLocation(String locationSetting, String cityName, double lat, double lon) {
//        long locationId;
//
//        // First, check if the location with this city name exists in the db
//        Cursor locationCursor = getContext().getContentResolver().query(
//                RestaurantContract.LocationEntry.CONTENT_URI,
//                new String[]{RestaurantContract.LocationEntry._ID},
//                RestaurantContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
//                new String[]{locationSetting},
//                null);
//
//        if (locationCursor.moveToFirst()) {
//            int locationIdIndex = locationCursor.getColumnIndex(RestaurantContract.LocationEntry._ID);
//            locationId = locationCursor.getLong(locationIdIndex);
//        } else {
//            // Now that the content provider is set up, inserting rows of data is pretty simple.
//            // First create a ContentValues object to hold the data you want to insert.
//            ContentValues locationValues = new ContentValues();
//
//            // Then add the data, along with the corresponding name of the data type,
//            // so the content provider knows what kind of value is being inserted.
//            locationValues.put(RestaurantContract.LocationEntry.COLUMN_CITY_NAME, cityName);
//            locationValues.put(RestaurantContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
//            locationValues.put(RestaurantContract.LocationEntry.COLUMN_COORD_LAT, lat);
//            locationValues.put(RestaurantContract.LocationEntry.COLUMN_COORD_LONG, lon);
//
//            // Finally, insert location data into the database.
//            Uri insertedUri = getContext().getContentResolver().insert(
//                    RestaurantContract.LocationEntry.CONTENT_URI,
//                    locationValues
//            );
//
//            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
//            locationId = ContentUris.parseId(insertedUri);
//        }
//
//        locationCursor.close();
//        // Wait, that worked?  Yes!
//        return locationId;
//    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);

//        syncImmediately(context);

    }
}