package com.example.domy.rewit;


import android.app.Activity;
import android.content.res.Resources;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.domy.rewit.dialogs.GenericDialogFragment;
import com.example.domy.rewit.myApi.MyApi;
import com.example.domy.rewit.myApi.model.MyBean;
import com.example.domy.rewit.myApi.model.ReviewBean;
import com.example.domy.rewit.tasks.EndpointsAsyncTask;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
Fragment che consente all'utente di rilasciare una recensione.Riceve come argomento un JsonObject (passato come stringa nel bundle)
contentente i dettagli dell'entita'.La prima volta che l'utente lascia una recensione e' necessario registrare
l'utente nel DB (cio' viene effettuato controllando il valore di una variabile booleana isRegistered contenuta nelle SharedPreferences).
Nel caso in cui l'utente abbia gia' rilasciato una recensione per una determinata entita' in passato gli verra' mostrato un dialogFragment
con i dettagli della recensione passata,consentendogli anche di aggiornarla con quella appena inserita.Inoltre,una volta rilasciata la recensione
viene aggiornata la classifica e viene incrementato il progresso dei vari achievements.
 */

public class Entity extends Fragment{

    private JSONObject objectToReview;
    private TextView entityNameTV;
    private TextView entityAddressTV;
    private EditText userReviewET;
    private RatingBar ratingBar;
    private SharedPreferences sp;
    private ProgressBar progressBar;
    private View rootView;
    public static final int DIALOG_FRAGMENT = 1;
    private Resources resources;
    private GoogleApiClient googleApiClient=MainActivity.mGoogleApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_entity, container, false);
        entityNameTV=(TextView) rootView.findViewById(R.id.textView5);
        entityAddressTV=(TextView) rootView.findViewById(R.id.textView6);
        userReviewET=(EditText) rootView.findViewById(R.id.editText);
        ratingBar=(RatingBar) rootView.findViewById(R.id.ratingBar);
        ratingBar.setStepSize(1f);
        progressBar=(ProgressBar) rootView.findViewById(R.id.progressBar3);
        sp=getActivity().getSharedPreferences(MainActivity.PREFS_NAME,Context.MODE_PRIVATE);
        resources=getResources();
        try {
            objectToReview = new JSONObject(getArguments().getString("JsonObj"));
            entityNameTV.setText(objectToReview.getString("name"));
            if(objectToReview.has("formatted_address"))
                entityAddressTV.setText(objectToReview.getString("formatted_address"));
            else
                entityAddressTV.setText(objectToReview.getString("vicinity"));
        }
        catch (JSONException exc){
            Log.e("Entity",exc.toString());
        }
        /*Alla pressione del tasto enter nella editTextBox della recensione nascondo la tastiera*/
        userReviewET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager inputManager = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
        });
        Button button=(Button) rootView.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userReviewET.getText().toString().isEmpty()){
                    Toast toast=Toast.makeText(getActivity(),"Inserire la recensione!",Toast.LENGTH_LONG);
                    toast.show();
                }
                else{
                    new reviewEndpointsTask().execute();
                }
            }
        });
        return rootView;
    }


    private class reviewEndpointsTask extends EndpointsAsyncTask {
        //Mediante tale asynctask effettuo l'inserimento della recensione all'interno del DB
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(String... params) {
            MyApi myApiService=getMyApiService();
            ReviewBean response=new ReviewBean();
            try {
                //alla prima recensione che rilascia l'utente lo registro nel DB
                if (!sp.contains(MainActivity.userID)) {
                    Log.e("USER","NOT REGISTERED");
                    String userLocation= MainActivity.userLocation;
                    if(userLocation==null)
                        userLocation="NULL";
                    myApiService.registerUser(MainActivity.userID, MainActivity.userName,
                            userLocation, MainActivity.userPictureLink).execute();
                    sp.edit().putString(MainActivity.userID,"").apply();
                }
                Float valutation=ratingBar.getRating();
                //inserisco la recensione nel DB
                response = myApiService.insertReview(objectToReview.getString("place_id"), MainActivity.userID,
                        valutation.intValue(), userReviewET.getText().toString()).execute();
                return response;
            } catch (Exception exc) {
                response.setResult(exc.toString());
                return response;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            ReviewBean response = (ReviewBean) result;
            if(response.getResult().equals("OK"))
                achievementLeaderboardUpdate();//aggiorno classifica/obiettivi
            if (isAdded()) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity().getApplicationContext(), response.getResult(), Toast.LENGTH_LONG).show();
                if (response.getResult().equals("PRIMARY KEY VIOLATION")) {
                    //Se l'utente ha gia' rilasciato una recensione per tale entita' mostro un dialog
                    //nel quale gli consento di aggiornare la sua recensione.
                    Bundle bundle = new Bundle();
                    bundle.putString("title", "Hai gia' recensito questa entità!");
                    String message = "Data:" + response.getDate() +
                            "\nValutazione:" + response.getValutation() +
                            "\nRecensione:" + response.getDescription() +
                            "\nDesideri aggiornare la tua recensione?";
                    bundle.putString("message", message);
                    bundle.putString("positiveButtonLabel", getString(R.string.update));
                    bundle.putString("negativeButtonLabel", getString(R.string.cancel));
                    showDialog(DIALOG_FRAGMENT, bundle);
                } else
                    getActivity().onBackPressed();
            }
        }
    }


    private class updateEndpointsTask extends EndpointsAsyncTask{
        //Mediante tale asynctask effettuo l'aggiornamento della recensione all'interno del DB

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(String... params) {
            MyApi myApiService=getMyApiService();
            MyBean response=new MyBean();
            try {
                Float valutation=ratingBar.getRating();
                response = myApiService.updateReview(objectToReview.getString("place_id"), MainActivity.userID, valutation.intValue(),
                        userReviewET.getText().toString()).execute();
                return response;
            }
            catch (Exception exc){
                response.setData(exc.toString());
                return response;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if(isAdded()) {
                progressBar.setVisibility(View.INVISIBLE);
                MyBean response = (MyBean) result;
                Toast.makeText(getActivity().getApplicationContext(), response.getData(), Toast.LENGTH_LONG).show();
                getActivity().onBackPressed();
            }
        }
    }

    private void showDialog(int type,Bundle bundle){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        switch (type) {
            case DIALOG_FRAGMENT:
                DialogFragment dialogFrag = GenericDialogFragment.newInstance(bundle);
                dialogFrag.setTargetFragment(this, DIALOG_FRAGMENT);
                dialogFrag.show(getFragmentManager().beginTransaction(), "dialog");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case DIALOG_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    new updateEndpointsTask().execute();
                } else if (resultCode == Activity.RESULT_CANCELED){
                    // After Cancel code.
                }
                break;
        }
    }

    private void achievementLeaderboardUpdate(){
        if (googleApiClient != null && googleApiClient.isConnected()) {
            Games.Achievements.unlock(googleApiClient, resources.getString(R.string.achievement1_id));
            Games.Achievements.increment(googleApiClient, resources.getString(R.string.achievement2_id), 1);
            Games.Achievements.increment(googleApiClient, resources.getString(R.string.achievement3_id), 1);
            Games.Achievements.increment(googleApiClient, resources.getString(R.string.achievement5_id), 1);
            Games.Achievements.increment(googleApiClient, resources.getString(R.string.achievement6_id), 1);
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            Log.e("Current date", dateFormat.format(date));
            if (!sp.getString("date", "").equals(dateFormat.format(date))) {
                sp.edit().putInt("daily_counter", 0).apply();
                sp.edit().putString("date", dateFormat.format(date)).apply();
            }
            Integer counter=sp.getInt("daily_counter", 0);
            sp.edit().putInt("daily_counter", ++counter).apply();
            //5 recensioni rilasciate nella stessa giornata
            if (counter == 5)
                Games.Achievements.unlock(googleApiClient, resources.getString(R.string.achievement7_id));
            //10 recensioni rilasciate nella stessa giornata
            else if (counter == 10)
                Games.Achievements.unlock(googleApiClient,resources.getString(R.string.achievement4_id));
            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(googleApiClient,
                    resources.getString(R.string.leaderboard_id),LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                @Override
                public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                    LeaderboardScore c = arg0.getScore();
                    //Se l'utente non si trova ancora nella classifica lo inserisco
                    if(c==null){
                        Games.Leaderboards.submitScore(googleApiClient,resources.getString(R.string.leaderboard_id),1);
                    }
                    //altrimenti recupero il suo punteggio e lo incremento di 1
                    else {
                        Long new_score=Long.parseLong(c.getDisplayScore())+1;
                        Log.e("NEW_SCORE",new_score.toString());
                        Games.Leaderboards.submitScore(googleApiClient,resources.getString(R.string.leaderboard_id),new_score);
                    }
                }

            });

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

