package com.example.domy.rewit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.domy.rewit.adapters.EntityListAdapter;
import com.example.domy.rewit.adapters.PlacesAutoCompleteAdapter;
import com.example.domy.rewit.googlePlaces.GooglePlaces;
import com.example.domy.rewit.interfaces.IFragment;
import com.example.domy.rewit.tasks.listTask;

import org.json.JSONObject;

/*
Tale fragment consente all'utente di ricercare un'entita',per recensirla o per leggere l'insieme delle sue recensioni.
Riceve come argomento un carattere che indica la volonta' dell'utente:'r'=>recensisci,'s'=>ricerca.
Mostra all'utente un'insieme di suggerimenti basati sulla sua posizione attuale (se i servizi di localizzazione sono attivi)
e in caso di ricerca,flaggando l'opzione nearby,consente di effettuare una ricerca per categorie (es. avvocato,dermatologo,ristorante cinese...)
nei paraggi dell'utente.Verra' quindi mostrato all'utente (mediante il fragment NearbySearch) una lista di entita' appartenenti alla
categoria inserita,ordinate tenendo conto del numero di recensioni ricevute e della media voti,in modo da consigliare all'utente i
migliori risultati.
 */


public class SearchEntity extends Fragment {

    private IFragment mListener;
    private PlacesAutoCompleteAdapter myAdapter;
    private EntityListAdapter suggestionsAdapt;

    @Override
    public void onAttach(Activity activity) {
        mListener = (IFragment) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    /*
    Siccome il fragment deve essere memorizzato sul back-stack,verra' salvato il suo stato (l'insieme dei suoi attributi).
    Memorizzo quindi gli adapter tra i suoi attributi in modo da non rieseguire il task per il popolamento della list-view dei suggerimenti in caso
    di pressione del tasto back da parte dell'utente.Memorizzo quindi il minor numero di attributi possibili nel fragment (es. evito di memorizzare
    la rootView) in modo da utilizzare meno RAM quando diversi fragment sono posti sul back-stack.I task vengono lanciati nell'onViewCreated()
    in modo da avere la certezza che la rootView sia disponibile (viene ottenuta con getView()) evitando quindi possibili
    nullPointerException nell'accesso a elementi dell'UI da parte del task.onCreate viene chiamata solo alla creazione del fragment,onCreateView
    ogni qualvolta il fragment deve essere visualizzato.
     */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Search icon status bar
        setHasOptionsMenu(true);
        myAdapter=new PlacesAutoCompleteAdapter(getActivity(),R.layout.actw_item,R.id.textContainer);
        if(MainActivity.userLatitude!=null && MainActivity.userLongitude!=null)
            myAdapter.setLocation(MainActivity.userLatitude, MainActivity.userLongitude);
        else
            Toast.makeText(getActivity(),getString(R.string.noLocationServices),Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_search_entity, container, false);
        CheckBox checkBox=(CheckBox) rootView.findViewById(R.id.checkBox);
        char choose=getArguments().getChar("Choose");
        if(MainActivity.userLatitude!=null && MainActivity.userLongitude!=null && choose=='s') {
            checkBox.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.textView24).setVisibility(View.VISIBLE);
        }
        AutoCompleteTextView acv=(AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTextView);
        acv.setAdapter(myAdapter);
        ListView suggestionsLv=(ListView) rootView.findViewById(R.id.listView2);
        suggestionsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle=new Bundle();
                if(getArguments().getChar("Choose")=='s') {
                    bundle.putString("JsonObj", suggestionsAdapt.getJSONobj(position).toString());
                    mListener.swapActiveFragment(9,bundle);//Start fragment DbReviewList
                }
                else {
                    bundle.putString("JsonObj", suggestionsAdapt.getJSONobj(position).toString());
                    mListener.swapActiveFragment(6,bundle);//Start fragment Entity
                }
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {
        View rootView=getView();
        ListView suggestionsLv=(ListView)rootView.findViewById(R.id.listView2);
        TextView textView=(TextView) rootView.findViewById(R.id.textView);
        if(suggestionsAdapt!=null) {
            textView.setVisibility(View.VISIBLE);
            if(getArguments().getChar("Choose")=='r'){
                //Centro orizzontalmente la stringa "Suggerimenti"
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                textView.setLayoutParams(lp);
            }
            suggestionsLv.setAdapter(suggestionsAdapt);
        }
        else
            new myTask().execute(GooglePlaces.TYPE_NEARBYSEARCH);
    }

    private class myTask extends listTask {
        /*
        Mediante tale task (la cui implementazione del metodo doInBackground() risiede nella classe astratta listTask),
        effettuo una ricerca delle entita' che si trovano nei dintorni dell'utente (mediante le google places API,opzione nearbysearch)
         */

        private View rootView=getView();
        private ProgressBar pb= (ProgressBar) rootView.findViewById(R.id.progressBar2);
        private ListView suggestionsLv=(ListView) rootView.findViewById(R.id.listView2);

        public myTask(){
            super(getActivity());
        }

        @Override
        protected void onPreExecute() {
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject[] jsonObjects) {
            //Verifico se il fragment e' ancora attaccato all'activity principale in modo da evitare possibili crash(NULLPOINTEREXC)
            if(isAdded()) {
                if (jsonObjects != null) {
                    TextView textView = (TextView) rootView.findViewById(R.id.textView);
                    textView.setVisibility(View.VISIBLE);
                    if(getArguments().getChar("Choose")=='r') {
                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        textView.setLayoutParams(lp);
                    }
                    suggestionsAdapt = new EntityListAdapter(getActivity(), jsonObjects);
                    suggestionsLv.setAdapter(suggestionsAdapt);
                }
                pb.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search_entity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View rootView=getView();
        AutoCompleteTextView acv=(AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTextView);
        CheckBox checkBox=(CheckBox) rootView.findViewById(R.id.checkBox);
        int id = item.getItemId();
        if (id == R.id.action_search) {
            //Se e' stata inserita una keyword per la ricerca
            if (!acv.getText().toString().isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("Entity", acv.getText().toString());
                //nascondo la tastiera
                InputMethodManager inputManager = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                //se l'opzione Nearby e' flaggata
                if (checkBox.isChecked()) {
                    mListener.swapActiveFragment(8, bundle);
                } else {
                    if (getArguments().getChar("Choose") == 'r')
                        bundle.putChar("Choose", 'r');
                    else
                        bundle.putChar("Choose", 'f');
                    //carico il fragment opportuno
                    mListener.swapActiveFragment(7, bundle);
                }
            } else
                Toast.makeText(getActivity(), R.string.error1, Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }
}



