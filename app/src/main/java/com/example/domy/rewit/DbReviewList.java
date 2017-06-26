package com.example.domy.rewit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.domy.rewit.cards.EntitySummaryCard;
import com.example.domy.rewit.cards.ReviewCard;
import com.example.domy.rewit.myApi.MyApi;
import com.example.domy.rewit.myApi.model.ReviewListBean;
import com.example.domy.rewit.tasks.EndpointsAsyncTask;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

/*
Tale fragment ospita una CardListView che mostra all'utente le recensioni ricevute dagli utenti di una determinata entità.
Riceve in input un JsonObject contenente i dettagli dell'entita'.
 */

public class DbReviewList extends Fragment {

    private JSONObject entity;
    CircularProgressBar progressBar;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_db_review_list, container, false);
        progressBar=(CircularProgressBar) rootView.findViewById(R.id.circularProgressBar);
        try {
            entity = new JSONObject(getArguments().getString("JsonObj"));
            new getListReviewEndpointsTask().execute(entity.getString("place_id"));
        }
        catch(JSONException exc){
            Log.e("DbReviewList",exc.toString());
        }
        return rootView;

    }

    private class getListReviewEndpointsTask extends EndpointsAsyncTask {

        @Override
        protected Object doInBackground(String... params) {
            MyApi myApiService = getMyApiService();
            ReviewListBean response = new ReviewListBean();
            try {
                response = myApiService.getListReview(params[0]).execute();
            } catch (IOException exc) {
                response.setResult(exc.toString());
            }
            return response;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (isAdded()) {
                ReviewListBean response = (ReviewListBean) result;
                Toast.makeText(getActivity().getApplicationContext(), response.getResult(), Toast.LENGTH_LONG).show();
                if(response.getResult().equals("OK")){
                    ArrayList<Card> cards = new ArrayList<>();
                    EntitySummaryCard card = new EntitySummaryCard(getActivity(), entity, response);
                    cards.add(card);
                    for (int i = 0; i < response.getTotalRev(); i++) {
                        ReviewCard reviewCard = new ReviewCard(getActivity(), response.getList().get(i));
                        cards.add(reviewCard);
                    }
                    CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
                    mCardArrayAdapter.setInnerViewTypeCount(2);
                    CardListView listView = (CardListView) rootView.findViewById(R.id.myList);
                    AnimationAdapter animCardArrayAdapter = new SwingBottomInAnimationAdapter(mCardArrayAdapter);
                    animCardArrayAdapter.setAbsListView(listView);
                    listView.setExternalAdapter(animCardArrayAdapter, mCardArrayAdapter);
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else
                    getActivity().onBackPressed();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
