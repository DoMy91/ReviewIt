package com.example.domy.rewit;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Domy on 29/01/15.
 */
public class EntityListBean {
    private String result;
    private ArrayList<HashMap<String,String>> list;

    public void setList(ArrayList<HashMap<String, String>> list) {
        this.list = list;
    }

    public ArrayList<HashMap<String, String>> getList() {
        return list;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
