package com.example.domy.rewit.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.domy.rewit.googlePlaces.GooglePlaces;
import com.example.domy.rewit.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Domy on 09/01/15.
 */

/*Tale classe definisce il metodo generico doInBackground comune alle activity che contengono
* listview ottenute da chiamate alle API di google places.Tale metodo consente di effettuare
* una chiamata asincrona alle google places API e restituisce al metodo onPostExecute l'array
* dei risultati sottoforma di JSONobject[]*/

abstract public class listTask extends AsyncTask<String,Void,JSONObject[]> {
    private Context context;

    public listTask(Context context) {
        this.context = context;
    }

    @Override
    protected JSONObject[] doInBackground(String... params) {

        //params[0]=tipo di ricerca da eseguire es. nearbysearch,textsearch,radarsearch
        //params[1] opzionale,in caso di textsearch/radarsearch rappresenta la stringa da ricercare

        JSONObject[] results = null;
        String latitude = null, longitude = null;
        if (MainActivity.userLatitude!=null && MainActivity.userLongitude!=null) {
            latitude = MainActivity.userLatitude;
            longitude = MainActivity.userLongitude;
        }
        else{
            if(params[0].equals(GooglePlaces.TYPE_NEARBYSEARCH) ||params[0].equals(GooglePlaces.TYPE_RADARSEARCH))
                return null;
        }
        String textSearch=null;
        if(params.length==2)
            textSearch=params[1];
        JSONObject queryResult = GooglePlaces.query(params[0], textSearch, latitude, longitude);
        try {
            if(params[0].equals(GooglePlaces.TYPE_DETAILS)) {
                results = new JSONObject[1];
                results[0]=queryResult.getJSONObject("result");
                return results;
            }
            JSONArray res = queryResult.getJSONArray("results");
            results = new JSONObject[res.length()];
            for (int i = 0; i < res.length(); i++) {
                results[i] = res.getJSONObject(i);
            }
        } catch (Exception exc) {
            Log.e("listTask",exc.toString());
        }
        return results;
    }
}
