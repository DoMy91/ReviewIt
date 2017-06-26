package com.example.domy.rewit;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Domy on 17/01/15.
 */
public class ReviewListBean {
    private String result;//Esito della richiesta:OK,EMPTY SET
    private ArrayList<HashMap<String,String>> list;//La lista delle recensioni
    private Float avg;//La media voto
    private int totalRev;//Il numero totale delle recensioni ricevute
    private HashMap<Integer,Integer> ratings;//Il numero di recensioni ricevute per ognuna delle 5 stelle

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public ArrayList<HashMap<String, String>> getList() {

        return list;
    }

    public void setList(ArrayList<HashMap<String, String>> list) {
        this.list = list;
    }

    public Float getAvg() {
        return avg;
    }

    public void setAvg(Float avg) {
        this.avg = avg;
    }

    public HashMap<Integer, Integer> getRatings() {
        return ratings;
    }

    public void setRatings() {
        ratings=new HashMap<>(5);
        for(int i=1;i<=5;i++)
            ratings.put(i,0);
    }

    public int getTotalRev() {
        return totalRev;
    }

    public void setTotalRev(int numberRev) {
        this.totalRev = numberRev;
    }
}
