package com.example.domy.rewit;


import java.util.ArrayList;
import java.util.HashMap;


/**
 * The object model for the data we are sending through endpoints
 */
public class MyBean {

    private String myData;

    private ArrayList<HashMap<String,String>> values;

    public String getData() {
        return myData;
    }

    public void setData(String data) {
        myData = data;
    }

    public ArrayList<HashMap<String, String>> getValues() {
        return values;
    }

    public void setValues() {
        values=new ArrayList<>(2);
        HashMap<String,String> myMap1=new HashMap<>(3);
        HashMap<String,String> myMap2=new HashMap<>(3);
        myMap1.put("userName","Domenico Scognamiglio");
        myMap1.put("userId","198432794872932");
        myMap1.put("number of review","3");
        myMap2.put("userName","Domenico");
        myMap2.put("userId","1984327");
        myMap2.put("number of review","10");
        values.add(myMap1);
        values.add(myMap2);
    }
}