package com.example.domy.rewit.interfaces;

import android.os.Bundle;

/**
 * Created by Domy on 22/01/15.
 */

/*
Tale interfaccia consente ai vari fragment di lanciare metodi contenuti nella MainActivity che li ospita
 */

    public interface IFragment{
        /*
        Mediante tale metodo aggiorno la listview del navigation drawer per mostrare
        tutti i comandi in caso di login effettuato
        */
        public abstract void onStatusChange(boolean isConnected);

        /*
        Mediante tale metodo sostituisco il fragment attivo specificando la posizione del nuovo fragment (metodo selectItem della MainActivity)
        e gli argomenti da passargli mediante il Bundle.
        */
        public abstract void swapActiveFragment(int position,Bundle args);
    }

