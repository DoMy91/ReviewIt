package com.example.domy.rewit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;

import com.example.domy.rewit.R;

/**
 * Created by Domy on 10/01/15.
 */
/*Tale classe mette disposizione i 2 metodi d'utilita' isOnline() e isLocationEnabled()
* che consentono di verificare rispettivamente la connessione a internet e lo stato
* dei servizi di localizzazione.Viene inoltre definita la struttura del DialogFragment
* che dovra' essere visualizzato nell'activity chiamante per invitare l'utente ad
* intraprendere le azioni opportune.*/

public class myDialogFragments{

    private static Context context;

    public myDialogFragments(Context context){
        this.context=context;
    }

    public static class AlertDialogFragment extends android.support.v4.app.DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String title,message,positiveButtonLabel,negativeButtonLabel;
            final Intent intent;
            Bundle bundle=getArguments();
            //build connection dialog
            if(bundle.getString("Service").equals(context.CONNECTIVITY_SERVICE)){
                title=getString(R.string.AlertConnectionTitle);
                message=getString(R.string.AlertConnectionMessage);
                intent=new Intent(Settings.ACTION_WIFI_SETTINGS);
                positiveButtonLabel=getString(R.string.AlertConnectionPositiveButtonLabel);
                negativeButtonLabel=getString(R.string.AlertConnectionNegativeButtonLabel);
            }
            //build location dialog
            else {
                title=getString(R.string.AlertLocalizationTitle);
                message=getString(R.string.AlertLocalizationMessage);
                intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                positiveButtonLabel=getString(R.string.AlertLocalizationPositiveButtonLabel);
                negativeButtonLabel=getString(R.string.AlertLocalizationNegativeButtonLabel);
            }
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
            builder.setMessage(message)
                    .setPositiveButton(positiveButtonLabel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getActivity().startActivity(intent);
                        }
                    })
                    .setNegativeButton(negativeButtonLabel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null &&
                cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public boolean isLocationEnabled(){
        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}

