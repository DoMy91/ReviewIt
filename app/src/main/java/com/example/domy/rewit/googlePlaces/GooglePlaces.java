package com.example.domy.rewit.googlePlaces;

import android.util.Log;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLEncoder;

/**
 * Created by Domy on 06/01/15.
 */
public class GooglePlaces {

    private static final String LOG_TAG = "GooglePlaces";
    public static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    public static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    public static final String TYPE_NEARBYSEARCH = "/nearbysearch";
    public static final String TYPE_TEXTSEARCH = "/textsearch";
    public static final String TYPE_RADARSEARCH = "/radarsearch";
    public static final String TYPE_DETAILS = "/details";
    public static final String OUT_JSON = "/json";
    public static final String API_KEY = "AIzaSyCal6_eGY4iVbykbKIeLH5gir-oQ9uw2DY";

    public static JSONObject query (String type,String input,String latitude,String longitude){
        HttpClient placesClient = new DefaultHttpClient();
        JSONObject jsonObj=null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            HttpGet placesGet = new HttpGet(toLink(type,input,latitude,longitude));
            HttpResponse placesResponse = placesClient.execute(placesGet);
            StatusLine placeSearchStatus = placesResponse.getStatusLine();
            if (placeSearchStatus.getStatusCode() == 200) {
            //we have an OK response
                HttpEntity placesEntity = placesResponse.getEntity();
                InputStream placesContent = placesEntity.getContent();
                InputStreamReader placesInput = new InputStreamReader(placesContent);
                BufferedReader placesReader = new BufferedReader(placesInput);
                String lineIn;
                while ((lineIn = placesReader.readLine()) != null) {
                    jsonResults.append(lineIn);
                }
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return null;
        }
        try {
            jsonObj = new JSONObject(jsonResults.toString());
        }
        catch (JSONException exc){
            Log.e(LOG_TAG, "Cannot process JSON results", exc);
        }
        return jsonObj;
    }

    public static String toLink(String type,String input,String latitude,String longitude){
        StringBuilder sb = new StringBuilder(PLACES_API_BASE + type + OUT_JSON);
        String radius="1500";
        sb.append("?key=" + API_KEY);
        try {
            switch (type) {
                case TYPE_AUTOCOMPLETE:
                    sb.append("&language=it");
                    sb.append("&components=country:it");
                    sb.append("&input=" + URLEncoder.encode(input, "utf8"));
                    break;
                case TYPE_DETAILS:
                    sb.append("&language=it");
                    sb.append("&placeid=" + URLEncoder.encode(input, "utf8"));
                    return sb.toString();
                case TYPE_NEARBYSEARCH:
                    sb.append("&language=it");
                    if (input != null)
                        sb.append("&keyword=" + URLEncoder.encode(input, "utf8"));
                    break;
                case TYPE_RADARSEARCH:
                    if (input != null)
                        sb.append("&keyword=" + URLEncoder.encode(input, "utf8"));
                    break;
                case TYPE_TEXTSEARCH:
                    sb.append("&language=it");
                    if (input != null)
                        sb.append("&query=" + URLEncoder.encode(input, "utf8"));
                    break;
            }
            if(latitude!=null && longitude!=null) {
                sb.append("&location=" + latitude + "," + longitude);
                sb.append("&radius=" + radius);
            }
        }
        catch (Exception exc){
            Log.e("GooglePlaces",exc.toString());
        }
        //Log.e("PLACES API LINK:",sb.toString());
        return sb.toString();
    }

    public static String photoLink(String photoReference){
        StringBuilder urlImage = new StringBuilder(GooglePlaces.PLACES_API_BASE + "/photo?");
        urlImage.append("key=" + GooglePlaces.API_KEY);
        urlImage.append("&photoreference=" + photoReference);
        urlImage.append("&maxheight=50");
        return urlImage.toString();
    }

}