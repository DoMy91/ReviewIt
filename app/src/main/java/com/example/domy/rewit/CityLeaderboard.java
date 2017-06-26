package com.example.domy.rewit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.domy.rewit.cards.LeaderboardCard;
import com.example.domy.rewit.myApi.MyApi;
import com.example.domy.rewit.myApi.model.JsonMap;
import com.example.domy.rewit.myApi.model.LeaderboardBean;
import com.example.domy.rewit.tasks.EndpointsAsyncTask;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

/*
Tale fragment visualizza la classifica dei migliori 10 reviewer che si trovano nella citta' dove l'utente risiede.
 */

public class CityLeaderboard extends Fragment {


    private CardArrayAdapter mCardArrayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_city_leaderboard, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        View rootView=getView();
        CardListView cardListView=(CardListView) rootView.findViewById(R.id.myList3);
        if(mCardArrayAdapter!=null)
            cardListView.setAdapter(mCardArrayAdapter);
        else
            new myTask().execute();
    }



    private class myTask extends EndpointsAsyncTask {

        private View rootView=getView();

        @Override
        protected void onPreExecute() {
            rootView.findViewById(R.id.circularProgressBar5).setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(String... params) {
            MyApi myApiService=getMyApiService();
            LeaderboardBean response=new LeaderboardBean();
            try {
                if (MainActivity.userLocation != null) {
                    response = myApiService.retrieveLocalLeaderboard(MainActivity.userID, MainActivity.userLocation).execute();
                    if(response.getList()==null)
                        response.setResult(getString(R.string.emptyLocalLeaderboard));
                }
                else
                    response.setResult(getString(R.string.fbLocationNotDefined));
            }
            catch(Exception exc){
                Log.e("CityLeaderboard", exc.toString());
                response.setResult(exc.toString());
            }
            return response;
        }

        @Override
        protected void onPostExecute(Object result) {
            if(isAdded()) {
                LeaderboardBean response = (LeaderboardBean) result;
                ArrayList<Card> cards;
                if(response.getResult().equals("OK")) {
                    int size = response.getList().size();
                    cards = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        //Creo le card che costituiscono la classifica
                        LeaderboardCard card = new LeaderboardCard(getActivity(), response.getList().get(i), i + 1);
                        if (response.getList().get(i).get("ID").toString().equals(MainActivity.userID))
                            card.setBackgroundResourceId(R.drawable.card_background_color3);//se la carta riguarda l'utente attuale la coloro di rosso
                        cards.add(card);
                    }
                    if (response.getUserPosition() > 10) {
                        /*
                        Se l'utente attuale non e' tra le prime 10 posizioni della classifica creo una card con i suoi dati e la appendo
                        in coda alla classifica.
                         */
                        JsonMap map = new JsonMap();
                        map.put("FULLNAME", MainActivity.userName);
                        map.put("LOCATION_NAME", MainActivity.userLocation);
                        map.put("PICTURE_LINK", MainActivity.userPictureLink);
                        map.put("NUMBER_REW", response.getUserNumRev());
                        LeaderboardCard currentUserCard = new LeaderboardCard(getActivity(), map, response.getUserPosition());
                        currentUserCard.setBackgroundResourceId(R.drawable.card_background_color3);
                        cards.add(currentUserCard);
                    }
                    mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
                    CardListView listView = (CardListView) rootView.findViewById(R.id.myList3);
                    listView.setAdapter(mCardArrayAdapter);
                    rootView.findViewById(R.id.circularProgressBar5).setVisibility(View.INVISIBLE);
                }
                else{
                    Toast.makeText(getActivity(),response.getResult(),Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            }

        }
    }


}
