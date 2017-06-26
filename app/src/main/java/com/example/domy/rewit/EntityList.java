package com.example.domy.rewit;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.domy.rewit.adapters.EntityListAdapter;
import com.example.domy.rewit.googlePlaces.GooglePlaces;
import com.example.domy.rewit.interfaces.IFragment;
import com.example.domy.rewit.tasks.listTask;

import org.json.JSONObject;

/*
Tale frament visualizza una listView di entita' da recensire/visualizzare.Riceve in input la stringa da ricercare e un carattere
che indica se si desidera recensire o leggere le recensioni dell'entita'.
 */

public class EntityList extends Fragment {

    private IFragment mListener;
    private EntityListAdapter myAdapter;

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
        View rootView=inflater.inflate(R.layout.activity_entity_list,container,false);
        ListView myListView=(ListView) rootView.findViewById(R.id.listView);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString("JsonObj", myAdapter.getJSONobj(position).toString());
                if (getArguments().getChar("Choose")=='r')
                    mListener.swapActiveFragment(6,bundle);//FRAGMENT_REVIEW
                else
                    mListener.swapActiveFragment(9,bundle);//FRAGMENT_DB_REVIEW_LIST
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        View rootView=getView();
        ListView myListView=(ListView) rootView.findViewById(R.id.listView);
        String entity=getArguments().getString("Entity");
        if(myAdapter!=null){
            myListView.setAdapter(myAdapter);
        }
        else
            new myTask().execute(GooglePlaces.TYPE_TEXTSEARCH,entity);
    }

    private class myTask extends listTask {

        private View rootView=getView();
        private ListView myListView=(ListView) rootView.findViewById(R.id.listView);

        public myTask(){
            super(getActivity());
        }

        @Override
        protected void onPreExecute() {
            rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject[] jsonObjects) {
            if (isAdded()) {
                if (jsonObjects != null) {
                    myAdapter = new EntityListAdapter(getActivity(), jsonObjects);
                    myListView.setAdapter(myAdapter);
                }
                rootView.findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
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
