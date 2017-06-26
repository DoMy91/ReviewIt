package com.example.domy.rewit;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Domy on 02/02/15.
 */
public class LeaderboardBean {
    private String result;
    private int user_position;
    private int userNumRev;
    private ArrayList<HashMap<String,String>> list;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getUser_position() {
        return user_position;
    }

    public void setUser_position(int user_position) {
        this.user_position = user_position;
    }

    public ArrayList<HashMap<String, String>> getList() {
        return list;
    }

    public void setList(ArrayList<HashMap<String, String>> list) {
        this.list = list;
    }

    public int getUserNumRev() {
        return userNumRev;
    }

    public void setUserNumRev(int userNumRev) {
        this.userNumRev = userNumRev;
    }

}
