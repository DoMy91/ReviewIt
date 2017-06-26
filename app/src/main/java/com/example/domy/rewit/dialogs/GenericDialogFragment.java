package com.example.domy.rewit.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Domy on 16/01/15.
 */
public class GenericDialogFragment extends DialogFragment {

    public static GenericDialogFragment newInstance(Bundle bundle){

        GenericDialogFragment dialogFragment = new GenericDialogFragment();
        dialogFragment.setArguments(bundle);
        return dialogFragment;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString("title"));
        builder.setMessage(getArguments().getString("message"))
                .setPositiveButton(getArguments().getString("positiveButtonLabel"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(),Activity.RESULT_OK,getActivity().getIntent());
                    }
                })
                .setNegativeButton(getArguments().getString("negativeButtonLabel"), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getTargetFragment().onActivityResult(getTargetRequestCode(),Activity.RESULT_CANCELED,getActivity().getIntent());
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}