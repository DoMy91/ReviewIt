package com.example.domy.rewit.adapters;


import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.example.domy.rewit.googlePlaces.GooglePlaces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
Adattatore che si occupa del popolamento dell'AutoCompleteTextView che mostra i suggerimenti all'utente durante l'inserimento.
 */

public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> resultList;
    private String latitude=null;
    private String longitude=null;

    public PlacesAutoCompleteAdapter(Context context,int resource, int textViewResourceId) {
        super(context,resource,textViewResourceId);
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    /*
    Ad ogni lettera inserita dall'utente viene avviato un thread effettua una query alle Google Places API (autocomplete)
    in modo da visualizzare una serie di suggerimenti nell'AutoCompleteTextView.
     */
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());
                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private ArrayList<String> autocomplete(String input) {
        JSONObject jsonResults= GooglePlaces.query(GooglePlaces.TYPE_AUTOCOMPLETE, input, latitude, longitude);
        ArrayList<String> resultList = null;
        try {
            // Create a JSON object hierarchy from the results
            JSONArray predsJsonArray = jsonResults.getJSONArray("predictions");
            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++)
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
        } catch (JSONException e) {
            Log.e("Cannot process JSON results", e.toString());
        }

        return resultList;
    }

    public void setLocation(String latitude,String longitude){
        this.latitude=latitude;
        this.longitude=longitude;
    }



}