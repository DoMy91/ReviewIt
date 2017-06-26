package com.example.domy.rewit;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.domy.rewit.cards.EntityCard;
import com.example.domy.rewit.googlePlaces.GooglePlaces;
import com.example.domy.rewit.interfaces.IFragment;
import com.example.domy.rewit.myApi.MyApi;
import com.example.domy.rewit.myApi.model.EntityListBean;
import com.example.domy.rewit.tasks.EndpointsAsyncTask;
import com.example.domy.rewit.tasks.listTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

public class NearbySearch extends Fragment {

    private IFragment mListener;
    private CardArrayAdapter mCardArrayAdapter;

    @Override
    public void onAttach(Activity activity) {
        mListener = (IFragment) activity;
        super.onAttach(activity);
        Log.e("OnAttach","CALLED");
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
        Log.e("OnDetach","CALLED");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_nearby_search,container,false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        View rootView=getView();
        CardListView listView = (CardListView) rootView.findViewById(R.id.myList2);
        if(mCardArrayAdapter!=null) {
            /*Se torno indietro al fragment siccome e' salvato sul back-stack  avro' che mCardArrayAdapter!=null
            e quindi evito di avviare nuovamente il task per il popolamento della listview.
             */
            listView.setAdapter(mCardArrayAdapter);
        }
        else {
            String entity = getArguments().getString("Entity");
            new myTask().execute(GooglePlaces.TYPE_RADARSEARCH, entity);
        }
    }

    private class myTask extends listTask {

        private View rootView=getView();
        public myTask(){
            super(getActivity());
        }

        @Override
        protected void onPreExecute() {
            rootView.findViewById(R.id.circularProgressBar4).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject[] jsonObjects) {
            if(isAdded()){
                if(jsonObjects!=null && jsonObjects.length>0){
                    String[] list_place_id=new String[jsonObjects.length];
                    try {
                        for (int i = 0; i < jsonObjects.length; i++)
                            list_place_id[i] = jsonObjects[i].getString("place_id");
                        new getNearbyEndpointsTask().execute(list_place_id);
                    }
                    catch (JSONException exc){
                        Log.e("NearbySearch",exc.toString());
                    }
                }
                else {
                    if(jsonObjects!=null && jsonObjects.length==0)
                        Toast.makeText(getActivity(),"ZERO_RESULT",Toast.LENGTH_LONG).show();
                    rootView.findViewById(R.id.circularProgressBar4).setVisibility(View.INVISIBLE);
                    getActivity().onBackPressed();
                }
            }
        }
    }

    private class getNearbyEndpointsTask extends EndpointsAsyncTask {

        private View rootView=getView();
        private EntityListBean response;
        private ArrayList<Card> cards;

        @Override
        protected Object doInBackground(String... params) {
            MyApi myApiService=getMyApiService();
            try {
                response = myApiService.nearbyEntityList(Arrays.asList(params)).execute();
                return response.getResult();
            }
            catch (Exception exc){
                return(exc.toString());
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            if(isAdded()) {
                String status=result.toString();
                Toast.makeText(getActivity().getApplicationContext(), status, Toast.LENGTH_LONG).show();
                if(status.equals("OK")) {
                    int size=response.getList().size();
                    cards=new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        new myTask2(i,response,cards).execute(GooglePlaces.TYPE_DETAILS, response.getList().get(i).get("PLACE_ID").toString());
                    }
                    mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
                    CardListView listView = (CardListView) rootView.findViewById(R.id.myList2);
                    listView.setAdapter(mCardArrayAdapter);
                    rootView.findViewById(R.id.circularProgressBar4).setVisibility(View.INVISIBLE);
                }
                else
                    getActivity().onBackPressed();
            }
        }
    }

    private class myTask2 extends listTask{
        private int index;
        private EntityListBean response;
        private ArrayList<Card> cards;

        public myTask2(int index,EntityListBean response,ArrayList<Card> cards){
            super(getActivity());
            this.index=index;
            this.response=response;
            this.cards=cards;
        }

        @Override
        protected void onPostExecute(final JSONObject[] jsonObjects) {
            if (isAdded()) {
                if (jsonObjects != null) {
                    try {
                        String name = jsonObjects[0].getString("name");
                        String address = jsonObjects[0].getString("vicinity");
                        String photoLink = null;
                        if (jsonObjects[0].has("photos")) {
                            photoLink = GooglePlaces.photoLink(jsonObjects[0].getJSONArray("photos").
                                    getJSONObject(0).getString("photo_reference"));
                            response.getList().get(index).set("photoLink", photoLink);
                        }
                        response.getList().get(index).set("name", name);
                        response.getList().get(index).set("address", address);
                        EntityCard entityCard = new EntityCard(getActivity(), response.getList().get(index));
                        entityCard.setOnClickListener(new Card.OnCardClickListener() {
                            @Override
                            public void onClick(Card card, View view) {
                                Bundle bundle = new Bundle(1);
                                bundle.putString("JsonObj", jsonObjects[0].toString());
                                mListener.swapActiveFragment(9,bundle);//FRAGMENT DbReviewList
                            }
                        });
                        cards.add(index, entityCard);
                        mCardArrayAdapter.notifyDataSetChanged();
                    } catch (JSONException exc) {
                        Log.e("NearbySearch", exc.toString());
                    }
                }
            }
        }
    }
}
