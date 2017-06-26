/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.example.domy.rewit;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.repackaged.com.google.api.client.json.JsonObjectParser;
import com.google.appengine.repackaged.org.codehaus.jackson.JsonFactory;
import com.google.protos.cloud.sql.Sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */



@Api(name = "myApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "rewit.domy.example.com", ownerName = "rewit.domy.example.com", packagePath = ""))
public class MyEndpoint {

    public final String dbLink="jdbc:google:mysql://big-depth-784:review/PROVA?user=root";

/*
Mediante il metodo registerUser registro l'utente nel DB.
 */
    @ApiMethod(name="registerUser")
    public MyBean registerUser(@Named("userID") String userID,@Named("userFullname") String userFullname,
                               @Named("userLocation") String userLocation,@Named("photoLink") String photoLink){
        MyBean response = new MyBean();
        try {
            Class.forName("com.mysql.jdbc.GoogleDriver");
        }
        catch (Exception e) {
            response.setData(e.toString());
        }
        try{
            Connection connection= DriverManager.getConnection(dbLink);
            try{
                String insert="INSERT INTO APP_USER VALUES(?,?,?,0,?)";
                PreparedStatement stmt = connection.prepareStatement(insert);
                stmt.setString(1,userID);
                stmt.setString(2,userFullname);
                stmt.setString(3,userLocation);
                stmt.setString(4,photoLink);
                stmt.executeUpdate();
                response.setData("OK");
            }
            finally {
                connection.close();
            }
        }
        catch(SQLException exc){

            response.setData(exc.toString());
        }
        return response;
    }

    /*
    Il metodo insertReview inserisce la recensione all'interno del DB.Nel caso in cui l'utente abbia gia' rilasciato in
    passato una recensione per la stessa entita' lo stato ritornato sara' "PRIMARY KEY VIOLATION".Inoltre vengono ritornati
    anche i dettagli della recensione precedentemente inserita.
     */

    @ApiMethod(name="insertReview")
    public ReviewBean insertReview(@Named("placeID") String placeID,@Named("userID") String userID,@Named("valutation") int valutation,
                                   @Named("description") String description){
        ReviewBean response = new ReviewBean();
        try {
            Class.forName("com.mysql.jdbc.GoogleDriver");
        }
        catch (Exception e) {
            response.setResult(e.toString());
        }
        try{
            Connection connection= DriverManager.getConnection(dbLink);
            try{
                String check="SELECT * FROM REVIEW WHERE PLACE_ID=? AND USER_ID=?";
                PreparedStatement stmt = connection.prepareStatement(check);
                stmt.setString(1,placeID);
                stmt.setString(2,userID);
                ResultSet rs=stmt.executeQuery();
                if(rs.next()) {
                    response.setDate(rs.getDate("DATE_REW").toString());
                    response.setDescription(rs.getString("DESCRIPTION"));
                    response.setValutation(rs.getInt("VALUTATION"));
                    response.setResult("PRIMARY KEY VIOLATION");
                    return response;
                }
                String insert="INSERT INTO REVIEW VALUES(?,?,?,?,?)";
                stmt = connection.prepareStatement(insert);
                stmt.setString(1,placeID);
                stmt.setString(2,userID);
                java.util.Date utilDate = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                stmt.setDate(3,sqlDate);
                stmt.setInt(4, valutation);
                stmt.setString(5,description);
                stmt.executeUpdate();
                response.setResult("OK");
            }
            finally {
                connection.close();
            }
        }
        catch(SQLException exc){
            response.setResult(exc.toString());
        }
        return response;
    }

    /*
    Il metodo updateReview effettua l'aggiornamento di una recensione all'interno del DB.
     */

    @ApiMethod(name="updateReview")
    public MyBean updateReview(@Named("placeID") String placeID,@Named("userID") String userID,@Named("valutation") int valutation,
                               @Named("description") String description){
        MyBean response = new MyBean();
        try {
            Class.forName("com.mysql.jdbc.GoogleDriver");
        }
        catch (Exception e) {
            response.setData(e.toString());
        }
        try{
            Connection connection= DriverManager.getConnection(dbLink);
            try{
                String insert="UPDATE REVIEW SET DATE_REW=?,VALUTATION=?,DESCRIPTION=? WHERE PLACE_ID=? AND USER_ID=?";
                PreparedStatement stmt = connection.prepareStatement(insert);
                java.util.Date utilDate = new java.util.Date();
                java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                stmt.setDate(1,sqlDate);
                stmt.setInt(2,valutation);
                stmt.setString(3, description);
                stmt.setString(4,placeID);
                stmt.setString(5,userID);
                stmt.executeUpdate();
                response.setData("OK");
            }
            finally {
                connection.close();
            }
        }
        catch(SQLException exc){
            response.setData(exc.toString());
        }
        return response;
    }

    /*
    Il metodo getListReview prende in input un placeID e ritorna l'insieme delle recensioni ricevute di tale entita'
    ordinate a partire dalla piu' recente,unitamente ai dati degli utenti che hanno rilasciato tali recensioni.
    Nel caso in cui siano presenti delle recensioni il metodo ritornera' anche:
    -la valutazione media;
    -il numero totale di recensioni ricevute;
    -il numero di recensioni ricevute per ogni possibile valutazione (es. num di rec da 1 stella,2 stelle...)
     */

    @ApiMethod(name="getListReview")
    public ReviewListBean getListReview(@Named("placeID") String placeID){
        ReviewListBean response = new ReviewListBean();
        try {
            Class.forName("com.mysql.jdbc.GoogleDriver");
        }
        catch (Exception e) {
            response.setResult(e.toString());
        }
        try{
            Connection connection= DriverManager.getConnection(dbLink);
            try{
                String query="SELECT FULLNAME,VALUTATION,DESCRIPTION,DATE_REW,PICTURE_LINK FROM " +
                        "REVIEW R JOIN APP_USER A ON R.USER_ID=A.ID WHERE PLACE_ID=? ORDER BY DATE_REW DESC";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1,placeID);
                ResultSet rs=stmt.executeQuery();
                ArrayList<HashMap<String, String>> list=new ArrayList<>();
                while(rs.next()) {
                    HashMap<String, String> singleRow = new HashMap<>(5);
                    singleRow.put("FULLNAME", rs.getString(1));
                    Integer val = rs.getInt(2);
                    singleRow.put("VALUTATION",val.toString());
                    singleRow.put("DESCRIPTION", rs.getString(3));
                    singleRow.put("DATE_REW", rs.getDate(4).toString());
                    singleRow.put("PICTURE_LINK", rs.getString(5));
                    list.add(singleRow);
                }
                if(list.isEmpty())
                    response.setResult("EMPTY SET");
                else {
                    response.setResult("OK");
                    response.setList(list);
                    query="SELECT AVG(VALUTATION),COUNT(*) FROM REVIEW WHERE PLACE_ID=?";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1,placeID);
                    rs=stmt.executeQuery();
                    rs.next();
                    response.setAvg(rs.getFloat(1));
                    response.setTotalRev(rs.getInt(2));
                    response.setRatings();
                    HashMap<Integer,Integer> ratings=response.getRatings();
                    query="SELECT VALUTATION,COUNT(*) FROM REVIEW WHERE PLACE_ID=? GROUP BY VALUTATION";
                    stmt = connection.prepareStatement(query);
                    stmt.setString(1,placeID);
                    rs=stmt.executeQuery();
                    while(rs.next()){
                        ratings.put(rs.getInt(1),rs.getInt(2));
                    }
                }
            }
            finally {
                connection.close();
            }
        }
        catch(SQLException exc){
            response.setResult(exc.toString());
        }
        return response;
    }

    /*
    Il metodo NearbyEntityList riceve in input una lista di placeID ottenuti da una radarSearch (Google places API).
    Tale metodo effettua una ricerca all'interno del DB basandosi sulla lista ricevuta e ritorna l'insieme delle entita' presenti
    nel database che sono contenute in tale lista,ordinate tenendo conto del numero di recensioni ricevute e della valutazione media.
     */

    @ApiMethod(name="NearbyEntityList")
    public EntityListBean NearbyEntity(@Named("placeID") ArrayList<String> placesID){
        EntityListBean response = new EntityListBean();
        int i;
        try {
            Class.forName("com.mysql.jdbc.GoogleDriver");
        }
        catch (Exception e) {
            response.setResult(e.toString());
        }
        try{
            Connection connection= DriverManager.getConnection(dbLink);
            try{
                String s=placesID.get(0);
                ArrayList<String> listPlaceId=new ArrayList<>();
                /*Siccome ricevo un'unica stringa contenente tutti i place id separati da virgole,li estraggo uno alla volta
                * in modo da costruire l'array di stringhe dei place id.*/
                for(i=0;i<s.length();i++)
                {
                    StringBuilder sb=new StringBuilder();
                    while(i<s.length() && s.charAt(i)!=',') {
                        sb.append(s.charAt(i));
                        i++;
                    }
                    listPlaceId.add(sb.toString());
                }
                //Costruisco la query inserendo tanti ? quanti sono i place id ricevuti in input
                StringBuilder sb=new StringBuilder();
                String str="SELECT PLACE_ID,AVG(VALUTATION),COUNT(*) FROM REVIEW WHERE PLACE_ID IN(";
                sb.append(str);
                for(i=0;i<listPlaceId.size();i++){
                    sb.append('?');
                    if(i!=listPlaceId.size()-1)
                        sb.append(',');
                }
                sb.append(')');
                sb.append("GROUP BY PLACE_ID ORDER BY ((AVG(VALUTATION)-2.5)*COUNT(*)) DESC");
                String query=sb.toString();
                PreparedStatement stmt = connection.prepareStatement(query);
                //Sostituisco a ogni ? il suo valore
                for(i=0;i<listPlaceId.size();i++)
                    stmt.setString(i+1,listPlaceId.get(i));
                ResultSet rs=stmt.executeQuery();
                ArrayList<HashMap<String, String>> list=new ArrayList<>();
                while(rs.next()) {
                    HashMap<String, String> singleRow = new HashMap<>(3);
                    singleRow.put("PLACE_ID", rs.getString(1));
                    Float val = rs.getFloat(2);
                    singleRow.put("AVG_VALUTATION", val.toString());
                    Integer num = rs.getInt(3);
                    singleRow.put("COUNT", num.toString());
                    list.add(singleRow);
                }
                if(list.isEmpty())
                    response.setResult("EMPTY SET");
                else {
                    response.setResult("OK");
                    response.setList(list);
                }
            }
            finally {
                connection.close();
            }
        }
        catch(SQLException exc){
            response.setResult(exc.toString());
        }
        return response;
    }

    /*
    Il metodo RetrieveLocalLeaderboard riceve in input l'id dell'utente e la sua citta di residenza e ritorna la top 10
    dei reviewers della sua citta'.Viene inoltre ritornata la posizione in classifica dell'utente e il numero totale di recensioni
    rilasciate.
     */

    @ApiMethod(name="RetrieveLocalLeaderboard")
    public LeaderboardBean RetrieveLocalLeaderboard(@Named("userID") String userID,@Named("userLocation") String userLocation) {
        LeaderboardBean response = new LeaderboardBean();
        try {
            Class.forName("com.mysql.jdbc.GoogleDriver");
        } catch (Exception e) {
            response.setResult(e.toString());
        }
        try {
            Connection connection = DriverManager.getConnection(dbLink);
            try {
                String query = "SELECT * FROM APP_USER WHERE LOCATION_NAME=? ORDER BY NUMBER_REW DESC LIMIT 10";
                PreparedStatement stmt = connection.prepareStatement(query);
                stmt.setString(1, userLocation);
                ResultSet rs = stmt.executeQuery();
                ArrayList<HashMap<String, String>> list = new ArrayList<>();
                while (rs.next()) {
                    HashMap<String, String> singleRow = new HashMap<>(5);
                    singleRow.put("ID", rs.getString(1));
                    singleRow.put("FULLNAME", rs.getString(2));
                    singleRow.put("LOCATION_NAME", rs.getString(3));
                    Integer num = rs.getInt(4);
                    singleRow.put("NUMBER_REW", num.toString());
                    singleRow.put("PICTURE_LINK", rs.getString(5));
                    list.add(singleRow);
                }
                if (list.isEmpty())
                    response.setResult("EMPTY SET");
                else
                    response.setList(list);
                connection.setAutoCommit(false);
                String ddl = "SET @RANK=0";
                stmt = connection.prepareStatement(ddl);
                stmt.execute();
                String positionQuery = "SELECT RANKING,NUMBER_REW FROM(SELECT *,@RANK:=@RANK+1 AS RANKING FROM APP_USER WHERE LOCATION_NAME=? ORDER BY NUMBER_REW DESC) TMP WHERE ID=?";
                stmt = connection.prepareStatement(positionQuery);
                stmt.setString(1, userLocation);
                stmt.setString(2, userID);
                rs = stmt.executeQuery();
                connection.commit();
                if (rs.next()) {
                    response.setUser_position(rs.getInt(1));
                    response.setUserNumRev(rs.getInt(2));
                }
                response.setResult("OK");
            } finally {
                connection.close();
            }
        } catch (SQLException exc) {

            response.setResult(exc.toString());
        }
        return response;
    }
}






