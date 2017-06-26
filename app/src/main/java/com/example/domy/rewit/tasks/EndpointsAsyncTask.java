package com.example.domy.rewit.tasks;


import android.os.AsyncTask;

import com.example.domy.rewit.myApi.MyApi;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

/*
Tale classe astratta viene utilizzata per accomunare lo stesso oggetto MyApi a tutti gli asynctask che la estendono.
I task di questo tipo effettuano delle chiamate alle API dell'app engine (tradotte in funzioni di libreria grazie a Google Endpoints)
consentendo all'app di interagire con il database Google Cloud SQL che contiene i dati relativi alle recensioni rilasciate dagli utenti
e i dati degli utenti stessi.
 */

/**
 * Created by Domy on 13/01/15.
 */
abstract public class EndpointsAsyncTask extends AsyncTask<String,Void,Object> {

    private static MyApi myApiService = null;

    public EndpointsAsyncTask(){
        if(myApiService == null) {  // Only do this once
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    .setRootUrl("https://big-depth-784.appspot.com/_ah/api/");
            myApiService = builder.build();
        }
    }

    public static MyApi getMyApiService() {
        return myApiService;
    }
}