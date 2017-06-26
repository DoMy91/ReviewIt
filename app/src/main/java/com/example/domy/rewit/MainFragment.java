package com.example.domy.rewit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.domy.rewit.dialogs.myDialogFragments;
import com.example.domy.rewit.interfaces.IFragment;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.Arrays;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by Domy on 05/01/15.
 */

public class MainFragment extends Fragment{

    private static final String TAG = "MainFragment";
    private UiLifecycleHelper uiHelper;
    private View view;
    private IFragment mListener;
    private TextView textView;
    private ImageView imageView;
    private CircularProgressBar pbImage,pbName;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_fragment, container, false);
        textView=(TextView) view.findViewById(R.id.textView22);
        imageView=(ImageView)view.findViewById(R.id.imageView8);
        pbName=(CircularProgressBar) view.findViewById(R.id.circularProgressBar2);
        pbImage=(CircularProgressBar) view.findViewById(R.id.circularProgressBar3);
        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("user_location"));
        if(Session.getActiveSession().getState().isOpened()) {
            if(new myDialogFragments(getActivity().getApplicationContext()).isOnline()) {
                view.findViewById(R.id.textView23).setVisibility(View.VISIBLE);
                if (MainActivity.userName != null) {
                    textView.setText(MainActivity.userName + "!");
                    mListener.onStatusChange(true);
                }
                else
                    getUserInfo(Session.getActiveSession());
            /*si verifica nel caso in cui durante l'esecuzione dei 2 asynctask il fragment viene distrutto (es. tasto back durante il caricamento).
            In tale situazione la sessione sara' aperta,ma le variabili globali della MainActivity saranno NULL.E' necessario allora
            procedere nuovamente alla richiesta dei dati dell'utente.
             */
                if (MainActivity.userPictureLink != null)
                    setProfileFBImage(MainActivity.userPictureLink);
                else
                    getUserPicture(Session.getActiveSession());
            }
            else
                Session.getActiveSession().close();
        }
        return view;
    }


    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
                onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened() && new myDialogFragments(getActivity().getApplicationContext()).isOnline()) {
            Log.i(TAG, "Logged in...");
            textView.setVisibility(View.INVISIBLE);
            pbName.setVisibility(View.VISIBLE);
            getUserInfo(session);
            pbImage.setVisibility(View.VISIBLE);
            getUserPicture(session);
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            view.findViewById(R.id.textView23).setVisibility(View.INVISIBLE);
            textView.setText("Accedi a Facebook per recensire!!");
            imageView.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.textView22).setVisibility(View.VISIBLE);
            if(!new myDialogFragments(getActivity().getApplicationContext()).isOnline()){
                myDialogFragments.AlertDialogFragment locDialog=new myDialogFragments.AlertDialogFragment();
                Bundle bundle=new Bundle();
                bundle.putString("Service",Context.CONNECTIVITY_SERVICE);
                locDialog.setArguments(bundle);
                locDialog.show(getActivity().getSupportFragmentManager(),"conDialog");
            }
            mListener.onStatusChange(false);
        }
    }

    public void getUserInfo(Session session){
        //Richiesta per le informazioni di base dell'utente(id,nome completo,citta' di residenza).
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (isAdded()) {
                    pbName.setVisibility(View.INVISIBLE);
                    if (user != null) {
                        view.findViewById(R.id.textView23).setVisibility(View.VISIBLE);
                        textView.setText(user.getName() + "!");
                        textView.setVisibility(View.VISIBLE);
                        if(user.getLocation()!=null)
                            Log.e(TAG, user.getId() + " " + user.getLocation().getName());
                        if (MainActivity.userID == null || !MainActivity.userID.equals(user.getId())) {
                            MainActivity.userID = user.getId();
                            MainActivity.userName = user.getName();
                            if(user.getLocation()!=null)
                                MainActivity.userLocation = user.getLocation().getName();
                            else
                                MainActivity.userLocation=null;
                        }
                        mListener.onStatusChange(true);
                    }
                }
            }
        }).executeAsync();
    }

    public void getUserPicture(Session session){
        //Richiesta del link della foto del profilo dell'utente.
        Bundle params = new Bundle();
        params.putBoolean("redirect", false);
        params.putString("height", "200");
        params.putString("type", "normal");
        params.putString("width", "200");
        new Request(
                session,
                "/me/picture",
                params,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        if (isAdded()) {
                            try {
                                pbImage.setVisibility(View.INVISIBLE);
                                String photoLink = response.getGraphObject().getInnerJSONObject().getJSONObject("data").getString("url");
                                setProfileFBImage(photoLink);
                                Log.e("Response", photoLink);
                                MainActivity.userPictureLink = photoLink;
                            } catch (JSONException exc) {
                                Log.e("JSON exception", exc.toString());
                            }
                        }
                    }
                }
        ).executeAsync();
    }


    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    public void setProfileFBImage(String link){
        imageView.setVisibility(View.VISIBLE);
        Picasso.with(getActivity())
                .load(link)
                .error(R.drawable.ic_launcher)
                .tag(getActivity())
                .into(imageView);
    }

}
